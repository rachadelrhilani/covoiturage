package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.request.ReservationRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.entity.Passager;
import com.covoiturage.covoiturage_api.entity.PointArret;
import com.covoiturage.covoiturage_api.entity.Reservation;
import com.covoiturage.covoiturage_api.entity.Trajet;
import com.covoiturage.covoiturage_api.enums.StatutReservation;
import com.covoiturage.covoiturage_api.enums.StatutTrajet;
import com.covoiturage.covoiturage_api.exception.AccessDeniedActionException;
import com.covoiturage.covoiturage_api.exception.BadRequestException;
import com.covoiturage.covoiturage_api.exception.BusinessRuleException;
import com.covoiturage.covoiturage_api.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage_api.mapper.ReservationMapper;
import com.covoiturage.covoiturage_api.repository.PassagerRepository;
import com.covoiturage.covoiturage_api.repository.ReservationRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import com.covoiturage.covoiturage_api.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final PassagerRepository passagerRepository;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional
    public ReservationResponse reserverPlaces(ReservationRequest request) {
        Passager passager = passagerRepository.findById(request.passagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Passager introuvable : " + request.passagerId()));

        Trajet trajet = trajetRepository.findDetailById(request.trajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable : " + request.trajetId()));

        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            throw new BusinessRuleException("Ce trajet n'est pas disponible à la réservation (statut : " + trajet.getStatut() + ").");
        }

        if (reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                passager.getId(), trajet.getId(), StatutReservation.CONFIRMEE)) {
            throw new BusinessRuleException("Vous avez déjà une réservation confirmée sur ce trajet.");
        }

        int placesRestantes = trajet.calculerPlacesRestantes();
        if (request.nbrPlaces() < 1 || request.nbrPlaces() > placesRestantes) {
            throw new BusinessRuleException("Places insuffisantes : " + placesRestantes + " place(s) disponible(s).");
        }

        PointArret montee = trouverPointSurTrajet(trajet, request.villeMontee());
        PointArret descente = trouverPointSurTrajet(trajet, request.villeDescente());

        if (montee.getOrdre() >= descente.getOrdre()) {
            throw new BadRequestException("La ville de montée doit être située avant la ville de descente.");
        }

        BigDecimal prixSegment = descente.getPrix().subtract(montee.getPrix());
        BigDecimal montantTotal = prixSegment
                .multiply(BigDecimal.valueOf(request.nbrPlaces()))
                .setScale(2, RoundingMode.HALF_UP);

        Reservation reservation = Reservation.builder()
                .villeMontee(montee.getVille())
                .villeDescente(descente.getVille())
                .nbrPlaces(request.nbrPlaces())
                .montantTotal(montantTotal)
                .statut(StatutReservation.CONFIRMEE)
                .dateReservation(LocalDate.now())
                .passager(passager)
                .trajet(trajet)
                .build();

        trajet.getReservations().add(reservation);
        mettreAJourStatutTrajet(trajet);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Réservation créée : id={}, trajetId={}, passagerId={}, places={}, total={} MAD",
                saved.getId(), trajet.getId(), passager.getId(), saved.getNbrPlaces(), saved.getMontantTotal());

        return reservationMapper.toResponse(saved);
    }

    @Override
    public List<ReservationResponse> mesReservations(Long passagerId) {
        if (!passagerRepository.existsById(passagerId)) {
            throw new ResourceNotFoundException("Passager introuvable : " + passagerId);
        }
        return reservationRepository.findByPassagerId(passagerId).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservationResponse annulerReservation(Long reservationId, Long passagerId) {
        Reservation reservation = reservationRepository.findDetailById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + reservationId));

        if (!reservation.getPassager().getId().equals(passagerId)) {
            throw new AccessDeniedActionException("Vous ne pouvez annuler que vos propres réservations.");
        }

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new BusinessRuleException("Cette réservation est déjà annulée.");
        }

        Trajet trajet = reservation.getTrajet();

        if (LocalDateTime.now().isAfter(trajet.getDateDepart().minusHours(24))) {
            throw new BusinessRuleException("Annulation impossible à moins de 24 heures du départ.");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        mettreAJourStatutTrajet(trajet);

        log.info("Réservation annulée : id={}, trajetId={}, passagerId={}",
                reservation.getId(), trajet.getId(), passagerId);

        return reservationMapper.toResponse(reservation);
    }

    private PointArret trouverPointSurTrajet(Trajet trajet, String ville) {
        return trajet.getPointsArret().stream()
                .filter(p -> p.getVille().equalsIgnoreCase(ville.trim()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("La ville '" + ville + "' ne fait pas partie de ce trajet."));
    }

    private void mettreAJourStatutTrajet(Trajet trajet) {
        if (trajet.getStatut() == StatutTrajet.ANNULE || trajet.getStatut() == StatutTrajet.TERMINE) {
            return;
        }
        if (trajet.calculerPlacesRestantes() == 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
        } else {
            trajet.setStatut(StatutTrajet.PUBLIE);
        }
    }
}
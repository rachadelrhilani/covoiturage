package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.request.PointArretRequest;
import com.covoiturage.covoiturage_api.dto.request.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.entity.PointArret;
import com.covoiturage.covoiturage_api.entity.Trajet;
import com.covoiturage.covoiturage_api.entity.Vehicule;
import com.covoiturage.covoiturage_api.enums.StatutReservation;
import com.covoiturage.covoiturage_api.enums.StatutTrajet;
import com.covoiturage.covoiturage_api.exception.AccessDeniedActionException;
import com.covoiturage.covoiturage_api.exception.BadRequestException;
import com.covoiturage.covoiturage_api.exception.BusinessRuleException;
import com.covoiturage.covoiturage_api.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage_api.mapper.PointArretMapper;
import com.covoiturage.covoiturage_api.mapper.ReservationMapper;
import com.covoiturage.covoiturage_api.mapper.TrajetMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.repository.ReservationRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import com.covoiturage.covoiturage_api.repository.VehiculeRepository;
import com.covoiturage.covoiturage_api.service.TrajetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ReservationRepository reservationRepository;
    private final TrajetMapper trajetMapper;
    private final PointArretMapper pointArretMapper;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional
    public TrajetResponse publierTrajet(TrajetRequest request) {
        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable : " + request.conducteurId()));

        Vehicule vehicule = vehiculeRepository.findDetailById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable : " + request.vehiculeId()));

        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            log.warn("Véhicule {} n'appartient pas au conducteur {}", vehicule.getId(), conducteur.getId());
            throw new AccessDeniedActionException("Vous ne pouvez utiliser que vos propres véhicules.");
        }

        if (request.placesPropose() < 1 || request.placesPropose() > vehicule.getCapacite()) {
            log.warn("Places proposées ({}) invalides pour capacité ({})", request.placesPropose(),
                    vehicule.getCapacite());
            throw new BadRequestException("Places proposées invalides (max : " + vehicule.getCapacite() + ").");
        }

        List<PointArretRequest> points = validerEtTrierPointsArret(request.pointsArret());
        PointArretRequest depart = points.get(0);
        PointArretRequest arrivee = points.get(points.size() - 1);

        Trajet trajet = Trajet.builder()
                .villeDepart(depart.ville().trim())
                .villeArrivee(arrivee.ville().trim())
                .dateDepart(depart.heureDePassage())
                .placesPropose(request.placesPropose())
                .statut(StatutTrajet.PUBLIE)
                .conducteur(conducteur)
                .vehicule(vehicule)
                .build();

        points.forEach(p -> {
            PointArret point = pointArretMapper.toEntity(p);
            point.setVille(p.ville().trim());
            trajet.addPointArret(point);
        });

        Trajet saved = trajetRepository.save(trajet);
        log.info("Trajet publié : id={}, {} -> {}", saved.getId(), saved.getVilleDepart(), saved.getVilleArrivee());

        return trajetMapper.toResponse(saved);
    }

    @Override
    public TrajetResponse consulterTrajet(Long id) {
        Trajet trajet = trajetRepository.findDetailById(id)
                .orElseThrow(() -> {
                    log.warn("Consultation trajet introuvable : id={}", id);
                    return new ResourceNotFoundException("Trajet introuvable avec l'identifiant : " + id);
                });
        return trajetMapper.toResponse(trajet);
    }

    @Override
    public List<ReservationResponse> consulterReservationsTrajet(Long trajetId, Long conducteurId) {
        Trajet trajet = trajetRepository.findDetailById(trajetId)
                .orElseThrow(() -> {
                    log.warn("Consultation réservations refusée : trajet introuvable id={}", trajetId);
                    return new ResourceNotFoundException("Trajet introuvable avec l'identifiant : " + trajetId);
                });

        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            log.warn("Consultation réservations trajet {} refusée : conducteur {} non propriétaire", trajetId,
                    conducteurId);
            throw new AccessDeniedActionException(
                    "Vous ne pouvez consulter que les réservations de vos propres trajets.");
        }

        return reservationRepository.findByTrajetId(trajetId).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    private List<PointArretRequest> validerEtTrierPointsArret(List<PointArretRequest> points) {
        if (points == null || points.size() < 2) {
            throw new BadRequestException("Il faut au moins 2 points d'arrêt (départ et arrivée).");
        }

        List<PointArretRequest> sorted = points.stream()
                .sorted(Comparator.comparingInt(PointArretRequest::ordre))
                .toList();

        PointArretRequest depart = sorted.get(0);

        if (!depart.heureDePassage().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La date de départ doit être dans le futur.");
        }

        if (depart.prix().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Le prix au point de départ doit être égal à 0.");
        }

        verifierProgressionEtapes(sorted);
        return sorted;
    }

    private void verifierProgressionEtapes(List<PointArretRequest> sorted) {
        Set<String> villes = new HashSet<>();

        for (int i = 0; i < sorted.size(); i++) {
            PointArretRequest courant = sorted.get(i);

            if (courant.ordre() != i) {
                throw new BadRequestException("L'ordre des points doit être séquentiel (0, 1, 2...).");
            }

            if (!villes.add(courant.ville().trim().toLowerCase())) {
                throw new BadRequestException("Ville en doublon : " + courant.ville());
            }

            if (i > 0) {
                PointArretRequest precedent = sorted.get(i - 1);

                if (!courant.heureDePassage().isAfter(precedent.heureDePassage())) {
                    throw new BadRequestException("Les heures de passage doivent être croissantes.");
                }

                if (courant.prix().compareTo(precedent.prix()) <= 0) {
                    throw new BadRequestException("Les prix depuis le départ doivent être croissants.");
                }
            }
        }
    }

    @Override
    public Page<TrajetResponse> rechercherTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDate date,
            BigDecimal prixMax,
            Integer nbPlaces,
            Pageable pageable) {

        int placesDemandees = (nbPlaces != null && nbPlaces > 0) ? nbPlaces : 1;
        String depart = villeDepart.trim();
        String arrivee = villeArrivee.trim();

        log.info("Recherche de trajets : {} -> {} le {} (places >= {}, prixMax={})",
                depart, arrivee, date, placesDemandees, prixMax);

        Page<Trajet> trajetsPage = trajetRepository.searchTrajets(
                depart,
                arrivee,
                date,
                prixMax,
                placesDemandees,
                pageable);

        return trajetsPage.map(trajet -> {
            BigDecimal prixMontee = trajet.getPointsArret().stream()
                    .filter(p -> p.getVille().equalsIgnoreCase(depart))
                    .map(PointArret::getPrix)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            BigDecimal prixDescente = trajet.getPointsArret().stream()
                    .filter(p -> p.getVille().equalsIgnoreCase(arrivee))
                    .map(PointArret::getPrix)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            BigDecimal prixSegment = prixDescente.subtract(prixMontee)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            return trajetMapper.toResponseWithSegmentPrice(trajet, prixSegment);
        });
    }

    @Override
    @Transactional
    public TrajetResponse terminerTrajet(Long trajetId, Long conducteurId) {
        Trajet trajet = trouverTrajetConducteur(trajetId, conducteurId);

        if (trajet.getStatut() != StatutTrajet.PUBLIE && trajet.getStatut() != StatutTrajet.COMPLET) {
            throw new BusinessRuleException("Impossible de terminer un trajet avec le statut : " + trajet.getStatut());
        }

        if (!LocalDateTime.now().isAfter(trajet.getDateDepart())) {
            log.warn("Terminaison trajet {} refusée : heure de départ ({}) non atteinte", trajetId,
                    trajet.getDateDepart());
            throw new BusinessRuleException("Un trajet ne peut être terminé qu'après son heure de départ.");
        }

        trajet.setStatut(StatutTrajet.TERMINE);
        log.info("Trajet terminé avec succès : id={}, conducteurId={}", trajetId, conducteurId);

        return trajetMapper.toResponse(trajet);
    }

    @Override
    @Transactional
    public TrajetResponse annulerTrajet(Long trajetId, Long conducteurId) {
        Trajet trajet = trouverTrajetConducteur(trajetId, conducteurId);

        if (trajet.getStatut() != StatutTrajet.PUBLIE && trajet.getStatut() != StatutTrajet.COMPLET) {
            throw new BusinessRuleException("Impossible d'annuler un trajet avec le statut : " + trajet.getStatut());
        }

        trajet.setStatut(StatutTrajet.ANNULE);

       
        long nbAnnulees = trajet.getReservations().stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .peek(r -> r.setStatut(StatutReservation.ANNULEE))
                .count();

        log.info("Trajet annulé : id={}, conducteurId={}, {} réservation(s) annulée(s) en cascade",
                trajetId, conducteurId, nbAnnulees);

        return trajetMapper.toResponse(trajet);
    }

    private Trajet trouverTrajetConducteur(Long trajetId, Long conducteurId) {
        Trajet trajet = trajetRepository.findDetailById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable : " + trajetId));

        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            log.warn("Action refusée sur trajet {} : conducteur {} non propriétaire", trajetId, conducteurId);
            throw new AccessDeniedActionException("Vous ne pouvez gérer que vos propres trajets.");
        }

        return trajet;
    }
}
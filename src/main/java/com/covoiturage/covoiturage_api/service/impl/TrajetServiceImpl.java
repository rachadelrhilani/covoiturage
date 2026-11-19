package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.request.PointArretRequest;
import com.covoiturage.covoiturage_api.dto.request.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.entity.PointArret;
import com.covoiturage.covoiturage_api.entity.Trajet;
import com.covoiturage.covoiturage_api.entity.Vehicule;
import com.covoiturage.covoiturage_api.enums.StatutTrajet;
import com.covoiturage.covoiturage_api.exception.AccessDeniedActionException;
import com.covoiturage.covoiturage_api.exception.BadRequestException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
                .orElseThrow(() -> {
                    log.warn("Publication trajet refusée : conducteur introuvable id={}", request.conducteurId());
                    return new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + request.conducteurId());
                });

        Vehicule vehicule = vehiculeRepository.findDetailById(request.vehiculeId())
                .orElseThrow(() -> {
                    log.warn("Publication trajet refusée : véhicule introuvable id={}", request.vehiculeId());
                    return new ResourceNotFoundException("Véhicule introuvable avec l'identifiant : " + request.vehiculeId());
                });

        // Règle : Le conducteur publie uniquement avec l'un de ses propres véhicules (403)
        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            log.warn("Publication trajet refusée : véhicule {} n'appartient pas au conducteur {}",
                    vehicule.getId(), conducteur.getId());
            throw new AccessDeniedActionException("Vous ne pouvez publier un trajet qu'avec l'un de vos propres véhicules.");
        }

        
        if (request.placesPropose() < 1 || request.placesPropose() > vehicule.getCapacite()) {
            log.warn("Publication trajet refusée : {} places proposées pour une capacité de {}",
                    request.placesPropose(), vehicule.getCapacite());
            throw new BadRequestException("Le nombre de places proposées doit être compris entre 1 et " + vehicule.getCapacite() + " (capacité du véhicule).");
        }

        List<PointArretRequest> pointsOrdonnes = validerEtTrierPointsArret(request.pointsArret());

        PointArretRequest depart = pointsOrdonnes.get(0);
        PointArretRequest arrivee = pointsOrdonnes.get(pointsOrdonnes.size() - 1);

        Trajet trajet = Trajet.builder()
                .villeDepart(depart.ville().trim())
                .villeArrivee(arrivee.ville().trim())
                .dateDepart(depart.heureDePassage())
                .placesPropose(request.placesPropose())
                .statut(StatutTrajet.PUBLIE)
                .conducteur(conducteur)
                .vehicule(vehicule)
                .build();

        pointsOrdonnes.forEach(pReq -> {
            PointArret point = pointArretMapper.toEntity(pReq);
            point.setVille(pReq.ville().trim());
            trajet.addPointArret(point);
        });

        Trajet saved = trajetRepository.save(trajet);
        log.info("Trajet publié avec succès : id={}, {} -> {}, conducteurId={}, places={}",
                saved.getId(), saved.getVilleDepart(), saved.getVilleArrivee(), conducteur.getId(), saved.getPlacesPropose());

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
            log.warn("Consultation réservations trajet {} refusée : conducteur {} non propriétaire", trajetId, conducteurId);
            throw new AccessDeniedActionException("Vous ne pouvez consulter que les réservations de vos propres trajets.");
        }

        return reservationRepository.findByTrajetId(trajetId).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    private List<PointArretRequest> validerEtTrierPointsArret(List<PointArretRequest> points) {
        if (points == null || points.size() < 2) {
            throw new BadRequestException("Un trajet doit contenir au moins deux points d'arrêt (départ et arrivée).");
        }

        List<PointArretRequest> sorted = points.stream()
                .sorted(Comparator.comparingInt(PointArretRequest::ordre))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).ordre() != i) {
                throw new BadRequestException("Les ordres des points d'arrêt doivent être séquentiels à partir de 0 (0, 1, 2...).");
            }
        }

        PointArretRequest premierPoint = sorted.get(0);
        if (!premierPoint.heureDePassage().isAfter(LocalDateTime.now())) {
            log.warn("Publication trajet refusée : heure de départ dans le passé ({})", premierPoint.heureDePassage());
            throw new BadRequestException("L'heure de départ du trajet doit être dans le futur.");
        }

        if (premierPoint.prix().compareTo(BigDecimal.ZERO) != 0) {
            log.warn("Publication trajet refusée : prix initial différent de 0 ({})", premierPoint.prix());
            throw new BadRequestException("Le prix depuis le départ doit être égal à 0 au premier point d'arrêt.");
        }

        Set<String> villesVues = new HashSet<>();
        villesVues.add(premierPoint.ville().trim().toLowerCase());

        for (int i = 1; i < sorted.size(); i++) {
            PointArretRequest precedent = sorted.get(i - 1);
            PointArretRequest courant = sorted.get(i);

            String villeNormalisee = courant.ville().trim().toLowerCase();
            if (!villesVues.add(villeNormalisee)) {
                log.warn("Publication trajet refusée : ville en doublon sur le parcours ({})", courant.ville());
                throw new BadRequestException("La ville '" + courant.ville() + "' apparaît plusieurs fois sur le parcours.");
            }

            if (!courant.heureDePassage().isAfter(precedent.heureDePassage())) {
                log.warn("Publication trajet refusée : heures de passage non croissantes entre {} et {}",
                        precedent.ville(), courant.ville());
                throw new BadRequestException("Les heures de passage doivent être strictement croissantes entre '"
                        + precedent.ville() + "' et '" + courant.ville() + "'.");
            }

            if (courant.prix().compareTo(precedent.prix()) <= 0) {
                log.warn("Publication trajet refusée : prix non croissants entre {} ({}) et {} ({})",
                        precedent.ville(), precedent.prix(), courant.ville(), courant.prix());
                throw new BadRequestException("Le prix depuis le départ doit être strictement croissant entre '"
                        + precedent.ville() + "' et '" + courant.ville() + "'.");
            }
        }

        return sorted;
    }
}
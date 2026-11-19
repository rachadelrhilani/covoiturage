package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Trajet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    boolean existsByVehiculeId(Long vehiculeId);

    @Query("""
            SELECT DISTINCT t FROM Trajet t
            JOIN FETCH t.conducteur
            JOIN FETCH t.vehicule
            LEFT JOIN FETCH t.pointsArret
            LEFT JOIN FETCH t.reservations
            WHERE t.id = :id
            """)
    Optional<Trajet> findDetailById(@Param("id") Long id);


    @Query("""
            SELECT t FROM Trajet t
            JOIN t.pointsArret pMontee
            JOIN t.pointsArret pDescente
            WHERE t.statut = com.covoiturage.covoiturage_api.enums.StatutTrajet.PUBLIE
              AND LOWER(pMontee.ville) = LOWER(:depart)
              AND LOWER(pDescente.ville) = LOWER(:arrivee)
              AND pMontee.ordre < pDescente.ordre
              AND CAST(pMontee.heureDePassage AS date) = :date
              AND (:prixMax IS NULL OR (pDescente.prix - pMontee.prix) <= :prixMax)
              AND (t.placesPropose - COALESCE((
                    SELECT SUM(r.nbrPlaces) FROM Reservation r
                    WHERE r.trajet = t
                      AND r.statut = com.covoiturage.covoiturage_api.enums.StatutReservation.CONFIRMEE
                  ), 0)) >= :nbPlaces
            ORDER BY t.dateDepart ASC
            """)
    Page<Trajet> searchTrajets(
            @Param("depart") String depart,
            @Param("arrivee") String arrivee,
            @Param("date") LocalDate date,
            @Param("prixMax") BigDecimal prixMax,
            @Param("nbPlaces") Integer nbPlaces,
            Pageable pageable
    );
}
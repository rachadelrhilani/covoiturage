package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Trajet;
import com.covoiturage.covoiturage_api.enums.StatutReservation;
import com.covoiturage.covoiturage_api.enums.StatutTrajet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            WHERE t.id = :id
            """)
    Optional<Trajet> findDetailById(@Param("id") Long id);


    @Query(
            value = """
                    SELECT t FROM Trajet t
                    JOIN t.pointsArret pMontee
                    JOIN t.pointsArret pDescente
                    WHERE t.statut = :statut
                      AND LOWER(pMontee.ville) = LOWER(:depart)
                      AND LOWER(pDescente.ville) = LOWER(:arrivee)
                      AND pMontee.ordre < pDescente.ordre
                      AND pMontee.heureDePassage >= :debutJour
                      AND pMontee.heureDePassage < :finJour
                      AND (:prixMax IS NULL OR (pDescente.prix - pMontee.prix) <= :prixMax)
                      AND (t.placesPropose - COALESCE((
                            SELECT SUM(r.nbrPlaces) FROM Reservation r
                            WHERE r.trajet = t AND r.statut = :statutResa
                          ), 0)) >= :nbPlaces
                    ORDER BY t.dateDepart ASC
                    """,
            countQuery = """
                    SELECT COUNT(t) FROM Trajet t
                    JOIN t.pointsArret pMontee
                    JOIN t.pointsArret pDescente
                    WHERE t.statut = :statut
                      AND LOWER(pMontee.ville) = LOWER(:depart)
                      AND LOWER(pDescente.ville) = LOWER(:arrivee)
                      AND pMontee.ordre < pDescente.ordre
                      AND pMontee.heureDePassage >= :debutJour
                      AND pMontee.heureDePassage < :finJour
                      AND (:prixMax IS NULL OR (pDescente.prix - pMontee.prix) <= :prixMax)
                      AND (t.placesPropose - COALESCE((
                            SELECT SUM(r.nbrPlaces) FROM Reservation r
                            WHERE r.trajet = t AND r.statut = :statutResa
                          ), 0)) >= :nbPlaces
                    """
    )
    Page<Trajet> searchTrajets(
            @Param("depart") String villeDepart,
            @Param("arrivee") String villeArrivee,
            @Param("debutJour") LocalDateTime debutJour,
            @Param("finJour") LocalDateTime finJour,
            @Param("prixMax") BigDecimal prixMax,
            @Param("nbPlaces") Integer nbPlaces,
            @Param("statut") StatutTrajet statut,
            @Param("statutResa") StatutReservation statutResa,
            Pageable pageable
    );
}
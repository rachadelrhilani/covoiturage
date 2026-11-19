package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Trajet;

import java.util.Optional;

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
}
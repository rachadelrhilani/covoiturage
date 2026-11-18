package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    boolean existsByVehiculeId(Long vehiculeId);
}
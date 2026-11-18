package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    boolean existsByImmatriculationIgnoreCase(String immatriculation);

    @Query("SELECT COUNT(v) > 0 FROM Vehicule v WHERE LOWER(v.immatriculation) = LOWER(:immat) AND v.id <> :id")
    boolean existsImmatForOther(@Param("immat") String immatriculation, @Param("id") Long id);

    @Query("SELECT v FROM Vehicule v JOIN FETCH v.conducteur WHERE v.conducteur.id = :conducteurId")
    List<Vehicule> findByConducteurId(@Param("conducteurId") Long conducteurId);

    @Query("SELECT v FROM Vehicule v JOIN FETCH v.conducteur WHERE v.id = :id")
    Optional<Vehicule> findDetailById(@Param("id") Long id);
}
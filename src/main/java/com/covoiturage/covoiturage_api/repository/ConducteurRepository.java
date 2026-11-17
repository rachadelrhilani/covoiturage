package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNumeroPermisIgnoreCase(String numeroPermis);

    @Query("SELECT COUNT(c) > 0 FROM Conducteur c WHERE LOWER(c.email) = LOWER(:email) AND c.id <> :id")
    boolean existsEmailForOther(@Param("email") String email, @Param("id") Long id);

    @Query("SELECT COUNT(c) > 0 FROM Conducteur c WHERE LOWER(c.numeroPermis) = LOWER(:permis) AND c.id <> :id")
    boolean existsPermisForOther(@Param("permis") String permis, @Param("id") Long id);

    @Query("SELECT c FROM Conducteur c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Conducteur> searchByNomOrEmail(@Param("q") String query);
}
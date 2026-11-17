package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Passager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PassagerRepository extends JpaRepository<Passager, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT COUNT(p) > 0 FROM Passager p WHERE LOWER(p.email) = LOWER(:email) AND p.id <> :id")
    boolean existsEmailForOther(@Param("email") String email, @Param("id") Long id);

    Optional<Passager> findByEmailIgnoreCase(String email);
}
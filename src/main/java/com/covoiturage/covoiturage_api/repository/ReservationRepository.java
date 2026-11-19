package com.covoiturage.covoiturage_api.repository;

import com.covoiturage.covoiturage_api.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.passager JOIN FETCH r.trajet WHERE r.passager.id = :passagerId ORDER BY r.dateReservation DESC")
    List<Reservation> findByPassagerId(@Param("passagerId") Long passagerId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.passager JOIN FETCH r.trajet WHERE r.trajet.id = :trajetId ORDER BY r.dateReservation DESC")
    List<Reservation> findByTrajetId(@Param("trajetId") Long trajetId);
}
package com.covoiturage.covoiturage_api.dto.response;

import com.covoiturage.covoiturage_api.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        Long trajetId,
        Long passagerId,
        String passagerNomComplet,
        String villeMontee,
        String villeDescente,
        Integer nbrPlaces,
        BigDecimal montantTotal,
        LocalDate dateReservation,
        StatutReservation statut
) {}
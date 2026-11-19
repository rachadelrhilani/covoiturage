package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.request.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

public interface TrajetService {
    TrajetResponse publierTrajet(TrajetRequest request);

    TrajetResponse consulterTrajet(Long id);

    List<ReservationResponse> consulterReservationsTrajet(Long trajetId, Long conducteurId);


    Page<TrajetResponse> rechercherTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDate date,
            BigDecimal prixMax,
            Integer nbPlaces,
            Pageable pageable
    );
}
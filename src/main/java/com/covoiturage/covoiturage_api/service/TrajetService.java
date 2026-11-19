package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.request.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;

import java.util.List;

public interface TrajetService {
    TrajetResponse publierTrajet(TrajetRequest request);

    TrajetResponse consulterTrajet(Long id);

    List<ReservationResponse> consulterReservationsTrajet(Long trajetId, Long conducteurId);
}
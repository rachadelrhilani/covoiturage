package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.request.ReservationRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {

    ReservationResponse reserverPlaces(ReservationRequest request);

    List<ReservationResponse> mesReservations(Long passagerId);

    ReservationResponse annulerReservation(Long reservationId, Long passagerId);
}
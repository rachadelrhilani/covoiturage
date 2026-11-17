package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.request.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;

import java.util.List;

public interface PassagerService {

    PassagerResponse creerPassager(PassagerRequest request);

    List<PassagerResponse> listerPassagers();

    PassagerResponse consulterPassager(Long id);

    PassagerResponse modifierPassager(Long id, PassagerRequest request);

    PassagerResponse rechercherParEmail(String email);

    List<ReservationResponse> consulterHistoriqueReservations(Long passagerId);
}
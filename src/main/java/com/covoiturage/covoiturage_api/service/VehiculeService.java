package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.request.VehiculeRequest;
import com.covoiturage.covoiturage_api.dto.response.VehiculeResponse;

import java.util.List;

public interface VehiculeService {

    VehiculeResponse ajouterVehicule(VehiculeRequest request);

    VehiculeResponse modifierVehicule(Long id, VehiculeRequest request);

    VehiculeResponse consulterVehicule(Long id);

    List<VehiculeResponse> listerVehiculesConducteur(Long conducteurId);

    void supprimerVehicule(Long id, Long conducteurId);
}
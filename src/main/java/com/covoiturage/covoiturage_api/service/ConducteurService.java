package com.covoiturage.covoiturage_api.service;

import com.covoiturage.covoiturage_api.dto.request.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.response.ConducteurResponse;

import java.util.List;

public interface ConducteurService {

    ConducteurResponse creerConducteur(ConducteurRequest request);

    List<ConducteurResponse> listerConducteurs();

    ConducteurResponse consulterConducteur(Long id);

    ConducteurResponse modifierConducteur(Long id, ConducteurRequest request);

    List<ConducteurResponse> rechercherConducteurs(String query);
}
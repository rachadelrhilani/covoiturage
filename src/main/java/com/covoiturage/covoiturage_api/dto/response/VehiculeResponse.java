package com.covoiturage.covoiturage_api.dto.response;

public record VehiculeResponse(
        Long id,
        String marque,
        String modele,
        String immatriculation,
        Integer capacite,
        Long conducteurId,
        String conducteurNomComplet
) {}
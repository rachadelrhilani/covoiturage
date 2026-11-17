package com.covoiturage.covoiturage_api.dto.response;

public record PassagerResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone
) {}
package com.covoiturage.covoiturage_api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehiculeRequest(
        @NotBlank(message = "La marque est obligatoire")
        String marque,

        @NotBlank(message = "Le modèle est obligatoire")
        String modele,

        @NotBlank(message = "L'immatriculation est obligatoire")
        String immatriculation,

        @NotBlank(message = "la couleur est obligatoire")
        String couleur,

        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "La capacité minimale est de 1 place passager")
        @Max(value = 8, message = "La capacité maximale est de 8 places passagers")
        Integer capacite,

        @NotNull(message = "L'identifiant du conducteur propriétaire est obligatoire")
        Long conducteurId
) {}
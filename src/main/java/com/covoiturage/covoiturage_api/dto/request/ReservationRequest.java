package com.covoiturage.covoiturage_api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull(message = "L'identifiant du passager est obligatoire")
        Long passagerId,

        @NotNull(message = "L'identifiant du trajet est obligatoire")
        Long trajetId,

        @NotBlank(message = "La ville de montée est obligatoire")
        String villeMontee,

        @NotBlank(message = "La ville de descente est obligatoire")
        String villeDescente,

        @NotNull(message = "Le nombre de places est obligatoire")
        @Min(value = 1, message = "Vous devez réserver au moins 1 place")
        Integer nbrPlaces
) {}
package com.covoiturage.covoiturage_api.dto.response;

import com.covoiturage.covoiturage_api.enums.StatutTrajet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TrajetResponse(
        Long id,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        Integer placesPropose,
        Integer placesRestantes,
        BigDecimal prixSegment,
        StatutTrajet statut,
        ConducteurResponse conducteur,
        VehiculeResponse vehicule,
        List<PointArretResponse> pointsArret
) {}
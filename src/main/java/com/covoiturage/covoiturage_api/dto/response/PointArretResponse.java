package com.covoiturage.covoiturage_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretResponse(
        Long id,
        String ville,
        Integer ordre,
        LocalDateTime heureDePassage,
        BigDecimal prix
) {}
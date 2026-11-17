package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.request.PointArretRequest;
import com.covoiturage.covoiturage_api.dto.response.PointArretResponse;
import com.covoiturage.covoiturage_api.entity.PointArret;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PointArretMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trajet", ignore = true)
    PointArret toEntity(PointArretRequest request);

    PointArretResponse toResponse(PointArret pointArret);
}
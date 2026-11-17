package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.entity.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {ConducteurMapper.class, VehiculeMapper.class, PointArretMapper.class})
public interface TrajetMapper {

    @Mapping(target = "placesRestantes", expression = "java(trajet.calculerPlacesRestantes())")
    @Mapping(target = "prixSegment", ignore = true)
    TrajetResponse toResponse(Trajet trajet);

    @Mapping(target = "placesRestantes", expression = "java(trajet.calculerPlacesRestantes())")
    @Mapping(target = "prixSegment", source = "prixSegment")
    TrajetResponse toResponseWithSegmentPrice(Trajet trajet, BigDecimal prixSegment);
}
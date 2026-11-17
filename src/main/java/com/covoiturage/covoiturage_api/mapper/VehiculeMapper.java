package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.request.VehiculeRequest;
import com.covoiturage.covoiturage_api.dto.response.VehiculeResponse;
import com.covoiturage.covoiturage_api.entity.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    Vehicule toEntity(VehiculeRequest request);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    @Mapping(target = "conducteurNomComplet", expression = "java(vehicule.getConducteur().getPrenom() + \" \" + vehicule.getConducteur().getNom())")
    VehiculeResponse toResponse(Vehicule vehicule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    void updateEntityFromRequest(VehiculeRequest request, @MappingTarget Vehicule vehicule);
}
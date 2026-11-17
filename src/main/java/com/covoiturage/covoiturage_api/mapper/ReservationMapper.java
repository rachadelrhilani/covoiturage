package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "trajetId", source = "trajet.id")
    @Mapping(target = "passagerId", source = "passager.id")
    @Mapping(target = "passagerNomComplet", expression = "java(reservation.getPassager().getPrenom() + \" \" + reservation.getPassager().getNom())")
    ReservationResponse toResponse(Reservation reservation);
}
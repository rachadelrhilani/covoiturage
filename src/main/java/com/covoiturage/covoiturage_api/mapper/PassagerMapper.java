package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.request.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.entity.Passager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PassagerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Passager toEntity(PassagerRequest request);

    PassagerResponse toResponse(Passager passager);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateEntityFromRequest(PassagerRequest request, @MappingTarget Passager passager);
}
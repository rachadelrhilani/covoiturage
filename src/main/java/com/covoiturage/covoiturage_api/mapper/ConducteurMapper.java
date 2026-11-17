package com.covoiturage.covoiturage_api.mapper;

import com.covoiturage.covoiturage_api.dto.request.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.response.ConducteurResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConducteurMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicules", ignore = true)
    @Mapping(target = "trajets", ignore = true)
    Conducteur toEntity(ConducteurRequest request);

    ConducteurResponse toResponse(Conducteur conducteur);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicules", ignore = true)
    @Mapping(target = "trajets", ignore = true)
    void updateEntityFromRequest(ConducteurRequest request, @MappingTarget Conducteur conducteur);
}
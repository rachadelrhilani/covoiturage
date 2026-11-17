package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.request.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.response.ConducteurResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.exception.BusinessRuleException;
import com.covoiturage.covoiturage_api.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage_api.mapper.ConducteurMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.service.ConducteurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurRepository conducteurRepository;
    private final ConducteurMapper conducteurMapper;

    @Override
    @Transactional
    public ConducteurResponse creerConducteur(ConducteurRequest request) {
        if (conducteurRepository.existsByEmailIgnoreCase(request.email())) {
            log.warn("Création conducteur refusée : email déjà utilisé ({})", request.email());
            throw new BusinessRuleException("Un conducteur avec l'email '" + request.email() + "' existe déjà.");
        }

        if (conducteurRepository.existsByNumeroPermisIgnoreCase(request.numeroPermis())) {
            log.warn("Création conducteur refusée : numéro de permis déjà utilisé ({})", request.numeroPermis());
            throw new BusinessRuleException("Un conducteur avec le numéro de permis '" + request.numeroPermis() + "' existe déjà.");
        }

        Conducteur conducteur = conducteurMapper.toEntity(request);
        Conducteur saved = conducteurRepository.save(conducteur);

        log.info("Conducteur créé avec succès : id={}, email={}", saved.getId(), saved.getEmail());
        return conducteurMapper.toResponse(saved);
    }

    @Override
    public List<ConducteurResponse> listerConducteurs() {
        return conducteurRepository.findAll().stream()
                .map(conducteurMapper::toResponse)
                .toList();
    }

    @Override
    public ConducteurResponse consulterConducteur(Long id) {
        Conducteur conducteur = findConducteurOrThrow(id);
        return conducteurMapper.toResponse(conducteur);
    }

    @Override
    @Transactional
    public ConducteurResponse modifierConducteur(Long id, ConducteurRequest request) {
        Conducteur conducteur = findConducteurOrThrow(id);

        if (conducteurRepository.existsEmailForOther(request.email(), id)) {
            log.warn("Modification conducteur {} refusée : email déjà utilisé ({})", id, request.email());
            throw new BusinessRuleException("L'email '" + request.email() + "' est déjà utilisé par un autre conducteur.");
        }

        if (conducteurRepository.existsPermisForOther(request.numeroPermis(), id)) {
            log.warn("Modification conducteur {} refusée : numéro de permis déjà utilisé ({})", id, request.numeroPermis());
            throw new BusinessRuleException("Le numéro de permis '" + request.numeroPermis() + "' est déjà utilisé par un autre conducteur.");
        }

        conducteurMapper.updateEntityFromRequest(request, conducteur);
        Conducteur updated = conducteurRepository.save(conducteur);

        log.info("Profil conducteur mis à jour avec succès : id={}", updated.getId());
        return conducteurMapper.toResponse(updated);
    }

    @Override
    public List<ConducteurResponse> rechercherConducteurs(String query) {
        return conducteurRepository.searchByNomOrEmail(query).stream()
                .map(conducteurMapper::toResponse)
                .toList();
    }

    private Conducteur findConducteurOrThrow(Long id) {
        return conducteurRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Conducteur introuvable avec l'id={}", id);
                    return new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + id);
                });
    }
}
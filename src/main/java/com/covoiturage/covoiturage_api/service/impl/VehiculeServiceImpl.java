package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.request.VehiculeRequest;
import com.covoiturage.covoiturage_api.dto.response.VehiculeResponse;
import com.covoiturage.covoiturage_api.entity.Conducteur;
import com.covoiturage.covoiturage_api.entity.Vehicule;
import com.covoiturage.covoiturage_api.exception.AccessDeniedActionException;
import com.covoiturage.covoiturage_api.exception.BusinessRuleException;
import com.covoiturage.covoiturage_api.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage_api.mapper.VehiculeMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import com.covoiturage.covoiturage_api.repository.VehiculeRepository;
import com.covoiturage.covoiturage_api.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final TrajetRepository trajetRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    @Transactional
    public VehiculeResponse ajouterVehicule(VehiculeRequest request) {
        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> {
                    log.warn("Ajout véhicule refusé : conducteur introuvable id={}", request.conducteurId());
                    return new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + request.conducteurId());
                });

        if (vehiculeRepository.existsByImmatriculationIgnoreCase(request.immatriculation())) {
            log.warn("Ajout véhicule refusé : immatriculation déjà enregistrée ({})", request.immatriculation());
            throw new BusinessRuleException("Un véhicule avec l'immatriculation '" + request.immatriculation() + "' existe déjà.");
        }

        Vehicule vehicule = vehiculeMapper.toEntity(request);
        vehicule.setConducteur(conducteur);
        Vehicule saved = vehiculeRepository.save(vehicule);

        log.info("Véhicule ajouté avec succès : id={}, immatriculation={}, conducteurId={}",
                saved.getId(), saved.getImmatriculation(), conducteur.getId());
        return vehiculeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VehiculeResponse modifierVehicule(Long id, VehiculeRequest request) {
        Vehicule vehicule = findVehiculeOrThrow(id);

        if (!vehicule.getConducteur().getId().equals(request.conducteurId())) {
            log.warn("Modification véhicule {} refusée : conducteur {} non propriétaire", id, request.conducteurId());
            throw new AccessDeniedActionException("Vous ne pouvez modifier que vos propres véhicules.");
        }

        if (vehiculeRepository.existsImmatForOther(request.immatriculation(), id)) {
            log.warn("Modification véhicule {} refusée : immatriculation déjà utilisée ({})", id, request.immatriculation());
            throw new BusinessRuleException("L'immatriculation '" + request.immatriculation() + "' est déjà utilisée par un autre véhicule.");
        }

        vehiculeMapper.updateEntityFromRequest(request, vehicule);
        Vehicule updated = vehiculeRepository.save(vehicule);

        log.info("Véhicule mis à jour avec succès : id={}", updated.getId());
        return vehiculeMapper.toResponse(updated);
    }

    @Override
    public VehiculeResponse consulterVehicule(Long id) {
        return vehiculeMapper.toResponse(findVehiculeOrThrow(id));
    }

    @Override
    public List<VehiculeResponse> listerVehiculesConducteur(Long conducteurId) {
        if (!conducteurRepository.existsById(conducteurId)) {
            log.warn("Liste véhicules refusée : conducteur introuvable id={}", conducteurId);
            throw new ResourceNotFoundException("Conducteur introuvable avec l'identifiant : " + conducteurId);
        }

        return vehiculeRepository.findByConducteurId(conducteurId).stream()
                .map(vehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void supprimerVehicule(Long id, Long conducteurId) {
        Vehicule vehicule = findVehiculeOrThrow(id);

        if (!vehicule.getConducteur().getId().equals(conducteurId)) {
            log.warn("Suppression véhicule {} refusée : conducteur {} non propriétaire", id, conducteurId);
            throw new AccessDeniedActionException("Vous ne pouvez supprimer que vos propres véhicules.");
        }

        if (trajetRepository.existsByVehiculeId(id)) {
            log.warn("Suppression véhicule {} refusée : véhicule déjà associé à un ou plusieurs trajets", id);
            throw new BusinessRuleException("Impossible de supprimer ce véhicule car il est associé à au moins un trajet.");
        }

        vehiculeRepository.delete(vehicule);
        log.info("Véhicule supprimé avec succès : id={}, conducteurId={}", id, conducteurId);
    }

    private Vehicule findVehiculeOrThrow(Long id) {
        return vehiculeRepository.findDetailById(id)
                .orElseThrow(() -> {
                    log.warn("Véhicule introuvable avec l'id={}", id);
                    return new ResourceNotFoundException("Véhicule introuvable avec l'identifiant : " + id);
                });
    }
}
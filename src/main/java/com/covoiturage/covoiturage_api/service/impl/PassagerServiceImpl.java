package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.request.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.entity.Passager;
import com.covoiturage.covoiturage_api.exception.BusinessRuleException;
import com.covoiturage.covoiturage_api.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage_api.mapper.PassagerMapper;
import com.covoiturage.covoiturage_api.mapper.ReservationMapper;
import com.covoiturage.covoiturage_api.repository.PassagerRepository;
import com.covoiturage.covoiturage_api.repository.ReservationRepository;
import com.covoiturage.covoiturage_api.service.PassagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PassagerServiceImpl implements PassagerService {

    private final PassagerRepository passagerRepository;
    private final ReservationRepository reservationRepository;
    private final PassagerMapper passagerMapper;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional
    public PassagerResponse creerPassager(PassagerRequest request) {
        if (passagerRepository.existsByEmailIgnoreCase(request.email())) {
            log.warn("Création passager refusée : email déjà utilisé ({})", request.email());
            throw new BusinessRuleException("Un passager avec l'email '" + request.email() + "' existe déjà.");
        }

        Passager passager = passagerMapper.toEntity(request);
        Passager saved = passagerRepository.save(passager);

        log.info("Passager créé avec succès : id={}, email={}", saved.getId(), saved.getEmail());
        return passagerMapper.toResponse(saved);
    }

    @Override
    public List<PassagerResponse> listerPassagers() {
        return passagerRepository.findAll().stream()
                .map(passagerMapper::toResponse)
                .toList();
    }

    @Override
    public PassagerResponse consulterPassager(Long id) {
        Passager passager = findPassagerOrThrow(id);
        return passagerMapper.toResponse(passager);
    }

    @Override
    @Transactional
    public PassagerResponse modifierPassager(Long id, PassagerRequest request) {
        Passager passager = findPassagerOrThrow(id);

        if (passagerRepository.existsEmailForOther(request.email(), id)) {
            log.warn("Modification passager {} refusée : email déjà utilisé ({})", id, request.email());
            throw new BusinessRuleException("L'email '" + request.email() + "' est déjà utilisé par un autre passager.");
        }

        passagerMapper.updateEntityFromRequest(request, passager);
        Passager updated = passagerRepository.save(passager);

        log.info("Profil passager mis à jour avec succès : id={}", updated.getId());
        return passagerMapper.toResponse(updated);
    }

    @Override
    public PassagerResponse rechercherParEmail(String email) {
        Passager passager = passagerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("Recherche passager par email sans résultat : {}", email);
                    return new ResourceNotFoundException("Aucun passager trouvé avec l'email : " + email);
                });
        return passagerMapper.toResponse(passager);
    }

    @Override
    public List<ReservationResponse> consulterHistoriqueReservations(Long passagerId) {
        findPassagerOrThrow(passagerId);
        return reservationRepository.findByPassagerId(passagerId).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    private Passager findPassagerOrThrow(Long id) {
        return passagerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Passager introuvable avec l'id={}", id);
                    return new ResourceNotFoundException("Passager introuvable avec l'identifiant : " + id);
                });
    }
}
package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.request.VehiculeRequest;
import com.covoiturage.covoiturage_api.dto.response.VehiculeResponse;
import com.covoiturage.covoiturage_api.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Véhicules", description = "Gestion des véhicules des conducteurs")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping("/vehicules")
    @Operation(summary = "Ajouter un véhicule à un conducteur")
    public ResponseEntity<VehiculeResponse> ajouterVehicule(@Valid @RequestBody VehiculeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculeService.ajouterVehicule(request));
    }

    @PutMapping("/vehicules/{id}")
    @Operation(summary = "Modifier les informations d'un véhicule")
    public ResponseEntity<VehiculeResponse> modifierVehicule(
            @PathVariable Long id,
            @Valid @RequestBody VehiculeRequest request) {
        return ResponseEntity.ok(vehiculeService.modifierVehicule(id, request));
    }

    @GetMapping("/vehicules/{id}")
    @Operation(summary = "Consulter un véhicule")
    public ResponseEntity<VehiculeResponse> consulterVehicule(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.consulterVehicule(id));
    }

    @GetMapping("/conducteurs/{conducteurId}/vehicules")
    @Operation(summary = "Lister les véhicules d'un conducteur")
    public ResponseEntity<List<VehiculeResponse>> listerVehiculesConducteur(@PathVariable Long conducteurId) {
        return ResponseEntity.ok(vehiculeService.listerVehiculesConducteur(conducteurId));
    }

    @DeleteMapping("/vehicules/{id}")
    @Operation(summary = "Supprimer un véhicule non associé à un trajet")
    public ResponseEntity<Void> supprimerVehicule(
            @PathVariable Long id,
            @RequestParam Long conducteurId) {
        vehiculeService.supprimerVehicule(id, conducteurId);
        return ResponseEntity.noContent().build();
    }
}
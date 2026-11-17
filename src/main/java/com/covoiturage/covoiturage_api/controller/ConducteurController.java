package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.request.ConducteurRequest;
import com.covoiturage.covoiturage_api.dto.response.ConducteurResponse;
import com.covoiturage.covoiturage_api.service.ConducteurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
@RequiredArgsConstructor
@Tag(name = "Conducteurs", description = "Gestion des profils conducteurs")
public class ConducteurController {

    private final ConducteurService conducteurService;

    @PostMapping
    @Operation(summary = "Créer un profil conducteur")
    public ResponseEntity<ConducteurResponse> creerConducteur(@Valid @RequestBody ConducteurRequest request) {
        ConducteurResponse response = conducteurService.creerConducteur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lister tous les conducteurs")
    public ResponseEntity<List<ConducteurResponse>> listerConducteurs() {
        return ResponseEntity.ok(conducteurService.listerConducteurs());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les informations d'un conducteur")
    public ResponseEntity<ConducteurResponse> consulterConducteur(@PathVariable Long id) {
        return ResponseEntity.ok(conducteurService.consulterConducteur(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier le profil d'un conducteur")
    public ResponseEntity<ConducteurResponse> modifierConducteur(
            @PathVariable Long id,
            @Valid @RequestBody ConducteurRequest request) {
        return ResponseEntity.ok(conducteurService.modifierConducteur(id, request));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher un conducteur par nom ou email")
    public ResponseEntity<List<ConducteurResponse>> rechercherConducteurs(@RequestParam String query) {
        return ResponseEntity.ok(conducteurService.rechercherConducteurs(query));
    }
}
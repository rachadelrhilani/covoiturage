package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.request.PassagerRequest;
import com.covoiturage.covoiturage_api.dto.response.PassagerResponse;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.service.PassagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passagers")
@RequiredArgsConstructor
@Tag(name = "Passagers", description = "Gestion des profils passagers et historique de réservations")
public class PassagerController {

    private final PassagerService passagerService;

    @PostMapping
    @Operation(summary = "Créer un profil passager")
    public ResponseEntity<PassagerResponse> creerPassager(@Valid @RequestBody PassagerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passagerService.creerPassager(request));
    }

    @GetMapping
    @Operation(summary = "Lister tous les passagers")
    public ResponseEntity<List<PassagerResponse>> listerPassagers() {
        return ResponseEntity.ok(passagerService.listerPassagers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le profil d'un passager")
    public ResponseEntity<PassagerResponse> consulterPassager(@PathVariable Long id) {
        return ResponseEntity.ok(passagerService.consulterPassager(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier les informations d'un passager")
    public ResponseEntity<PassagerResponse> modifierPassager(
            @PathVariable Long id,
            @Valid @RequestBody PassagerRequest request) {
        return ResponseEntity.ok(passagerService.modifierPassager(id, request));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher un passager par email")
    public ResponseEntity<PassagerResponse> rechercherParEmail(@RequestParam String email) {
        return ResponseEntity.ok(passagerService.rechercherParEmail(email));
    }

    @GetMapping("/{id}/reservations")
    @Operation(summary = "Consulter l'historique des réservations d'un passager")
    public ResponseEntity<List<ReservationResponse>> consulterHistoriqueReservations(@PathVariable Long id) {
        return ResponseEntity.ok(passagerService.consulterHistoriqueReservations(id));
    }
}
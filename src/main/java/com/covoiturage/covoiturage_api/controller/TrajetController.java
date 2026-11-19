package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.request.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.service.TrajetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
@Tag(name = "Trajets", description = "Publication, recherche et gestion du cycle de vie des trajets")
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping
    @Operation(summary = "Publier un nouveau trajet multi-étapes")
    public ResponseEntity<TrajetResponse> publierTrajet(@Valid @RequestBody TrajetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trajetService.publierTrajet(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail complet d'un trajet (parcours, places restantes, conducteur)")
    public ResponseEntity<TrajetResponse> consulterTrajet(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.consulterTrajet(id));
    }

    @GetMapping("/{id}/reservations")
    @Operation(summary = "Consulter les réservations d'un trajet (conducteur propriétaire)")
    public ResponseEntity<List<ReservationResponse>> consulterReservationsTrajet(
            @PathVariable Long id,
            @RequestParam Long conducteurId) {
        return ResponseEntity.ok(trajetService.consulterReservationsTrajet(id, conducteurId));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des trajets multi-étapes disponibles avec pagination")
    public ResponseEntity<Page<TrajetResponse>> rechercherTrajets(
            @RequestParam String villeDepart,
            @RequestParam String villeArrivee,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(defaultValue = "1") Integer nbPlaces,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                trajetService.rechercherTrajets(villeDepart, villeArrivee, date, prixMax, nbPlaces, pageable));
    }
}
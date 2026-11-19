package com.covoiturage.covoiturage_api.controller;

import com.covoiturage.covoiturage_api.dto.request.ReservationRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Création, consultation et annulation des réservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Réserver une ou plusieurs places sur un trajet")
    public ResponseEntity<ReservationResponse> reserverPlaces(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.reserverPlaces(request));
    }

    @GetMapping("/passager/{passagerId}")
    @Operation(summary = "Consulter les réservations d'un passager")
    public ResponseEntity<List<ReservationResponse>> mesReservations(@PathVariable Long passagerId) {
        return ResponseEntity.ok(reservationService.mesReservations(passagerId));
    }

    @PatchMapping("/{id}/annuler")
    @Operation(summary = "Annuler une réservation (jusqu'à 24h avant le départ)")
    public ResponseEntity<ReservationResponse> annulerReservation(
            @PathVariable Long id,
            @RequestParam Long passagerId) {
        return ResponseEntity.ok(reservationService.annulerReservation(id, passagerId));
    }
}
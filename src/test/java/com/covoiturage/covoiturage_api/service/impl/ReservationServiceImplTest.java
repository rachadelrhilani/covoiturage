package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.request.ReservationRequest;
import com.covoiturage.covoiturage_api.dto.response.ReservationResponse;
import com.covoiturage.covoiturage_api.entity.Passager;
import com.covoiturage.covoiturage_api.entity.PointArret;
import com.covoiturage.covoiturage_api.entity.Reservation;
import com.covoiturage.covoiturage_api.entity.Trajet;
import com.covoiturage.covoiturage_api.enums.StatutReservation;
import com.covoiturage.covoiturage_api.enums.StatutTrajet;
import com.covoiturage.covoiturage_api.exception.AccessDeniedActionException;
import com.covoiturage.covoiturage_api.exception.BadRequestException;
import com.covoiturage.covoiturage_api.exception.BusinessRuleException;
import com.covoiturage.covoiturage_api.mapper.ReservationMapper;
import com.covoiturage.covoiturage_api.repository.PassagerRepository;
import com.covoiturage.covoiturage_api.repository.ReservationRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private PassagerRepository passagerRepository;
    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Passager passager;
    private Trajet trajet;

    @BeforeEach
    void setUp() {
        passager = Passager.builder()
                .id(1L)
                .nom("Fassi")
                .prenom("Salma")
                .email("salma@test.com")
                .build();

        trajet = Trajet.builder()
                .id(10L)
                .villeDepart("Rabat")
                .villeArrivee("Marrakech")
                .dateDepart(LocalDateTime.now().plusDays(3))
                .placesPropose(3)
                .statut(StatutTrajet.PUBLIE)
                .pointsArret(List.of(
                        PointArret.builder().ville("Rabat").ordre(0).prix(BigDecimal.ZERO).build(),
                        PointArret.builder().ville("Casablanca").ordre(1).prix(new BigDecimal("40.00")).build(),
                        PointArret.builder().ville("Marrakech").ordre(2).prix(new BigDecimal("120.00")).build()
                ))
                .reservations(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Réservation nominale : calcule le montant total et passe le trajet à COMPLET quand placesRestantes == 0")
    void reserverPlaces_ToutesPlaces_BasculeTrajetEnComplet() {
        ReservationRequest request = new ReservationRequest(1L, 10L, "Casablanca", "Marrakech", 3);
        ReservationResponse expectedResponse = mock(ReservationResponse.class);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findDetailById(10L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 10L, StatutReservation.CONFIRMEE))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationMapper.toResponse(any(Reservation.class))).thenReturn(expectedResponse);

        ReservationResponse result = reservationService.reserverPlaces(request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.COMPLET);
        verify(reservationRepository).save(argThat(r ->
                r.getMontantTotal().compareTo(new BigDecimal("240.00")) == 0 &&
                r.getStatut() == StatutReservation.CONFIRMEE
        ));
    }

    @Test
    @DisplayName("Réservation refusée : trajet n'est pas au statut PUBLIE (409)")
    void reserverPlaces_TrajetNonPublie_ThrowsBusinessRule() {
        trajet.setStatut(StatutTrajet.COMPLET);
        ReservationRequest request = new ReservationRequest(1L, 10L, "Rabat", "Marrakech", 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findDetailById(10L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> reservationService.reserverPlaces(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("pas disponible");
    }

    @Test
    @DisplayName("Réservation refusée : le passager a déjà une réservation CONFIRMEE sur ce trajet (409)")
    void reserverPlaces_DoublonPassager_ThrowsBusinessRule() {
        ReservationRequest request = new ReservationRequest(1L, 10L, "Rabat", "Marrakech", 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findDetailById(10L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 10L, StatutReservation.CONFIRMEE))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserverPlaces(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("déjà une réservation confirmée");
    }

    @Test
    @DisplayName("Réservation refusée : nombre de places demandées > places restantes (409)")
    void reserverPlaces_PlacesInsuffisantes_ThrowsBusinessRule() {
        ReservationRequest request = new ReservationRequest(1L, 10L, "Rabat", "Marrakech", 4);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findDetailById(10L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 10L, StatutReservation.CONFIRMEE))
                .thenReturn(false);

        assertThatThrownBy(() -> reservationService.reserverPlaces(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Places insuffisantes");
    }

    @Test
    @DisplayName("Réservation refusée : ville de montée située après la ville de descente (400)")
    void reserverPlaces_MonteeApresDescente_ThrowsBadRequest() {
        ReservationRequest request = new ReservationRequest(1L, 10L, "Marrakech", "Rabat", 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findDetailById(10L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 10L, StatutReservation.CONFIRMEE))
                .thenReturn(false);

        assertThatThrownBy(() -> reservationService.reserverPlaces(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("avant la ville de descente");
    }

    @Test
    @DisplayName("Réservation refusée : ville absente du parcours (400)")
    void reserverPlaces_VilleInexistante_ThrowsBadRequest() {
        ReservationRequest request = new ReservationRequest(1L, 10L, "Rabat", "Agadir", 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findDetailById(10L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(1L, 10L, StatutReservation.CONFIRMEE))
                .thenReturn(false);

        assertThatThrownBy(() -> reservationService.reserverPlaces(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ne fait pas partie de ce trajet");
    }

    @Test
    @DisplayName("Annulation réservation nominale : libère les places et rebascule le trajet de COMPLET à PUBLIE")
    void annulerReservation_PlusDe24hAvantDepart_ReouvreTrajet() {
        trajet.setStatut(StatutTrajet.COMPLET);
        Reservation reservation = Reservation.builder()
                .id(50L)
                .nbrPlaces(3)
                .statut(StatutReservation.CONFIRMEE)
                .passager(passager)
                .trajet(trajet)
                .build();
        trajet.getReservations().add(reservation);

        when(reservationRepository.findDetailById(50L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(mock(ReservationResponse.class));

        reservationService.annulerReservation(50L, 1L);

        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
    }

    @Test
    @DisplayName("Annulation réservation refusée : moins de 24 heures avant le départ (409)")
    void annulerReservation_MoinsDe24hAvantDepart_ThrowsBusinessRule() {
        trajet.setDateDepart(LocalDateTime.now().plusHours(10));
        Reservation reservation = Reservation.builder()
                .id(50L)
                .statut(StatutReservation.CONFIRMEE)
                .passager(passager)
                .trajet(trajet)
                .build();

        when(reservationRepository.findDetailById(50L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.annulerReservation(50L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("24 heures");
    }

    @Test
    @DisplayName("Annulation réservation refusée : réservation appartient à un autre passager (403)")
    void annulerReservation_AutrePassager_ThrowsAccessDenied() {
        Reservation reservation = Reservation.builder()
                .id(50L)
                .statut(StatutReservation.CONFIRMEE)
                .passager(passager)
                .trajet(trajet)
                .build();

        when(reservationRepository.findDetailById(50L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.annulerReservation(50L, 99L))
                .isInstanceOf(AccessDeniedActionException.class)
                .hasMessageContaining("propres réservations");
    }
}
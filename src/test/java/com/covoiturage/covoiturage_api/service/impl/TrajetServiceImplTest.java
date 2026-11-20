package com.covoiturage.covoiturage_api.service.impl;

import com.covoiturage.covoiturage_api.dto.request.PointArretRequest;
import com.covoiturage.covoiturage_api.dto.request.TrajetRequest;
import com.covoiturage.covoiturage_api.dto.response.TrajetResponse;
import com.covoiturage.covoiturage_api.entity.*;
import com.covoiturage.covoiturage_api.enums.StatutReservation;
import com.covoiturage.covoiturage_api.enums.StatutTrajet;
import com.covoiturage.covoiturage_api.exception.AccessDeniedActionException;
import com.covoiturage.covoiturage_api.exception.BadRequestException;
import com.covoiturage.covoiturage_api.exception.BusinessRuleException;
import com.covoiturage.covoiturage_api.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage_api.mapper.PointArretMapper;
import com.covoiturage.covoiturage_api.mapper.ReservationMapper;
import com.covoiturage.covoiturage_api.mapper.TrajetMapper;
import com.covoiturage.covoiturage_api.repository.ConducteurRepository;
import com.covoiturage.covoiturage_api.repository.ReservationRepository;
import com.covoiturage.covoiturage_api.repository.TrajetRepository;
import com.covoiturage.covoiturage_api.repository.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private ConducteurRepository conducteurRepository;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TrajetMapper trajetMapper;
    @Mock
    private PointArretMapper pointArretMapper;
    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private TrajetServiceImpl trajetService;

    private Conducteur conducteur;
    private Vehicule vehicule;
    private LocalDateTime departFutur;

    @BeforeEach
    void setUp() {
        conducteur = Conducteur.builder()
                .id(1L)
                .nom("Alami")
                .prenom("Hamid")
                .email("hamid@test.com")
                .build();

        vehicule = Vehicule.builder()
                .id(10L)
                .marque("Dacia")
                .modele("Logan")
                .capacite(4)
                .conducteur(conducteur)
                .build();

        departFutur = LocalDateTime.now().plusDays(2);
    }

    @Test
    @DisplayName("Publication trajet : cas nominal réussi")
    void publierTrajet_Nominal_Success() {
        List<PointArretRequest> points = List.of(
                new PointArretRequest("Rabat", 0, departFutur, BigDecimal.ZERO),
                new PointArretRequest("Casablanca", 1, departFutur.plusHours(1), new BigDecimal("40.00")),
                new PointArretRequest("Marrakech", 2, departFutur.plusHours(4), new BigDecimal("120.00"))
        );
        TrajetRequest request = new TrajetRequest(1L, 10L, 3, points);
        TrajetResponse expectedResponse = mock(TrajetResponse.class);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findDetailById(10L)).thenReturn(Optional.of(vehicule));
        when(pointArretMapper.toEntity(any(PointArretRequest.class))).thenAnswer(inv -> {
            PointArretRequest p = inv.getArgument(0);
            return PointArret.builder().ville(p.ville()).ordre(p.ordre()).heureDePassage(p.heureDePassage()).prix(p.prix()).build();
        });
        when(trajetRepository.save(any(Trajet.class))).thenAnswer(inv -> {
            Trajet t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });
        when(trajetMapper.toResponse(any(Trajet.class))).thenReturn(expectedResponse);

        TrajetResponse result = trajetService.publierTrajet(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(trajetRepository).save(argThat(t ->
                t.getVilleDepart().equals("Rabat") &&
                t.getVilleArrivee().equals("Marrakech") &&
                t.getStatut() == StatutTrajet.PUBLIE &&
                t.getPointsArret().size() == 3
        ));
    }

    @Test
    @DisplayName("Publication trajet refusée : véhicule n'appartient pas au conducteur (403)")
    void publierTrajet_VehiculeAutreConducteur_ThrowsAccessDenied() {
        Conducteur autreConducteur = Conducteur.builder().id(2L).build();
        vehicule.setConducteur(autreConducteur);

        TrajetRequest request = new TrajetRequest(1L, 10L, 3, List.of());

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findDetailById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(AccessDeniedActionException.class)
                .hasMessageContaining("propres véhicules");

        verify(trajetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Publication trajet refusée : places proposées > capacité du véhicule (400)")
    void publierTrajet_PlacesDepassentCapacite_ThrowsBadRequest() {
        TrajetRequest request = new TrajetRequest(1L, 10L, 5, List.of());

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findDetailById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Places proposées invalides");
    }

    @Test
    @DisplayName("Publication trajet refusée : date de départ dans le passé (400)")
    void publierTrajet_DepartDansLePasse_ThrowsBadRequest() {
        LocalDateTime passe = LocalDateTime.now().minusHours(2);
        List<PointArretRequest> points = List.of(
                new PointArretRequest("Rabat", 0, passe, BigDecimal.ZERO),
                new PointArretRequest("Casablanca", 1, passe.plusHours(1), new BigDecimal("40.00"))
        );
        TrajetRequest request = new TrajetRequest(1L, 10L, 2, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findDetailById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("dans le futur");
    }

    @Test
    @DisplayName("Publication trajet refusée : prix au point de départ différent de 0 (400)")
    void publierTrajet_PrixInitialNonNul_ThrowsBadRequest() {
        List<PointArretRequest> points = List.of(
                new PointArretRequest("Rabat", 0, departFutur, new BigDecimal("10.00")),
                new PointArretRequest("Casablanca", 1, departFutur.plusHours(1), new BigDecimal("40.00"))
        );
        TrajetRequest request = new TrajetRequest(1L, 10L, 2, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findDetailById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("égal à 0");
    }

    @Test
    @DisplayName("Publication trajet refusée : ville en doublon sur le parcours (400)")
    void publierTrajet_VilleEnDoublon_ThrowsBadRequest() {
        List<PointArretRequest> points = List.of(
                new PointArretRequest("Rabat", 0, departFutur, BigDecimal.ZERO),
                new PointArretRequest("rabat", 1, departFutur.plusHours(1), new BigDecimal("40.00"))
        );
        TrajetRequest request = new TrajetRequest(1L, 10L, 2, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findDetailById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("doublon");
    }

    @Test
    @DisplayName("Publication trajet refusée : heures ou prix non strictement croissants (400)")
    void publierTrajet_PrixNonCroissant_ThrowsBadRequest() {
        List<PointArretRequest> points = List.of(
                new PointArretRequest("Rabat", 0, departFutur, BigDecimal.ZERO),
                new PointArretRequest("Casablanca", 1, departFutur.plusHours(1), new BigDecimal("50.00")),
                new PointArretRequest("Settat", 2, departFutur.plusHours(2), new BigDecimal("50.00"))
        );
        TrajetRequest request = new TrajetRequest(1L, 10L, 2, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findDetailById(10L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publierTrajet(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("croissants");
    }

    @Test
    @DisplayName("Recherche multi-étapes : calcule correctement le prix du segment")
    void rechercherTrajets_CalculePrixSegment() {
        Trajet trajet = Trajet.builder()
                .id(100L)
                .pointsArret(List.of(
                        PointArret.builder().ville("Rabat").ordre(0).prix(BigDecimal.ZERO).build(),
                        PointArret.builder().ville("Settat").ordre(1).prix(new BigDecimal("70.00")).build(),
                        PointArret.builder().ville("Marrakech").ordre(2).prix(new BigDecimal("120.00")).build()
                ))
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Trajet> page = new PageImpl<>(List.of(trajet));
        TrajetResponse mappedResponse = mock(TrajetResponse.class);

        when(trajetRepository.searchTrajets(eq("Settat"), eq("Marrakech"), any(LocalDate.class), any(), eq(1), eq(pageable)))
                .thenReturn(page);
        when(trajetMapper.toResponseWithSegmentPrice(trajet, new BigDecimal("50.00"))).thenReturn(mappedResponse);

        Page<TrajetResponse> result = trajetService.rechercherTrajets(
                "Settat", "Marrakech", LocalDate.now().plusDays(2), new BigDecimal("60.00"), 1, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(trajetMapper).toResponseWithSegmentPrice(trajet, new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Terminer trajet refusé : heure de départ pas encore dépassée (409)")
    void terminerTrajet_AvantHeureDepart_ThrowsBusinessRule() {
        Trajet trajet = Trajet.builder()
                .id(100L)
                .conducteur(conducteur)
                .statut(StatutTrajet.PUBLIE)
                .dateDepart(LocalDateTime.now().plusHours(3))
                .build();

        when(trajetRepository.findDetailById(100L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> trajetService.terminerTrajet(100L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("après son heure de départ");
    }

    @Test
    @DisplayName("Terminer trajet : cas nominal après l'heure de départ")
    void terminerTrajet_ApresHeureDepart_Success() {
        Trajet trajet = Trajet.builder()
                .id(100L)
                .conducteur(conducteur)
                .statut(StatutTrajet.PUBLIE)
                .dateDepart(LocalDateTime.now().minusHours(1))
                .build();

        when(trajetRepository.findDetailById(100L)).thenReturn(Optional.of(trajet));
        when(trajetMapper.toResponse(trajet)).thenReturn(mock(TrajetResponse.class));

        trajetService.terminerTrajet(100L, 1L);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.TERMINE);
    }

    @Test
    @DisplayName("Annuler trajet : passe le trajet à ANNULE et annule toutes les réservations CONFIRMEE en cascade")
    void annulerTrajet_AnnuleReservationsEnCascade() {
        Reservation resa1 = Reservation.builder().id(1L).statut(StatutReservation.CONFIRMEE).build();
        Reservation resa2 = Reservation.builder().id(2L).statut(StatutReservation.CONFIRMEE).build();

        Trajet trajet = Trajet.builder()
                .id(100L)
                .conducteur(conducteur)
                .statut(StatutTrajet.COMPLET)
                .reservations(new ArrayList<>(List.of(resa1, resa2)))
                .build();

        when(trajetRepository.findDetailById(100L)).thenReturn(Optional.of(trajet));
        when(trajetMapper.toResponse(trajet)).thenReturn(mock(TrajetResponse.class));

        trajetService.annulerTrajet(100L, 1L);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.ANNULE);
        assertThat(resa1.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        assertThat(resa2.getStatut()).isEqualTo(StatutReservation.ANNULEE);
    }

    @Test
    @DisplayName("Consultation trajet introuvable : lève ResourceNotFoundException (404)")
    void consulterTrajet_NotFound_ThrowsException() {
        when(trajetRepository.findDetailById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trajetService.consulterTrajet(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
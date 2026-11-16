package com.covoiturage.covoiturage_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conducteurs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Conducteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(name = "numero_permis", nullable = false, unique = true)
    private String numeroPermis;

    @OneToMany(mappedBy = "conducteur", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vehicule> vehicules = new ArrayList<>();

    @OneToMany(mappedBy = "conducteur", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Trajet> trajets = new ArrayList<>();
}

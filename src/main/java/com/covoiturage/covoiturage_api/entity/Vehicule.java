package com.covoiturage.covoiturage_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    @Column(nullable = false, unique = true)
    private String immatriculation;

    @Column(nullable = false)
    private Integer capacite;

    @Column(nullable = false)
    private String couleur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Conducteur conducteur;
}
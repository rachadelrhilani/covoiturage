# Covoiturage API — Plateforme Backend Multi-Étapes

API RESTful développée avec **Java 17** et **Spring Boot 4** permettant de gérer une plateforme de covoiturage interurbain avec itinéraires multi-étapes, tarification dynamique par segment, gestion automatique des places disponibles et cycle de vie complet des trajets.

---

## Table des matières

- [Fonctionnalités principales](#-fonctionnalités-principales)
- [Stack technique](#-stack-technique)
- [Architecture du projet](#-architecture-du-projet)
- [Prérequis](#-prérequis)
- [Installation et démarrage](#-installation-et-démarrage)
- [Documentation API (Swagger)](#-documentation-api-swagger)
- [Endpoints principaux](#-endpoints-principaux)
- [Règles métier implémentées](#-règles-métier-implémentées)
- [Tests unitaires](#-tests-unitaires)

---

## Fonctionnalités principales

- **Gestion des utilisateurs :** Inscription, consultation et mise à jour des profils **Conducteurs** (avec numéro de permis unique) et **Passagers**.
- **Gestion de la flotte de véhicules :** Ajout, modification, consultation et suppression sécurisée des véhicules d'un conducteur (blocage si lié à un trajet).
- **Publication de trajets multi-étapes :** Création d'itinéraires composés d'une liste ordonnée de points d'arrêt avec validation stricte des horaires et tarifs cumulatifs.
- **Recherche segmentée & paginée :** Recherche de trajets disponibles entre deux étapes quelconques du parcours (montée avant descente), filtrée par date, nombre de places et prix maximal du segment.
- **Gestion des réservations :** Réservation de places sur un segment donné, calcul automatique du montant total, bascule automatique du statut du trajet (`PUBLIE` $\leftrightarrow$ `COMPLET`) et annulation sous condition de délai (24h).
- **Cycle de vie des trajets :** Clôture d'un trajet après l'heure de départ (`TERMINE`) ou annulation (`ANNULE`) entraînant l'annulation automatique en cascade des réservations confirmées.

---

## Stack technique

| Catégorie | Technologies |
| :--- | :--- |
| **Langage & Core** | Java 17 (Records, Stream API, Java Time API) |
| **Framework Backend** | Spring Boot 4.1.2, Spring Web MVC, Bean Validation |
| **Persistance & ORM** | Spring Data JPA, Hibernate 6, Requêtes JPQL optimisées (`JOIN FETCH`) |
| **Base de données** | PostgreSQL 16 |
| **Conteneurisation** | Docker & Docker Compose |
| **Mapping & Productivité** | MapStruct 1.6.3, Lombok, SLF4J |
| **Documentation API** | Springdoc OpenAPI 3 (Swagger UI) |
| **Tests & Qualité** | JUnit 5, Mockito, AssertJ |
| **Build & Versioning** | Maven, Git & GitHub (`main`, `dev`) |

---

## Architecture du projet

Le projet suit une **architecture en couches** stricte avec séparation des responsabilités :

```text
src/main/java/com/covoiturage/covoiturage_api/
├── controller/       # Exposition des endpoints REST & validation @Valid
├── service/          # Interfaces des services métier
│   └── impl/         # Implémentation des règles de gestion & transactions
├── repository/       # Accès aux données Spring Data JPA & requêtes JPQL
├── entity/           # Entités JPA (Conducteur, Passager, Vehicule, Trajet, PointArret, Reservation)
├── enums/            # Énumérations (StatutTrajet, StatutReservation)
├── dto/              # Java Records pour les requêtes (request) et réponses (response)
├── mapper/           # Interfaces MapStruct pour la conversion Entité <-> DTO
└── exception/        # Exceptions métier personnalisées & GlobalExceptionHandler (RFC 7807)
```

---

## Prérequis

- **Java JDK 17** ou supérieur
- **Maven 3.9+**
- **Docker** et **Docker Compose** (pour la base de données PostgreSQL)

---

## Installation et démarrage

### 1. Cloner le dépôt
```bash
git clone https://github.com/<votre-utilisateur>/covoiturage-api.git
cd covoiturage-api
```

### 2. Démarrer PostgreSQL avec Docker Compose
```bash
docker compose up -d
```
La base de données `covoiturage_db` sera accessible sur le port `5432` (utilisateur : `postgres`, mot de passe : `postgres`).

### 3. Compiler et lancer l'application Spring Boot
```bash
mvn clean spring-boot:run
```
L'API démarre sur `http://localhost:8080`.

---

## Documentation API (Swagger)

Une fois l'application démarrée, l'interface interactive **Swagger UI** est accessible à l'adresse :
- **Swagger UI :** `http://localhost:8080/swagger-ui.html`
- **Spécification OpenAPI JSON :** `http://localhost:8080/v3/api-docs`

---

## Endpoints principaux

### Conducteurs & Passagers
| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/conducteurs` | Inscrire un nouveau conducteur |
| `GET` | `/api/conducteurs/{id}` | Consulter le profil d'un conducteur |
| `PUT` | `/api/conducteurs/{id}` | Modifier le profil d'un conducteur |
| `GET` | `/api/conducteurs/{id}/vehicules` | Lister les véhicules d'un conducteur |
| `POST` | `/api/passagers` | Inscrire un nouveau passager |
| `GET` | `/api/passagers/{id}` | Consulter le profil d'un passager |
| `PUT` | `/api/passagers/{id}` | Modifier le profil d'un passager |

### Véhicules
| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/vehicules` | Ajouter un véhicule à un conducteur |
| `GET` | `/api/vehicules/{id}` | Consulter le détail d'un véhicule |
| `PUT` | `/api/vehicules/{id}` | Modifier un véhicule |
| `DELETE` | `/api/vehicules/{id}?conducteurId={id}` | Supprimer un véhicule (si non utilisé dans un trajet) |

### Trajets
| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/trajets` | Publier un trajet multi-étapes |
| `GET` | `/api/trajets/{id}` | Consulter le détail complet d'un trajet |
| `GET` | `/api/trajets/recherche` | Rechercher des trajets par segment, date, places et prix max |
| `GET` | `/api/trajets/{id}/reservations?conducteurId={id}` | Lister les réservations d'un trajet |
| `PATCH` | `/api/trajets/{id}/terminer?conducteurId={id}` | Marquer un trajet comme `TERMINE` |
| `PATCH` | `/api/trajets/{id}/annuler?conducteurId={id}` | Annuler un trajet (`ANNULE`) et ses réservations |

### Réservations
| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/reservations` | Réserver des places entre deux étapes |
| `GET` | `/api/reservations/passager/{passagerId}` | Consulter les réservations d'un passager |
| `PATCH` | `/api/reservations/{id}/annuler?passagerId={id}` | Annuler une réservation ($\ge$ 24h avant départ) |

---

## Règles métier implémentées

1. **Intégrité des parcours :**
   - Minimum 2 points d'arrêt séquentiels (`0, 1, 2...`).
   - Villes uniques sur le trajet, départ dans le futur, heures de passage et prix cumulatifs strictement croissants (prix initial = `0.00`).
   - Le conducteur doit être propriétaire du véhicule et les places proposées doivent être comprises entre `1` et la capacité du véhicule.
2. **Calculs dynamiques :**
   - **Places restantes :** $\text{Places proposées} - \sum \text{Places des réservations CONFIRMEE}$ (jamais stockées en base).
   - **Montant réservation :** $(\text{Prix}_{\text{descente}} - \text{Prix}_{\text{montée}}) \times \text{Nombre de places}$.
3. **Gestion automatique des statuts :**
   - Le trajet passe automatiquement à `COMPLET` lorsque `placesRestantes == 0`, et repasse à `PUBLIE` si une annulation libère des places.
   - Un passager ne peut posséder qu'une seule réservation `CONFIRMEE` par trajet et peut annuler jusqu'à **24 heures avant le départ**.
   - L'annulation d'un trajet bascule toutes ses réservations `CONFIRMEE` vers `ANNULEE` dans la même transaction.
4. **Gestion des erreurs normalisée (RFC 7807) :**
   - `400 Bad Request` (validation / paramètres invalides), `403 Forbidden` (action non autorisée), `404 Not Found` (ressource inexistante), `409 Conflict` (violation d'une règle métier ou contrainte d'unicité).

---

## Tests unitaires

Le projet inclut une suite de **21 tests unitaires** (`TrajetServiceImplTest` et `ReservationServiceImplTest`) développée avec **JUnit 5**, **Mockito** et **AssertJ** couvrant l'ensemble des scénarios nominaux et des refus métier.

Pour exécuter les tests :
```bash
mvn test
```
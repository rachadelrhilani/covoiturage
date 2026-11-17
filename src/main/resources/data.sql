
INSERT INTO conducteurs (id, nom, prenom, email, telephone, numero_permis) VALUES
(1, 'Alami', 'Hamid', 'hamid.alami@email.com', '0661112233', 'MA-10001'),
(2, 'Bennani', 'Youssef', 'youssef.bennani@email.com', '0662223344', 'MA-10002'),
(3, 'Chraibi', 'Nadia', 'nadia.chraibi@email.com', '0663334455', 'MA-10003'),
(4, 'Daoudi', 'Karim', 'karim.daoudi@email.com', '0664445566', 'MA-10004'),
(5, 'El Idrissi', 'Omar', 'omar.elidrissi@email.com', '0665556677', 'MA-10005')
ON CONFLICT (id) DO NOTHING;


INSERT INTO vehicules (id, marque, modele, immatriculation, capacite, conducteur_id) VALUES
(1, 'Dacia', 'Logan', '12345-A-1', 4, 1),
(2, 'Renault', 'Clio 5', '23456-B-26', 4, 2),
(3, 'Peugeot', '208', '34567-D-6', 3, 3),
(4, 'Volkswagen', 'Golf 8', '45678-E-1', 4, 4),
(5, 'Hyundai', 'Tucson', '56789-H-26', 5, 5)
ON CONFLICT (id) DO NOTHING;


INSERT INTO passagers (id, nom, prenom, email, telephone) VALUES
(1, 'Fassi', 'Salma', 'salma.fassi@email.com', '0671112233'),
(2, 'Tazi', 'Karim', 'karim.tazi@email.com', '0672223344'),
(3, 'Berrada', 'Imane', 'imane.berrada@email.com', '0673334455'),
(4, 'Mansouri', 'Mehdi', 'mehdi.mansouri@email.com', '0674445566'),
(5, 'Kabbaj', 'Sara', 'sara.kabbaj@email.com', '0675556677')
ON CONFLICT (id) DO NOTHING;


INSERT INTO trajets (id, ville_depart, ville_arrivee, date_depart, places_propose, statut, conducteur_id, vehicule_id) VALUES
(1, 'Rabat', 'Marrakech', '2026-11-25 17:00:00', 3, 'PUBLIE', 1, 1),
(2, 'Casablanca', 'Fes', '2026-11-26 09:00:00', 3, 'PUBLIE', 2, 2),
(3, 'Marrakech', 'Agadir', '2026-11-27 14:00:00', 2, 'PUBLIE', 3, 3),
(4, 'Tanger', 'Rabat', '2026-11-28 08:30:00', 4, 'PUBLIE', 4, 4),
(5, 'Fes', 'Oujda', '2026-11-29 10:00:00', 4, 'PUBLIE', 5, 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO points_arret (id, ville, ordre, heure_de_passage, prix, trajet_id) VALUES
(1, 'Rabat', 0, '2026-11-25 17:00:00', 0.00, 1),
(2, 'Casablanca', 1, '2026-11-25 18:15:00', 40.00, 1),
(3, 'Settat', 2, '2026-11-25 19:15:00', 70.00, 1),
(4, 'Marrakech', 3, '2026-11-25 21:30:00', 120.00, 1),
(5, 'Casablanca', 0, '2026-11-26 09:00:00', 0.00, 2),
(6, 'Meknes', 1, '2026-11-26 11:30:00', 80.00, 2),
(7, 'Fes', 2, '2026-11-26 12:30:00', 100.00, 2),
(8, 'Marrakech', 0, '2026-11-27 14:00:00', 0.00, 3),
(9, 'Agadir', 1, '2026-11-27 17:15:00', 90.00, 3),
(10, 'Tanger', 0, '2026-11-28 08:30:00', 0.00, 4),
(11, 'Kenitra', 1, '2026-11-28 10:45:00', 70.00, 4),
(12, 'Rabat', 2, '2026-11-28 11:30:00', 90.00, 4),
(13, 'Fes', 0, '2026-11-29 10:00:00', 0.00, 5),
(14, 'Taza', 1, '2026-11-29 11:30:00', 50.00, 5),
(15, 'Oujda', 2, '2026-11-29 14:00:00', 110.00, 5)
ON CONFLICT (id) DO NOTHING;


INSERT INTO reservations (id, nbr_places, montant_total, date_reservation, ville_montee, ville_descente, statut, passager_id, trajet_id) VALUES
(1, 1, 80.00, '2026-11-17', 'Casablanca', 'Meknes', 'CONFIRMEE', 1, 2),
(2, 1, 90.00, '2026-11-17', 'Marrakech', 'Agadir', 'CONFIRMEE', 2, 3),
(3, 2, 140.00, '2026-11-17', 'Tanger', 'Kenitra', 'CONFIRMEE', 3, 4),
(4, 1, 50.00, '2026-11-17', 'Fes', 'Taza', 'CONFIRMEE', 4, 5),
(5, 1, 60.00, '2026-11-17', 'Taza', 'Oujda', 'ANNULEE', 5, 5)
ON CONFLICT (id) DO NOTHING;

SELECT setval('conducteurs_id_seq', (SELECT MAX(id) FROM conducteurs));
SELECT setval('vehicules_id_seq', (SELECT MAX(id) FROM vehicules));
SELECT setval('passagers_id_seq', (SELECT MAX(id) FROM passagers));
SELECT setval('trajets_id_seq', (SELECT MAX(id) FROM trajets));
SELECT setval('points_arret_id_seq', (SELECT MAX(id) FROM points_arret));
SELECT setval('reservations_id_seq', (SELECT MAX(id) FROM reservations));
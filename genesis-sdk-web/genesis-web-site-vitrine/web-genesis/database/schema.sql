-- Tables de la base du backend Siteback (PostgreSQL, base « site_genesis »).
-- Même structure que les entités de Siteback (TEquipe, TNavbar, TContent, TCharteGraphique).
-- Puis exécuter data.sql pour les données initiales.

CREATE TABLE t_equipe (
    id        SERIAL PRIMARY KEY,
    nom       VARCHAR(100) NOT NULL,
    prenom    VARCHAR(100),
    poste     VARCHAR(100),
    email     VARCHAR(150) UNIQUE,
    photo     VARCHAR(255),
    github    VARCHAR(255),
    linkedin  VARCHAR(255)
);

CREATE TABLE t_navbar (
    id     SERIAL PRIMARY KEY,
    nom    VARCHAR(100) NOT NULL UNIQUE,
    ordre  INT NOT NULL DEFAULT 0
);

CREATE TABLE t_content (
    id         SERIAL PRIMARY KEY,
    nom        VARCHAR(150) NOT NULL,
    route      VARCHAR(255) NOT NULL UNIQUE,
    fichier    VARCHAR(255),
    ordre      INT NOT NULL DEFAULT 0,
    navbar_id  INT NOT NULL REFERENCES t_navbar(id)
);

CREATE TABLE t_charte_graphique (
    id          SERIAL PRIMARY KEY,
    file        VARCHAR(255) NOT NULL,
    version     VARCHAR(20),
    date_ajout  DATE DEFAULT CURRENT_DATE
);

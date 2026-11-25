-- Données initiales du site vitrine (section DATA du TODO) pour la base de Siteback (PostgreSQL).
-- t_content.fichier = chemin du fichier HTML envoyé, servi par le site Nuxt (public/upload/navbar/...).
-- La page /playground est une page applicative : pas de fichier.

INSERT INTO t_navbar (nom, ordre) VALUES ('Docs', 1);
INSERT INTO t_navbar (nom, ordre) VALUES ('Playground', 2);
INSERT INTO t_navbar (nom, ordre) VALUES ('Ecosystem', 3);
INSERT INTO t_navbar (nom, ordre) VALUES ('About', 4);

INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Quick start', '/docs/quick-start', '/upload/navbar/docs-quick-start.html', 1, (SELECT id FROM t_navbar WHERE nom = 'Docs'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Vs IA', '/docs/vs-ia', '/upload/navbar/docs-vs-ia.html', 2, (SELECT id FROM t_navbar WHERE nom = 'Docs'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Guide', '/docs/guide', '/upload/navbar/docs-guide.html', 3, (SELECT id FROM t_navbar WHERE nom = 'Docs'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Générer un projet', '/playground', NULL, 1, (SELECT id FROM t_navbar WHERE nom = 'Playground'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Structure du code Genesis', '/ecosystem/structure-genesis', '/upload/navbar/ecosystem-structure-genesis.html', 1, (SELECT id FROM t_navbar WHERE nom = 'Ecosystem'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Structure du projet généré', '/ecosystem/structure-projet', '/upload/navbar/ecosystem-structure-projet.html', 2, (SELECT id FROM t_navbar WHERE nom = 'Ecosystem'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Liste des frameworks', '/ecosystem/frameworks', '/upload/navbar/ecosystem-frameworks.html', 3, (SELECT id FROM t_navbar WHERE nom = 'Ecosystem'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Équipe', '/about/team', '/upload/navbar/about-team.html', 1, (SELECT id FROM t_navbar WHERE nom = 'About'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Releases', '/about/release', '/upload/navbar/about-release.html', 2, (SELECT id FROM t_navbar WHERE nom = 'About'));
INSERT INTO t_content (nom, route, fichier, ordre, navbar_id) VALUES ('Charte graphique', '/about/charte-graphique', '/upload/navbar/about-charte-graphique.html', 3, (SELECT id FROM t_navbar WHERE nom = 'About'));

INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Toky', NULL, 'Contributeur·rice', NULL, NULL, 'https://github.com/Toky20', NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Chan', 'Alex', 'Contributeur·rice', NULL, NULL, 'https://github.com/ChanAlex2357', NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Nomena', 'Vahatriniaina', 'Contributeur·rice', NULL, NULL, 'https://github.com/Nomena-1918', NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Voarisoa', 'Marinah', 'Contributeur·rice', NULL, NULL, NULL, NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Tahiana', NULL, 'Contributeur·rice', NULL, NULL, 'https://github.com/tahianarak', NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Wesley', NULL, 'Contributeur·rice', NULL, NULL, 'https://github.com/WesleyRS-creator', NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Sergiana', NULL, 'Contributeur·rice', NULL, NULL, 'https://github.com/sergianafr', NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Thomis', 'Rasolomandimby', 'Contributeur·rice', NULL, NULL, NULL, NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('SoaSoa', NULL, 'Contributeur·rice', NULL, NULL, NULL, NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Myah', 'Ny Aintso', 'Contributeur·rice', NULL, NULL, NULL, NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Toky', 'Ny Aina', 'Contributeur·rice', NULL, NULL, 'https://github.com/tookNyAina0907', NULL);
INSERT INTO t_equipe (nom, prenom, poste, email, photo, github, linkedin) VALUES ('Eriq', NULL, 'Contributeur·rice', NULL, NULL, 'https://github.com/Eriq606', NULL);

INSERT INTO t_charte_graphique (file, version, date_ajout) VALUES ('/upload/icon/charte_graphique-V2.pdf', 'V2', CURRENT_DATE);

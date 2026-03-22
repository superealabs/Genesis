-- ============================================================
-- Database: racing_db
-- Create the database (uncomment if needed)
-- ============================================================
-- CREATE DATABASE IF NOT EXISTS racing_db;
-- USE racing_db;

-- ============================================================
-- Table: circuits
-- Stores information about racing circuits
-- ============================================================
CREATE TABLE circuits
(
    circuit_id  INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    location    VARCHAR(100)  NOT NULL,
    country     VARCHAR(50)   NOT NULL,
    length_km   DECIMAL(6, 3) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_circuits_length CHECK (length_km > 0),
    CONSTRAINT chk_circuits_name_not_blank CHECK (name <> '')
) ENGINE=InnoDB;

-- ============================================================
-- Table: teams
-- Stores racing teams / constructors
-- ============================================================
CREATE TABLE teams
(
    team_id      INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    nationality  VARCHAR(50),
    founded_year INT,
    headquarters VARCHAR(100),
    website      VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_teams_founded_year CHECK (founded_year >= 1900 AND founded_year <= 2100),
    CONSTRAINT chk_teams_name_not_blank CHECK (name <> '')
) ENGINE=InnoDB;

-- ============================================================
-- Table: drivers
-- Stores driver details, linked to a team
-- ============================================================
CREATE TABLE drivers
(
    driver_id     INT AUTO_INCREMENT PRIMARY KEY,
    first_name    VARCHAR(50) NOT NULL,
    last_name     VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    nationality   VARCHAR(50),
    team_id       INT,
    debut_year    INT,
    bio           TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams (team_id) ON DELETE SET NULL,
    CONSTRAINT chk_drivers_debut_year CHECK (debut_year >= 1950),
    CONSTRAINT chk_drivers_name_not_blank CHECK (first_name <> '' AND last_name <> '')
) ENGINE=InnoDB;

-- Index on foreign key for better performance
CREATE INDEX idx_drivers_team ON drivers (team_id);

-- ============================================================
-- Table: races
-- Stores race events, each held at a circuit
-- ============================================================
CREATE TABLE races
(
    race_id    INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    circuit_id INT          NOT NULL,
    race_date  DATE         NOT NULL,
    season     INT         NOT NULL,
    laps       INT,
    status     VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (circuit_id) REFERENCES circuits (circuit_id) ON DELETE CASCADE,
    CONSTRAINT chk_races_laps CHECK (laps > 0),
    CONSTRAINT chk_races_season CHECK (season >= 1950)
) ENGINE=InnoDB;

-- Index on foreign key
CREATE INDEX idx_races_circuit ON races (circuit_id);

-- ============================================================
-- Table: results
-- Stores individual driver results for each race
-- ============================================================
CREATE TABLE results
(
    result_id      INT AUTO_INCREMENT PRIMARY KEY,
    race_id        INT  NOT NULL,
    driver_id      INT  NOT NULL,
    team_id        INT  NOT NULL,
    grid_position  INT,
    final_position INT,
    points         DECIMAL(5, 2) DEFAULT 0,
    fastest_lap    TIME NULL,
    status         VARCHAR(100),
    notes          TEXT,
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (race_id) REFERENCES races (race_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers (driver_id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams (team_id) ON DELETE CASCADE,
    UNIQUE KEY unique_result (race_id, driver_id),
    CONSTRAINT chk_results_position CHECK (grid_position > 0 AND final_position > 0),
    CONSTRAINT chk_results_points CHECK (points >= 0)
) ENGINE=InnoDB;

-- Indexes on foreign keys
CREATE INDEX idx_results_race ON results (race_id);
CREATE INDEX idx_results_driver ON results (driver_id);
CREATE INDEX idx_results_team ON results (team_id);

-- ============================================================
-- Test Data for Racing Database
-- Insert in order: circuits, teams, drivers, races, results
-- ============================================================

-- -----------------------------
-- Circuits
-- -----------------------------
INSERT INTO circuits (circuit_id, name, location, country, length_km, description)
VALUES (1, 'Monaco Grand Prix Circuit', 'Monte Carlo', 'Monaco', 3.337,
        'Street circuit with tight corners and tunnel, famous for glamour and difficulty to overtake.'),
       (2, 'Silverstone Circuit', 'Silverstone', 'United Kingdom', 5.891,
        'High-speed circuit, home of the British Grand Prix, known for Maggotts-Becketts complex.'),
       (3, 'Autodromo Nazionale Monza', 'Monza', 'Italy', 5.793,
        'Temple of Speed, long straights and chicanes, historic circuit.'),
       (4, 'Circuit de Spa-Francorchamps', 'Stavelot', 'Belgium', 7.004,
        'Challenging track with Eau Rouge-Raidillon, longest on calendar.'),
       (5, 'Suzuka International Racing Course', 'Suzuka', 'Japan', 5.807,
        'Figure-eight layout, technical corners, fan favorite.');

-- -----------------------------
-- Teams
-- -----------------------------
INSERT INTO teams (team_id, name, nationality, founded_year, headquarters, website)
VALUES (1, 'Scuderia Ferrari', 'Italian', 1950, 'Maranello, Italy', 'https://www.ferrari.com'),
       (2, 'Mercedes-AMG Petronas', 'German', 1970, 'Brackley, United Kingdom', 'https://www.mercedesamgf1.com'),
       (3, 'Red Bull Racing', 'Austrian', 2005, 'Milton Keynes, United Kingdom', 'https://www.redbullracing.com'),
       (4, 'McLaren F1 Team', 'British', 1963, 'Woking, United Kingdom', 'https://www.mclaren.com/racing');

-- -----------------------------
-- Drivers
-- -----------------------------
INSERT INTO drivers (driver_id, first_name, last_name, date_of_birth, nationality, team_id, debut_year, bio)
VALUES (1, 'Charles', 'Leclerc', '1997-10-16', 'Monegasque', 1, 2018,
        'Ferrari academy graduate, multiple pole positions, race winner.'),
       (2, 'Carlos', 'Sainz', '1994-09-01', 'Spanish', 1, 2015,
        'Experienced driver, race winner, known for consistency.'),
       (3, 'Lewis', 'Hamilton', '1985-01-07', 'British', 2, 2007,
        'Seven-time world champion, most wins in F1 history.'),
       (4, 'George', 'Russell', '1998-02-15', 'British', 2, 2019, 'Young talent, race winner, Mercedes future.'),
       (5, 'Max', 'Verstappen', '1997-09-30', 'Dutch', 3, 2015,
        'Reigning world champion, aggressive style, record-breaking season.'),
       (6, 'Sergio', 'Perez', '1990-01-26', 'Mexican', 3, 2011,
        'Experienced, street circuit specialist, multiple wins.'),
       (7, 'Lando', 'Norris', '1999-11-13', 'British', 4, 2019,
        'Charismatic, podium finisher, improving year by year.'),
       (8, 'Oscar', 'Piastri', '2001-04-06', 'Australian', 4, 2023,
        'Rookie, Formula 2 and Formula 3 champion, high potential.');

-- -----------------------------
-- Races
-- -----------------------------
INSERT INTO races (race_id, name, circuit_id, race_date, season, laps, status)
VALUES (1, 'Monaco Grand Prix', 1, '2023-05-28', 2023, 78, 'Completed'),
       (2, 'British Grand Prix', 2, '2023-07-09', 2023, 52, 'Completed'),
       (3, 'Italian Grand Prix', 3, '2023-09-03', 2023, 53, 'Completed'),
       (4, 'Belgian Grand Prix', 4, '2023-07-30', 2023, 44, 'Completed'),
       (5, 'Japanese Grand Prix', 5, '2023-09-24', 2023, 53, 'Scheduled');
-- keep one future race

-- -----------------------------
-- Results (for races 1-4)
-- -----------------------------
-- Race 1: Monaco Grand Prix
INSERT INTO results (race_id, driver_id, team_id, grid_position, final_position, points, fastest_lap, status, notes)
VALUES (1, 1, 1, 1, 1, 25.0, '01:14:05', 'Finished', 'Pole to win, perfect weekend'),
       (1, 2, 1, 4, 4, 12.0, '01:15:02', 'Finished', 'Solid drive, no mistakes'),
       (1, 3, 2, 5, 5, 10.0, '01:15:08', 'Finished', 'Struggled with setup'),
       (1, 4, 2, 6, 6, 8.0, '01:16:00', 'Finished', 'Good recovery after poor qualifying'),
       (1, 5, 3, 2, 2, 18.0, '01:14:08', 'Finished', 'Could not pass Leclerc'),
       (1, 6, 3, 3, 3, 15.0, '01:15:01', 'Finished', 'Podium finish, strong pace'),
       (1, 7, 4, 7, 7, 6.0, '01:16:03', 'Finished', 'Points finish'),
       (1, 8, 4, 8, 8, 4.0, '01:16:07', 'Finished', 'Solid debut in Monaco');

-- Race 2: British Grand Prix
INSERT INTO results (race_id, driver_id, team_id, grid_position, final_position, points, fastest_lap, status, notes)
VALUES (2, 1, 1, 3, 3, 15.0, '01:30:02', 'Finished', 'Podium, good strategy'),
       (2, 2, 1, 5, 5, 10.0, '01:31:00', 'Finished', 'Fought hard for points'),
       (2, 3, 2, 7, 9, 2.0, '01:31:05', 'Finished', 'Damage limitation after contact'),
       (2, 4, 2, 6, 4, 12.0, '01:30:08', 'Finished', 'Strong home race'),
       (2, 5, 3, 1, 1, 25.0, '01:29:08', 'Finished', 'Dominant win, fastest lap'),
       (2, 6, 3, 4, 6, 8.0, '01:31:02', 'Finished', 'Recovered from poor start'),
       (2, 7, 4, 2, 2, 18.0, '01:30:05', 'Finished', 'Best result of season so far'),
       (2, 8, 4, 8, 8, 4.0, '01:31:08', 'Finished', 'Consistent drive');

-- Race 3: Italian Grand Prix
INSERT INTO results (race_id, driver_id, team_id, grid_position, final_position, points, fastest_lap, status, notes)
VALUES (3, 1, 1, 2, 2, 18.0, '01:22:01', 'Finished', 'Close battle with Verstappen'),
       (3, 2, 1, 6, 6, 8.0, '01:23:04', 'Finished', 'Struggled with tire wear'),
       (3, 3, 2, 8, 8, 4.0, '01:23:09', 'Finished', 'Could not make progress'),
       (3, 4, 2, 4, 5, 10.0, '01:22:09', 'Finished', 'Good defense against Perez'),
       (3, 5, 3, 1, 1, 25.0, '01:21:08', 'Finished', 'Another win, lights to flag'),
       (3, 6, 3, 5, 4, 12.0, '01:23:01', 'Finished', 'Recovered after pit stop issue'),
       (3, 7, 4, 3, 3, 15.0, '01:22:05', 'Finished', 'Podium at Monza'),
       (3, 8, 4, 9, 10, 1.0, '01:24:02', 'Finished', 'Just managed to score point');

-- Race 4: Belgian Grand Prix
INSERT INTO results (race_id, driver_id, team_id, grid_position, final_position, points, fastest_lap, status, notes)
VALUES (4, 1, 1, 3, 3, 15.0, '01:48:05', 'Finished', 'Solid podium in wet conditions'),
       (4, 2, 1, 6, 8, 4.0, '01:49:08', 'Finished', 'Spun but recovered'),
       (4, 3, 2, 4, 5, 10.0, '01:49:00', 'Finished', 'Quiet race'),
       (4, 4, 2, 5, 6, 8.0, '01:49:03', 'Finished', 'Consistent points'),
       (4, 5, 3, 1, 1, 25.0, '01:47:09', 'Finished', 'Masterclass in wet, fastest lap'),
       (4, 6, 3, 2, 2, 18.0, '01:48:02', 'Finished', 'Great team result'),
       (4, 7, 4, 7, 4, 12.0, '01:48:09', 'Finished', 'Strong drive through field'),
       (4, 8, 4, 8, 7, 6.0, '01:49:05', 'Finished', 'Rookie shines in tricky conditions');

-- ============================================================
-- Views for Racing Database
-- Provide different analytical perspectives on racing data
-- ============================================================

-- ============================================================
-- View 1: race_summary
-- Shows a summary of each race with circuit details
-- ============================================================
CREATE OR REPLACE VIEW race_summary AS
SELECT
    r.race_id AS id, -- Aliased for genesis-core
    r.name AS race_name,
    r.race_date,
    r.season,
    r.laps,
    r.status AS race_status,
    c.name AS circuit_name,
    c.location,
    c.country,
    c.length_km,
    c.description AS circuit_description
FROM races r
         JOIN circuits c ON r.circuit_id = c.circuit_id;

-- ============================================================
-- View 2: driver_standings
-- Calculates total points per driver across all races
-- ============================================================
CREATE OR REPLACE VIEW driver_standings AS
SELECT
    d.driver_id AS id, -- Aliased for genesis-core
    d.first_name,
    d.last_name,
    d.nationality,
    t.name AS current_team,
    COUNT(DISTINCT res.race_id) AS races_entered,
    SUM(res.points) AS total_points,
    AVG(res.final_position) AS avg_finish_position,
    COUNT(CASE WHEN res.final_position = 1 THEN 1 END) AS wins,
    COUNT(CASE WHEN res.final_position <= 3 THEN 1 END) AS podiums,
    COUNT(CASE WHEN res.status = 'Finished' THEN 1 END) AS races_finished,
    MIN(res.fastest_lap) AS best_fastest_lap
FROM drivers d
         LEFT JOIN teams t ON d.team_id = t.team_id
         LEFT JOIN results res ON d.driver_id = res.driver_id
GROUP BY d.driver_id, d.first_name, d.last_name, d.nationality, t.name;

-- ============================================================
-- View 3: team_standings
-- Calculates total points per team (constructor championship)
-- ============================================================
CREATE OR REPLACE VIEW team_standings AS
SELECT
    t.team_id AS id, -- Aliased for genesis-core
    t.name AS team_name,
    t.nationality,
    t.headquarters,
    COUNT(DISTINCT res.race_id) AS races_entered,
    SUM(res.points) AS total_points,
    COUNT(CASE WHEN res.final_position = 1 THEN 1 END) AS wins,
    COUNT(CASE WHEN res.final_position <= 3 THEN 1 END) AS podiums,
    ROUND(AVG(res.final_position), 2) AS avg_finish_position,
    COUNT(DISTINCT d.driver_id) AS drivers_used
FROM teams t
         LEFT JOIN results res ON t.team_id = res.team_id
         LEFT JOIN drivers d ON t.team_id = d.team_id
GROUP BY t.team_id, t.name, t.nationality, t.headquarters;

-- ============================================================
-- View 4: race_results_detail
-- Detailed race results with driver and team information
-- ============================================================
CREATE OR REPLACE VIEW race_results_detail AS
SELECT
    res.result_id AS id, -- Aliased for genesis-core
    r.race_id,
    r.name AS race_name,
    r.race_date,
    r.season,
    c.name AS circuit_name,
    c.country,
    d.driver_id,
    CONCAT(d.first_name, ' ', d.last_name) AS driver_name,
    d.nationality AS driver_nationality,
    t.name AS team_name,
    res.grid_position,
    res.final_position,
    res.points,
    res.fastest_lap, -- Removed TIME_FORMAT for compatibility
    res.status AS result_status,
    CASE
        WHEN res.final_position = 1 THEN 'Winner'
        WHEN res.final_position <= 3 THEN 'Podium'
        WHEN res.final_position <= 10 THEN 'Points'
        ELSE 'No points'
        END AS points_category,
    res.notes
FROM results res
         JOIN races r ON res.race_id = r.race_id
         JOIN circuits c ON r.circuit_id = c.circuit_id
         JOIN drivers d ON res.driver_id = d.driver_id
         JOIN teams t ON res.team_id = t.team_id;

-- ============================================================
-- View 5: circuit_performance
-- Analyzes performance at each circuit
-- ============================================================
CREATE OR REPLACE VIEW circuit_performance AS
SELECT
    c.circuit_id AS id, -- Aliased for genesis-core
    c.name AS circuit_name,
    c.country,
    c.length_km,
    COUNT(DISTINCT r.race_id) AS total_races_held,
    COUNT(DISTINCT res.driver_id) AS unique_drivers,
    MIN(res.fastest_lap) AS all_time_fastest_lap,
    (SELECT CONCAT(d_win.first_name, ' ', d_win.last_name)
     FROM results res_win
     JOIN drivers d_win ON res_win.driver_id = d_win.driver_id
     WHERE res_win.race_id = MAX(r.race_id) AND res_win.final_position = 1
     LIMIT 1) AS most_recent_winner,
    MAX(CASE WHEN res.final_position = 1 THEN r.race_date END) AS last_win_date
FROM circuits c
         LEFT JOIN races r ON c.circuit_id = r.circuit_id
         LEFT JOIN results res ON r.race_id = res.race_id
GROUP BY c.circuit_id, c.name, c.country, c.length_km;

-- ============================================================
-- View 6: driver_form
-- Shows recent performance for each driver (last 3 races)
-- ============================================================
CREATE OR REPLACE VIEW driver_form AS
WITH ranked_results AS (
    SELECT
        d.driver_id,
        CONCAT(d.first_name, ' ', d.last_name) AS driver_name,
        r.race_id,
        r.name AS race_name,
        r.race_date,
        res.final_position,
        res.points,
        ROW_NUMBER() OVER (PARTITION BY d.driver_id ORDER BY r.race_date DESC) AS race_recency
    FROM drivers d
             JOIN results res ON d.driver_id = res.driver_id
             JOIN races r ON res.race_id = r.race_id
    -- Removed CURDATE() for compatibility
)
SELECT
    driver_id AS id, -- Aliased for genesis-core
    driver_name,
    MAX(CASE WHEN race_recency = 1 THEN race_name END) AS last_race,
    MAX(CASE WHEN race_recency = 1 THEN final_position END) AS last_position,
    MAX(CASE WHEN race_recency = 1 THEN points END) AS last_points,
    MAX(CASE WHEN race_recency = 2 THEN race_name END) AS previous_race,
    MAX(CASE WHEN race_recency = 2 THEN final_position END) AS previous_position,
    MAX(CASE WHEN race_recency = 2 THEN points END) AS previous_points,
    MAX(CASE WHEN race_recency = 3 THEN race_name END) AS race_before,
    MAX(CASE WHEN race_recency = 3 THEN final_position END) AS position_before,
    MAX(CASE WHEN race_recency = 3 THEN points END) AS points_before,
    AVG(final_position) AS avg_last_3_positions,
    SUM(points) AS points_last_3_races
FROM ranked_results
WHERE race_recency <= 3
GROUP BY driver_id, driver_name;

-- ============================================================
-- View 7: season_statistics
-- Overall statistics for each season
-- ============================================================
CREATE OR REPLACE VIEW season_statistics AS
SELECT
    r.season AS id, -- Aliased for genesis-core
    COUNT(DISTINCT r.race_id) AS total_races,
    COUNT(DISTINCT r.circuit_id) AS different_circuits,
    COUNT(DISTINCT res.driver_id) AS drivers_participated,
    COUNT(DISTINCT res.team_id) AS teams_participated,
    SUM(res.points) AS total_points_awarded,
    AVG(res.points) AS avg_points_per_race,
    COUNT(CASE WHEN res.status = 'Finished' THEN 1 END) AS total_finishes,
    COUNT(CASE WHEN res.status != 'Finished' THEN 1 END) AS total_retirements
FROM races r
         LEFT JOIN results res ON r.race_id = res.race_id
GROUP BY r.season;

-- ============================================================
-- View 8: head_to_head
-- Compares teammates' performance (simplified for compatibility)
-- ============================================================
CREATE OR REPLACE VIEW head_to_head AS
SELECT
    CONCAT(t.team_id, '-', d1.driver_id, '-', d2.driver_id) AS id, -- Composite ID for uniqueness
    t.name AS team_name,
    d1.driver_id AS driver1_id,
    CONCAT(d1.first_name, ' ', d1.last_name) AS driver1,
    d2.driver_id AS driver2_id,
    CONCAT(d2.first_name, ' ', d2.last_name) AS driver2,
    SUM(CASE WHEN res1.final_position < res2.final_position THEN 1 ELSE 0 END) AS driver1_wins,
    SUM(CASE WHEN res2.final_position < res1.final_position THEN 1 ELSE 0 END) AS driver2_wins,
    SUM(res1.points) AS driver1_points,
    SUM(res2.points) AS driver2_points
FROM results res1
JOIN results res2 ON res1.race_id = res2.race_id AND res1.team_id = res2.team_id AND res1.driver_id < res2.driver_id
JOIN teams t ON res1.team_id = t.team_id
JOIN drivers d1 ON res1.driver_id = d1.driver_id
JOIN drivers d2 ON res2.driver_id = d2.driver_id
GROUP BY t.team_id, t.name, d1.driver_id, d2.driver_id;

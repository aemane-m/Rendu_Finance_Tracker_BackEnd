-- =============================================================================
-- DONNÉES DE TEST — Finance Tracker
-- =============================================================================
-- 1. Remplacez 'VOTRE-USER-ID-ICI' par votre UUID Supabase (auth.users.id)
-- 2. Exécutez ce script via le SQL Editor de Supabase ou psql
-- =============================================================================

DO $$
DECLARE
    v_user  UUID        := '476053f8-bef5-4396-9114-7f110e0b17e1';
    now_ts  TIMESTAMPTZ := NOW();

    -- IDs catégories
    c_alim    BIGINT;
    c_trans   BIGINT;
    c_loyer   BIGINT;
    c_loisirs BIGINT;
    c_sante   BIGINT;
    c_resto   BIGINT;
    c_abos    BIGINT;

    -- IDs goals
    g_vacances  BIGINT;
    g_iphone    BIGINT;
    g_formation BIGINT;
    g_voiture   BIGINT;
    g_mariage   BIGINT;
    g_urgence   BIGINT;
    g_laptop    BIGINT;

    -- IDs dettes
    d_voiture BIGINT;
    d_ami     BIGINT;

    -- IDs transactions planifiées
    pt_loyer   BIGINT;
    pt_salaire BIGINT;
    pt_netflix BIGINT;
    pt_spotify BIGINT;

    -- IDs transactions (pour les liens planned_runs)
    tr_loyer_jan   BIGINT;  tr_salaire_jan BIGINT;
    tr_loyer_feb   BIGINT;  tr_salaire_feb BIGINT;
    tr_loyer_mar   BIGINT;  tr_salaire_mar BIGINT;

BEGIN

-- =============================================================================
-- NETTOYAGE (permet de relancer le script sans conflit)
-- =============================================================================
DELETE FROM planned_transaction_runs     WHERE user_id = v_user;
DELETE FROM planned_transactions         WHERE user_id = v_user;
DELETE FROM goal_contributions           WHERE user_id = v_user;
DELETE FROM goals                        WHERE user_id = v_user;
DELETE FROM debt_payments                WHERE user_id = v_user;
DELETE FROM debts                        WHERE user_id = v_user;
DELETE FROM calendar_events              WHERE user_id = v_user;
DELETE FROM budgets                      WHERE user_id = v_user;
DELETE FROM transactions                 WHERE user_id = v_user;
DELETE FROM categories                   WHERE user_id = v_user;

-- =============================================================================
-- CATEGORIES PERSONNALISÉES
-- =============================================================================
INSERT INTO categories (user_id, name, icon, color, is_default, created_at)
VALUES (v_user, 'Alimentation', '🛒', '#4CAF50', FALSE, now_ts) RETURNING id INTO c_alim;

INSERT INTO categories (user_id, name, icon, color, is_default, created_at)
VALUES (v_user, 'Transport', '🚗', '#2196F3', FALSE, now_ts) RETURNING id INTO c_trans;

INSERT INTO categories (user_id, name, icon, color, is_default, created_at)
VALUES (v_user, 'Loyer', '🏠', '#9C27B0', FALSE, now_ts) RETURNING id INTO c_loyer;

INSERT INTO categories (user_id, name, icon, color, is_default, created_at)
VALUES (v_user, 'Loisirs', '🎮', '#FF9800', FALSE, now_ts) RETURNING id INTO c_loisirs;

INSERT INTO categories (user_id, name, icon, color, is_default, created_at)
VALUES (v_user, 'Santé', '💊', '#F44336', FALSE, now_ts) RETURNING id INTO c_sante;

INSERT INTO categories (user_id, name, icon, color, is_default, created_at)
VALUES (v_user, 'Restaurants', '🍽️', '#E91E63', FALSE, now_ts) RETURNING id INTO c_resto;

INSERT INTO categories (user_id, name, icon, color, is_default, created_at)
VALUES (v_user, 'Abonnements', '📱', '#00BCD4', FALSE, now_ts) RETURNING id INTO c_abos;


-- =============================================================================
-- TRANSACTIONS — Octobre 2025
-- =============================================================================
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 2800.00, 'INCOME', NULL,     'Salaire Octobre',       '2025-10-01', NULL,                     'MANUAL', now_ts, now_ts) RETURNING id INTO tr_salaire_jan; -- réutilisé plus bas (juste variable tampon)

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 900.00,  'EXPENSE', c_loyer, 'Loyer Octobre',         '2025-10-02', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 45.00,   'EXPENSE', c_trans, 'Navigo Octobre',        '2025-10-03', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 87.50,   'EXPENSE', c_alim,  'Courses Carrefour',     '2025-10-05', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 17.99,   'EXPENSE', c_loisirs,'Netflix',              '2025-10-08', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 63.20,   'EXPENSE', c_alim,  'Monoprix',              '2025-10-12', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 32.00,   'EXPENSE', c_loisirs,'Ciné + restaurant',    '2025-10-15', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 25.00,   'EXPENSE', c_sante, 'Pharmacie',             '2025-10-18', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 500.00,  'INCOME', NULL,     'Freelance Octobre',     '2025-10-20', 'Mission React',          'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 55.80,   'EXPENSE', c_alim,  'Lidl',                  '2025-10-25', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 9.99,    'EXPENSE', c_loisirs,'Spotify',              '2025-10-10', NULL,                     'MANUAL', now_ts, now_ts);

-- =============================================================================
-- TRANSACTIONS — Novembre 2025
-- =============================================================================
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 2800.00, 'INCOME', NULL,     'Salaire Novembre',      '2025-11-01', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 900.00,  'EXPENSE', c_loyer, 'Loyer Novembre',        '2025-11-02', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 45.00,   'EXPENSE', c_trans, 'Navigo Novembre',       '2025-11-03', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 95.40,   'EXPENSE', c_alim,  'Courses semaine 1',     '2025-11-06', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 17.99,   'EXPENSE', c_loisirs,'Netflix',              '2025-11-08', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 120.00,  'EXPENSE', c_loisirs,'Week-end Lyon',        '2025-11-09', 'Hôtel + restaurant',     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 9.99,    'EXPENSE', c_loisirs,'Spotify',              '2025-11-10', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 150.00,  'EXPENSE', c_sante, 'Dentiste',              '2025-11-14', 'Détartrage',             'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 78.30,   'EXPENSE', c_alim,  'Courses semaine 3',     '2025-11-20', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 35.00,   'EXPENSE', c_trans, 'Plein essence',         '2025-11-22', NULL,                     'MANUAL', now_ts, now_ts);

-- =============================================================================
-- TRANSACTIONS — Décembre 2025
-- =============================================================================
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 2800.00, 'INCOME', NULL,     'Salaire Décembre',      '2025-12-01', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 900.00,  'EXPENSE', c_loyer, 'Loyer Décembre',        '2025-12-02', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 45.00,   'EXPENSE', c_trans, 'Navigo Décembre',       '2025-12-03', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 60.00,   'EXPENSE', c_sante, 'Opticien',              '2025-12-05', 'Lentilles de contact',   'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 17.99,   'EXPENSE', c_loisirs,'Netflix',              '2025-12-08', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 9.99,    'EXPENSE', c_loisirs,'Spotify',              '2025-12-10', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 85.00,   'EXPENSE', c_alim,  'Courses semaine 2',     '2025-12-10', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 500.00,  'INCOME', NULL,     'Prime de fin d''année', '2025-12-15', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 350.00,  'EXPENSE', c_loisirs,'Cadeaux Noël',         '2025-12-18', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 200.00,  'EXPENSE', c_alim,  'Courses fêtes',         '2025-12-20', 'Réveillon',              'MANUAL', now_ts, now_ts);

-- =============================================================================
-- TRANSACTIONS — Janvier 2026
-- =============================================================================
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 2800.00, 'INCOME', NULL,     'Salaire Janvier',       '2026-01-01', NULL,                     'PLANNED', now_ts, now_ts) RETURNING id INTO tr_salaire_jan;

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 900.00,  'EXPENSE', c_loyer, 'Loyer Janvier',         '2026-01-02', NULL,                     'PLANNED', now_ts, now_ts) RETURNING id INTO tr_loyer_jan;

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 45.00,   'EXPENSE', c_trans, 'Navigo Janvier',        '2026-01-03', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 39.00,   'EXPENSE', c_sante, 'Salle de sport',        '2026-01-05', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 92.60,   'EXPENSE', c_alim,  'Courses Carrefour',     '2026-01-07', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 17.99,   'EXPENSE', c_loisirs,'Netflix',              '2026-01-08', NULL,                     'PLANNED', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 9.99,    'EXPENSE', c_loisirs,'Spotify',              '2026-01-10', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 75.00,   'EXPENSE', c_alim,  'Courses semaine 3',     '2026-01-21', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 50.00,   'EXPENSE', c_loisirs,'Concert',              '2026-01-25', NULL,                     'MANUAL', now_ts, now_ts);

-- =============================================================================
-- TRANSACTIONS — Février 2026
-- =============================================================================
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 2800.00, 'INCOME', NULL,     'Salaire Février',       '2026-02-01', NULL,                     'PLANNED', now_ts, now_ts) RETURNING id INTO tr_salaire_feb;

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 900.00,  'EXPENSE', c_loyer, 'Loyer Février',         '2026-02-02', NULL,                     'PLANNED', now_ts, now_ts) RETURNING id INTO tr_loyer_feb;

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 45.00,   'EXPENSE', c_trans, 'Navigo Février',        '2026-02-03', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 39.00,   'EXPENSE', c_sante, 'Salle de sport',        '2026-02-05', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 88.40,   'EXPENSE', c_alim,  'Courses Carrefour',     '2026-02-05', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 17.99,   'EXPENSE', c_loisirs,'Netflix',              '2026-02-08', NULL,                     'PLANNED', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 9.99,    'EXPENSE', c_loisirs,'Spotify',              '2026-02-10', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 200.00,  'EXPENSE', c_loisirs,'Saint-Valentin',       '2026-02-14', 'Restaurant',             'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 65.00,   'EXPENSE', c_alim,  'Courses semaine 3',     '2026-02-18', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 500.00,  'INCOME', NULL,     'Freelance Février',     '2026-02-20', 'Mission backend Java',   'MANUAL', now_ts, now_ts);

-- =============================================================================
-- TRANSACTIONS — Mars 2026
-- =============================================================================
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 2800.00, 'INCOME', NULL,     'Salaire Mars',          '2026-03-01', NULL,                     'PLANNED', now_ts, now_ts) RETURNING id INTO tr_salaire_mar;

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 900.00,  'EXPENSE', c_loyer, 'Loyer Mars',            '2026-03-02', NULL,                     'PLANNED', now_ts, now_ts) RETURNING id INTO tr_loyer_mar;

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 45.00,   'EXPENSE', c_trans, 'Navigo Mars',           '2026-03-03', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 39.00,   'EXPENSE', c_sante, 'Salle de sport',        '2026-03-05', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 95.20,   'EXPENSE', c_alim,  'Courses Carrefour',     '2026-03-05', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 17.99,   'EXPENSE', c_loisirs,'Netflix',              '2026-03-08', NULL,                     'PLANNED', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 9.99,    'EXPENSE', c_loisirs,'Spotify',              '2026-03-10', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 28.00,   'EXPENSE', c_trans, 'Plein essence',         '2026-03-12', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 72.50,   'EXPENSE', c_alim,  'Monoprix',              '2026-03-15', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 85.00,   'EXPENSE', c_loisirs,'Sortie entre amis',    '2026-03-20', 'Restaurant + bar',       'MANUAL', now_ts, now_ts);

-- Mars 2026 — transactions supplémentaires pour déclencher les alertes de budget
-- Alimentation : pousser au-dessus de 320€ (seuil 80% × 400€)
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 58.30,   'EXPENSE', c_alim,  'Courses Lidl',          '2026-03-08', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 52.70,   'EXPENSE', c_alim,  'Courses Aldi',          '2026-03-14', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 61.40,   'EXPENSE', c_alim,  'Carrefour Bio',         '2026-03-19', 'Fruits légumes',         'MANUAL', now_ts, now_ts);
-- → Total alimentation mars : ~340€  (85% > seuil 80%) ✅

-- Transport : pousser au-dessus de 112€ (seuil 75% × 150€)
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 18.50,   'EXPENSE', c_trans, 'Uber',                  '2026-03-10', 'Retour soirée',          'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 32.00,   'EXPENSE', c_trans, 'Péage + essence A6',    '2026-03-16', 'Week-end Bourgogne',     'MANUAL', now_ts, now_ts);
-- → Total transport mars : ~123€  (82% > seuil 75%) ✅

-- Loisirs : pousser au-dessus de 150€ (seuil 75% × 200€)
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 42.00,   'EXPENSE', c_loisirs,'Bowling + bar',        '2026-03-14', NULL,                     'MANUAL', now_ts, now_ts);
-- → Total loisirs mars : ~155€  (77.5% > seuil 75%) ✅

-- Santé : pousser au-dessus de 70€ (seuil 70% × 100€)
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 25.00,   'EXPENSE', c_sante, 'Médecin généraliste',   '2026-03-09', 'Consultation + ordonnance','MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 18.50,   'EXPENSE', c_sante, 'Pharmacie ordonnance',  '2026-03-17', NULL,                     'MANUAL', now_ts, now_ts);
-- → Total santé mars : ~82€  (82% > seuil 70%) ✅

-- Restaurants (nouvelle catégorie) : pousser au-dessus de 160€ (seuil 80% × 200€)
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 45.00,   'EXPENSE', c_resto, 'Brasserie du Marché',   '2026-03-07', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 65.00,   'EXPENSE', c_resto, 'Restaurant anniversaire','2026-03-14', 'Dîner entre amis',       'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 38.50,   'EXPENSE', c_resto, 'Pizza Pino',            '2026-03-19', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 35.00,   'EXPENSE', c_resto, 'Sushi en semaine',      '2026-03-22', NULL,                     'MANUAL', now_ts, now_ts);
-- → Total restaurants mars : 183.50€  (91.75% > seuil 80%) ✅

-- Abonnements (nouvelle catégorie) : pousser au-dessus de 29.75€ (seuil 85% × 35€)
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 8.99,    'EXPENSE', c_abos,  'Disney+',               '2026-03-01', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 6.99,    'EXPENSE', c_abos,  'Amazon Prime Video',    '2026-03-01', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 14.99,   'EXPENSE', c_abos,  'Microsoft 365',         '2026-03-01', NULL,                     'MANUAL', now_ts, now_ts);
-- → Total abonnements mars : 30.97€  (88.5% > seuil 85%) ✅

-- Quelques transactions restaurants sur les mois précédents
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 52.00,   'EXPENSE', c_resto, 'Bistrot des Artistes',  '2026-02-08', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 38.00,   'EXPENSE', c_resto, 'Sushi Osaka',           '2026-01-18', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 75.00,   'EXPENSE', c_resto, 'Repas Noël famille',    '2025-12-24', 'Réveillon en famille',   'MANUAL', now_ts, now_ts);

-- Quelques abonnements sur mois précédents
INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 8.99,    'EXPENSE', c_abos,  'Disney+',               '2026-02-01', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 6.99,    'EXPENSE', c_abos,  'Amazon Prime Video',    '2026-02-01', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 8.99,    'EXPENSE', c_abos,  'Disney+',               '2026-01-01', NULL,                     'MANUAL', now_ts, now_ts);

INSERT INTO transactions (user_id, amount, type, category_id, label, date, note, source, created_at, updated_at)
VALUES (v_user, 6.99,    'EXPENSE', c_abos,  'Amazon Prime Video',    '2026-01-01', NULL,                     'MANUAL', now_ts, now_ts);


-- =============================================================================
-- BUDGETS (1 par catégorie)
-- =============================================================================
INSERT INTO budgets (user_id, category_id, monthly_limit, alert_threshold, created_at, updated_at)
VALUES (v_user, c_alim,    400.00, 80.00,  now_ts, now_ts);

INSERT INTO budgets (user_id, category_id, monthly_limit, alert_threshold, created_at, updated_at)
VALUES (v_user, c_trans,   150.00, 75.00,  now_ts, now_ts);

INSERT INTO budgets (user_id, category_id, monthly_limit, alert_threshold, created_at, updated_at)
VALUES (v_user, c_loyer,   950.00, 100.00, now_ts, now_ts);

INSERT INTO budgets (user_id, category_id, monthly_limit, alert_threshold, created_at, updated_at)
VALUES (v_user, c_loisirs, 200.00, 75.00,  now_ts, now_ts);

INSERT INTO budgets (user_id, category_id, monthly_limit, alert_threshold, created_at, updated_at)
VALUES (v_user, c_sante,   100.00, 70.00,  now_ts, now_ts);

INSERT INTO budgets (user_id, category_id, monthly_limit, alert_threshold, created_at, updated_at)
VALUES (v_user, c_resto,   200.00, 80.00,  now_ts, now_ts);

INSERT INTO budgets (user_id, category_id, monthly_limit, alert_threshold, created_at, updated_at)
VALUES (v_user, c_abos,     35.00, 85.00,  now_ts, now_ts);


-- =============================================================================
-- GOALS (7 objectifs d'épargne — tous les statuts et cas représentés)
-- =============================================================================
-- ACTIVE — en bonne progression (42.5%)
INSERT INTO goals (user_id, name, target_amount, current_amount, deadline, status, created_at, updated_at)
VALUES (v_user, 'Vacances été 2026',        2000.00,  850.00, '2026-06-15', 'ACTIVE',    now_ts, now_ts) RETURNING id INTO g_vacances;

-- ACTIVE — fonds d'urgence (60%)
INSERT INTO goals (user_id, name, target_amount, current_amount, deadline, status, created_at, updated_at)
VALUES (v_user, 'Fonds d''urgence',         5000.00, 3000.00, NULL,        'ACTIVE',     now_ts, now_ts) RETURNING id INTO g_urgence;

-- ACTIVE — tout juste commencé (8%)
INSERT INTO goals (user_id, name, target_amount, current_amount, deadline, status, created_at, updated_at)
VALUES (v_user, 'Mariage 2027',            15000.00, 1200.00, '2027-06-01', 'ACTIVE',    now_ts, now_ts) RETURNING id INTO g_mariage;

-- ACTIVE — presque atteint (92%)
INSERT INTO goals (user_id, name, target_amount, current_amount, deadline, status, created_at, updated_at)
VALUES (v_user, 'Nouveau laptop MacBook',   2500.00, 2300.00, '2026-04-30', 'ACTIVE',    now_ts, now_ts) RETURNING id INTO g_laptop;

-- ACTIVE — apport voiture neuve (35%)
INSERT INTO goals (user_id, name, target_amount, current_amount, deadline, status, created_at, updated_at)
VALUES (v_user, 'Apport voiture neuve',     4000.00, 1400.00, '2026-12-01', 'ACTIVE',    now_ts, now_ts) RETURNING id INTO g_voiture;

-- COMPLETED — iPhone acheté
INSERT INTO goals (user_id, name, target_amount, current_amount, deadline, status, created_at, updated_at)
VALUES (v_user, 'iPhone 16 Pro',            1200.00, 1200.00, NULL,        'COMPLETED',  now_ts, now_ts) RETURNING id INTO g_iphone;

-- ARCHIVED — formation abandonnée
INSERT INTO goals (user_id, name, target_amount, current_amount, deadline, status, created_at, updated_at)
VALUES (v_user, 'Formation Angular avancée', 500.00,  120.00, NULL,        'ARCHIVED',   now_ts, now_ts) RETURNING id INTO g_formation;


-- =============================================================================
-- GOAL CONTRIBUTIONS
-- =============================================================================
-- Vacances (en cours)
INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_vacances, v_user, 200.00, '2025-11-01', 'Mise de départ',     NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_vacances, v_user, 150.00, '2025-12-01', 'Bonus fin d''année', NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_vacances, v_user, 200.00, '2026-01-15', 'Économies janvier',  NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_vacances, v_user, 150.00, '2026-02-15', 'Économies février',  NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_vacances, v_user, 150.00, '2026-03-10', 'Économies mars',     NULL, now_ts);

-- iPhone (complété)
INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_iphone, v_user, 400.00, '2025-10-01', 'Première tranche', NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_iphone, v_user, 400.00, '2025-11-01', 'Deuxième tranche', NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_iphone, v_user, 400.00, '2025-12-15', 'Solde final',      NULL, now_ts);

-- Formation (archivée)
INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_formation, v_user, 120.00, '2025-10-20', 'Début épargne', NULL, now_ts);

-- Fonds d'urgence (60% — contributions régulières depuis longtemps)
INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 500.00, '2025-07-01', 'Mise de départ',        NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 500.00, '2025-08-01', 'Versement août',         NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 500.00, '2025-09-01', 'Versement septembre',    NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 300.00, '2025-10-15', 'Versement octobre',      NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 300.00, '2025-11-15', 'Versement novembre',     NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 400.00, '2025-12-15', 'Versement décembre',     NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 200.00, '2026-01-20', 'Versement janvier',      NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 200.00, '2026-02-20', 'Versement février',      NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_urgence, v_user, 100.00, '2026-03-15', 'Versement mars',         NULL, now_ts);

-- Mariage 2027 (8% — tout juste démarré)
INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_mariage, v_user, 500.00, '2026-01-10', 'Ouverture livret mariage', NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_mariage, v_user, 400.00, '2026-02-10', 'Versement février',        NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_mariage, v_user, 300.00, '2026-03-10', 'Versement mars',           NULL, now_ts);

-- MacBook (92% — presque atteint)
INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_laptop, v_user, 800.00, '2025-11-01', 'Mise de départ',      NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_laptop, v_user, 600.00, '2025-12-01', 'Bonus Noël investi',  NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_laptop, v_user, 500.00, '2026-01-10', 'Versement janvier',   NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_laptop, v_user, 400.00, '2026-02-10', 'Versement février',   NULL, now_ts);

-- Apport voiture (35%)
INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_voiture, v_user, 500.00, '2025-11-15', 'Mise de départ',     NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_voiture, v_user, 300.00, '2025-12-15', 'Versement décembre', NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_voiture, v_user, 300.00, '2026-01-15', 'Versement janvier',  NULL, now_ts);

INSERT INTO goal_contributions (goal_id, user_id, amount, contribution_date, note, transaction_id, created_at)
VALUES (g_voiture, v_user, 300.00, '2026-02-15', 'Versement février',  NULL, now_ts);


-- =============================================================================
-- DEBTS (2 dettes)
-- =============================================================================
INSERT INTO debts (user_id, creditor_name, total_amount, remaining_amount, monthly_payment, interest_rate, start_date, due_date, status, created_at, updated_at)
VALUES (v_user, 'Crédit Agricole - Voiture', 8000.00, 5200.00, 220.00, 3.50, '2024-06-01', '2027-06-01', 'ACTIVE', now_ts, now_ts) RETURNING id INTO d_voiture;

INSERT INTO debts (user_id, creditor_name, total_amount, remaining_amount, monthly_payment, interest_rate, start_date, due_date, status, created_at, updated_at)
VALUES (v_user, 'Prêt Marc',                  500.00,    0.00,  100.00, NULL, '2025-06-01', '2026-01-01', 'PAID',   now_ts, now_ts) RETURNING id INTO d_ami;


-- =============================================================================
-- DEBT PAYMENTS
-- =============================================================================
-- Voiture (active — 6 mensualités)
INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_voiture, v_user, '2025-10-05', 220.00, 'Mensualité Oct 2025', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_voiture, v_user, '2025-11-05', 220.00, 'Mensualité Nov 2025', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_voiture, v_user, '2025-12-05', 220.00, 'Mensualité Déc 2025', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_voiture, v_user, '2026-01-05', 220.00, 'Mensualité Jan 2026', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_voiture, v_user, '2026-02-05', 220.00, 'Mensualité Fév 2026', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_voiture, v_user, '2026-03-05', 220.00, 'Mensualité Mar 2026', NULL, now_ts);

-- Prêt Marc (soldé — 5 remboursements)
INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_ami, v_user, '2025-07-01', 100.00, 'Remboursement 1/5', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_ami, v_user, '2025-08-01', 100.00, 'Remboursement 2/5', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_ami, v_user, '2025-09-01', 100.00, 'Remboursement 3/5', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_ami, v_user, '2025-10-01', 100.00, 'Remboursement 4/5', NULL, now_ts);

INSERT INTO debt_payments (debt_id, user_id, payment_date, amount, note, transaction_id, created_at)
VALUES (d_ami, v_user, '2025-11-01', 100.00, 'Dernier remboursement', NULL, now_ts);


-- =============================================================================
-- PLANNED TRANSACTIONS (5 récurrentes)
-- =============================================================================
INSERT INTO planned_transactions (user_id, label, amount, type, category_id, frequency, next_due_date, auto_create, is_active, created_at, updated_at)
VALUES (v_user, 'Loyer mensuel',        900.00, 'EXPENSE', c_loyer,    'MONTHLY', '2026-04-02', TRUE,  TRUE,  now_ts, now_ts) RETURNING id INTO pt_loyer;

INSERT INTO planned_transactions (user_id, label, amount, type, category_id, frequency, next_due_date, auto_create, is_active, created_at, updated_at)
VALUES (v_user, 'Salaire',             2800.00, 'INCOME',  NULL,       'MONTHLY', '2026-04-01', TRUE,  TRUE,  now_ts, now_ts) RETURNING id INTO pt_salaire;

INSERT INTO planned_transactions (user_id, label, amount, type, category_id, frequency, next_due_date, auto_create, is_active, created_at, updated_at)
VALUES (v_user, 'Netflix',               17.99, 'EXPENSE', c_loisirs,  'MONTHLY', '2026-04-08', TRUE,  TRUE,  now_ts, now_ts) RETURNING id INTO pt_netflix;

INSERT INTO planned_transactions (user_id, label, amount, type, category_id, frequency, next_due_date, auto_create, is_active, created_at, updated_at)
VALUES (v_user, 'Spotify',                9.99, 'EXPENSE', c_loisirs,  'MONTHLY', '2026-04-10', FALSE, FALSE, now_ts, now_ts) RETURNING id INTO pt_spotify;

INSERT INTO planned_transactions (user_id, label, amount, type, category_id, frequency, next_due_date, auto_create, is_active, created_at, updated_at)
VALUES (v_user, 'Assurance habitation',  320.00, 'EXPENSE', NULL,       'YEARLY',  '2026-09-01', TRUE,  TRUE,  now_ts, now_ts);


-- =============================================================================
-- PLANNED TRANSACTION RUNS (historique d'exécutions)
-- =============================================================================
-- Loyer (3 exécutions réussies)
INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_loyer, v_user, '2026-01-02', now_ts, 'SUCCESS', tr_loyer_jan, NULL);

INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_loyer, v_user, '2026-02-02', now_ts, 'SUCCESS', tr_loyer_feb, NULL);

INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_loyer, v_user, '2026-03-02', now_ts, 'SUCCESS', tr_loyer_mar, NULL);

-- Salaire (3 exécutions réussies)
INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_salaire, v_user, '2026-01-01', now_ts, 'SUCCESS', tr_salaire_jan, NULL);

INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_salaire, v_user, '2026-02-01', now_ts, 'SUCCESS', tr_salaire_feb, NULL);

INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_salaire, v_user, '2026-03-01', now_ts, 'SUCCESS', tr_salaire_mar, NULL);

-- Netflix (1 FAILED + 2 SUCCESS pour tester les deux statuts)
INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_netflix, v_user, '2026-01-08', now_ts, 'FAILED',  NULL, 'Timeout connexion base de données');

INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_netflix, v_user, '2026-02-08', now_ts, 'SUCCESS', NULL, NULL);

INSERT INTO planned_transaction_runs (planned_transaction_id, user_id, due_date, executed_at, status, created_transaction_id, error_message)
VALUES (pt_netflix, v_user, '2026-03-08', now_ts, 'SUCCESS', NULL, NULL);


-- =============================================================================
-- CALENDAR EVENTS (mois courant + à venir)
-- =============================================================================
INSERT INTO calendar_events (user_id, title, event_date, amount, color, notes, created_at, updated_at)
VALUES (v_user, 'Renouvellement assurance auto', '2026-03-25', 480.00, '#F44336', 'Payer avant le 25 mars',          now_ts, now_ts);

INSERT INTO calendar_events (user_id, title, event_date, amount, color, notes, created_at, updated_at)
VALUES (v_user, 'Anniversaire Léa — cadeau',     '2026-03-28',  60.00, '#4CAF50', 'Commander sur Amazon',            now_ts, now_ts);

INSERT INTO calendar_events (user_id, title, event_date, amount, color, notes, created_at, updated_at)
VALUES (v_user, 'Déclaration impôts',            '2026-04-30',   NULL, '#FF9800', 'Date limite déclaration en ligne', now_ts, now_ts);

INSERT INTO calendar_events (user_id, title, event_date, amount, color, notes, created_at, updated_at)
VALUES (v_user, 'Révision voiture',              '2026-04-05', 150.00, '#9C27B0', 'Vidange + contrôle technique',    now_ts, now_ts);

INSERT INTO calendar_events (user_id, title, event_date, amount, color, notes, created_at, updated_at)
VALUES (v_user, 'Acompte hôtel Barcelone',       '2026-04-15', 300.00, '#2196F3', 'Vacances juillet 2026',           now_ts, now_ts);

INSERT INTO calendar_events (user_id, title, event_date, amount, color, notes, created_at, updated_at)
VALUES (v_user, 'Formation React — inscription', '2026-05-10', 399.00, '#607D8B', 'Udemy - React avancé',            now_ts, now_ts);

INSERT INTO calendar_events (user_id, title, event_date, amount, color, notes, created_at, updated_at)
VALUES (v_user, 'Remboursement prêt voiture',    '2026-04-05', 220.00, '#F44336', 'Prélèvement automatique',         now_ts, now_ts);


RAISE NOTICE ' Données de test insérées pour l''utilisateur %', v_user;
RAISE NOTICE '   - 7 catégories personnalisées (dont Restaurants et Abonnements)';
RAISE NOTICE '   - ~90 transactions sur 6 mois (Oct 2025 → Mar 2026)';
RAISE NOTICE '   - 7 budgets';
RAISE NOTICE '   - 6 alertes budget déclenchées en mars 2026 :';
RAISE NOTICE '       Alimentation 85%% / Transport 82%% / Loisirs 77%%';
RAISE NOTICE '       Santé 82%% / Restaurants 91%% / Abonnements 88%%';
RAISE NOTICE '   - 7 objectifs d''épargne + 26 contributions';
RAISE NOTICE '   - 2 dettes + 11 paiements';
RAISE NOTICE '   - 5 transactions planifiées + 9 exécutions';
RAISE NOTICE '   - 7 événements calendrier';

END $$;

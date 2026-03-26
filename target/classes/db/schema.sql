-- =============================================================================
-- Tables métier
-- =============================================================================

CREATE TABLE IF NOT EXISTS categories (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID NULL,
    name       VARCHAR(255) NOT NULL,
    icon       VARCHAR(255),
    color      VARCHAR(255),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_categories_user_name UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS transactions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID NOT NULL,
    amount      NUMERIC(14,2) NOT NULL,
    type        VARCHAR(20) NOT NULL,
    category_id BIGINT NULL,
    label       VARCHAR(255) NOT NULL,
    date        DATE NOT NULL,
    note        VARCHAR(2000),
    source      VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT ck_transactions_type     CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT ck_transactions_amount   CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS budgets (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL,
    category_id     BIGINT NOT NULL,
    monthly_limit   NUMERIC(14,2) NOT NULL,
    alert_threshold NUMERIC(5,2) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_budgets_category        FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT uk_budgets_user_category   UNIQUE (user_id, category_id)
);

CREATE TABLE IF NOT EXISTS goals (
    id             BIGSERIAL PRIMARY KEY,
    user_id        UUID NOT NULL,
    name           VARCHAR(255) NOT NULL,
    target_amount  NUMERIC(14,2) NOT NULL,
    current_amount NUMERIC(14,2) NOT NULL,
    deadline       DATE,
    status         VARCHAR(20) NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_goals_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'ARCHIVED'))
);

CREATE TABLE IF NOT EXISTS goal_contributions (
    id                BIGSERIAL PRIMARY KEY,
    goal_id           BIGINT NOT NULL,
    user_id           UUID NOT NULL,
    amount            NUMERIC(14,2) NOT NULL,
    contribution_date DATE NOT NULL,
    note              VARCHAR(255),
    transaction_id    BIGINT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_goal_contributions_goal        FOREIGN KEY (goal_id)        REFERENCES goals(id) ON DELETE CASCADE,
    CONSTRAINT fk_goal_contributions_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT ck_goal_contributions_amount      CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS planned_transactions (
    id            BIGSERIAL PRIMARY KEY,
    user_id       UUID NOT NULL,
    label         VARCHAR(255) NOT NULL,
    amount        NUMERIC(14,2) NOT NULL,
    type          VARCHAR(20) NOT NULL,
    category_id   BIGINT NULL,
    frequency     VARCHAR(20) NOT NULL,
    next_due_date DATE NOT NULL,
    auto_create   BOOLEAN NOT NULL DEFAULT TRUE,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_planned_transactions_category    FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT ck_planned_transactions_type        CHECK (type      IN ('INCOME', 'EXPENSE')),
    CONSTRAINT ck_planned_transactions_frequency   CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT ck_planned_transactions_amount      CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS planned_transaction_runs (
    id                     BIGSERIAL PRIMARY KEY,
    planned_transaction_id BIGINT NOT NULL,
    user_id                UUID NOT NULL,
    due_date               DATE NOT NULL,
    executed_at            TIMESTAMP WITH TIME ZONE,
    status                 VARCHAR(20) NOT NULL,
    created_transaction_id BIGINT NULL,
    error_message          TEXT,
    CONSTRAINT fk_planned_transaction_runs_planned     FOREIGN KEY (planned_transaction_id) REFERENCES planned_transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_planned_transaction_runs_transaction FOREIGN KEY (created_transaction_id) REFERENCES transactions(id),
    CONSTRAINT uk_planned_run_due                      UNIQUE (planned_transaction_id, due_date),
    CONSTRAINT ck_planned_transaction_runs_status      CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'SKIPPED'))
);

CREATE TABLE IF NOT EXISTS debts (
    id               BIGSERIAL PRIMARY KEY,
    user_id          UUID NOT NULL,
    creditor_name    VARCHAR(255) NOT NULL,
    total_amount     NUMERIC(14,2) NOT NULL,
    remaining_amount NUMERIC(14,2) NOT NULL,
    monthly_payment  NUMERIC(14,2) NOT NULL,
    interest_rate    NUMERIC(5,2),
    start_date       DATE NOT NULL,
    due_date         DATE,
    status           VARCHAR(20) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_debts_status           CHECK (status IN ('ACTIVE', 'PAID', 'CANCELLED')),
    CONSTRAINT ck_debts_total_amount     CHECK (total_amount     >  0),
    CONSTRAINT ck_debts_remaining_amount CHECK (remaining_amount >= 0),
    CONSTRAINT ck_debts_monthly_payment  CHECK (monthly_payment  >= 0)
);

CREATE TABLE IF NOT EXISTS debt_payments (
    id             BIGSERIAL PRIMARY KEY,
    debt_id        BIGINT NOT NULL,
    user_id        UUID NOT NULL,
    payment_date   DATE NOT NULL,
    amount         NUMERIC(14,2) NOT NULL,
    note           VARCHAR(255),
    transaction_id BIGINT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_debt_payments_debt        FOREIGN KEY (debt_id)        REFERENCES debts(id) ON DELETE CASCADE,
    CONSTRAINT fk_debt_payments_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT ck_debt_payments_amount      CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS calendar_events (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID NOT NULL,
    title      VARCHAR(255) NOT NULL,
    event_date DATE NOT NULL,
    amount     NUMERIC(14,2),
    color      VARCHAR(255),
    notes      VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- =============================================================================
-- Profils utilisateurs (données applicatives, identité gérée par Supabase Auth)
-- =============================================================================

CREATE TABLE IF NOT EXISTS profiles (
    user_id      UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name VARCHAR(255),
    avatar_url   TEXT,
    locale       VARCHAR(10)  NOT NULL DEFAULT 'fr',
    timezone     VARCHAR(50)  NOT NULL DEFAULT 'Europe/Paris',
    currency     VARCHAR(3)   NOT NULL DEFAULT 'EUR',
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- Index
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_transactions_user_id               ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date                  ON transactions(date);
CREATE INDEX IF NOT EXISTS idx_transactions_category_id           ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_budgets_user_id                    ON budgets(user_id);
CREATE INDEX IF NOT EXISTS idx_goals_user_id                      ON goals(user_id);
CREATE INDEX IF NOT EXISTS idx_goal_contributions_goal_id         ON goal_contributions(goal_id);
CREATE INDEX IF NOT EXISTS idx_goal_contributions_user_id         ON goal_contributions(user_id);
CREATE INDEX IF NOT EXISTS idx_planned_transactions_user_id       ON planned_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_planned_transactions_next_due_date ON planned_transactions(next_due_date);
CREATE INDEX IF NOT EXISTS idx_planned_transaction_runs_planned_id ON planned_transaction_runs(planned_transaction_id);
CREATE INDEX IF NOT EXISTS idx_debts_user_id                      ON debts(user_id);
CREATE INDEX IF NOT EXISTS idx_debt_payments_debt_id              ON debt_payments(debt_id);
CREATE INDEX IF NOT EXISTS idx_debt_payments_user_id              ON debt_payments(user_id);
CREATE INDEX IF NOT EXISTS idx_calendar_events_user_id_date       ON calendar_events(user_id, event_date);

-- =============================================================================
-- RLS profiles : chaque utilisateur ne voit et ne modifie que son propre profil
-- =============================================================================

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "profiles_select_own" ON profiles FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "profiles_insert_own" ON profiles FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "profiles_update_own" ON profiles FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "profiles_delete_own" ON profiles FOR DELETE USING (auth.uid() = user_id);

-- =============================================================================
-- Trigger : création automatique du profil à l'inscription
-- =============================================================================

CREATE OR REPLACE FUNCTION handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER SET search_path = public
AS $$
BEGIN
    INSERT INTO public.profiles (user_id, display_name, avatar_url)
    VALUES (
        NEW.id,
        NEW.raw_user_meta_data ->> 'display_name',
        NEW.raw_user_meta_data ->> 'avatar_url'
    );
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION handle_new_user();

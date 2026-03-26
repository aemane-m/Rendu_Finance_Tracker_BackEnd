# Finance Tracker — Backend API

API REST développée avec **Spring Boot 3** pour l'application de gestion de finances personnelles Finance Tracker.

| | Lien |
|---|---|
| **Backend** | https://github.com/aemane-m/Rendu_Finance_Tracker_BackEnd |
| **Frontend** | https://github.com/saraldv/Rendu_Finance_Tracker_FrontEnd |

---

## Stack technique

| Technologie | Rôle |
|---|---|
| Java 21 + Spring Boot 3 | Framework principal, auto-configuration |
| Spring Security + OAuth2 | Validation des JWT Supabase |
| Spring Data JPA / Hibernate | Accès à la base de données |
| Spring Scheduler | Exécution automatique des transactions planifiées |
| Lombok | Génération du boilerplate sur les entités |
| PostgreSQL (Supabase) | Base de données relationnelle |
| Swagger / OpenAPI | Documentation interactive de l'API |

---

## Prérequis

- **Java 21** ou supérieur
- **Maven** (ou utiliser le wrapper `./mvnw` inclus)
- Accès internet (connexion à la base Supabase déjà configurée)

---

## Lancement

```bash
./mvnw spring-boot:run
```

L'application démarre sur :

- **API** : `http://localhost:8080`
- **Swagger UI** : `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON** : `http://localhost:8080/v3/api-docs`

---

## Configuration

La configuration est prête à l'emploi. Les paramètres suivants sont déjà renseignés dans `src/main/resources/application.yaml` :

| Paramètre | Valeur |
|---|---|
| Base de données | PostgreSQL Supabase (SSL activé) |
| Issuer JWT | `https://skctavviydqbpndrflfp.supabase.co/auth/v1` |
| Port serveur | `8080` |
| Scheduler (transactions planifiées) | Tous les jours à 06h00 |

Aucun fichier `.env` ou configuration locale supplémentaire n'est nécessaire.

---

## Compte de démonstration

Pour tester l'application avec des données réelles :

| Champ | Valeur |
|---|---|
| Email | `user1@example.com` |
| Mot de passe | `Test1234` |
| UUID utilisateur | `476053f8-bef5-4396-9114-7f110e0b17e1` |

---

## Données de démonstration

Le script `test-data.sql` insère un jeu de données complet pour le compte de démonstration.
Il est **idempotent** : il peut être rejoué sans altérer les données existantes.

---

## Architecture

```
src/main/java/.../
├── config/          # Sécurité, CORS, Swagger, Supabase Admin
├── controllers/     # Endpoints REST (reçoit, délègue, répond)
├── dto/             # Records Java : requêtes et réponses
├── models/          # Entités JPA (tables PostgreSQL)
├── repository/      # Accès aux données (Spring Data JPA)
├── services/        # Logique métier
│   └── impl/
├── mapper/          # Conversion entité → DTO
├── exceptions/      # Exceptions métier + GlobalExceptionHandler
└── scheduler/       # Exécution automatique des transactions planifiées
```

---

## Endpoints principaux

| Domaine | Préfixe |
|---|---|
| Transactions | `GET/POST/PUT/DELETE /v1/transactions` |
| Catégories | `GET/POST/PUT/DELETE /v1/categories` |
| Budgets | `GET/POST/PUT/DELETE /v1/budgets` |
| Objectifs | `GET/POST/PUT/DELETE /v1/goals` |
| Dettes | `GET/POST/PUT/DELETE /v1/debts` |
| Transactions planifiées | `GET/POST/PUT/DELETE /v1/planned-transactions` |
| Calendrier | `GET/POST/PUT/DELETE /v1/calendar/events` |
| Dashboard | `GET /v1/dashboard/summary`, `/spending-by-category`, `/goals-progress`, `/budget-alerts` |
| Compte | `POST /v1/account/logout-all` |

La documentation complète est disponible sur Swagger UI après lancement.

---

## Remarque

Ce dépôt est destiné à un usage pédagogique dans le cadre du Master MIAGE — Université Paris Dauphine (2025-2026). La configuration a été simplifiée pour permettre une exécution immédiate sans étape de préparation locale.
# Rendu_Finance_Tracker_BackEnd

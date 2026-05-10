# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

**Pre-code stage.** No source, no build system, no commits yet. The repo currently contains only design documents under `docs/` (in Chinese). Any task here is likely either (a) extending the design docs or (b) bootstrapping the project skeleton per the planned stack below.

When asked to "start coding," consult `docs/开发计划.md` first — it defines a strict vertical-slice order (user auth → score upload → practice plan → setlist/social → AI integration → badges). Don't jump ahead; each slice is meant to ship end-to-end (DB + backend + frontend) before the next begins.

## Stack & layout

- **Frontend** (`frontend/`): Vue 3 + Element Plus + Vite, Pinia for state, Vue Router for routing, Axios for HTTP. npm registry pinned to `registry.npmmirror.com` via `.npmrc`.
- **Backend** (`backend/`): Java 17 + Spring Boot 3.2 + MyBatis (raw XML mappers, not MyBatis-Plus — preserves the SQL teaching goal). Maven repository pinned to Aliyun mirror inside `pom.xml`. Build encoding forced to UTF-8 (the host runs Chinese Windows / GBK by default).
- **Database**: MySQL 8.0, schema name `lyrascore`, charset `utf8mb4`. DDL not yet generated — see `backend/src/main/resources/db/schema.sql` (placeholder).

## Common commands

| Task | Command (run from project root unless noted) |
|---|---|
| Run backend (dev) | `cd backend && mvn spring-boot:run` — listens on `http://localhost:8080/api` |
| Build backend jar | `cd backend && mvn clean package` |
| Run backend tests | `cd backend && mvn test` |
| Run a single backend test | `cd backend && mvn test -Dtest=ClassName#methodName` |
| Install frontend deps | `cd frontend && npm install` |
| Run frontend (dev) | `cd frontend && npm run dev` — listens on `http://localhost:5200`, proxies `/api` to `:8080` |
| Build frontend | `cd frontend && npm run build` |

The backend reads runtime config from environment variables (`DB_PASSWORD`, `JWT_SECRET`, `DASHSCOPE_API_KEY`, `AI_ENABLED`) — see `.env.example`. None are committed. On Windows PowerShell, source them with the snippet in `README.md`.

## Configuration & runtime conventions

- **Context path**: backend serves under `/api` (`server.servlet.context-path` in `application.yml`). All controller paths are relative to that.
- **Upload directory**: `lyrascore.upload-dir` (default `./uploads`, resolved relative to the backend working dir). The `WebMvcConfigurer` (to be written) must map `/uploads/**` to that directory so images are served. Store **relative paths** in DB columns like `t_score.image_url`, never full URLs.
- **AI toggle**: `lyrascore.ai.enabled` (env `AI_ENABLED`) defaults to `false`. The AI module (when written) must check this flag — if disabled or the API call fails, write the raw input with `ai_song`/`ai_artist` left null and `t_song_request.status = 2` (待人工审核). Never block the user's request on AI failure.
- **MyBatis**: `map-underscore-to-camel-case: true` is on, so DB columns like `created_at` map to entity fields like `createdAt` automatically. Mapper XMLs go under `src/main/resources/mapper/`.
- **Mapper scan**: `LyraScoreApplication` scans `com.lyrascore.**.mapper` — keep mapper interfaces in a `mapper` sub-package within each feature module.

## Locked-in technical decisions

These supersede any conflicting hints in `docs/`. If a doc still implies the old approach, the doc is stale — follow this section.

- **Image storage: local disk.** No object storage / no MinIO. Score images and avatars are written to a local directory and served by the backend. When implementing, pick one root directory (e.g. `uploads/`), namespace by feature (`uploads/scores/`, `uploads/avatars/`), and store only the relative path in the DB — never a full URL with host, so deployment moves don't break links. Long-image stitching stays client-side.
- **Auth: JWT + manually written interceptor, with a `role` column on `t_user`.** Do **not** pull in Spring Security. Implement a `HandlerInterceptor` registered via `WebMvcConfigurer`, parse the `Authorization: Bearer …` header, validate the JWT, and stash the user into a `ThreadLocal` or request attribute. Role check (`user` vs `admin`) is done in the same interceptor against path patterns, or via a custom `@RequireRole` annotation.
- **Badges: Spring `@EventListener`, not DB triggers.** When a practice log is written, publish a domain event (e.g. `PracticeLogCreatedEvent`); listeners evaluate badge conditions and insert into `t_user_badge`. This replaces the "DB trigger" approach mentioned in `docs/PRD.md` and `docs/开发计划.md` — those notes are now obsolete on this point. Keep listeners idempotent (check `t_user_badge` for existing row before insert).
- **AI provider: Aliyun Bailian (阿里云百炼), model `qwen-turbo`.** Used only by the song-request cleaning feature. API key lives in an environment variable — never hard-coded, never committed. Suggested env var name: `DASHSCOPE_API_KEY` (matches Aliyun's official SDK convention). On API failure or timeout, fall back to writing the raw input with `ai_song` / `ai_artist` left null and a status flag indicating "needs manual review" — do not block the user's request.

## Domain model

The authoritative schema is `docs/E-R 图.md` (Mermaid ERD). Treat it as the source of truth when generating DDL or JPA entities. Key conventions baked into the design:

- All tables use the `t_` prefix (e.g., `t_user`, `t_score`, `t_practice_log`).
- Status flags are `tinyint` with documented enum meanings inline (e.g., `status: 0待通过/1已成为好友`). Preserve those enum semantics when writing code.
- Logical deletion / state changes are preferred over hard deletes (admin "freezes" users via status bit).
- Three relationship patterns to recognize:
  - **1:N** — user → scores, plans, logs, setlists
  - **N:M via join table** — `t_setlist_items`, `t_plan_items`, `t_user_badge`
  - **Self-referential N:M** — `t_friendship` uses `user_id_1` / `user_id_2`
- `t_dictionary` is a generic config table (quick-reply phrases, etc.) — admin-editable, referenced from features like quick-DM to avoid free-text moderation risk.
- `t_badge` + `t_user_badge` are auto-awarded via Spring `@EventListener` (see Locked-in technical decisions). The PRD's mention of DB triggers is superseded.

## Feature scope (per `docs/PRD.md`)

Six modules, organized as concentric layers:

1. **Score vault** (`t_score`) — upload, metadata, memo. Long-image stitching is done **client-side** to spare backend I/O.
2. **Practice plan + log** — timed practice sessions feed aggregate stats (`GROUP BY` / `SUM` queries are an explicit teaching goal).
3. **Setlists** — N:M curation with custom ordering.
4. **Social** — friendship graph, AI-cleaned song requests, dictionary-backed quick DMs.
5. **Badges** — gamification, trigger-driven.
6. **Admin** — dashboard aggregates, dictionary CRUD, user freeze.

The "AI 智能点歌台" feature calls an external LLM API to normalize free-text song requests into `(ai_song, ai_artist)` before storing in `t_song_request`. Keep raw input alongside the cleaned fields — the schema preserves both.

## Working language

Source docs, UI copy, and likely code comments are in **Chinese**. Identifiers (table/column/class names) are English. When editing docs or writing user-facing strings, match the existing Chinese tone; when writing code, follow the English identifier convention already established in the ERD.

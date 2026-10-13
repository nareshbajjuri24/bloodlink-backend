# BloodLink backend (Spring Boot 3.4, Maven, Java 21-24)

## Run locally
1. Create a database `bloodlink_v2` in Neon.
2. Copy `local.properties.example` to `local.properties` and fill it in.
3. Run `BloodLinkApplication` in IntelliJ, or `mvn spring-boot:run`.

Tables are created/updated automatically (`database/schema.sql` is a reference copy).

## Roles
- `USER` (default): request blood and donate.
- `ADMIN`: manage users (block, make/remove admin, delete) and view or close/delete every request, under `/api/admin/**`.
  The first admin comes from `app.admin.email` / `app.admin.password` at startup.

## Forgot password
`POST /api/auth/forgot-password` e-mails a one-time link (valid 30 minutes). `POST /api/auth/reset-password` sets the new password.

## E-mail providers
- `app.mail.provider=smtp` (default): Gmail SMTP, for local development.
- `app.mail.provider=brevo`: Brevo HTTP API, for hosts that block SMTP ports. Needs `BREVO_API_KEY` and `MAIL_FROM` (a sender verified in Brevo).

## Deploy (Docker)
The included `Dockerfile` builds and runs the app. Settings come from environment variables:
`DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, FRONTEND_URL, CORS_ORIGINS, MAIL_PROVIDER, BREVO_API_KEY, MAIL_FROM, ADMIN_EMAIL, ADMIN_PASSWORD`.
Health check: `GET /api/health`.

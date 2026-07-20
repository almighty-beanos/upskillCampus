# MultiClientWebsite — Dual Marketplace Platform

A Spring Boot + Thymeleaf marketplace connecting customers and merchants, supporting both physical goods and bookable services with an in-house wallet settlement system.

## Running the Application

**Requirements:** Java 17+, Maven, a PostgreSQL database, a Supabase project (Storage bucket for media).

1. Configure `src/main/resources/application.properties` with your database connection and Supabase credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://<host>:<port>/<db>
   spring.datasource.username=<user>
   spring.datasource.password=<password>

   SUPABASE_URL=https://<project>.supabase.co
   SUPABASE_KEY=<service-role-key>
   SUPABASE_BUCKET=media

   spring.servlet.multipart.max-file-size=50MB
   spring.servlet.multipart.max-request-size=50MB
   ```
2. Build and run:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Visit `http://localhost:8080`. New accounts start with a seeded wallet balance for testing purchases and bookings.

## Functional Requirements Covered

**Authentication & Accounts**
- Customer and merchant signup/login with hashed passwords and role-based session access.
- Merchant self-service profile editing (name, email, phone, password) with current-password confirmation.

**Discover (Customer)**
- Unified browsing of all goods and services, grouped into tabs.
- Client-side search, category filtering, and sorting by name or price.
- Media served from Supabase Storage with a default-image fallback for listings with no uploads.

**Merchant Catalog Management**
- Full CRUD for goods and services listings.
- Per-item media upload and deletion, synced with Supabase Storage and the listing's stored URLs.

**Merchant Sales Dashboard**
- Tabbed views of transactions (goods sales) and appointments (service bookings), scoped to the logged-in merchant.
- Search, category filtering, time-window filtering (week/month/all-time), and sorting by date or amount.

**Checkout & Settlement**
- Clickable listings leading to a payment page: quantity selection for goods, date/time/duration selection for service appointments, with live total calculation.
- Wallet-to-wallet transfer between customer and merchant on payment, with an atomic balance check and transaction/appointment record creation.
- Post-payment confirmation page exposing merchant contact details for two minutes, with automatic redirection back to Discover afterward.

## Architecture Notes

The application uses a closed-loop wallet ledger in place of a real payment gateway integration — customer and merchant balances are held and transferred within the platform's own database, with all monetary fields using `BigDecimal` for precision. This keeps the scope self-contained without requiring external payment processor credentials, at the cost of not handling real currency on/off-ramping, which would be a prerequisite for production use.
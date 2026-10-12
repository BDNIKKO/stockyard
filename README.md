# StockYard

**Know what you have, what you sold, and where it sold.**

StockYard is a simple web app for people who make and sell things (candles, mugs, jewelry, prints) and have outgrown their spreadsheet. You keep a list of your products, log each sale as it happens, and StockYard does the math: stock goes down, revenue and profit go up, and the dashboard shows what is selling where. It also tells you when something is running low.

No complicated setup, no monthly fee, no features you will never touch.

## What you can do

- **Keep a product catalog.** Add each item with its SKU, photo, what it costs you to make, what you sell it for, and how many you have.
- **Log sales in seconds.** Pick the product, the channel, the quantity and the price. Stock updates automatically, and you can't sell more than you have.
- **Use your own sales channels.** Etsy, craft fairs, your own website, a pop-up. Add whatever matches how you really sell.
- **See your month at a glance.** Revenue, units sold, profit, a chart of which channel earns the most, and a list of items to restock.
- **Filter your sales history** by date and channel, so you can compare Etsy against in-person without exporting anything.
- **Retire products safely.** Archiving hides an item from your day-to-day view without changing any past sales.

Each account is private. Nobody else can see your products or numbers.

## Try it on your computer

You only need [Docker](https://www.docker.com/products/docker-desktop/) installed.

```bash
git clone https://github.com/BDNIKKO/stockyard.git
cd stockyard
docker compose up --build
```

Wait a few minutes the first time, then open **http://localhost:8080**.

1. Click **Create an account**.
2. Add a product on the **Products** page.
3. Log a sale on the **Sales** page.
4. Head back to the **Dashboard** and watch it fill in.

To stop it, press `Ctrl+C`. To stop it and erase all the data, run `docker compose down -v`.

## Live demo

**[https://stockyard-ucr9.onrender.com](https://stockyard-ucr9.onrender.com)**

The hosted copy runs the same Docker image as the one above, on [Render](https://render.com) with a managed PostgreSQL database. It sleeps when nobody is using it, so the first visit after a quiet spell can take up to a minute to wake up. Create your own account to try it; each account is private.

Want to host your own copy? The repo includes a Render blueprint and a step-by-step [deployment guide](docs/DEPLOYMENT.md). It takes about ten minutes and costs nothing.

## Capstone presentation

The capstone presentation is a self-running slide deck in [`docs/presentation/`](docs/presentation/). Open `index.html` in any browser (no install, works offline) and press any key: it plays itself in about nine minutes, with a product tour, five step-by-step code walkthroughs, and a generated background score. Turn your sound on.

| Key | Action |
|---|---|
| `P` | Pause or resume the presentation |
| `→` / `←` | Next / previous step (code slides highlight line by line) |
| `M` | Mute or unmute the background score |
| `N` | Show the narration text for each slide |
| `F` | Fullscreen |

Prefer a static copy? [`StockYard_Slides.pdf`](docs/presentation/StockYard_Slides.pdf) has every slide. To use your own background track instead of the built-in generated score, drop an `assets/music.mp3` next to `index.html`.

---

## Technical overview

*For reviewers and contributors.*

### Stack

| Layer | Technology |
|---|---|
| Front end | React 18, Vite, React Router, Axios (with interceptors), Recharts |
| Styling | CSS Modules over shared design tokens (spacing grid, color system, type scale); breakpoints at 480, 768 and 1024 px; no component library |
| Back end | Java 21, Spring Boot 3.5, Spring Security (stateless JWT), Spring Data JPA, Jakarta Bean Validation, Springdoc OpenAPI |
| Database | PostgreSQL 16, schema managed by Flyway migrations |
| Delivery | Single multi-stage Docker image (the React build is bundled into the Spring Boot jar), Docker Compose for local use, GitHub Actions CI, Render blueprint |

### Architecture

```
React SPA ──HTTP/JSON + Bearer JWT──▶ Controllers ─▶ Services ─▶ Repositories ─▶ PostgreSQL
                                          │              │
                                       DTO records   @Transactional
```

- Layers are strictly separated: REST controllers, service orchestration, Spring Data repositories, and DTO records (the API never exposes entities).
- Every query is scoped to the authenticated maker's id, so tenants cannot read or modify each other's data. This is covered by an automated test.
- Errors come back as a consistent JSON shape with field-level messages for validation failures.

### Data integrity decisions

- **Atomic sales.** Recording a sale runs a guarded `UPDATE products SET stock = stock - :qty WHERE stock >= :qty` and inserts the ledger row in the same transaction. Overselling is impossible, even under concurrent requests, and a rejected sale leaves no trace (HTTP 409).
- **Immutable history.** Each sale stores the unit cost and price at the time of sale, so editing or archiving a product never rewrites past profit.
- **Soft archive.** Products are archived rather than deleted; channels with sales cannot be deleted.
- **Constraints in the database**, not just the app: non-negative money and stock, unique SKU per maker, unique channel name per maker.

### REST API

Interactive docs are served at `/swagger-ui.html` (OpenAPI JSON at `/v3/api-docs`).

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` |
| Products | `GET/POST /api/products`, `PUT /api/products/{id}`, `POST /api/products/{id}/archive`, `/restore`, `/image` |
| Channels | `GET/POST /api/channels`, `PUT/DELETE /api/channels/{id}` |
| Sales | `GET /api/sales?from=&to=&channelId=`, `POST /api/sales` |
| Dashboard | `GET /api/dashboard` |

### Project layout

```
stockyard/
├── backend/            Spring Boot API (Java 21, Maven)
│   └── src/main/java/com/stockyard/   auth · product · channel · sale · dashboard · security · common
│   └── src/main/resources/db/migration/   Flyway SQL migrations
├── frontend/           React 18 + Vite app (pages, components, CSS Modules, design tokens)
├── docs/presentation/  Capstone slide deck (HTML + PDF)
├── Dockerfile          Builds the React app and API into one image
├── docker-compose.yml  App + PostgreSQL for one-command local runs
├── render.yaml         Render blueprint for a hosted copy
└── .github/workflows/  CI: backend tests + front-end build
```

### Run for development (hot reload)

Prerequisites: Java 21, Maven 3.9+, Node 20+, Docker.

```bash
docker compose up -d db                      # PostgreSQL on :5432
cd backend && mvn spring-boot:run            # API on :8080
cd frontend && npm install && npm run dev    # UI on :5173
```

### Tests

```bash
cd backend && mvn test
```

Integration tests run the full Spring context with Flyway migrations on in-memory H2 (PostgreSQL mode). They cover the sale/stock/dashboard flow, rejection of overselling, tenant isolation, and authentication enforcement. CI (`.github/workflows/ci.yml`) runs the backend tests and the frontend build on every push and pull request.

### Configuration

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | localhost, 5432, stockyard, stockyard, stockyard | Database connection |
| `JWT_SECRET` | dev-only value | **Set a random 32+ character value in production** |
| `JWT_EXPIRATION_MINUTES` | 720 | Token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Only needed if the UI is served from a different origin |
| `UPLOADS_DIR`, `UPLOADS_PUBLIC_BASE_URL` | `./uploads`, `/uploads` | Product image storage |
| `VITE_API_URL` (front-end build) | same origin in production | API base URL override |

### Known limitations and roadmap

- Product images are stored on local disk behind an `ImageStorage` class; swapping in object storage (for example Azure Blob) is a one-class change.
- The session token is kept in `localStorage`. The planned upgrade is refresh-token rotation in `httpOnly`, `SameSite=Strict` cookies.
- Planned next: low-stock email alerts, CSV import of historical sales, a public read-only catalog page, and product tags.

## License

MIT, see [LICENSE](LICENSE).

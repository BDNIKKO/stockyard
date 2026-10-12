# Deploying StockYard

This guide puts StockYard on the web for free using [Render](https://render.com). The whole app (React front end plus API) ships as one Docker image, so you deploy one web service and one database. Allow about ten minutes, most of it waiting for the first build.

## Before you start

- A free Render account (sign up with GitHub for the easiest connection).
- This repository on GitHub (already at `github.com/BDNIKKO/stockyard`).
- Render's free tier allows **one free PostgreSQL database per account**. If you already have one, delete it or use a paid plan, otherwise the blueprint will fail on the database step.

## Step by step

1. Sign in at **dashboard.render.com**.
2. Click **New +**, then **Blueprint**.
3. Connect your GitHub account if asked, and select the **stockyard** repository.
4. Render reads [`render.yaml`](../render.yaml) and shows what it will create:
   - a web service named `stockyard` (built from the `Dockerfile`)
   - a PostgreSQL database named `stockyard-db`
5. Click **Apply** (or **Deploy Blueprint**).
6. Wait for the build. The first one takes roughly 5 to 10 minutes while it compiles the React app and the Java API. Watch progress under the service's **Logs** tab. You are done when you see a line like `Started StockyardApplication`.
7. Open the service page. The public address is shown near the top and ends in `.onrender.com`. That is your live site.

Everything the app needs is wired up by the blueprint: the database connection comes from the database Render creates, and `JWT_SECRET` is generated for you. You do not have to set any environment variables by hand.

## Check that it works

1. Open the live address. You should see the StockYard login page.
2. Click **Create an account** and register.
3. Add a product, log a sale, and open the **Dashboard**. Revenue, profit and the channel chart should update.
4. Open `<your address>/swagger-ui.html` to see the interactive API documentation.
5. Resize the browser (or open it on your phone) to confirm the layout adapts.

## After deploying

- Put the live address in the README where it says **LIVE URL**, in the presentation's demo slide notes, and in your project submission.
- Commit and push the change. Render redeploys automatically on every push to `main`.

## What to expect on the free tier

| Behavior | Why it happens | What to do |
|---|---|---|
| First page load takes about 30 seconds | The service sleeps after about 15 minutes without traffic | Open the site a minute before a demo |
| Database is deleted after 30 days | Free Render databases expire | Upgrade the database plan, or redeploy and re-register |
| Uploaded product photos disappear after a redeploy | Images are stored on the container's disk | Re-upload them, or move image storage to object storage (see the roadmap) |
| Build is slow on the first deploy | Compiling both apps from scratch | Later builds reuse cached layers |

## Troubleshooting

- **Build fails at the database step:** you probably already have a free database. See "Before you start".
- **Service restarts or shows "Out of memory":** the free plan has 512 MB. The image already limits the JVM heap to 60% of available memory; if it still restarts, move the web service to the Starter plan.
- **Site loads but login fails with a server error:** open **Logs** and look for a database connection error. Confirm the `stockyard-db` database shows as **Available** and that the web service lists the `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` environment variables.
- **You see the Render "service waking up" page:** wait about 30 seconds and refresh.

## Configuration reference

| Variable | Set by | Purpose |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Blueprint (from the database) | Database connection |
| `JWT_SECRET` | Blueprint (generated) | Signs login tokens |
| `PORT` | Render | Port the app listens on (the image honors it) |
| `UPLOADS_DIR` | Optional | Where product photos are stored (default `./uploads`) |

## Hosting somewhere else

Any host that runs a Docker image works. Build with `docker build -t stockyard .`, run it with the variables above, and point it at a PostgreSQL 16 database. The database schema is created automatically on first start by the Flyway migrations.

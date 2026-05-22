# geoportal-api — Spring Boot

Backend REST para el Geoportal Entidad-Relación. Maven + Java 21 + Spring Boot 3.5.

## Stack

- Spring Boot 3.5, Maven, Java 21
- Spring Data JPA → Neon (PostgreSQL)
- Proxy WFS via RestTemplate
- Docker para deploy en Render

## Estructura

```
src/main/java/cl/geoportal/api/
├── config/CorsConfig.java          — CORS para localhost:3000 y *.vercel.app
├── controller/
│   ├── HealthController.java       — GET /health
│   ├── CatalogController.java      — GET /api/catalog
│   ├── LayerController.java        — GET /api/layers/{id}
│   └── JoinController.java         — GET /api/joins, GET /api/joins/{src}/{tgt}
├── service/
│   ├── CatalogService.java         — lee catalog/layers/*.yaml, cachea en memoria
│   ├── LayerService.java           — despacha a JPA o WfsProxyService
│   ├── JoinService.java            — lógica left/inner join + transforms
│   └── WfsProxyService.java        — proxy a GeoServer
├── entity/                         — JPA entities para capas estáticas en Neon
├── repository/                     — Spring Data JPA repositories
└── dto/                            — record DTOs para responses REST
src/main/resources/
├── application.properties
└── catalog/layers/                 — copias de los YAML del coordinator
```

## Variables de entorno requeridas

- `NEON_DATABASE_URL` — JDBC connection string de Neon (con `?sslmode=require`)
- `WFS_BASE_URL` — base URL del GeoServer (default en application.properties)

## Capas en Neon (JPA entities — pendiente implementar)

- `division_politica_administrativa_2023` → `DivisionPoliticaAdministrativa.java`
- `resultados_censo_de_poblacion_y_vivienda_2024` → `ResultadoCenso2024.java`

## Capas proxy en tiempo real (WFS — pendiente implementar)

- `establecimientos_de_salud_de_chile_febrero_2026` → `WfsProxyService.java`

## Catálogo

Los YAML de `catalog/layers/` están copiados en `src/main/resources/catalog/layers/`.
`CatalogService` los lee al arrancar y los cachea en memoria.
Cuando se actualice un YAML en el coordinator, copiarlo acá también.

## Correr local

```bash
cp .env.example .env.local
# Editar .env.local con NEON_DATABASE_URL real
./mvnw spring-boot:run
# API disponible en http://localhost:8080
```

## Build Docker

```bash
docker build -t geoportal-api .
docker run -p 8080:8080 -e NEON_DATABASE_URL=... geoportal-api
```

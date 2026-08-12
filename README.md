# Chemiconsult — Sistema de gestión de laboratorio

Sistema de gestión integral para Laboratorio Chemiconsult. Cubre el ciclo completo de análisis: recepción de muestras, asignación a analistas, carga de resultados y generación de informes PDF.

## Stack

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21 · Spring Boot 3.5 · Spring Security (JWT) |
| Persistencia | PostgreSQL · Spring Data JPA · HikariCP |
| Generación PDF | OpenPDF 1.3 |
| Storage | Supabase Storage (REST) |
| Email | Brevo API (WebFlux WebClient) |
| Frontend | Vanilla JS · Bootstrap 5 (estático servido por Spring) |
| Deploy | Fly.io · Docker (build Maven → JDK 21 slim) |

## Requisitos previos

- Java 21
- Maven 3.9+
- PostgreSQL 15+
- Cuenta Supabase (bucket para PDFs)
- Cuenta Brevo (emails transaccionales)

## Variables de entorno

Crear `chemiconsult/src/main/resources/application-local.properties` con:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chemiconsult
spring.datasource.username=postgres
spring.datasource.password=tupassword

jwt.secret=clave_larga_minimo_256_bits

supabase.url=https://xxxx.supabase.co
supabase.service-role-key=eyJ...

brevo.api.key=xkeysib-...
brevo.sender.email=lab@tudominio.com
brevo.sender.name=Chemiconsult

ALLOWED_ORIGINS=http://localhost:8080
```

## Correr localmente

```bash
cd chemiconsult
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

La app queda disponible en `http://localhost:8080`.

La base de datos se inicializa automáticamente (`ddl-auto=update` + `data.sql`).
Para poblar datos maestros (parámetros, resoluciones, matrices) ejecutar `seed_produccion.sql` manualmente la primera vez.

## Estructura del proyecto

```
chemiconsult/src/main/
├── java/com/chemiconsult/
│   ├── controller/          # Controllers REST (uno por módulo)
│   ├── service/             # Lógica de negocio
│   ├── entity/              # Entidades JPA (sufijo DE)
│   ├── to/                  # Transfer Objects / DTOs (sufijo TO)
│   ├── mapper/              # Conversión entity ↔ TO
│   ├── repository/          # Spring Data JPA repos
│   ├── security/            # Filtro JWT, JwtUtil, AuthEntryPoint
│   ├── configuration/       # SecurityConfig (CORS, rutas públicas)
│   ├── supabase/service/    # Cliente HTTP para Supabase Storage
│   ├── brevo/service/       # Cliente Brevo para emails
│   └── enums/               # 14 enumeraciones del dominio
└── resources/
    ├── application.properties          # Config base
    ├── application-local.properties    # Config desarrollo (no commitear)
    ├── application-prod.properties     # Config producción (todo por env vars)
    ├── data.sql                        # Migraciones y seed inicial
    └── static/                         # Frontend HTML/JS/CSS
```

## Módulos principales

| Módulo | Descripción |
|--------|-------------|
| **Muestras** | Estudios de análisis: creación, asignación de parámetros con límites reglamentarios, resultados, informe PDF |
| **Cola de análisis** | Cola de trabajo por analista: PENDIENTE → EN_CURSO → ANALIZADO → REVISADO |
| **Clientes** | CRM: personas físicas y empresas, sucursales, contactos, vinculación con usuario |
| **Agenda** | Eventos: muestreos, compras, vencimientos, visitas técnicas |
| **Panel técnico** | Matrices, resoluciones, parámetros, límites reglamentarios, numeradores |
| **Presupuesto** | Cotizaciones con número automático y PDF |
| **Documentos** | Archivo de documentos internos con seguimiento de vencimientos |
| **Stock** | Inventario de reactivos y materiales |
| **Tareas** | Tablero kanban interno |
| **Usuarios** | Alta y permisos por módulo (ADMIN / EMPLEADO / CLIENTE) |
| **Chat** | Mensajería 1 a 1 y canal grupal |

## Deploy en Fly.io

```bash
# Primer deploy
fly launch

# Deploys posteriores
fly deploy

# Configurar secretos
fly secrets set DB_URL=jdbc:... DB_USERNAME=... DB_PASSWORD=... \
  JWT_SECRET=... SUPABASE_URL=... SUPABASE_SERVICE_ROLE_KEY=... \
  BREVO_API_KEY=... ALLOWED_ORIGINS=https://tudominio.com

# Ver logs en tiempo real
fly logs

# Health check
curl https://chemiconsult-api.fly.dev/actuator/health
```

**Configuración actual (fly.toml):**
- App: `chemiconsult-api` · Región: `gru` (São Paulo)
- Recursos: 1 CPU shared · 1 GB RAM · Puerto interno 8080
- Auto-stop habilitado (arranca con el primer request)

## Seguridad

- Autenticación stateless con JWT (expira en 1 hora)
- Contraseñas hasheadas con BCrypt
- Rutas públicas: `/login`, `/actuator/health`, archivos estáticos
- Todos los demás endpoints requieren Bearer token
- Módulos habilitables por usuario (`USER_MODULOS`)

## Numeradores automáticos

Secuencias con lock pesimista en BD para evitar duplicados:
- `NUMERO_PROTOCOLO` — número de protocolo de análisis
- `NUMERO_PRESUPUESTO` — número de presupuesto

Se pueden consultar y ajustar desde `GET/PUT /api/numeradores`.

# 🎓 Sistema de Matrícula — Colegio Ada A. Byron

**Curso Integrador I: Sistemas Software | UTP 2026 | Sección 56474**
**Profesor:** Genns Eduardo Yataco Silva

## 👥 Integrantes

| Nombre | Código | Módulo |
|--------|--------|--------|
| Raul Arteaga | U23252314 | Pasarela de Pagos |
| Luciano Rojas | U23271185 | Portal del Padre |
| Franco Capcha | U23243027 | Panel Secretaría |
| Alexander Ortiz | U23208392 | Landing Page |

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 17 + Spring Boot 3.2.5 + Maven |
| Frontend | Angular 17 (standalone, lazy loading) |
| Base de datos | PostgreSQL 15 |
| Seguridad | JWT + Spring Security + BCrypt |
| Reportes | Apache POI (Excel) |
| Logs | Logback (via Spring Boot) |
| Utilidades | Google Guava 32, Apache Commons |

---

## 🚀 Cómo ejecutar el proyecto

### 1. Base de datos
Abre pgAdmin → adabyron_db → Query Tool → ejecuta `database/schema.sql`

### 2. Backend
```bash
cd backend
mvn spring-boot:run
# → http://localhost:8080
```

### 3. Frontend
```bash
cd frontend
npm install
ng serve
# → http://localhost:4200
```

### Credenciales por defecto
| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| admin | 1234 | ADMIN |

---

## 🗺️ Mapa de rutas

| URL | Descripción | Acceso |
|-----|-------------|--------|
| `localhost:4200/` | Página principal institucional | Público |
| `localhost:4200/login` | Inicio de sesión | Público |
| `localhost:4200/registro` | Registro de alumno | Público |
| `localhost:4200/portal-padre` | Solicitar matrícula virtual | Público |
| `localhost:4200/dashboard` | Panel administrador | 🔐 Login |
| `localhost:4200/alumnos` | Gestión de alumnos | 🔐 Login |
| `localhost:4200/grados` | Gestión de grados | 🔐 Login |
| `localhost:4200/secciones` | Gestión de secciones | 🔐 Login |
| `localhost:4200/matricula` | Registrar matrículas | 🔐 Login |
| `localhost:4200/pagos` | Control de pagos (Yape/Plin/Tarjeta) | 🔐 Login |
| `localhost:4200/secretaria` | Aprobar solicitudes de padres | 🔐 Login |
| `localhost:4200/usuarios` | Gestión de usuarios y roles | 🔐 Admin |

---

## 📡 Endpoints REST

```
POST /api/auth/login                → JWT token
GET  /api/auth/setup                → Crear admin (temporal)
POST /api/auth/register             → Registro usuario
GET  /api/estudiantes               → Listar alumnos
POST /api/estudiantes               → Crear alumno
GET  /api/estudiantes/buscar/dni/{d}→ Buscar por DNI
GET  /api/grados                    → Listar grados
GET  /api/secciones                 → Listar secciones
GET  /api/secciones/{id}/vacantes   → Vacantes disponibles
GET  /api/matriculas                → Listar matrículas
POST /api/matriculas                → Registrar matrícula
GET  /api/matriculas/stats          → Estadísticas
POST /api/solicitudes/registrar     → Nueva solicitud (público)
GET  /api/solicitudes/estado/{id}   → Consultar estado (público)
GET  /api/solicitudes               → Listar todas (secretaria)
PUT  /api/solicitudes/{id}/aprobar  → Aprobar
PUT  /api/solicitudes/{id}/observar → Observar
PUT  /api/solicitudes/{id}/rechazar → Rechazar
GET  /api/pagos                     → Listar pagos
POST /api/pagos/pasarela/procesar   → Procesar pago (Yape/Plin/Tarjeta)
GET  /api/pagos/pasarela/historial/{id} → Historial transacciones
GET  /api/usuarios                  → Listar usuarios (admin)
POST /api/usuarios                  → Crear usuario
PUT  /api/usuarios/{id}/estado      → Activar/Desactivar
PUT  /api/usuarios/{id}/password    → Cambiar contraseña
GET  /api/reportes/matriculas/excel → Exportar Excel
```

---

## 🏗️ Arquitectura — Principios aplicados

### MVC
- **Model:** Entidades JPA (`entity/`) + Repositorios (`repository/`)
- **View:** Componentes Angular (`frontend/components/`)
- **Controller:** Controladores REST (`controller/`)

### DAO
Todos los repositorios extienden `JpaRepository<T, ID>`, implementando el patrón DAO con Spring Data.

### TDD
Tests unitarios con JUnit 5 + MockMvc:
- `AuthControllerTest.java` — prueba login con credenciales válidas e inválidas
- `EstudianteControllerTest.java` — prueba CRUD de estudiantes
- `MatriculaControllerTest.java` — prueba registro de matrículas

### SOLID
- **S** — SRP: cada clase tiene una sola responsabilidad
- **O** — OCP: interfaces Repository extensibles sin modificar
- **D** — DIP: inyección de dependencias con `@RequiredArgsConstructor`

### Seguridad
- Contraseñas con BCrypt (cost factor 10)
- JWT para autenticación stateless
- Spring Security con roles por endpoint
- CORS configurado solo para `localhost:4200`

### Librerías Java (APF3)
- **Apache POI** — generación de reportes Excel
- **Google Guava** — validaciones con `Preconditions`
- **Logback** — logging en controllers (via Spring Boot)
- **Apache Commons** — incluido en Spring Boot

---

## 💳 Pasarela de Pago

Soporta 3 métodos:

| Método | Simulación de rechazo |
|--------|----------------------|
| VISA/MC/AMEX | Número que empieza con `4000` |
| Yape | Código que empieza con `0000` |
| Plin | Celular que empieza con `900` |

Cada transacción genera un código único `TXN-YYYYMMDD-XXXX` y queda guardada en `transaccion_pago`.

---

## 🌿 Ramas Git

| Rama | Responsable | Contenido |
|------|-------------|-----------|
| `main` | Todos | Código estable |
| `develop` | Todos | Integración |
| `feature/landing-page` | Alexander | Página institucional |
| `feature/portal-padre` | Luciano | Matrícula virtual |
| `feature/secretaria` | Franco | Panel secretaría |
| `feature/pasarela-pago` | Raul | Yape + Plin + Tarjeta |
| `feature/gestion-usuarios` | Todos | CRUD usuarios |

---

## ✅ Comparativa vs Colegio2 (referencia)

| Aspecto | Colegio2 | Ada Byron |
|---------|----------|-----------|
| Arquitectura | Servlet JSP | Spring Boot REST + Angular 17 |
| Seguridad | Sin hash, sin JWT | BCrypt + JWT |
| Roles | Admin / Alumno | Admin / Secretaria / Directivo / Padre |
| Pasarela de pago | Básica, solo tarjeta | Yape + Plin + VISA/MC/AMEX |
| Portal padres | No tiene | 4 pasos + consulta estado |
| Landing page | Sí tiene | Mejorada con más secciones |
| Reportes | No tiene | Excel con Apache POI |
| Tests | No tiene | JUnit + MockMvc |
| Logging | No tiene | Logback |

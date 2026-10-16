# Guía de subida — Taller Facade (Juan, Campiño y Andre)

Todo va en **un solo repositorio** de Juan, con dos carpetas: `backend/` y `frontend/`. Cada uno sube **sus propios archivos con su propia cuenta de GitHub**, para que el profesor vea los commits de los tres.

## 0. Repositorio: https://github.com/Juandavid100/Taller-Facade

1. Juan agrega a Campiño y a Andre en **Settings → Collaborators → Add people**. Ellos deben **aceptar la invitación** (llega al correo).
2. Campiño y Andre, en su computador:

```bash
git clone https://github.com/Juandavid100/Taller-Facade.git
cd Taller-Facade
```

3. Cada uno descomprime **su parte** y copia el contenido **dentro** de la carpeta `Taller-Facade` (respetando las carpetas `backend/` y `frontend/` que trae el zip).

> Los archivos se copian todos de una vez; los commits se hacen por grupos con `git add` de cada ruta, como se indica abajo.

## Orden de subida

```
Juan (commits 1 y 2) ──► Campiño (commits 3 a 6) ──► Juan (commits 7 a 9)
Andre (commits A1 a A5) en paralelo, apenas Juan suba la parte 1
```

El backend debe ir en ese orden porque la fachada de Juan usa las clases de Campiño. Antes de cada turno, hacer `git pull`.

---

## Juan — parte 1 (empezar ya)

```bash
git add README.md GUIA-COMMITS.md .gitignore backend/pom.xml backend/src/main/resources backend/src/main/java/com/clinica/gestion/GestionMedicaApplication.java backend/src/main/java/com/clinica/gestion/config/CorsConfig.java backend/src/main/java/com/clinica/gestion/exception
git commit -m "Estructura inicial del backend con Spring Boot"

git add backend/src/main/java/com/clinica/gestion/model/Paciente.java backend/src/main/java/com/clinica/gestion/model/Medico.java backend/src/main/java/com/clinica/gestion/model/Cita.java backend/src/main/java/com/clinica/gestion/service/PacienteService.java backend/src/main/java/com/clinica/gestion/service/AgendaService.java backend/src/main/java/com/clinica/gestion/dto/RegistroPacienteRequest.java backend/src/main/java/com/clinica/gestion/dto/LoginRequest.java backend/src/main/java/com/clinica/gestion/dto/CitaRequest.java backend/src/main/java/com/clinica/gestion/dto/MedicoDisponible.java
git commit -m "Subsistemas PacienteService y AgendaService"

git push
```

Avisar a Campiño: "ya subí, haga pull".

## Campiño — subsistemas clínicos

```bash
git pull

git add backend/src/main/java/com/clinica/gestion/model/Consulta.java backend/src/main/java/com/clinica/gestion/model/Medicamento.java backend/src/main/java/com/clinica/gestion/model/Prescripcion.java backend/src/main/java/com/clinica/gestion/model/ParametroLaboratorio.java backend/src/main/java/com/clinica/gestion/model/ResultadoLaboratorio.java
git commit -m "Modelos de consultas, prescripciones y laboratorio"

git add backend/src/main/java/com/clinica/gestion/service/HistoriaClinicaService.java
git commit -m "Subsistema HistoriaClinicaService con validacion de alergias"

git add backend/src/main/java/com/clinica/gestion/service/PrescripcionService.java backend/src/main/java/com/clinica/gestion/service/LaboratorioService.java
git commit -m "Subsistemas PrescripcionService y LaboratorioService"

git add backend/src/test/java/com/clinica/gestion/SubsistemasClinicosTest.java
git commit -m "Pruebas de los subsistemas clinicos"

git push
```

Avisar a Juan: "ya subí, haga pull".

## Juan — parte 2 (la fachada)

```bash
git pull

git add backend/src/main/java/com/clinica/gestion/facade backend/src/main/java/com/clinica/gestion/dto/HistoriaCompleta.java backend/src/main/java/com/clinica/gestion/dto/PrescripcionRequest.java backend/src/main/java/com/clinica/gestion/dto/LaboratorioRequest.java
git commit -m "Patron Facade: ClinicaFacade coordina los 5 subsistemas"

git add backend/src/main/java/com/clinica/gestion/controller backend/src/main/java/com/clinica/gestion/config/DatosIniciales.java
git commit -m "Endpoints REST /api/clinica y datos iniciales"

git add backend/Dockerfile backend/.dockerignore backend/src/test/java/com/clinica/gestion/ClinicaFacadeTest.java
git commit -m "Dockerfile para Railway y pruebas de la fachada"

git push
```

Luego desplegar en **Railway** (pasos en el README) y pasarle la URL a Andre.

## Andre — frontend (en paralelo)

Empezar cuando Juan avise que subió la parte 1 (el repositorio no puede estar vacío para hacer `pull`).

```bash
git pull

git add frontend/package.json frontend/vite.config.js frontend/index.html frontend/.gitignore frontend/.env.example frontend/src/api.js frontend/src/utils.js frontend/src/styles.css
git commit -m "Estructura del frontend con React y Vite"

git add frontend/src/components/Acceso.jsx
git commit -m "Registro e inicio de sesion"

git add frontend/src/components/ListaCitas.jsx frontend/src/components/ResultadoLaboratorio.jsx frontend/src/components/Dashboard.jsx frontend/src/components/AgendarCita.jsx
git commit -m "Dashboard y modulo para agendar citas"

git add frontend/src/components/NuevaPrescripcion.jsx frontend/src/components/HistoriaClinica.jsx frontend/src/components/Laboratorio.jsx
git commit -m "Historia clinica con pestanas y resultados de laboratorio"

git add frontend/src/App.jsx frontend/src/main.jsx
git commit -m "Navegacion principal del portal del paciente"

git pull --rebase
git push
```

Si al hacer `push` sale "rejected", es porque otro subió antes: hacer `git pull --rebase` y otra vez `git push`.

Cuando Juan pase la URL de Railway, desplegar en **Vercel** (pasos en el README) con la variable `VITE_API_URL`. Al final, Juan pone la URL de Vercel en la variable `FRONTEND_URL` de Railway.

---

## Revisión final antes de entregar

- [ ] `https://<railway>/` muestra el JSON con `"estado": "ok"` y los integrantes
- [ ] `https://<railway>/api/clinica/medicos?especialidad=Cardiología` devuelve 2 médicos
- [ ] La página de Vercel abre, se puede entrar con **1085000000 / demo123**
- [ ] Se puede crear una cuenta nueva y agendar una cita
- [ ] Recetar "Amoxicilina" al paciente de prueba muestra el error de alergia a la penicilina
- [ ] Laboratorio muestra valores en verde (normal) y rojo (fuera de rango)
- [ ] Los tres aparecen en **Insights → Contributors** del repositorio
- [ ] Las dos URLs quedaron pegadas en el README

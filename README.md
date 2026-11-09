# Táctica — Gestión deportiva (Android)

App Android en **Kotlin + Jetpack Compose** construida a partir del mockup de Figma
"Aplicación de gestión deportiva".

## 1. Conectar Firebase (una sola vez)

La app guarda los datos en la nube con **Firebase Authentication** (cuentas) y
**Cloud Firestore** (base de datos). Sin este paso el proyecto **no compila**.

1. Entra a https://console.firebase.google.com con tu cuenta de Google → **Crear proyecto**
   (por ejemplo `tactica`). Google Analytics es opcional.
2. En el proyecto: ícono de **Android** → *Registrar app*:
   - Nombre del paquete: `com.juandiego.tactica` (tiene que ser exactamente ese)
   - Apodo: Táctica → **Registrar app**
3. Descarga **google-services.json** y cópialo dentro de la carpeta `Tactica/app/`
   (al lado de `build.gradle.kts` del módulo app). Los pasos del SDK que muestra la
   consola ya están hechos en el proyecto: dale *Siguiente* hasta terminar.
4. Menú izquierdo → **Compilación → Authentication → Comenzar** → pestaña *Método de acceso*
   → **Correo electrónico/contraseña** → Habilitar → Guardar.
5. Menú izquierdo → **Compilación → Firestore Database → Crear base de datos**
   → elige una ubicación (p. ej. `southamerica-east1` o `us-central`) → *Modo de producción*.
6. En Firestore → pestaña **Reglas** → pega el contenido de `firestore.rules` → **Publicar**.
7. En Android Studio: **File → Sync Project with Gradle Files** y luego ▶ Run.

## 2. Cómo abrirla

1. Descomprime el .zip.
2. En Android Studio: **File → Open** y elige la carpeta `Tactica`.
3. Espera a que termine el *Gradle Sync* (la primera vez descarga dependencias).
   Si Android Studio sugiere actualizar el Android Gradle Plugin, puedes aceptar.
4. Ejecuta ▶ en un emulador o celular (Android 8.0 / API 26 o superior).

Crea una cuenta en la app con tu correo y una contraseña de al menos 6 caracteres.

## Pantallas

| Pantalla | Archivo |
|---|---|
| Login / crear cuenta | `ui/screens/AuthScreen.kt` |
| Mis equipos (crear, entrar, salir) | `ui/screens/ClubHubScreen.kt` |
| Contenedor del equipo + barra inferior | `ui/screens/TeamScreen.kt` |
| Inicio (próxima actividad, resumen, alertas) y Agenda | `ui/screens/DashboardTab.kt` |
| Partido en vivo (marcador, cronómetro, acciones, alineación, cambios, estadísticas) | `ui/screens/LiveMatchTab.kt` |
| Equipo (plantilla y estado de pagos) | `ui/screens/PlayersTab.kt` |
| Gestión de pagos (matrícula y mensualidades) | `ui/screens/PaymentSheet.kt` |
| Estadísticas e informes | `ui/screens/StatsTab.kt` |
| Formularios (equipo con escudo, actividad, jugador) | `ui/screens/Forms.kt` |

## Estructura

```
app/src/main/java/com/juandiego/tactica/
├── MainActivity.kt
├── data/            Modelos, datos de ejemplo y reglas (pagos, fútbol, voleibol)
│   └── remote/      Firebase: login, lectura/escritura en Firestore y conversión de datos
└── ui/
    ├── AppViewModel.kt   Estado de la app + cronómetro
    ├── TacticaApp.kt     Navegación entre login, equipos y equipo
    ├── theme/            Colores del diseño (paleta de Figma)
    ├── components/       Botones, campos, hojas inferiores, tarjetas, etc.
    └── screens/          Pantallas
```

## Notas

- **Datos en la nube:** cada usuario tiene `users/{uid}/teams/{teamId}` en Firestore, con un
  documento por equipo (plantilla, pagos, agenda e historial). Firestore guarda una copia
  local, así que la app funciona sin internet y sincroniza al reconectarse.
- **Sesión:** Firebase la recuerda; al abrir la app entras directo hasta que toques *Salir*.
- **Escudos:** se reducen a 256 px y se guardan en Base64 dentro del documento del equipo,
  así no hace falta Firebase Storage (que exige plan de pago).
- El **partido en vivo** se lleva en memoria mientras se juega; al terminarlo, su resultado
  queda guardado en el historial del equipo.
- Arquitectura: app Android (MVVM) como cliente + Firebase como backend en la nube (BaaS).
- Reglas implementadas: fútbol con 11 titulares y 5 cambios; voleibol con 6 en cancha,
  6 cambios por set, sets a 25 (el 5.º a 15) con diferencia de 2, gana quien llega a 3 sets,
  y reemplazos de líbero aparte.
- Estado de pago: *Vencido* si falta la matrícula o hay meses anteriores sin pagar;
  *Próximo a pagar* desde el día 20 si falta el mes actual; *Pendiente* antes del 20.

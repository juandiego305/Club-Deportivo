package com.juandiego.tactica.data.remote

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.juandiego.tactica.data.LiveMatch
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.data.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthException(message: String) : Exception(message)

/**
 * Único punto de acceso a la nube.
 * - Firebase Authentication: cuentas con correo y contraseña (la sesión se recuerda sola).
 * - Cloud Firestore: un documento por equipo en users/{uid}/teams/{teamId}.
 *   Firestore guarda una copia local, así que la app también funciona sin internet
 *   y sincroniza cuando vuelve la conexión.
 */
class FirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    // ─── Autenticación ──────────────────────────────────────────────────────

    fun currentUser(): User? = auth.currentUser?.let { u ->
        val email = u.email.orEmpty()
        User(u.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@"), email)
    }

    suspend fun register(name: String, email: String, password: String): User = authCall {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw AuthException("No se pudo crear la cuenta.")
        firebaseUser.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
        db.collection("users").document(firebaseUser.uid)
            .set(mapOf("name" to name, "email" to email)).await()
        User(name, email)
    }

    suspend fun login(email: String, password: String): User = authCall {
        auth.signInWithEmailAndPassword(email, password).await()
        currentUser() ?: throw AuthException("No se pudo iniciar sesión.")
    }

    /** Envía el correo de Firebase para restablecer la contraseña. */
    suspend fun sendPasswordReset(email: String): Unit = authCall {
        auth.sendPasswordResetEmail(email).await()
        Unit
    }

    fun logout() = auth.signOut()

    // ─── Equipos ────────────────────────────────────────────────────────────

    private fun teamsRef(): CollectionReference {
        val uid = auth.currentUser?.uid ?: error("No hay sesión iniciada")
        return db.collection("users").document(uid).collection("teams")
    }

    /** Escucha en tiempo real los equipos del usuario. */
    fun observeTeams(): Flow<List<Team>> = callbackFlow {
        val registration = teamsRef().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // el ViewModel muestra el error y permite reintentar
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { doc -> doc.data?.let(::teamFromMap) }.sortedBy { it.id })
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * Guarda el equipo completo. No hace falta esperar: Firestore lo escribe
     * primero en la caché local (la UI se actualiza al instante) y luego lo sube.
     */
    fun saveTeam(team: Team) {
        teamsRef().document(team.id.toString()).set(team.toMap())
    }

    fun deleteTeam(teamId: Long) {
        teamsRef().document(teamId.toString()).delete()
        deleteLiveMatch(teamId)
    }

    // ─── Partido en vivo ────────────────────────────────────────────────────

    private fun liveRef(): CollectionReference {
        val uid = auth.currentUser?.uid ?: error("No hay sesión iniciada")
        return db.collection("users").document(uid).collection("liveMatches")
    }

    /** Se llama en cada acción (gol, punto, pausa…), no cada segundo. */
    fun saveLiveMatch(teamId: Long, match: LiveMatch) {
        liveRef().document(teamId.toString()).set(match.toMap(System.currentTimeMillis()))
    }

    fun deleteLiveMatch(teamId: Long) {
        liveRef().document(teamId.toString()).delete()
    }

    /** Partidos que estaban en curso al cerrar la app (por id de equipo). */
    suspend fun loadLiveMatches(): Map<Long, LiveMatch> {
        val now = System.currentTimeMillis()
        return liveRef().get().await().documents.mapNotNull { doc ->
            val id = doc.id.toLongOrNull() ?: return@mapNotNull null
            val data = doc.data ?: return@mapNotNull null
            id to liveMatchFromMap(data, now)
        }.toMap()
    }

    private suspend fun <T> authCall(block: suspend () -> T): T = try {
        block()
    } catch (e: AuthException) {
        throw e
    } catch (e: FirebaseAuthUserCollisionException) {
        throw AuthException("Ya existe una cuenta con ese correo.")
    } catch (e: FirebaseAuthWeakPasswordException) {
        throw AuthException("La contraseña debe tener al menos 6 caracteres.")
    } catch (e: FirebaseAuthInvalidUserException) {
        throw AuthException("No existe una cuenta con ese correo.")
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        throw AuthException("Correo o contraseña incorrectos.")
    } catch (e: FirebaseNetworkException) {
        throw AuthException("Sin conexión a internet. Revisa tu red e inténtalo de nuevo.")
    } catch (e: Exception) {
        throw AuthException("No se pudo completar la operación. Inténtalo de nuevo.")
    }
}

package com.ehedgehog.android.amonger.screen

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.coroutines.resumeWithException

class PlayersManager(private val playersReference: DatabaseReference, private val imagesReference: StorageReference) {

    private var newPlayerRef: DatabaseReference? = null
    private var playersListener: ValueEventListener? = null

    //TODO make result receiving inside manager
    fun monitorConnectionState() = callbackFlow<Result<Boolean>> {
        val connectedRef = Firebase.database.getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                this@callbackFlow.trySendBlocking(Result.success(connected))
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.trySendBlocking(Result.failure(error.toException()))
            }
        }

        connectedRef.addValueEventListener(listener)

        awaitClose {
            connectedRef.removeEventListener(listener)
        }

    }

    suspend fun addNewPlayer(playerTemp: PlayerItemTemp) {
        if (newPlayerRef == null)
            newPlayerRef = playersReference.push()

        newPlayerRef?.setValue(
            PlayerItem(
                newPlayerRef?.key,
                playerTemp.name,
                playerTemp.code,
                playerTemp.aka,
                playerTemp.host,
                playerTemp.imageUrl,
                playerTemp.notes,
                playerTemp.tags
            )
        )?.await()
        newPlayerRef = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun uploadPlayerImage(imageUri: Uri, playerId: String? = null) = suspendCancellableCoroutine { continuation ->
        if (playerId == null)
            newPlayerRef = playersReference.push()

        val id = playerId?:newPlayerRef?.key
        val storageRef = imagesReference.child("$id.png")
        val uri = Uri.fromFile(imageUri.path?.let { File(it) })
        storageRef.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("ImageTest", "upload image complete: ${task.result}")
                continuation.resume(task.result.toString(), null)
            } else {
                task.exception?.let {
                    Log.i("ImageTest", "upload image error")
                    continuation.resumeWithException(it)
                }
            }
        }
    }

    suspend fun updatePlayer(player: PlayerItem) {
        player.id?.let { playersReference.child(it).setValue(player).await() }
    }

    suspend fun removePlayer(player: PlayerItem) {
        player.id?.let {
            playersReference.child(it).removeValue().await()
            if (!player.imageUrl.isNullOrEmpty()) imagesReference.child("$it.png").delete().await()
        }
    }

    fun fetchStoredPlayers() = callbackFlow<Result<List<PlayerItem>>> {
        playersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.map {
                    it.getValue(PlayerItem::class.java)
                }
                this@callbackFlow.trySendBlocking(Result.success(players.filterNotNull()))
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.trySendBlocking(Result.failure(error.toException()))
            }
        }

        playersReference.addValueEventListener(playersListener as ValueEventListener)

        awaitClose {
            if (playersListener != null)
                playersReference.removeEventListener(playersListener as ValueEventListener)
        }
    }

    fun clearPlayersListener() {
        playersListener?.let {
            playersReference.removeEventListener(it)
            playersListener = null
        }
    }

}
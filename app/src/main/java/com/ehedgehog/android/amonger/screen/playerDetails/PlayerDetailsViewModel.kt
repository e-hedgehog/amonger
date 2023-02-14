package com.ehedgehog.android.amonger.screen.playerDetails

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ehedgehog.android.amonger.Application
import com.ehedgehog.android.amonger.screen.PlayerItem
import com.ehedgehog.android.amonger.screen.PlayerItemTemp
import com.ehedgehog.android.amonger.screen.PlayersManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerDetailsViewModel(private val currentPlayer: PlayerItem) : ViewModel() {

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val playerName = MutableLiveData<String>()
    val playerCode = MutableLiveData<String>()
    val playerAka = MutableLiveData<String>()
    val playerIsHost = MutableLiveData<Boolean>()
    val playerNotes = MutableLiveData<String>()
    val playerImageUrl = MutableLiveData<String>()

    private val _navigateToImageCropper = MutableLiveData<Boolean>()
    val navigateToImageCropper: LiveData<Boolean> get() = _navigateToImageCropper

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    @Inject
    lateinit var playersManager: PlayersManager

    init {
        Application.appComponent.injectPlayerDetailsViewModel(this)
        currentPlayer.name?.let { playerName.value = it }
        currentPlayer.code?.let { playerCode.value = it }
        currentPlayer.aka?.let { playerAka.value = it }
        currentPlayer.host?.let { playerIsHost.value = it }
        currentPlayer.notes?.let { playerNotes.value = it }
        currentPlayer.imageUrl?.let { playerImageUrl.value = it }
    }

    fun displayImageCropper() {
        _navigateToImageCropper.value = true
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayImageCropperComplete() {
        _navigateToImageCropper.value = null
    }

    fun savePlayer() {
        if (currentPlayer.id != null) updatePlayerWithImage() else addPlayerWithImage()
    }

    private fun addPlayerWithImage() {
        coroutineScope.launch {
            _isLoading.value = true
            if (playerImageUrl.value != null) {
                try {
                    val url = playersManager.uploadPlayerImage(Uri.parse(playerImageUrl.value))
                    Log.i("ImageTest", "add player received url: $url")
                    addPlayer(url)
//                    .collect {
//                    when {
//                        it.isSuccess -> addPlayer(it.getOrNull())
//                        it.isFailure -> it.exceptionOrNull()?.printStackTrace()
//                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                addPlayer()
            }
            _isLoading.value = false
        }
    }

    private suspend fun addPlayer(playerImageUrl: String? = null) {
        playersManager.addNewPlayer(PlayerItemTemp(playerName.value, playerCode.value, playerAka.value, playerIsHost.value, playerImageUrl, playerNotes.value))
    }

    private fun updatePlayerWithImage() {
        coroutineScope.launch {
            _isLoading.value = true
            if (currentPlayer.imageUrl != playerImageUrl.value) {
                currentPlayer.id?.let { id ->
                    try {
                        val url = playersManager.uploadPlayerImage(Uri.parse(playerImageUrl.value), id)
                        Log.i("ImageTest", "update player received url: $url")
                        updatePlayer(url)
                    } catch (e: Exception) {
                        Log.i("ImageTest", "received error", e)
                        e.printStackTrace()
                    }

//                        .collect {
//                        when {
//                            it.isSuccess -> updatePlayer(it.getOrNull())
//                            it.isFailure -> it.exceptionOrNull()?.printStackTrace()
//                        }
//                    }
                }
            } else {
                updatePlayer()
            }
            _isLoading.value = false
        }
    }

    private suspend fun updatePlayer(imageUrl: String? = null) {
        val url = imageUrl ?: playerImageUrl.value
        playersManager.updatePlayer(PlayerItem(currentPlayer.id, playerName.value, playerCode.value, playerAka.value, playerIsHost.value, url, playerNotes.value))
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class PlayerDetailsViewModelFactory(val player: PlayerItem) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            modelClass.getConstructor(PlayerItem::class.java).newInstance(player)
    }
}
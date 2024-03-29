package com.ehedgehog.android.amonger.screen.playerDetails

import android.annotation.SuppressLint
import android.net.Uri
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ehedgehog.android.amonger.Application
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.screen.PlayerItem
import com.ehedgehog.android.amonger.screen.PlayerItemTemp
import com.ehedgehog.android.amonger.screen.PlayersManager
import com.ehedgehog.android.amonger.screen.base.BaseViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerDetailsViewModel(private val currentPlayer: PlayerItem) : BaseViewModel() {

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val playerName = MutableLiveData<String>()
    val playerCode = MutableLiveData<String>()
    val playerAka = MutableLiveData<String>()
    val playerIsHost = MutableLiveData<Boolean>()
    val playerNotes = MutableLiveData<String>()
    val playerImageUrl = MutableLiveData<String?>()
    val playerTagsList = MutableLiveData<List<String>>()

    private val _navigateToImageCropper = MutableLiveData<Boolean>()
    val navigateToImageCropper: LiveData<Boolean> get() = _navigateToImageCropper

    private val _openImageContext = MutableLiveData<Boolean>()
    val openImageContext: LiveData<Boolean> get() = _openImageContext

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
        currentPlayer.tags?.let { playerTagsList.value = it }
    }

    fun displayImageCropper() {
        _navigateToImageCropper.value = true
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayImageCropperComplete() {
        _navigateToImageCropper.value = null
    }

    fun openImageContext(): Boolean {
        if (playerImageUrl.value != null) {
            _openImageContext.value = true
            return true
        }
        return false
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun openImageContextComplete() {
        _openImageContext.value = null
    }

    fun savePlayer() {
        if (Firebase.auth.currentUser == null) {
            displayErrorSnackbar("You must be authorized to write data.")
            return
        } else if (playerCode.value.isNullOrEmpty() || playerName.value.isNullOrEmpty()) {
            displayErrorSnackbar("You haven't filled out all the required fields. \"Nickname\" and \"Friend code\" are mandatory.")
            return
        }

        doIfOnline {
            if (currentPlayer.id != null) updatePlayerWithImage() else addPlayerWithImage()
        }
    }

    fun onContextMenuItemClicked(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.context_delete_image -> {
                playerImageUrl.value = null
                true
            }
            else -> false
        }
    }

    private fun addPlayerWithImage() {
        coroutineScope.launch {
            _isLoading.value = true
            if (playerImageUrl.value != null) {
                try {
                    val url = playersManager.uploadPlayerImage(Uri.parse(playerImageUrl.value))
                    addPlayer(url)
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
        playersManager.addNewPlayer(PlayerItemTemp(playerName.value, playerCode.value, playerAka.value, playerIsHost.value, playerImageUrl, playerNotes.value, playerTagsList.value))
    }

    private fun updatePlayerWithImage() {
        coroutineScope.launch {
            _isLoading.value = true
            if (currentPlayer.imageUrl != playerImageUrl.value) {
                currentPlayer.id?.let { id ->
                    try {
                        val url = if (playerImageUrl.value != null) {
                            playersManager.uploadPlayerImage(Uri.parse(playerImageUrl.value), id)
                        } else {
                            playersManager.removeImage(currentPlayer)
                            null
                        }
                        updatePlayer(url)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                updatePlayer()
            }
            _isLoading.value = false
        }
    }

    private suspend fun updatePlayer(imageUrl: String? = null) {
        val url = imageUrl ?: playerImageUrl.value
        playersManager.updatePlayer(PlayerItem(currentPlayer.id, playerName.value, playerCode.value, playerAka.value, playerIsHost.value, url, playerNotes.value, playerTagsList.value))
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
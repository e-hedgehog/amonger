package com.ehedgehog.android.amonger.screen.playerDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ehedgehog.android.amonger.Application
import com.ehedgehog.android.amonger.screen.PlayerItem
import com.ehedgehog.android.amonger.screen.base.BaseViewModel

class PlayerDetailsViewModel(currentPlayer: PlayerItem) : BaseViewModel() {

    private val _player = MutableLiveData<PlayerItem>()
    val player: LiveData<PlayerItem> get() = _player

    init {
        Application.appComponent.injectPlayerDetailsViewModel(this)
        _player.value = currentPlayer
    }

    class PlayerDetailsViewModelFactory(private val player: PlayerItem) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            modelClass.getConstructor(PlayerItem::class.java).newInstance(player)
    }
}
package com.ehedgehog.android.amonger.screen.playersList

import android.annotation.SuppressLint
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ehedgehog.android.amonger.Application
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.screen.PlayerItem
import com.ehedgehog.android.amonger.screen.PlayersManager
import com.ehedgehog.android.amonger.screen.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayersListViewModel : BaseViewModel() {

    enum class SearchMode {CODE, NAME}
    enum class ThemeMode {DAY, NIGHT}

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _searchMode = MutableLiveData<SearchMode>().apply { value = SearchMode.CODE }
    val searchMode: LiveData<SearchMode> get() = _searchMode

    private val _themeMode = MutableLiveData<ThemeMode>().apply { value = ThemeMode.DAY }
    val themeMode: LiveData<ThemeMode> get() = _themeMode

    private var fullPlayersList: List<PlayerItem>? = emptyList()
    private val _playersList = MutableLiveData<List<PlayerItem>>()
    val playersList: LiveData<List<PlayerItem>> get() = _playersList

    private val _navigateToSelectedPlayer = MutableLiveData<PlayerItem>()
    val navigateToSelectedPlayer: LiveData<PlayerItem> get() = _navigateToSelectedPlayer

    @Inject
    lateinit var playersManager: PlayersManager

    init {
        Application.appComponent.injectPlayersListViewModel(this)
    }

    fun createNewPlayer() {
        displayPlayerDetails(PlayerItem())
    }

    fun displayPlayerDetails(player: PlayerItem) {
        _navigateToSelectedPlayer.value = player
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayPlayerDetailsComplete() {
        _navigateToSelectedPlayer.value = null
    }

    fun changeSearchMode() {
        _searchMode.value = when (searchMode.value) {
            SearchMode.CODE -> SearchMode.NAME
            SearchMode.NAME -> SearchMode.CODE
            else -> SearchMode.CODE
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        _themeMode.value = themeMode
    }

    fun searchPlayers(query: String) {
        val resultList: ArrayList<PlayerItem> = arrayListOf()
        if (query.isEmpty()) {
            fullPlayersList?.let { resultList.addAll(it) }
        } else {
            val queryString = query.lowercase().trim()
            fullPlayersList?.forEach {
                val playerField = if (searchMode.value == SearchMode.CODE) it.code else it.name
                if (playerField?.lowercase()?.trim()?.contains(queryString) == true) {
                    resultList.add(it)
                }
            }
        }
        _playersList.value = resultList
    }

    fun loadStoredPlayers() {
        coroutineScope.launch {
            playersManager.fetchStoredPlayers().collect {
                when {
                    it.isSuccess -> {
                        _playersList.value = it.getOrNull()
                        fullPlayersList = it.getOrNull()
                    }
                    it.isFailure -> it.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    fun removeStoredPlayer(player: PlayerItem) {
        coroutineScope.launch {
            playersManager.removePlayer(player)
        }
    }

    fun onContextMenuItemClicked(menuItem: MenuItem, player: PlayerItem): Boolean {
        return when (menuItem.itemId) {
            R.id.context_remove_item -> {
                removeStoredPlayer(player)
                true
            }
            else -> false
        }
    }

    fun clearListeners() {
        playersManager.clearPlayersListener()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
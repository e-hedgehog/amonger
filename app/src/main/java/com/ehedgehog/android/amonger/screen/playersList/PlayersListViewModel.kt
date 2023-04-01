package com.ehedgehog.android.amonger.screen.playersList

import android.annotation.SuppressLint
import android.util.Log
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

    private val _filtersVisible = MutableLiveData<Boolean>().apply { value = false }
    val filtersVisible: LiveData<Boolean> get() = _filtersVisible

    private val _selectedFiltersList = MutableLiveData<MutableMap<String, String>>().apply { value = mutableMapOf() }
    val selectedFiltersList: LiveData<MutableMap<String, String>> get() = _selectedFiltersList

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

    fun filtersButtonClicked() {
        filtersVisible.value?.let {
            _filtersVisible.value = !it
            if (filtersVisible.value == false) _selectedFiltersList.value = mutableMapOf()
        }
    }

    fun searchPlayers(query: String) {
        val resultList: ArrayList<PlayerItem> = arrayListOf()
        if (query.isEmpty()) {
            fullPlayersList?.let { resultList.addAll(it) }
        } else {
            val queryString = query.lowercase().trim()
            fullPlayersList?.forEach {
                val playerField = if (searchMode.value == SearchMode.CODE) it.code else "${it.name}, ${it.aka}"
                if (playerField?.lowercase()?.trim()?.contains(queryString) == true) {
                    resultList.add(it)
                }
            }
        }

        if (!selectedFiltersList.value.isNullOrEmpty()) {
            Log.i("FiltersTest", "filtration")
            _playersList.value = resultList.filter { item -> item.tags?.containsAll(selectedFiltersList.value!!.values) == true }
        } else
            _playersList.value = resultList
    }

    fun applyFilter(key: String, filter: CharSequence) {
        val filtersList = selectedFiltersList.value
        if (filtersList?.contains(key) == true && (filtersList[key] == filter || filter == "Not selected")) {
            Log.i("FiltersTest", "minus filter")
            filtersList.remove(key)
        } else {
            Log.i("FiltersTest", "plus filter")
            filtersList?.put(key, filter.toString())
        }
        Log.i("FiltersTest", "applyFilter($filter): $filtersList")
        _selectedFiltersList.value = filtersList
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

    fun removePlayer(player: PlayerItem) {
        displayConfirmationDialog("Are you sure you want to delete this player?") {
            removeStoredPlayer(player)
        }
    }

    private fun removeStoredPlayer(player: PlayerItem) {
        coroutineScope.launch {
            playersManager.removePlayer(player)
        }
    }

    fun onContextMenuItemClicked(menuItem: MenuItem, player: PlayerItem): Boolean {
        return when (menuItem.itemId) {
            R.id.context_remove_item -> {
                removePlayer(player)
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
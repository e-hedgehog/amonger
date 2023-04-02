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

    val searchQuery = MutableLiveData<String>()

    private val _filtersVisible = MutableLiveData<Boolean>().apply { value = false }
    val filtersVisible: LiveData<Boolean> get() = _filtersVisible

    private val _selectedFiltersMap = MutableLiveData<MutableMap<String, String>>().apply { value = mutableMapOf() }
    val selectedFiltersMap: LiveData<MutableMap<String, String>> get() = _selectedFiltersMap

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
            if (filtersVisible.value == false) _selectedFiltersMap.value = mutableMapOf()
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

        if (!selectedFiltersMap.value.isNullOrEmpty()) {
            Log.i("FiltersTest", "filtration with ${selectedFiltersMap.value.toString()}")
            _playersList.value = resultList.filter { item -> item.tags?.containsAll(selectedFiltersMap.value!!.values) == true }
            Log.i("FiltersTest", "resultList = ${resultList.map { it.name }}")
        } else
            _playersList.value = resultList
    }

    fun applyFilter(key: String, filter: CharSequence) {
        val filtersMap = selectedFiltersMap.value
        if ((filtersMap?.get(key) == filter || filter == "Not selected")) {
            filtersMap?.remove(key)
        } else {
            filtersMap?.put(key, filter.toString())
        }
        Log.i("FiltersTest", "applyFilter($filter): $filtersMap")
        _selectedFiltersMap.value = filtersMap
    }

    fun loadStoredPlayers() {
        coroutineScope.launch {
            playersManager.fetchStoredPlayers().collect {
                when {
                    it.isSuccess -> {
                        if (selectedFiltersMap.value.isNullOrEmpty() && searchQuery.value.isNullOrEmpty())
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
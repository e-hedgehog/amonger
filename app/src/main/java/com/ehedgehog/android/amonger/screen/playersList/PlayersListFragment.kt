package com.ehedgehog.android.amonger.screen.playersList

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.FragmentPlayersListBinding
import com.ehedgehog.android.amonger.screen.PlayersPreferences
import com.ehedgehog.android.amonger.screen.playersList.PlayersListViewModel.ThemeMode

class PlayersListFragment : Fragment(), OnQueryTextListener, MenuProvider {

    private val viewModel: PlayersListViewModel by viewModels()
    private lateinit var binding: FragmentPlayersListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_players_list, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.playersRecyclerView.adapter = PlayersListAdapter(viewModel::displayPlayerDetails)
        binding.playersRecyclerView.itemAnimator = null

        binding.playersSearch.setOnQueryTextListener(this)

        viewModel.setThemeMode(PlayersPreferences.getStoredThemeMode(requireContext()))

        viewModel.searchMode.observe(viewLifecycleOwner) {
            onQueryTextChange(binding.playersSearch.query.toString())
        }

        viewModel.navigateToSelectedPlayer.observe(viewLifecycleOwner) { player ->
            if (player != null) {
                findNavController().navigate(
                    PlayersListFragmentDirections.actionPlayersListToPlayerDetails(
                        player
                    )
                )
                viewModel.displayPlayerDetailsComplete()
            }
        }

        viewModel.loadStoredPlayers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onStop() {
        super.onStop()
        viewModel.clearListeners()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            viewModel.searchPlayers(newText)
            return true
        }
        return false
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_players_list, menu)
        val themeModeMenuItem = menu.findItem(R.id.theme_mode)
        when (PlayersPreferences.getStoredThemeMode(requireContext())) {
            ThemeMode.DAY -> setupDayModeMenuItem(themeModeMenuItem)
            ThemeMode.NIGHT -> setupNightModeMenuItem(themeModeMenuItem)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.theme_mode -> {
                when (viewModel.themeMode.value) {
                    ThemeMode.NIGHT -> setupDayMode(menuItem)
                    ThemeMode.DAY -> setupNightMode(menuItem)
                    else -> return false
                }
                true
            }
            else -> false
        }
    }

    private fun setupDayMode(menuItem: MenuItem) {
        viewModel.setThemeMode(ThemeMode.DAY)
        setupDayModeMenuItem(menuItem)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        PlayersPreferences.setStoredThemeMode(requireContext(), ThemeMode.DAY)
    }

    private fun setupDayModeMenuItem(menuItem: MenuItem) {
        menuItem.title = getString(R.string.night_mode_label)
        menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_night_mode)
    }

    private fun setupNightMode(menuItem: MenuItem) {
        viewModel.setThemeMode(ThemeMode.NIGHT)
        setupNightModeMenuItem(menuItem)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        PlayersPreferences.setStoredThemeMode(requireContext(), ThemeMode.NIGHT)
    }

    private fun setupNightModeMenuItem(menuItem: MenuItem) {
        menuItem.title = getString(R.string.day_mode_label)
        menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_day_mode)
    }

}
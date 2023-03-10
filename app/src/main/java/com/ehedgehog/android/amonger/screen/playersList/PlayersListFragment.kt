package com.ehedgehog.android.amonger.screen.playersList

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.FragmentPlayersListBinding
import com.ehedgehog.android.amonger.screen.PlayersPreferences
import com.ehedgehog.android.amonger.screen.base.BaseFragment
import com.ehedgehog.android.amonger.screen.playersList.PlayersListViewModel.ThemeMode

class PlayersListFragment : BaseFragment<PlayersListViewModel, FragmentPlayersListBinding>(R.layout.fragment_players_list), OnQueryTextListener, MenuProvider {

    override val viewModel: PlayersListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = viewModel

        binding.playersRecyclerView.adapter = PlayersListAdapter(
            viewModel::displayPlayerDetails,
            viewModel::onContextMenuItemClicked
        )
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

        return view
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
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.theme_mode -> {
                when (viewModel.themeMode.value) {
                    ThemeMode.NIGHT -> setupDayMode()
                    ThemeMode.DAY -> setupNightMode()
                    else -> return false
                }
                true
            }
            else -> false
        }
    }

    private fun setupDayMode() {
        viewModel.setThemeMode(ThemeMode.DAY)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        PlayersPreferences.setStoredThemeMode(requireContext(), ThemeMode.DAY)
    }

    private fun setupNightMode() {
        viewModel.setThemeMode(ThemeMode.NIGHT)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        PlayersPreferences.setStoredThemeMode(requireContext(), ThemeMode.NIGHT)
    }

}
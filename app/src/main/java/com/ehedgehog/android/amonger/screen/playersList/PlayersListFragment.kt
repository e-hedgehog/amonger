package com.ehedgehog.android.amonger.screen.playersList

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.forEach
import androidx.core.view.updateMarginsRelative
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.ehedgehog.android.amonger.BuildConfig
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.ButtonSearchFiltersBinding
import com.ehedgehog.android.amonger.databinding.FragmentPlayersListBinding
import com.ehedgehog.android.amonger.screen.PlayersPreferences
import com.ehedgehog.android.amonger.screen.base.BaseFragment
import com.ehedgehog.android.amonger.screen.playersList.PlayersListViewModel.ThemeMode
import com.google.android.material.chip.Chip

class PlayersListFragment : BaseFragment<PlayersListViewModel, FragmentPlayersListBinding>(R.layout.fragment_players_list), OnQueryTextListener, MenuProvider {

    override val viewModel: PlayersListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = viewModel

        @Suppress("KotlinConstantConditions")
        if (BuildConfig.FLAVOR == "user")
            binding.addPlayerButton.visibility = View.GONE

        binding.playersRecyclerView.adapter = PlayersListAdapter(
            viewModel::displayPlayerDetails,
            viewModel::onContextMenuItemClicked
        )
        binding.playersRecyclerView.itemAnimator = null

        binding.playersSearch.setOnQueryTextListener(this)

        viewModel.setThemeMode(PlayersPreferences.getStoredThemeMode(requireContext()))

        val filtersButtonBinding: ButtonSearchFiltersBinding =
            DataBindingUtil.inflate(inflater, R.layout.button_search_filters, null, false)
        filtersButtonBinding.lifecycleOwner = viewLifecycleOwner
        filtersButtonBinding.viewModel = viewModel
        val layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16F, resources.displayMetrics)
        (layoutParams as MarginLayoutParams).updateMarginsRelative(end = margin.toInt())
        filtersButtonBinding.filtersButton.layoutParams = layoutParams
        (binding.playersSearch.getChildAt(0) as LinearLayout).addView(filtersButtonBinding.filtersButton)

        viewModel.searchMode.observe(viewLifecycleOwner) {
            onQueryTextChange(binding.playersSearch.query.toString())
        }

        if (viewModel.selectedFiltersMap.value?.contains("map") == true) {
            binding.filterMap.text = viewModel.selectedFiltersMap.value?.get("map")
            setCheckedFilter(binding.filterMap, checked = true, checkable = false)
        }
        if (viewModel.selectedFiltersMap.value?.contains("cd") == true) {
            binding.filterCd.text = viewModel.selectedFiltersMap.value?.get("cd")
            setCheckedFilter(binding.filterCd, checked = true, checkable = false)
        }
        viewModel.selectedFiltersMap.observe(viewLifecycleOwner) {
            it?.let {
                if (viewModel.filtersVisible.value == false) {
                    binding.searchFilterChips.forEach { view ->
                        val chip = view as Chip
                        setCheckedFilter(chip, false, chip.text == getString(R.string.host_label))
                    }
                    binding.filterMap.setText(R.string.map_filter_label)
                    binding.filterCd.setText(R.string.cd_filter_label)
                }
                viewModel.searchPlayers(binding.playersSearch.query.toString())
            }
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
            viewModel.searchQuery.value = newText
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

    private fun setCheckedFilter(filter: Chip, checked: Boolean, checkable: Boolean) {
        filter.isCheckable = true
        filter.isChecked = checked
        filter.isCheckable = checkable
    }

}
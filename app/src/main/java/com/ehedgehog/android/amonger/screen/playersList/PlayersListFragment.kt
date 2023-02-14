package com.ehedgehog.android.amonger.screen.playersList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.FragmentPlayersListBinding

class PlayersListFragment : Fragment(), OnQueryTextListener {

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

}
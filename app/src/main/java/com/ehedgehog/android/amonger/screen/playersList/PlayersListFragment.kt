package com.ehedgehog.android.amonger.screen.playersList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.FragmentPlayersListBinding

class PlayersListFragment : Fragment() {

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

        viewModel.navigateToSelectedPlayer.observe(viewLifecycleOwner, Observer { player ->
            if (player != null) {
                findNavController().navigate(PlayersListFragmentDirections.actionPlayersListToPlayerDetails(player))
                viewModel.displayPlayerDetailsComplete()
            }
        })

        viewModel.loadStoredPlayers()

        return binding.root
    }

}
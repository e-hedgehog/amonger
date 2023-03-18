package com.ehedgehog.android.amonger.screen.playerDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.FragmentPlayerDetailsBinding
import com.ehedgehog.android.amonger.screen.base.BaseFragment
import com.ehedgehog.android.amonger.screen.playerDetails.PlayerDetailsViewModel.PlayerDetailsViewModelFactory

class PlayerDetailsFragment: BaseFragment<PlayerDetailsViewModel, FragmentPlayerDetailsBinding>(R.layout.fragment_player_details) {

    override val viewModel: PlayerDetailsViewModel by viewModels {
        PlayerDetailsViewModelFactory(PlayerDetailsFragmentArgs.fromBundle(requireArguments()).player)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = viewModel
        binding.playerImage.clipToOutline = true

        return view
    }

}
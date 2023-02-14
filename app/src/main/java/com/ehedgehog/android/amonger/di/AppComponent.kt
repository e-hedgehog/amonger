package com.ehedgehog.android.amonger.di

import com.ehedgehog.android.amonger.screen.playerDetails.PlayerDetailsViewModel
import com.ehedgehog.android.amonger.screen.playersList.PlayersListViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class])
interface AppComponent {

    fun injectPlayersListViewModel(viewModel: PlayersListViewModel)
    fun injectPlayerDetailsViewModel(viewModel: PlayerDetailsViewModel)

}
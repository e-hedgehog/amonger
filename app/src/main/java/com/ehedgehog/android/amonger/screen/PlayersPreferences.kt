package com.ehedgehog.android.amonger.screen

import android.content.Context
import com.ehedgehog.android.amonger.screen.playersList.PlayersListViewModel

class PlayersPreferences {

    companion object {

        private const val PREF_THEME_MODE = "storedThemeMode"

        fun getStoredThemeMode(context: Context): PlayersListViewModel.ThemeMode {
            val index = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
                .getInt(PREF_THEME_MODE, -1)
            return PlayersListViewModel.ThemeMode.values()[index]
        }

        fun setStoredThemeMode(context: Context, themeMode: PlayersListViewModel.ThemeMode) {
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
                .edit()
                .putInt(PREF_THEME_MODE, themeMode.ordinal)
                .apply()
        }

    }

}
package com.ehedgehog.android.amonger.screen.base

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel: ViewModel() {

    private val _showErrorSnackbar = MutableLiveData<String>()
    val showErrorSnackbar: LiveData<String> get() = _showErrorSnackbar

    fun displayErrorSnackbar(message: String) {
        _showErrorSnackbar.value = message
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayErrorSnackbarComplete() {
        _showErrorSnackbar.value = null
    }

}
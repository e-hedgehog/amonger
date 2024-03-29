package com.ehedgehog.android.amonger.screen.base

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel: ViewModel() {

    private val _showErrorSnackbar = MutableLiveData<String>()
    val showErrorSnackbar: LiveData<String> get() = _showErrorSnackbar

    private val _showConfirmationDialog = MutableLiveData<Pair<String, () -> Unit>>()
    val showConfirmationDialog: LiveData<Pair<String, () -> Unit>> get() = _showConfirmationDialog

    private val _doIfOnline = MutableLiveData<() -> Unit>()
    val doIfOnline: LiveData<() -> Unit> get() = _doIfOnline

    fun displayErrorSnackbar(message: String) {
        _showErrorSnackbar.value = message
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayErrorSnackbarComplete() {
        _showErrorSnackbar.value = null
    }

    fun displayConfirmationDialog(message: String, onConfirm: () -> Unit) {
        _showConfirmationDialog.value = Pair(message, onConfirm)
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayConfirmationDialogCompleted() {
        _showConfirmationDialog.value = null
    }

    fun doIfOnline(action: () -> Unit) {
        _doIfOnline.value = action
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun doIfOnlineCompleted() {
        _doIfOnline.value = null
    }

}
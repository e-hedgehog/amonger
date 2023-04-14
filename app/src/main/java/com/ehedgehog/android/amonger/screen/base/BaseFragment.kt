package com.ehedgehog.android.amonger.screen.base

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment<VM: BaseViewModel, B: ViewDataBinding>(@LayoutRes val layoutId: Int): Fragment() {

    protected abstract val viewModel: VM
    protected lateinit var binding: B

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.showErrorSnackbar.observe(viewLifecycleOwner) { message ->
            message?.let {
                hideSoftKeyboard()
                Snackbar.make(
                    binding.root,
                    it,
                    Snackbar.LENGTH_SHORT
                ).show()
                viewModel.displayErrorSnackbarComplete()
            }
        }

        viewModel.showConfirmationDialog.observe(viewLifecycleOwner) { pair ->
            pair?.let {
                AlertDialog.Builder(requireContext())
                    .setMessage(pair.first)
                    .setPositiveButton("Yes") { _, _ -> pair.second.invoke() }
                    .setNegativeButton("Cancel", null)
                    .show()
                viewModel.displayConfirmationDialogCompleted()
            }
        }

        viewModel.doIfOnline.observe(viewLifecycleOwner) { action ->
            action?.let {
                if (isOnline()) it.invoke() else viewModel.displayErrorSnackbar("No internet connection.")
                viewModel.doIfOnlineCompleted()
            }
        }

        return binding.root
    }

    private fun hideSoftKeyboard() {
        requireActivity().currentFocus?.let { view ->
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

}
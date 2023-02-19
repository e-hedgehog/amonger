package com.ehedgehog.android.amonger.screen.playerDetails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.FragmentPlayerDetailsBinding
import com.ehedgehog.android.amonger.screen.playerDetails.PlayerDetailsViewModel.PlayerDetailsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.io.File

class PlayerDetailsFragment: Fragment(), MenuProvider {

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.getUriFilePath(requireContext())?.let {
                viewModel.playerImageUrl.value = Uri.fromFile(File(it)).toString()
            }
        } else {
            result.error?.printStackTrace()
        }
    }

    private val viewModel: PlayerDetailsViewModel by viewModels {
        PlayerDetailsViewModelFactory(PlayerDetailsFragmentArgs.fromBundle(requireArguments()).player)
    }
    private lateinit var binding: FragmentPlayerDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_player_details, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.playerImage.clipToOutline = true

        viewModel.monitorConnectionState()

        viewModel.navigateToImageCropper.observe(viewLifecycleOwner) {
            it?.let {
                cropImage.launch(
                    CropImageContractOptions(
                        null, CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = false,
                            guidelines = CropImageView.Guidelines.ON,
                            outputCompressFormat = Bitmap.CompressFormat.PNG,
                            fixAspectRatio = true,
                            maxCropResultWidth = 500,
                            maxCropResultHeight = 500,
                            activityBackgroundColor = Color.BLACK,
                        )
                    )
                )
                viewModel.displayImageCropperComplete()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it == false) findNavController().navigateUp()
        }

        viewModel.showNoConnectionSnackbar.observe(viewLifecycleOwner) {
            it?.let {
                hideSoftKeyboard()
                Snackbar.make(
                    binding.playerDetailsContainer,
                    "No internet connection.",
                    Snackbar.LENGTH_SHORT
                ).show()
                viewModel.displayNoConnectionSnackbarComplete()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_player_details, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_save_player -> {
                viewModel.savePlayer()
                true
            }
            else -> false
        }
    }

    private fun hideSoftKeyboard() {
        requireActivity().currentFocus?.let { view ->
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}
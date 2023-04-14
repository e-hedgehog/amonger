package com.ehedgehog.android.amonger.screen.playerDetails

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.forEach
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.FragmentPlayerDetailsBinding
import com.ehedgehog.android.amonger.screen.base.BaseFragment
import com.ehedgehog.android.amonger.screen.playerDetails.PlayerDetailsViewModel.PlayerDetailsViewModelFactory
import com.google.android.material.chip.Chip
import java.io.File

class PlayerDetailsFragment: BaseFragment<PlayerDetailsViewModel, FragmentPlayerDetailsBinding>(R.layout.fragment_player_details), MenuProvider {

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.getUriFilePath(requireContext())?.let {
                viewModel.playerImageUrl.value = Uri.fromFile(File(it)).toString()
            }
        } else {
            result.error?.printStackTrace()
        }
    }

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

        viewModel.playerIsHost.observe(viewLifecycleOwner) {
            binding.chipHost.isChecked = it
        }
        
        binding.playerChipGroup.forEach {
            (it as? Chip)?.setOnCheckedChangeListener { _, _ ->
                val checkedTitles = mutableListOf<String>()
                binding.playerChipGroup.checkedChipIds.forEach { id ->
                    checkedTitles.add(binding.playerChipGroup.findViewById<Chip>(id).text.toString())
                }
                binding.playerHostSwitch.isChecked = checkedTitles.contains(getString(R.string.host_label))
                viewModel.playerTagsList.value = checkedTitles
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it == false) findNavController().navigateUp()
        }

        return view
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

}
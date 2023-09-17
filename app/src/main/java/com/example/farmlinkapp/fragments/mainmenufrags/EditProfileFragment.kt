package com.example.farmlinkapp.fragments.mainmenufrags

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.FragmentEditProfileBinding
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private val viewModel by viewModels<UserAccountViewModel>()
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            imageUri = it.data?.data
            Glide.with(this)
                .load(imageUri)
                .into(binding.editProfilePictureView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        showInterfaceLoading()
                    }
                    is Resource.Success ->{
                        hideInterfaceLoading()
                        showUserInformation(it.data!!)
                    }
                    is Resource.Error ->{

                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.editInfo.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.saveChanges.startAnimation()
                    }
                    is Resource.Success ->{
                        binding.saveChanges.revertAnimation()
                        findNavController().navigate(R.id.action_editProfileFragment_pop)
                    }
                    is Resource.Error ->{
                        binding.saveChanges.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        binding.saveChanges.setOnClickListener {
            binding.apply {
                val userId = userIDView.text.toString().trim()
                val fullName = editFullName.text.toString().trim()
                val email = editEmail.text.toString().trim()
                val user = User(fullName, email, userId)
                viewModel.updateInfo(user, imageUri)
            }
        }

        binding.addProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageActivityResultLauncher.launch(intent)
        }

    }

    private fun showUserInformation(data: User) {
        binding.apply {
            userIDView.setText(data.userId)
            Glide.with(this@EditProfileFragment)
                .load(data.imagePath)
                .error(R.mipmap.profile_mini_round)
                .into(editProfilePictureView)
            editFullName.setText(data.fullName)
            editEmail.setText(data.email)
        }

    }

    private fun hideInterfaceLoading() {
        binding.editUserProgressBar.visibility = View.INVISIBLE
        binding.editFullName.visibility = View.VISIBLE
        binding.editEmail.visibility = View.VISIBLE
        binding.editContactNumber.visibility = View.VISIBLE
        binding.changePassword.visibility = View.VISIBLE
        binding.saveChanges.visibility = View.VISIBLE
    }

    private fun showInterfaceLoading() {
        binding.editUserProgressBar.visibility = View.VISIBLE
        binding.editFullName.visibility = View.INVISIBLE
        binding.editEmail.visibility = View.INVISIBLE
        binding.editContactNumber.visibility = View.INVISIBLE
        binding.changePassword.visibility = View.INVISIBLE
        binding.saveChanges.visibility = View.INVISIBLE
    }
}
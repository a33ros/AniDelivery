package com.example.farmlinkapp.fragments.mainmenufrags

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.activities.LoginRegisterActivity
import com.example.farmlinkapp.databinding.FragmentProfileBinding
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.ProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment: Fragment(){
    private lateinit var binding: FragmentProfileBinding
    val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firestore = Firebase.firestore
        val auth = Firebase.auth

        lifecycleScope.launchWhenStarted {
            firestore.collection("user").document(auth.uid!!)
                .collection("sellerInformation")
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty){
                        binding.statusProfile.text = "Buyer/Seller"
                        binding.sellerRegister.visibility = View.INVISIBLE
                        binding.sellerProfileButton.visibility = View.VISIBLE
                    } else {
                        binding.statusProfile.text = "Buyer"
                        binding.sellerRegister.visibility = View.VISIBLE
                        binding.sellerProfileButton.visibility = View.INVISIBLE
                    }
                }
        }

        binding.forum.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_forumFragment)
        }

        binding.notifications.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_notificationsFragment)
        }

        binding.orders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_trackOrderFragment)
        }

        binding.sellerProfileButton.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_sellerProfileFragment)
        }

        binding.sellerRegister.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_verificationIntroFragment)
        }

        binding.addShippingAddressButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addShippingAddressFragment)
        }

        binding.profileSettingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.logOutButton.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it){
                    is Resource.Loading ->{
                    }
                    is Resource.Success ->{
                        Glide.with(requireView())
                            .load(it.data?.imagePath)
                            .error(R.mipmap.profile_mini_round)
                            .into(binding.userProfilePictureView)
                        binding.userNameViewProfile.text = it.data?.fullName
                        binding.userEmailProfileView.text = it.data?.email
                    }
                    is Resource.Error ->{
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }

            }
        }

    }
}
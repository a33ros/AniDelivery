package com.example.farmlinkapp.fragments.mainmenufrags

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
import com.example.farmlinkapp.databinding.FragmentHomeBinding
import com.example.farmlinkapp.databinding.FragmentIntroductionBinding
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.LoginVM
import com.example.farmlinkapp.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    private val viewModel by viewModels<LoginVM>()
    private val profileVM by viewModels<ProfileViewModel>()
    private val auth = FirebaseAuth.getInstance()

    var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = auth.uid
        viewModel.updateUid(userId!!)

        lifecycleScope.launchWhenStarted {
            profileVM.user.collectLatest {
                when(it){
                    is Resource.Loading ->{
                    }
                    is Resource.Success ->{
                        Glide.with(requireView())
                            .load(it.data?.imagePath)
                            .error(R.mipmap.profile_mini_round)
                            .into(binding.miniProfile)
                        binding.miniName.text = it.data?.fullName
                        val userId = it.data?.userId
                        binding.miniUserID.text = "User ID: ${userId}"
                    }
                    is Resource.Error ->{
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }

            }
        }

        binding.buyButton.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_buyFragment)
        }

        binding.trackOrderButton.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_trackOrderFragment)
        }

        binding.transactionHistoryButton.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_transactionHistoryFragment)
        }

        binding.feedbackButton.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_feedbackFragment)
        }

        binding.forumButton.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_forumFragment)
        }

        binding.aboutUsButton.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_aboutUsFragment)
        }
    }
}
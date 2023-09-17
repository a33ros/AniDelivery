package com.example.farmlinkapp.farmer_verification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.farmlinkapp.R
import com.example.farmlinkapp.databinding.FragmentVerificationIntroBinding

class VerificationIntroFragment : Fragment() {
    private lateinit var binding: FragmentVerificationIntroBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerificationIntroBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBuyButton.setOnClickListener {
            findNavController().navigate(R.id.action_verificationIntroFragment_pop)
        }

        binding.verificationStartButton.setOnClickListener {
            findNavController().navigate(R.id.action_verificationIntroFragment_to_verificationFarmerFragment)
        }

    }

}
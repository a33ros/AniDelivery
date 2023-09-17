package com.example.farmlinkapp.seller_fragments.product_status

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.farmlinkapp.R
import com.example.farmlinkapp.databinding.FragmentToBePickedUpBinding

class ToBePickedUpFragment : Fragment() {
    private lateinit var binding: FragmentToBePickedUpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentToBePickedUpBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}
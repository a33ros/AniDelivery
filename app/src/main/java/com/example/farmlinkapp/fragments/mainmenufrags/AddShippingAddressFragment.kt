package com.example.farmlinkapp.fragments.mainmenufrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.ShippingAddress
import com.example.farmlinkapp.databinding.FragmentAddShippingAddressBinding
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.AddShippingAddressViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddShippingAddressFragment : Fragment() {
    private lateinit var binding: FragmentAddShippingAddressBinding
    val viewModel by viewModels<AddShippingAddressViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.addShippingAddress.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.addShippingAddressButton.startAnimation()
                    }
                    is Resource.Success ->{
                        binding.addShippingAddressButton.revertAnimation()
                        findNavController().navigate(R.id.action_addShippingAddressFragment_pop)
                        Toast.makeText(requireContext(), "Shipping Address added!", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error ->{
                        binding.addShippingAddressButton.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddShippingAddressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBillingAddressButton.setOnClickListener {
            findNavController().navigate(R.id.action_addShippingAddressFragment_pop)
        }

        binding.apply {
            addShippingAddressButton.setOnClickListener {
                val addressType = addressTypeTextBox.text.toString()
                val fullNameShipping = fullNameBillingTextBox.text.toString()
                val phone = billingPhoneTextBox.text.toString()
                val street = billingStreetTextBox.text.toString()
                val barangay = billingBarangayTextBox.text.toString()
                val city = billingCityTextBox.text.toString()
                val province = billingProvinceTextBox.text.toString()
                val postalCode = billingPostalCodeTextBox.text.toString()

                val shippingAddress = ShippingAddress(addressType, fullNameShipping, phone, street, barangay, city, province, postalCode)

                viewModel.addShippingAddress(shippingAddress)
            }
        }
    }

}
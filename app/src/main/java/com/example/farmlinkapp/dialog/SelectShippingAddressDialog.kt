package com.example.farmlinkapp.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmlinkapp.R
import com.example.farmlinkapp.adapters.ShippingAddressAdapter
import com.example.farmlinkapp.data.ShippingAddress
import com.example.farmlinkapp.databinding.SelectShippingAddressLayoutBinding
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.CheckOutViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SelectShippingAddressDialog : BottomSheetDialogFragment() {

    interface OnAddressSelectedListener {
        fun onAddressSelected(address: ShippingAddress)
    }

    private lateinit var binding: SelectShippingAddressLayoutBinding
    private val shippingAddressAdapter by lazy { ShippingAddressAdapter() }
    private val viewModel by viewModels<CheckOutViewModel>()

    var onAddressSelectedListener: OnAddressSelectedListener? = null
    private var selectedAddress: ShippingAddress? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SelectShippingAddressLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSelectionRecyclerView()

        shippingAddressAdapter.onClick = {shippingAddress ->
            selectedAddress = shippingAddress
            onAddressSelectedListener?.onAddressSelected(selectedAddress!!)

            dismiss()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.address.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        //do nothing
                    }
                    is Resource.Success ->{
                        if (it.data!!.isEmpty()){
                            binding.selectionAddress.visibility = View.INVISIBLE
                            binding.noShippingAddress.visibility = View.VISIBLE
                        }
                        shippingAddressAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error ->{
                        Toast.makeText(requireContext(), "Error: $${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setupSelectionRecyclerView() {
        binding.selectionAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = shippingAddressAdapter
        }
    }
}
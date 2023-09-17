package com.example.farmlinkapp.fragments.home_buttonfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmlinkapp.adapters.OrdersAdapter
import com.example.farmlinkapp.databinding.FragmentTrackOrderBinding
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TrackOrderFragment : Fragment() {
    private lateinit var binding: FragmentTrackOrderBinding
    val viewModel by viewModels<OrdersViewModel>()
    val ordersAdapter by lazy { OrdersAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackOrderBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOrderList()

        lifecycleScope.launchWhenStarted {
            viewModel.allOrders.collectLatest {
                when(it) {
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        ordersAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        ordersAdapter.onClick ={
            val action = TrackOrderFragmentDirections.actionTrackOrderFragmentToTrackOrderProductFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun setupOrderList() {
        binding.orderList.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

}
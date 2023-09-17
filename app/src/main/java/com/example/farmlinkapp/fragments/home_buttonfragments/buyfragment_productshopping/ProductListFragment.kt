package com.example.farmlinkapp.fragments.home_buttonfragments.buyfragment_productshopping


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.farmlinkapp.R
import com.example.farmlinkapp.adapters.ProductsAdapter
import com.example.farmlinkapp.data.Product
import com.example.farmlinkapp.databinding.FragmentProductListBinding
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.ProductsListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductListFragment : Fragment() {
    private lateinit var binding: FragmentProductListBinding
    private lateinit var productsAdapter: ProductsAdapter
    private val viewModel by viewModels<ProductsListViewModel>()

    private lateinit var productList: String
    private var shuffledProductsList: List<Product> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productList = arguments?.getString("Products") ?: throw IllegalArgumentException("Products not found in arguments bundle")

        binding.titleFragment.text = productList

        if (productList == "Crops"){
            lifecycleScope.launchWhenStarted {
                viewModel.cropsList.collectLatest { crops ->
                    when(crops){
                        is Resource.Loading ->{
                            showLoading()
                        }
                        is Resource.Success ->{
                            hideLoading()
                            shuffledProductsList = crops.data?.shuffled()!!
                            productsAdapter.differ.submitList(crops.data)
                        }
                        is Resource.Error ->{
                            hideLoading()
                            Toast.makeText(requireContext(),crops.message , Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        } else if (productList == "Vegetables") {
            lifecycleScope.launchWhenStarted {
                viewModel.vegetableList.collectLatest { vegetables ->
                    when(vegetables){
                        is Resource.Loading ->{
                            showLoading()
                        }
                        is Resource.Success ->{
                            hideLoading()
                            shuffledProductsList = vegetables.data?.shuffled()!!
                            productsAdapter.differ.submitList(vegetables.data)
                        }
                        is Resource.Error ->{
                            hideLoading()
                            Toast.makeText(requireContext(),vegetables.message , Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }

        setupProducts()

        productsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_productListFragment_to_productDetailsFragment,b)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_productListFragment_pop)
        }

    }

    private fun hideLoading(){
        binding.loadingOn.visibility = View.GONE
    }

    private fun showLoading(){
        binding.loadingOn.visibility = View.VISIBLE
    }

    private fun setupProducts() {
        productsAdapter = ProductsAdapter()
        binding.productsListView.apply {
            layoutManager = GridLayoutManager(requireContext(),2, GridLayoutManager.VERTICAL, false)
            adapter = productsAdapter
        }
    }

    private fun refreshProducts() {
        if (productList == "Crops"){
            lifecycleScope.launchWhenStarted {
                viewModel.cropsList.collectLatest { crops ->
                    when(crops){
                        is Resource.Loading ->{
                            showLoading()
                        }
                        is Resource.Success ->{
                            hideLoading()
                            shuffledProductsList = crops.data?.shuffled()!!
                            productsAdapter.differ.submitList(crops.data)
                        }
                        is Resource.Error ->{
                            hideLoading()
                            Toast.makeText(requireContext(),crops.message , Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        } else if (productList == "Vegetables") {
            lifecycleScope.launchWhenStarted {
                viewModel.vegetableList.collectLatest { vegetables ->
                    when(vegetables){
                        is Resource.Loading ->{
                            showLoading()
                        }
                        is Resource.Success ->{
                            hideLoading()
                            shuffledProductsList = vegetables.data?.shuffled()!!
                            productsAdapter.differ.submitList(vegetables.data)
                        }
                        is Resource.Error ->{
                            hideLoading()
                            Toast.makeText(requireContext(),vegetables.message , Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshProducts()
    }

}
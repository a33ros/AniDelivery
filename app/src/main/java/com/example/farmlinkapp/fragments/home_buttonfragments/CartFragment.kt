package com.example.farmlinkapp.fragments.home_buttonfragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmlinkapp.R
import com.example.farmlinkapp.adapters.CartProductAdapter
import com.example.farmlinkapp.databinding.FragmentCartBinding
import com.example.farmlinkapp.firebase.FirebaseCommon
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.CartViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormat

class CartFragment : Fragment(R.layout.fragment_cart) {
    private lateinit var binding: FragmentCartBinding
    private val cartAdapter by lazy { CartProductAdapter() }
    private val viewModel by activityViewModels<CartViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartView()

        binding.checkOutButton.setOnClickListener {
            findNavController().navigate(R.id.action_cartFragment_to_checkOutFragment)
        }

        var totalPrice = 0f

        lifecycleScope.launchWhenStarted {
            viewModel.productsPrice.collectLatest {price ->
                price?.let {
                    val decimalFormat = DecimalFormat("#.##")
                    val finalPrice = decimalFormat.format(price)
                    binding.grandTotalPriceCart.text = "₱ ${finalPrice}"
                    totalPrice = finalPrice.toFloat()
                }
            }
        }

        binding.checkOutButton.setOnClickListener {
            val action = CartFragmentDirections.actionCartFragmentToCheckOutFragment(
                totalPrice,
                cartAdapter.differ.currentList.toTypedArray()
            )
            findNavController().navigate(action)
        }

        cartAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it.product) }
            findNavController().navigate(R.id.action_cartFragment_to_productDetailsFragment, b)
        }

        cartAdapter.onPlusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.INCREASE)
        }

        cartAdapter.onMinusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.DECREASE)
        }

        cartAdapter.onDeleteClick = {
            lifecycleScope.launchWhenStarted {
                viewModel.deleteDialog.collectLatest {
                    val alrtDialog = AlertDialog.Builder(requireContext()).apply {
                        setTitle("Delete item from cart")
                        setMessage("Do you want to remove this item from your cart?")
                        setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        setPositiveButton("Yes") { dialog, _ ->
                            viewModel.deleteCartProductPlain(it)
                            dialog.dismiss()
                        }
                    }
                    alrtDialog.create()
                    alrtDialog.show()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.deleteDialog.collectLatest {
                val alrtDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Delete item from cart")
                    setMessage("Do you want to remove this item from your cart?")
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    setPositiveButton("Yes") { dialog, _ ->
                        viewModel.deleteCartProduct(it)
                        dialog.dismiss()
                    }
                }
                alrtDialog.create()
                alrtDialog.show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.cartProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressBarCart.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressBarCart.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()) {
                            showEmptyCart()
                        } else {
                            hideEmptyCart()
                            cartAdapter.differ.submitList(it.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBarCart.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            cartEmpty.visibility = View.INVISIBLE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            cartEmpty.visibility = View.VISIBLE
            cartItemList.visibility = View.INVISIBLE
            checkOutCard.visibility = View.INVISIBLE
            checkOutButton.visibility = View.INVISIBLE
        }
    }

    private fun setupCartView() {
        binding.cartItemList.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = cartAdapter
        }
    }
}
package com.example.farmlinkapp.fragments.mainmenufrags

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmlinkapp.R
import com.example.farmlinkapp.adapters.CheckOutProductsAdapter
import com.example.farmlinkapp.adapters.ShippingAddressAdapter
import com.example.farmlinkapp.data.CartProduct
import com.example.farmlinkapp.data.Notification
import com.example.farmlinkapp.data.ShippingAddress
import com.example.farmlinkapp.data.order.Order
import com.example.farmlinkapp.data.order.OrderStatus
import com.example.farmlinkapp.databinding.FragmentCheckOutBinding
import com.example.farmlinkapp.dialog.SelectShippingAddressDialog
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.CheckOutViewModel
import com.example.farmlinkapp.viewmodel.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CheckOutFragment : Fragment(), SelectShippingAddressDialog.OnAddressSelectedListener {
    private lateinit var binding: FragmentCheckOutBinding
    private val checkOutProductAdapter by lazy { CheckOutProductsAdapter() }
    private val args by navArgs<CheckOutFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f

    private var selectedAddress: ShippingAddress? = null
    private val orderViewModel by viewModels<OrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckOutBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCheckOutProducts()

        binding.checkOutFinishButton.setOnClickListener {
            if (selectedAddress == null){
                Toast.makeText(requireContext(), "Please select an address", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }

        binding.backCheckOutButton.setOnClickListener {
            findNavController().navigate(R.id.action_checkOutFragment_pop)
        }

        binding.deliveryAddressCard.setOnClickListener {
            val bottomSheetDialog = SelectShippingAddressDialog()
            bottomSheetDialog.onAddressSelectedListener = this
            bottomSheetDialog.show(parentFragmentManager, "SelectShippingAddressDialog")
        }

        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.checkOutFinishButton.startAnimation()
                    }
                    is Resource.Success ->{
                        binding.checkOutFinishButton.revertAnimation()
                        findNavController().navigate(R.id.action_checkOutFragment_pop)
                        Snackbar.make(requireView(), "Order placed!", Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error ->{
                        binding.checkOutFinishButton.revertAnimation()
                        Toast.makeText(requireContext(), it.message,Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        checkOutProductAdapter.differ.submitList(products)


        val decimalFormat = DecimalFormat("#.##")
        val finalPrice = decimalFormat.format(totalPrice)
        binding.totalPriceCheckOut.text = "Php ${finalPrice}"

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showOrderConfirmationDialog() {
        val alrtDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order Confirmation")
            setMessage("Are you ready to place order of your cart items?")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { dialog, _ ->
                val order = Order(
                    OrderStatus.Ordered.status,
                    totalPrice,
                    products,
                    selectedAddress!!
                )

                orderViewModel.placeOrder(order)

                dialog.dismiss()
            }
        }
        alrtDialog.create()
        alrtDialog.show()
    }

    private fun setupCheckOutProducts() {
        binding.checkOutProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = checkOutProductAdapter
        }
    }

    override fun onAddressSelected(address: ShippingAddress) {
        selectedAddress = address
        val cardTextFullName = binding.deliveryFullName
        cardTextFullName.text = address.fullNameShipping
        val cardTextPhoneNumber = binding.deliveryPhoneNumber
        cardTextPhoneNumber.text = "(${address.phone})"
        val cardTextAddressFull = binding.deliveryAddress
        cardTextAddressFull.text = "${address.street}, ${address.barangay} ${address.city},\n${address.province} ${address.postalCode}"
    }
}
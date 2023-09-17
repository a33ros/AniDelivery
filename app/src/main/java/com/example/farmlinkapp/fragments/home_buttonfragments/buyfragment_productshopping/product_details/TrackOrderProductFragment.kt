package com.example.farmlinkapp.fragments.home_buttonfragments.buyfragment_productshopping.product_details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmlinkapp.R
import com.example.farmlinkapp.adapters.CheckOutProductsAdapter
import com.example.farmlinkapp.data.order.OrderStatus
import com.example.farmlinkapp.databinding.FragmentTrackOrderProductBinding
import java.text.DecimalFormat

class TrackOrderProductFragment : Fragment() {
    private lateinit var binding: FragmentTrackOrderProductBinding
    private val checkOutAdapter by lazy { CheckOutProductsAdapter() }
    private val args by navArgs<TrackOrderProductFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackOrderProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val order = args.order

        setupProducts()

        binding.backProductDetailsButton.setOnClickListener {
            findNavController().navigate(R.id.action_trackOrderProductFragment_pop)
        }

        binding.apply {
            orderIDDetails.text = "#${order.orderId}"
            orderDateDetails.text = order.date

            orderStatusView.setSteps(
                mutableListOf(
                    OrderStatus.Ordered.status,
                    OrderStatus.Confirmed.status,
                    OrderStatus.Shipped.status,
                    OrderStatus.Delivered.status
                )
            )

            TOdeliveryFullName.text = order.address.fullNameShipping
            TOdeliveryPhoneNumber.text = "(${order.address.phone})"
            TOdeliveryAddress.text = "${order.address.street}, ${order.address.barangay} ${order.address.city},\n${order.address.province} ${order.address.postalCode}"

            val decimalFormat = DecimalFormat("#.##")
            val finalPrice = decimalFormat.format(order.totalPrice)
            totalPriceOrder.text = "Php ${finalPrice}"
        }

        checkOutAdapter.differ.submitList(order.products)

    }

    private fun setupProducts() {
        binding.trackOrderProducts.apply {
            adapter = checkOutAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }
}
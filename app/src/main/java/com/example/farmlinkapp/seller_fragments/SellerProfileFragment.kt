package com.example.farmlinkapp.seller_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.farmlinkapp.R
import com.example.farmlinkapp.adapters.OrderViewpagerAdapter
import com.example.farmlinkapp.data.SellerInformation
import com.example.farmlinkapp.databinding.FragmentSellerProfileBinding
import com.example.farmlinkapp.seller_fragments.product_status.CancelledFragment
import com.example.farmlinkapp.seller_fragments.product_status.CompletedFragment
import com.example.farmlinkapp.seller_fragments.product_status.OrderedFragment
import com.example.farmlinkapp.seller_fragments.product_status.ToBePickedUpFragment
import com.example.farmlinkapp.seller_fragments.product_status.ToDeliverFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class SellerProfileFragment : Fragment() {
    private lateinit var binding: FragmentSellerProfileBinding

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellerProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.analyticsAndReport.setOnClickListener {
            findNavController().navigate(R.id.action_sellerProfileFragment_to_analyticsAndReportFragment)
        }

        val ordersTabs = arrayListOf<Fragment>(
            OrderedFragment(),
            ToDeliverFragment(),
            ToBePickedUpFragment(),
            CancelledFragment(),
            CompletedFragment()
        )

        val viewPager2Adapter =
            OrderViewpagerAdapter(ordersTabs, childFragmentManager, lifecycle)
        binding.orderViewPager.adapter = viewPager2Adapter
        TabLayoutMediator(binding.ordersTab, binding.orderViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Ordered"
                1 -> tab.text = "To Deliver"
                2 -> tab.text = "To Be Picked Up"
                3 -> tab.text = "Cancelled"
                4 -> tab.text = "Completed"
            }
        }.attach()

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_sellerProfileFragment_pop)
        }

        binding.postProductButton.setOnClickListener {
            findNavController().navigate(R.id.action_sellerProfileFragment_to_addProductActivity)
        }

        lifecycleScope.launchWhenStarted {
            firestore.collection("user").document(auth.uid!!)
                .collection("sellerInformation")
                .document("sellerInfoDocument")
                .addSnapshotListener { value, error ->
                    if (error == null){
                        val sellerInfo = value?.toObject(SellerInformation::class.java)
                        binding.apply {
                            shopName.text = sellerInfo?.shopName
                            shopAddress.text = sellerInfo?.shopLocation
                        }
                    }
                }
        }
    }
}
package com.example.farmlinkapp.fragments.home_buttonfragments.buyfragment_productshopping.product_details

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.adapters.ViewPager2Images
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.FragmentProductDetailsBinding
import com.example.farmlinkapp.dialog.GetQuantityDialog
import com.example.farmlinkapp.dialog.NewMessageDialog
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.DetailsViewModel
import com.example.farmlinkapp.viewmodel.UserAccountViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormat

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductDetailsBinding
    private val ViewPager2Images by lazy { ViewPager2Images() }
    private val viewModel by viewModels<DetailsViewModel>()
    private val viewModelProfile by viewModels<UserAccountViewModel>()

    var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        val db = Firebase.firestore
        val usersRef = db.collection("user")
        val sellerId = product.seller.userId

        usersRef.whereEqualTo("userId", sellerId).get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val sellerUser = document.toObject(User::class.java)

                binding.apply {
                    sellerName.text = sellerUser?.fullName
                    Glide.with(requireView()).load(sellerUser?.imagePath).into(binding.sellerIcon)
                }
            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Error getting documents: ", exception)
        }


        setupViewPager()

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_productDetailsFragment_pop)
        }

        lifecycleScope.launchWhenStarted {
            viewModelProfile.user.collectLatest {
                when(it){
                    is Resource.Success ->{
                        currentUser = it.data
                    }
                    else -> Unit
                }

            }
        }

        binding.directCheckOutButton.setOnClickListener {
            val getPrice = GetQuantityDialog(product.price, product)
            getPrice.show(childFragmentManager, "GetQuantityDialog")
        }

        binding.messageButton.setOnClickListener {
            val newMessage = NewMessageDialog(product.seller)
            newMessage.show(childFragmentManager, "NewMessageDialog")
        }

        binding.apply {
            productPreviewName.text = product.name
            productPreviewLocation.text = product.productLocation

            val decimalFormat = DecimalFormat("#.##")
            val finalPrice = decimalFormat.format(product.price)

            productPreviewPrice.text = "₱ $finalPrice per kg"
            productPreviewDescription.text = product.description
        }

        ViewPager2Images.differ.submitList(product.images)


        /*lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.addToCartButton.startAnimation()
                    }
                    is Resource.Success ->{
                        binding.addToCartButton.revertAnimation()
                        Toast.makeText(requireContext(), "The product has been added to your cart!" ,Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error ->{
                        binding.addToCartButton.revertAnimation()
                        Toast.makeText(requireContext(),it.message ,Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }*/

        /*binding.addToCartButton.setOnClickListener {
            if (currentUser == product.seller){
                binding.addToCartButton.startAnimation()
                Toast.makeText(requireContext(), "You cannot buy your own product!" ,Toast.LENGTH_SHORT).show()
                binding.addToCartButton.revertAnimation()
            }else{
                viewModel.addUpdateProductInCart(CartProduct(product, 1))
            }
        }*/

    }

    private fun setupViewPager() {
        binding.apply {
            productPreviewImage.adapter = ViewPager2Images
        }
    }
}
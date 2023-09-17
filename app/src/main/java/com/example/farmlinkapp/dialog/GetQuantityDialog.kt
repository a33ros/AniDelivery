package com.example.farmlinkapp.dialog

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.Product
import com.example.farmlinkapp.databinding.GetPriceDialogBinding
import com.example.farmlinkapp.fragments.home_buttonfragments.buyfragment_productshopping.product_details.DirectCheckOutFragment
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat

class GetQuantityDialog(private val productPrice: Double, private val product: Product) : DialogFragment() {
    private lateinit var binding: GetPriceDialogBinding

    private var checkOutPrice: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GetPriceDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.totalView.text = "₱ 0.00"

        val decimalFormat = DecimalFormat("#.##")
        val finalPrice = decimalFormat.format(productPrice)
        binding.priceView.text = "₱ $finalPrice"

        binding.quantityTextBox.addTextChangedListener( object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                calculatePrice()
            }

            override fun afterTextChanged(p0: Editable?) {
                calculatePrice()
            }
        })

        binding.proceedButton.setOnClickListener {
            hideKeyboard()
            if (binding.quantityTextBox.text!!.isEmpty() || binding.quantityTextBox.text!!.isBlank()){
                Toast.makeText(requireContext(), "Provide a valid number of kilograms", Toast.LENGTH_SHORT).show()
            } else {
                val quantityText = binding.quantityTextBox.text.toString()
                val quantity = quantityText.toDouble()
                if (quantity == 0.00 || quantityText.isEmpty() || quantityText.isBlank()){
                    Toast.makeText(requireContext(), "Provide a valid number of kilograms", Toast.LENGTH_SHORT).show()
                } else {
                    val valuePrice = binding.quantityTextBox.text.toString()
                    val valuePass = valuePrice.toDouble()

                    val bundle = Bundle()
                    bundle.putDouble("FinalPrice", checkOutPrice!!)
                    bundle.putDouble("FinalQuantity", valuePass)
                    bundle.putParcelable("FinalProduct", product)

                    val auth = FirebaseAuth.getInstance()

                    if (product.seller.userId == auth.uid){
                        Toast.makeText(requireContext(), "You can't buy your own product!", Toast.LENGTH_SHORT).show()
                    }else{
                        dismiss()
                        findNavController().navigate(R.id.action_productDetailsFragment_to_checkOutDirectActivity, bundle)
                    }
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

    }

    private fun calculatePrice() {
        val initialPrice = productPrice
        val quantityText = binding.quantityTextBox.text.toString()
        if (quantityText.isEmpty()) {
            return
        }
        val quantity = quantityText.toDouble()

        val buyPrice = initialPrice * quantity

        checkOutPrice = buyPrice

        val decimalFormat = DecimalFormat("#.##")
        val finalPrice1 = decimalFormat.format(buyPrice)
        binding.totalView.text = "₱ $finalPrice1"

    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
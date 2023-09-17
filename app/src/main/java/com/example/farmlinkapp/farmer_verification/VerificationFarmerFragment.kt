package com.example.farmlinkapp.farmer_verification

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.farmlinkapp.FarmLinkApp
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.CardInformation
import com.example.farmlinkapp.data.PaymentClientDetails
import com.example.farmlinkapp.data.SellerInformation
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.FragmentVerificationFarmerBinding
import com.example.farmlinkapp.dialog.ProductLocationDialog
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.ProfileViewModel
import com.example.farmlinkapp.viewmodel.SellerVerificationViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.protobuf.Empty
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

@AndroidEntryPoint
class VerificationFarmerFragment : Fragment() {
    private lateinit var binding: FragmentVerificationFarmerBinding
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    private val profileViewModel by viewModels<ProfileViewModel>()
    private val viewModel by viewModels<SellerVerificationViewModel>()
    val storage = FirebaseStorage.getInstance().reference

    var currentUser: User? = null
    private var imageUri: Uri? = null
    private var imageUrl: String? = null

    val TAG = "VerificationFarmerFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            imageUri = it.data?.data
            binding.farmCertUpload.text = imageUri?.lastPathSegment
        }

        lifecycleScope.launch {
            viewModel.setSellerInfo.collectLatest {
                when(it){
                    is Resource.Loading ->{
                    }
                    is Resource.Success ->{
                        binding.verifyFinish.revertAnimation()
                        findNavController().navigate(R.id.action_verificationFarmerFragment_to_profileFragment)
                        Toast.makeText(requireContext(), "You are now a verified seller!", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error ->{
                        binding.verifyFinish.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerificationFarmerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentLocationTextBox = binding.locationTextBox
        currentLocationTextBox.inputType = InputType.TYPE_NULL
        currentLocationTextBox.isClickable = false
        currentLocationTextBox.isFocusable = false

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_verificationFarmerFragment_pop)
        }

        binding.farmCertUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageActivityResultLauncher.launch(intent)
        }

        val locationText = binding.locationTextBox

        binding.markLocation.setOnClickListener {
            val bottomSheetDialog = ProductLocationDialog(locationText)
            bottomSheetDialog.show(childFragmentManager, "ProductLocationDialog")
        }

        binding.expiryDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 2) {
                    binding.expiryDate.setText("$s/")
                    binding.expiryDate.setSelection(binding.expiryDate.text.toString().length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.clearExpiry.setOnClickListener {
            binding.expiryDate.text?.clear()
        }

        binding.verifyFinish.setOnClickListener {
            val verifyValidation = validateInformation()

            if (!verifyValidation){
                Toast.makeText(requireContext(), "Review your Inputs", Toast.LENGTH_SHORT).show()
            } else {

                binding.verifyFinish.startAnimation()
                uploadFarmCertSaveInformation()
            }
        }
        
    }

    private fun uploadFarmCertSaveInformation(){
        val auth = Firebase.auth
        val imageUri = imageUri
        if (imageUri != null) {
            lifecycleScope.launch {
                try {
                    val imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
                    val imageByteArray = byteArrayOutputStream.toByteArray()
                    val imageDirectory = storage.child("farmCertificates/${auth.uid}/${UUID.randomUUID()}")
                    val result = imageDirectory.putBytes(imageByteArray).await()
                    val url = result.storage.downloadUrl.await().toString()

                    val shopName = binding.shopNameTextBox.text.toString().trim()
                    val shopLocation = binding.locationTextBox.text.toString().trim()
                    val gCashNumber = binding.paymentNumber.text.toString().trim()
                    val holderName = binding.cardHolderName.text.toString().trim()
                    val cardNumber = binding.cardNumber.text.toString().trim()
                    val cvv = binding.cvvInput.text.toString().trim()
                    val cardType = "VISA"

                    val cardInfo = CardInformation(
                        holderName,
                        cardNumber,
                        cvv,
                        cardType
                    )
                    val paymentClientDetails = PaymentClientDetails(
                        gCashNumber,
                        cardInfo
                    )

                    val sellerInfo = SellerInformation(
                        shopName,
                        url,
                        shopLocation,
                        paymentClientDetails
                    )

                    viewModel.setSellerInformation(sellerInfo)

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
        }

    }

    private fun validateInformation(): Boolean {
        if (binding.shopNameTextBox.text.toString().trim().isEmpty())
            return false
        if (imageUri == null)
            return false
        if (binding.paymentNumber.text.toString().trim().isEmpty() || binding.cardHolderName.text.toString().trim().isEmpty() && binding.cardNumber.text.toString().trim().isEmpty() && binding.expiryDate.text.toString().trim().isEmpty() && binding.cvvInput.text.toString().trim().isEmpty())
            return false
        return true
    }

}
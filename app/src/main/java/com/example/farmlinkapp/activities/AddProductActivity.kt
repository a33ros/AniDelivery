package com.example.farmlinkapp.activities


import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.Product
import com.example.farmlinkapp.data.SellerInformation
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.ActivityAddProductBinding
import com.example.farmlinkapp.dialog.ProductLocationDialog
import com.example.farmlinkapp.dialog.SelectShippingAddressDialog
import com.example.farmlinkapp.helper.TableRow
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.ProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

@AndroidEntryPoint
class AddProductActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAddProductBinding.inflate(layoutInflater) }

    val viewModel by viewModels<ProfileViewModel>()

    private var selectedImages = mutableListOf<Uri>()
    private val productsStorage = Firebase.storage.reference
    private val firestore = Firebase.firestore

    lateinit var itemLocationTextBox: EditText
    var selectedCategory: String? = null

    var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val productTypeList = resources.getStringArray(R.array.product_category)
        val productArray = ArrayAdapter(this, R.layout.dropdown_item, productTypeList)
        binding.productCategory.setAdapter(productArray)

        binding.productCategory.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedCategory = (binding.productDropdown.editText as? AutoCompleteTextView)?.text.toString()
                if (selectedCategory == "Crops") {
                    val productNameList1 = resources.getStringArray(R.array.crop_category)
                    val productArrayCrops = ArrayAdapter(this@AddProductActivity, R.layout.dropdown_item, productNameList1)
                    binding.productNameSelection.text.clear()
                    binding.productNameSelection.setAdapter(productArrayCrops)
                } else if (selectedCategory == "Vegetables") {
                    val productNameList2 = resources.getStringArray(R.array.vegetable_category)
                    val productArrayVegetable = ArrayAdapter(this@AddProductActivity, R.layout.dropdown_item, productNameList2)
                    binding.productNameSelection.text.clear()
                    binding.productNameSelection.setAdapter(productArrayVegetable)
                }
            }

        }

        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when(it){
                    is Resource.Success ->{
                        currentUser = it.data
                    }
                    else -> Unit
                }

            }
        }

        binding.postProductButton.setOnClickListener {
            val productValidation = validateInformation()

            if (!productValidation) {
                Toast.makeText(this, "Review your Inputs", Toast.LENGTH_SHORT).show()
            }

            saveProduct()
        }

        val selectedImagesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
                if (result.resultCode == RESULT_OK) {
                    val intent = result.data

                    //Multiple images selected aha true true yes yes fr
                    //hirap na hirap na ako please uwu

                    if (intent?.clipData != null){
                        val count = intent.clipData?.itemCount ?: 0
                        (0 until count ).forEach{
                            val imageUri= intent.clipData?.getItemAt(it)?.uri
                            imageUri?.let {
                                selectedImages.add(it)
                            }
                        }
                    }else{
                        val imageUri = intent?.data
                        imageUri?.let {
                            selectedImages.add(it)
                        }
                        updateImages()
                    }
                }
        }

        binding.addProductImageButton.setOnClickListener {
            val intent = Intent(ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            selectedImagesActivityResult.launch(intent)
        }
    }

    //Outside onCreate function

    private fun updateImages() {
        binding.selectedImagesNumber.text = selectedImages.size.toString()
    }

    private fun saveProduct() {
        val categoryProduct = binding.productCategory.text.toString().trim()
        val nameProduct = binding.productNameSelection.text.toString().trim()
        val descriptionProduct = binding.itemDescriptionTextBox.text.toString().trim()
        val quantityProduct = binding.itemQuantityTextBox.text.toString()
        val priceProduct = binding.itemAddPriceTextBox.text.toString()
        val imagesByteArrays = getImagesByteArrays()
        val images = mutableListOf<String>()

        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                showLoading()
            }
            try {
                async {
                    imagesByteArrays.forEach {
                        val id = UUID.randomUUID().toString()
                        launch {
                            val imageStorage = productsStorage.child("products/images/$id")
                            val result = imageStorage.putBytes(it).await()
                            val downloadUrl = result.storage.downloadUrl.await().toString()
                            images.add(downloadUrl)
                        }
                    }
                }.await()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showLoading()
                }
            }

            lifecycleScope.launch {
                val userSeller = Firebase.auth
                firestore.collection("user")
                    .document(userSeller.uid!!)
                    .collection("sellerInformation")
                    .document("sellerInfoDocument")
                    .addSnapshotListener { value, error ->
                        if (error == null) {
                            val sellerInfo = value?.toObject(SellerInformation::class.java)!!

                            val product = currentUser?.let {
                                Product(
                                    UUID.randomUUID().toString(),
                                    nameProduct,
                                    it,
                                    categoryProduct,
                                    sellerInfo.shopLocation,
                                    priceProduct.toDouble(),
                                    if (descriptionProduct.isEmpty()) null else descriptionProduct,
                                    quantityProduct.toDouble(),
                                    images
                                )
                            }
                            if (product != null) {
                                firestore.collection("Products").add(product).addOnSuccessListener {
                                    hideLoading()
                                    finish()
                                }.addOnFailureListener {
                                    hideLoading()
                                    Log.e("Error", it.message.toString())
                                }

                            }
                        }
                    }

                val ref = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val addProductToDb = ref.getReference("dataReports/${categoryProduct}/${nameProduct}")

                val productUpdate = TableRow(
                    nameProduct,
                    quantityProduct.toDouble()
                )

                addProductToDb.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val previousQuantity = snapshot.child("quantity").getValue(Double::class.java)
                            val newQuantity = previousQuantity?.plus(quantityProduct.toDouble())
                            addProductToDb.child("quantity").setValue(newQuantity)
                        } else {
                            addProductToDb.setValue(productUpdate)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Error", error.message)
                    }
                })

            }
        }
    }

    private fun hideLoading() {
        binding.postProductButton.revertAnimation()
    }

    private fun showLoading() {
        binding.postProductButton.startAnimation()
    }

    private fun getImagesByteArrays(): List<ByteArray> {
        val imagesByteArray = mutableListOf<ByteArray>()
        selectedImages.forEach{
            val stream = ByteArrayOutputStream()
            val imageBmp = MediaStore.Images.Media.getBitmap(contentResolver,it)
            if (imageBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)){
                imagesByteArray.add(stream.toByteArray())
            }
        }
        return imagesByteArray
    }

    private fun validateInformation(): Boolean {
        if (binding.itemAddPriceTextBox.text.toString().trim().isEmpty())
            return false
        if (binding.productNameSelection.text.toString().trim().isEmpty())
            return false
        if (binding.itemDescriptionTextBox.text.toString().trim().isEmpty())
            return false
        if (binding.itemQuantityTextBox.text.toString().trim().isEmpty())
            return false
        if (selectedImages.isEmpty())
            return false

        return true
    }
}
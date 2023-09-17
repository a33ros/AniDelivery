package com.example.farmlinkapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmlinkapp.data.ShippingAddress
import com.example.farmlinkapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddShippingAddressViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _addShippingAddress = MutableStateFlow<Resource<ShippingAddress>>(Resource.Unspecified())
    val addShippingAddress = _addShippingAddress.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun addShippingAddress(shippingAddress: ShippingAddress){
        val validateInputs = validateInputs(shippingAddress)

        if (validateInputs) {
            viewModelScope.launch { _addShippingAddress.emit(Resource.Loading()) }
            firestore.collection("user").document(auth.uid!!).collection("shippingAddress")
                .document()
                .set(shippingAddress).addOnSuccessListener {
                    viewModelScope.launch { _addShippingAddress.emit(Resource.Success(shippingAddress)) }
                }.addOnFailureListener {
                    viewModelScope.launch { _addShippingAddress.emit(Resource.Error(it.message.toString())) }
                }
        }else{
            viewModelScope.launch {
                _error.emit("All fields are required!")
            }
        }
    }

    private fun validateInputs(shippingAddress: ShippingAddress): Boolean {
        return shippingAddress.addressType.trim().isNotEmpty() &&
                shippingAddress.fullNameShipping.trim().isNotEmpty() &&
                shippingAddress.phone.trim().isNotEmpty() &&
                shippingAddress.street.trim().isNotEmpty() &&
                shippingAddress.barangay.trim().isNotEmpty() &&
                shippingAddress.city.trim().isNotEmpty() &&
                shippingAddress.province.trim().isNotEmpty() &&
                shippingAddress.postalCode.trim().isNotEmpty()
    }

}
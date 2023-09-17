package com.example.farmlinkapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmlinkapp.data.SellerInformation
import com.example.farmlinkapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SellerVerificationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _setSellerInfo = MutableStateFlow<Resource<SellerInformation>>(Resource.Unspecified())
    val setSellerInfo = _setSellerInfo.asStateFlow()

    fun setSellerInformation(sellerInformation: SellerInformation){
        viewModelScope.launch { _setSellerInfo.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("sellerInformation")
            .document("sellerInfoDocument")
            .set(sellerInformation).addOnSuccessListener {
                viewModelScope.launch { _setSellerInfo.emit(Resource.Success(sellerInformation)) }
            }.addOnFailureListener {
                viewModelScope.launch { _setSellerInfo.emit(Resource.Error(it.message.toString())) }
            }
    }

}
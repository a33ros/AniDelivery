package com.example.farmlinkapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.util.Constants.USER_COLLECTION
import com.example.farmlinkapp.util.RegisterFieldState
import com.example.farmlinkapp.util.RegisterValidation
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.util.validateEmail
import com.example.farmlinkapp.util.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking

@HiltViewModel
class RegisterVM @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val database: FirebaseDatabase
): ViewModel() {

    private val _register = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> = _register

    private val _validation = Channel<RegisterFieldState> ()
    val validation = _validation.receiveAsFlow()

    fun createAccountWithEmailAndPassword(user: User,password: String, rePass: String){
       if (checkValidation(user, password, rePass)) {
           runBlocking {
               _register.emit(Resource.Loading())
           }
           firebaseAuth.createUserWithEmailAndPassword(user.email,password)
               .addOnSuccessListener {
                   it.user?.let {
                       saveUserInfo(it.uid, user)
                   }
               } .addOnFailureListener {
                   _register.value = Resource.Error(it.message.toString())
               }
       } else {
           val registerFieldState = RegisterFieldState(
               validateEmail(user.email), validatePassword(password, rePass)
           )
           runBlocking{
               _validation.send(registerFieldState)
           }
       }
    }

    private fun saveUserInfo(userUid: String, user: User) {
        db.collection(USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            }.addOnFailureListener{
                _register.value = Resource.Error(it.message.toString())
            }
    }

    private fun checkValidation(user: User, password: String, rePass: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password, rePass)
        val shouldRegister = emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success

        return shouldRegister
    }

}
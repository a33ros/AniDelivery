package com.example.farmlinkapp.util

import android.util.Patterns

fun validateEmail(email: String): RegisterValidation{

    if(email.isEmpty())
        return RegisterValidation.Failed("Email cannot be empty")

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Wrong email format")

    return RegisterValidation.Success
}

        fun validatePassword(password: String, rePass: String): RegisterValidation{
            if(password.isEmpty())
                return RegisterValidation.Failed("Password cannot be empty")

            if(password.length < 6)
                return RegisterValidation.Failed("Password length must be 6 characters long")

            if(password != rePass)
                return RegisterValidation.Failed("Passwords don't match!")

            return RegisterValidation.Success
        }
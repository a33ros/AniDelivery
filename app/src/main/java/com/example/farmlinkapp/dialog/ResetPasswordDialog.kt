package com.example.farmlinkapp.dialog

import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.example.farmlinkapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog

fun Fragment.setupBottomSheetDialog(
    onSendClick: (String) -> Unit
){
    val dialog = BottomSheetDialog(requireContext())
    val view = layoutInflater.inflate(R.layout.reset_password_dialog, null)
    dialog.setContentView(view)
    dialog.show()

    val edEmail = view.findViewById<AppCompatEditText>(R.id.resetInput)
    val sendEmail = view.findViewById<AppCompatButton>(R.id.resetConfirm)
    val cancelReset = view.findViewById<AppCompatButton>(R.id.resetCancel)

    sendEmail.setOnClickListener{
        val email = edEmail.text.toString().trim()
        onSendClick(email)
        dialog.dismiss()
    }

    cancelReset.setOnClickListener{
        dialog.dismiss()
    }
}
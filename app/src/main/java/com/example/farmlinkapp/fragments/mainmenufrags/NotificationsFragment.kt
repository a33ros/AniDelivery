package com.example.farmlinkapp.fragments.mainmenufrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.farmlinkapp.R
import com.example.farmlinkapp.databinding.FragmentNotificationsBinding
import com.example.farmlinkapp.helper.TableRow
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {
    private lateinit var binding: FragmentNotificationsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_notificationsFragment_pop)
        }

        binding.upload.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val quantity0 = binding.quantity.text.toString()
            if (quantity0.isEmpty()){
                return@setOnClickListener
            }

            val quantity = quantity0.toDouble()
            val crop = TableRow(
                name,
                quantity
            )

            lifecycleScope.launch {
                val ref = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val uploadCrop = ref.getReference("dataReports/crops/${name}")
                uploadCrop.setValue(crop)
            }

        }

        binding.uploadVeg.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val quantity0 = binding.quantity.text.toString()
            if (quantity0.isEmpty()){
                return@setOnClickListener
            }

            val quantity = quantity0.toDouble()
            val vegetable = TableRow(
                name,
                quantity
            )

            lifecycleScope.launch {
                val ref = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val uploadVegetable = ref.getReference("dataReports/vegetables/${name}")
                uploadVegetable.setValue(vegetable)
            }
        }

    }
}
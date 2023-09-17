package com.example.farmlinkapp.seller_fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.farmlinkapp.R
import com.example.farmlinkapp.databinding.FragmentAnalyticsAndReportBinding
import com.github.mikephil.charting.data.Entry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class AnalyticsAndReportFragment : Fragment() {
    private lateinit var binding: FragmentAnalyticsAndReportBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAnalyticsAndReportBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_analyticsAndReportFragment_pop)
        }

        val lineChart = binding.salesChart

        val currentMonth = Month.values()[java.time.LocalDateTime.now().monthValue - 1]
        val monthName = currentMonth.getDisplayName(TextStyle.FULL, Locale.getDefault())

        val currentYear = LocalDateTime.now().year

        binding.chartTitle.text = "Sales Chart (${monthName}, ${currentYear})"

        val datalist = mutableListOf<Entry>()
        val database = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("sellerAnalytics/sales/${currentYear}/$monthName")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {
                //
            }

        })
    }
}
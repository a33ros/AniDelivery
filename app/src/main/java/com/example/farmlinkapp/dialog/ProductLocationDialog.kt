package com.example.farmlinkapp.dialog

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.farmlinkapp.R
import com.example.farmlinkapp.activities.AddProductActivity
import com.example.farmlinkapp.databinding.ProductLocationLayoutBinding
import com.example.farmlinkapp.farmer_verification.VerificationFarmerFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class ProductLocationDialog(private val locationText: TextInputEditText) : BottomSheetDialogFragment(), OnMapReadyCallback {
    private lateinit var binding: ProductLocationLayoutBinding
    private lateinit var mGoogleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProductLocationLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.isDraggable = false

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        var marker: Marker? = null

        mGoogleMap.setOnMapClickListener {latLng ->
            if (marker == null) {
                marker = mGoogleMap.addMarker(MarkerOptions().position(latLng))
            } else {
                marker?.position = latLng
            }
            val location = LatLng(marker?.position!!.latitude, marker?.position!!.longitude)

            val geocoder = context?.let { Geocoder(it) }
            val addresses = geocoder?.getFromLocation(location.latitude, location.longitude, 1)
            val locationName = addresses?.get(0)?.getAddressLine(0)

            Toast.makeText(context, "Location: $locationName", Toast.LENGTH_SHORT).show()

            binding.selectLocationButton.setOnClickListener {
                locationText.setText(locationName)
                dismiss()
            }
        }

        val pampanga = LatLng(15.0794, 120.6207)
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pampanga, 10f))
    }

}
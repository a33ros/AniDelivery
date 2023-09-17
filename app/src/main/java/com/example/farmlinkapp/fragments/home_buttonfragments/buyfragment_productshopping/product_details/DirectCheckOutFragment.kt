package com.example.farmlinkapp.fragments.home_buttonfragments.buyfragment_productshopping.product_details

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.Notification
import com.example.farmlinkapp.data.Product
import com.example.farmlinkapp.data.ShippingAddress
import com.example.farmlinkapp.data.order.DirectOrder
import com.example.farmlinkapp.databinding.FragmentDirectCheckOutBinding
import com.example.farmlinkapp.dialog.SelectShippingAddressDialog
import com.example.farmlinkapp.util.PaymentsUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.button.ButtonConstants.ButtonTheme
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.button.PayButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class DirectCheckOutFragment : Fragment(), SelectShippingAddressDialog.OnAddressSelectedListener {
    private lateinit var binding: FragmentDirectCheckOutBinding

    private lateinit var googlePayButton: PayButton

    private var product: Product? = null
    private var checkOutPrice: Double? = null
    private var checkOutQuantity: Double? = null

    private val SHIPPING_COST_CENTS = 9 * PaymentsUtil.CENTS.toLong()

    private lateinit var paymentsClient: PaymentsClient

    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDirectCheckOutBinding.inflate(inflater)
        return binding.root
    }

    private var selectedAddress: ShippingAddress? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product = arguments?.getParcelable("FinalProduct") ?: throw IllegalArgumentException("Product not found in arguments bundle")
        checkOutPrice = arguments?.getDouble("FinalPrice") ?: throw IllegalArgumentException("Price not found in arguments bundle")
        checkOutQuantity = arguments?.getDouble("FinalQuantity") ?: throw IllegalArgumentException("Quantity not found in arguments bundle")

        paymentsClient = PaymentsUtil.createPaymentsClient(requireContext() as Activity)
        possiblyShowGooglePayButton()

        val decimalFormat1 = DecimalFormat("#.##")
        val finalPrice1 = decimalFormat1.format(checkOutQuantity)
        binding.calculationItemKgView.text = "$finalPrice1"

        val finalPrice2 = decimalFormat1.format(checkOutPrice)
        binding.totalPriceCheckOut.text = "₱ $finalPrice2"

        binding.checkOutProductName.text = product?.name
        Glide.with(requireContext())
            .load(product!!.images[0])
            .error(R.mipmap.profile_mini_round)
            .into(binding.checkOutProductImage)

        val decimalFormat = DecimalFormat("#.##")
        val finalPrice = decimalFormat.format(product?.price)
        binding.checkOutProductPrice.text = "₱ $finalPrice"

        binding.backCheckOutButton.setOnClickListener {
            findNavController().navigate(R.id.action_directCheckOutFragment_pop)
        }

        binding.deliveryAddressCard.setOnClickListener {
            val bottomSheetDialog = SelectShippingAddressDialog()
            bottomSheetDialog.onAddressSelectedListener = this
            bottomSheetDialog.show(parentFragmentManager, "SelectShippingAddressDialog")
        }

        binding.cashOnDelivery.setOnCheckedChangeListener { _, isChecked ->
            binding.cashless.isEnabled = !isChecked
            binding.gPayButton.visibility = View.INVISIBLE
            binding.payCheckOutButton.visibility = View.VISIBLE
        }

        binding.cashless.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.cashOnDelivery.isEnabled = false
                binding.gPayButton.visibility = View.VISIBLE
                binding.payCheckOutButton.visibility = View.INVISIBLE
            } else {
                binding.cashOnDelivery.isEnabled = true
                binding.gPayButton.visibility = View.INVISIBLE
                binding.payCheckOutButton.visibility = View.VISIBLE
            }
        }

        binding.pickUp.setOnCheckedChangeListener { _, isChecked ->
            binding.standardShipping.isEnabled = !isChecked
        }

        binding.standardShipping.setOnCheckedChangeListener { _, isChecked ->
            binding.pickUp.isEnabled = !isChecked
        }

        googlePayButton = binding.gPayButton

        googlePayButton.initialize(ButtonOptions.newBuilder()
            .setAllowedPaymentMethods(PaymentsUtil.allowedPaymentMethods.toString())
            .setButtonTheme(ButtonTheme.LIGHT)
            .setButtonType(ButtonType.CHECKOUT)
            .build())

        googlePayButton.setOnClickListener {requestPayment(this)}

        binding.payCheckOutButton.setOnClickListener {
            if (!binding.cashless.isChecked && !binding.cashOnDelivery.isChecked) {
                Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show()
            } else {
                if (!binding.pickUp.isChecked && !binding.standardShipping.isChecked) {
                    Toast.makeText(requireContext(), "Please select a delivery option", Toast.LENGTH_SHORT).show()
                } else if (binding.pickUp.isChecked) {
                    val notificationName = "Your product has been ordered"
                    val notificationType = "Order Bought: Cash-on-Delivery"
                    val alertDescription = "Confirm order of your product on seller profile with the id of : ${product?.id}"
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    val date = LocalDate.now().toString()
                    val timestamp0 = currentDateTime.format(formatter)
                    val timeStamp = "${date}, $timestamp0"

                    val sellerNotification = Notification(
                        notificationName,
                        notificationType,
                        alertDescription,
                        timeStamp
                    )

                    val firestore = Firebase.firestore
                    val auth = FirebaseAuth.getInstance()

                    val orderStatus = "Ordered"
                    val orderType = "Cash-on-Delivery"
                    val deliveryType = "Pick Up"
                    val totalPrice = checkOutPrice!!
                    val buyQuantity = checkOutQuantity!!
                    val product1 = product!!
                    val shippingAddress = selectedAddress!!
                    val currentDateTime1 = LocalDateTime.now()
                    val formatter1 = DateTimeFormatter.ofPattern("hh:mm a")
                    val date1 = LocalDate.now().toString()
                    val timestamp2 = currentDateTime1.format(formatter1)
                    val timeStamp1 = "${date1}, $timestamp2"

                    val order = DirectOrder(
                        orderStatus,
                        orderType,
                        deliveryType,
                        totalPrice,
                        buyQuantity,
                        product1,
                        shippingAddress,
                        timeStamp1
                    )

                    lifecycleScope.launch {
                        firestore.collection("user").document(auth.uid!!).collection("orders")
                            .document()
                            .set(order)

                        firestore.collection("user").document("${product!!.seller.userId}")
                            .collection("sellerOrders")
                            .document()
                            .set(order)

                        firestore.collection("user").document("${product!!.seller.userId}")
                            .collection("notifications")
                            .document()
                            .set(sellerNotification)
                    }

                } else if (binding.pickUp.isChecked) {
                    val notificationName = "Your product has been ordered"
                    val notificationType = "Order Bought: Cash-on-Delivery"
                    val alertDescription = "Confirm order of your product on seller profile with the id of : ${product?.id}"
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    val date = LocalDate.now().toString()
                    val timestamp0 = currentDateTime.format(formatter)
                    val timeStamp = "${date}, $timestamp0"

                    val sellerNotification = Notification(
                        notificationName,
                        notificationType,
                        alertDescription,
                        timeStamp
                    )

                    val firestore = Firebase.firestore
                    val auth = FirebaseAuth.getInstance()
                    val orderStatus = "Ordered"
                    val orderType = "Cash-on-Delivery"
                    val deliveryType = "Standard Courier"
                    val totalPrice = checkOutPrice!!
                    val buyQuantity = checkOutQuantity!!
                    val product1 = product!!
                    val shippingAddress = selectedAddress!!
                    val currentDateTime1 = LocalDateTime.now()
                    val formatter1 = DateTimeFormatter.ofPattern("hh:mm a")
                    val date1 = LocalDate.now().toString()
                    val timestamp2 = currentDateTime1.format(formatter1)
                    val timeStamp1 = "${date1}, $timestamp2"

                    val order = DirectOrder(
                        orderStatus,
                        orderType,
                        deliveryType,
                        totalPrice,
                        buyQuantity,
                        product1,
                        shippingAddress,
                        timeStamp1
                    )

                    lifecycleScope.launch {
                        firestore.collection("user").document(auth.uid!!).collection("orders")
                            .document()
                            .set(order)

                        firestore.collection("user").document("${product!!.seller.userId}")
                            .collection("sellerOrders")
                            .document()
                            .set(order)

                        firestore.collection("user").document("${product!!.seller.userId}")
                            .collection("notifications")
                            .document()
                            .set(sellerNotification)
                    }
                }
            }
        }

    }

    private fun possiblyShowGooglePayButton() {
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            Log.d("payment", "Google Pay is available on this device");
        } else {
            Toast.makeText(
                requireContext(),
                "Unfortunately, Google Pay is not available on this device",
                Toast.LENGTH_LONG).show();
        }
    }

    private fun requestPayment(fragment: Fragment) {
        // Disables the button to prevent multiple clicks.
        googlePayButton.isClickable = false
        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val cropPrice = checkOutPrice!!
        val priceCents = (cropPrice * PaymentsUtil.CENTS.toLong()).roundToInt() + SHIPPING_COST_CENTS

        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents.toDouble())
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        if (request != null) {
            AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(request), fragment.requireParentFragment() as Activity, LOAD_PAYMENT_DATA_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        val paymentData = PaymentData.getFromIntent(data!!)
                        handlePaymentSuccess(paymentData!!)
                    }
                    RESULT_CANCELED -> {
                        Toast.makeText(requireContext(), "Transaction Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        val status = AutoResolveHelper.getStatusFromIntent(data!!)
                        val statusCode = status!!.statusCode
                        handleError(statusCode)
                        }
                    }
                }
            }
            googlePayButton.isEnabled = true
        }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson()
        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)
            Toast.makeText(requireContext(), getString(R.string.payments_show_name, billingName), Toast.LENGTH_SHORT).show()
            // Logging token string.
            Log.d("GooglePaymentToken", paymentMethodData
                .getJSONObject("tokenizationData")
                .getString("token"))

        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $e")
        }

    }

    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }


    override fun onAddressSelected(address: ShippingAddress) {
        selectedAddress = address
        val cardTextFullName = binding.deliveryFullName
        cardTextFullName.text = address.fullNameShipping
        val cardTextPhoneNumber = binding.deliveryPhoneNumber
        cardTextPhoneNumber.text = "(${address.phone})"
        val cardTextAddressFull = binding.deliveryAddress
        cardTextAddressFull.text = "${address.street}, ${address.barangay} ${address.city},\n${address.province} ${address.postalCode}"
    }

}
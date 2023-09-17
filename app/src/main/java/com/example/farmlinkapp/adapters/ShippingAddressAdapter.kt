package com.example.farmlinkapp.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.ShippingAddress
import com.example.farmlinkapp.databinding.ShippingAddressSelectionLayoutBinding

class ShippingAddressAdapter : Adapter<ShippingAddressAdapter.ShippingAddressViewHolder>() {

    inner class ShippingAddressViewHolder(val binding: ShippingAddressSelectionLayoutBinding) :
        ViewHolder(binding.root) {
            fun bind(shippingAddress: ShippingAddress, isSelected: Boolean) {
                binding.apply {
                    deliveryShippingAddressType.text = shippingAddress.addressType
                    deliveryShippingFullName.text = shippingAddress.fullNameShipping
                    deliveryShippingPhoneNumber.text = "(${shippingAddress.phone})"
                    deliveryShippingStreet.text = "${shippingAddress.street}, "
                    deliveryShippingBarangay.text = "${shippingAddress.barangay} "
                    deliveryShippingCity.text = "${shippingAddress.city}, "
                    deliveryShippingProvince.text = shippingAddress.province
                    deliveryShippingPostalCode.text = shippingAddress.postalCode

                    if (isSelected){
                        backgroundSelection.background = ColorDrawable(itemView.context.resources.getColor(R.color.white))
                    }else{
                        backgroundSelection.background = ColorDrawable(itemView.context.resources.getColor(R.color.lighter_gray))
                    }
                }
            }

        }

    private val diffUtil = object : DiffUtil.ItemCallback<ShippingAddress>() {
        override fun areItemsTheSame(oldItem: ShippingAddress, newItem: ShippingAddress): Boolean {
            return oldItem.addressType == newItem.addressType && oldItem.fullNameShipping == newItem.fullNameShipping && oldItem.phone == newItem.phone &&
                    oldItem.street == newItem.street && oldItem.barangay == newItem.barangay && oldItem.city == newItem.city && oldItem.province == newItem.province &&
                    oldItem.postalCode == newItem.postalCode
        }

        override fun areContentsTheSame(
            oldItem: ShippingAddress,
            newItem: ShippingAddress
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShippingAddressViewHolder {
        return ShippingAddressViewHolder(
            ShippingAddressSelectionLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    var selectedShippingAddress = -1

    override fun onBindViewHolder(holder: ShippingAddressViewHolder, position: Int) {
        val shippingAddress = differ.currentList[position]
        holder.bind(shippingAddress, selectedShippingAddress == position)

        holder.binding.selectionCardShippingAddress.setOnClickListener {
            if (selectedShippingAddress >= 0)
                notifyItemChanged(selectedShippingAddress)
            selectedShippingAddress = holder.adapterPosition
            notifyItemChanged(selectedShippingAddress)
            onClick?.invoke(shippingAddress)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((ShippingAddress) -> Unit)? = null
}
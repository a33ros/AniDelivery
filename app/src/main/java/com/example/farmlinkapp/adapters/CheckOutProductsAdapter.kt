package com.example.farmlinkapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.farmlinkapp.data.CartProduct
import com.example.farmlinkapp.databinding.CheckOutCartItemBinding

class CheckOutProductsAdapter: Adapter<CheckOutProductsAdapter.CheckOutProductsViewHolder>() {

    inner class CheckOutProductsViewHolder(val binding: CheckOutCartItemBinding): ViewHolder(binding.root) {

        fun bind(checkOutProduct: CartProduct){
            binding.apply {
                Glide.with(itemView).load(checkOutProduct.product.images[0]).into(checkOutProductImage)
                checkOutProductName.text = checkOutProduct.product.name
                checkOutProductPrice.text = "Php ${checkOutProduct.product.price}"
                checkOutItemQuantity.text = checkOutProduct.quantity.toString()
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product == newItem.product
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckOutProductsViewHolder {
        return CheckOutProductsViewHolder(
            CheckOutCartItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: CheckOutProductsViewHolder, position: Int) {
        val checkOutProduct = differ.currentList[position]

        holder.bind(checkOutProduct)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
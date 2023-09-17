package com.example.farmlinkapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.farmlinkapp.data.CartProduct
import com.example.farmlinkapp.data.Product
import com.example.farmlinkapp.databinding.CartItemLayoutBinding
import com.example.farmlinkapp.databinding.ProductLayoutBinding
import com.example.farmlinkapp.viewmodel.ProductsListViewModel
import org.checkerframework.checker.units.qual.C
import java.text.DecimalFormat

class ProductsAdapter() : RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>() {

    inner class ProductsViewHolder(val binding: ProductLayoutBinding) :
        RecyclerView.ViewHolder(binding.root){

        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(productImageLayout)
                productNameLayout.text = product.name
                productPlaceLayout.text = product.productLocation
                val decimalFormat = DecimalFormat("#.##")
                val finalPrice = decimalFormat.format(product.price)
                productPriceLayout.text = "₱ $finalPrice per kg"
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        return ProductsViewHolder(
            ProductLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener{
            onClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null
}
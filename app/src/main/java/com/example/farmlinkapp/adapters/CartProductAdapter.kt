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
import org.checkerframework.checker.units.qual.C

class CartProductAdapter : RecyclerView.Adapter<CartProductAdapter.CartProductsViewHolder>() {

    inner class CartProductsViewHolder(val binding: CartItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root){

        fun bind(cartProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(cartProduct.product.images[0]).into(cartProductImage)
                cartProductName.text = cartProduct.product.name
                cartProductPrice.text = "Php ${cartProduct.product.price}"
                itemQuantity.text = cartProduct.quantity.toString()
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductsViewHolder {
        return CartProductsViewHolder(
            CartItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CartProductsViewHolder, position: Int) {
        val cartProduct = differ.currentList[position]
        holder.bind(cartProduct)

        holder.itemView.setOnClickListener{
            onProductClick?.invoke(cartProduct)
        }

        holder.binding.addQuantityButton.setOnClickListener{
            onPlusClick?.invoke(cartProduct)
        }

        holder.binding.minusQuantityButton.setOnClickListener{
            onMinusClick?.invoke(cartProduct)
        }

        holder.binding.deleteCart.setOnClickListener {
            onDeleteClick?.invoke(cartProduct)
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((CartProduct) -> Unit)? = null
    var onDeleteClick: ((CartProduct) -> Unit)? = null
    var onPlusClick: ((CartProduct) -> Unit)? = null
    var onMinusClick: ((CartProduct) -> Unit)? = null
}
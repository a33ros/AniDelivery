package com.example.farmlinkapp.fragments.home_buttonfragments

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.farmlinkapp.R
import com.example.farmlinkapp.data.ForumPost
import com.example.farmlinkapp.data.User
import com.example.farmlinkapp.databinding.ForumPostLayoutBinding
import com.example.farmlinkapp.databinding.FragmentBuyBinding
import com.example.farmlinkapp.databinding.ProductStatBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


class BuyFragment: Fragment(R.layout.fragment_buy) {
    private lateinit var binding: FragmentBuyBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_buyFragment_pop)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBuyBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cropStat.adapter = adapterCrops
        binding.cropStat.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.vegetableStat.adapter = adapterVegetable
        binding.vegetableStat.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.backBuyButton.setOnClickListener {
            findNavController().navigate(R.id.action_buyFragment_pop)
        }

        binding.cropsFragment.setOnClickListener {
            val list = "Crops"
            val b = Bundle().apply {
                putString("Products", list)
            }
            findNavController().navigate(R.id.action_buyFragment_to_productListFragment,b)
        }

        binding.vegetablesFragment.setOnClickListener {
            val list = "Vegetables"
            val b = Bundle().apply {
                putString("Products", list)
            }
            findNavController().navigate(R.id.action_buyFragment_to_productListFragment,b)
        }

        val ref = FirebaseDatabase.getInstance("https://farmlink-app-b781b-default-rtdb.asia-southeast1.firebasedatabase.app/")

        val cropRef = ref.getReference("dataReports/Crops")
        cropRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val products = snapshot.getValue(com.example.farmlinkapp.helper.TableRow::class.java) ?: return
                latestProductMap[snapshot.key!!] = products
                refreshRecyclerViewPost()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val products = snapshot.getValue(com.example.farmlinkapp.helper.TableRow::class.java) ?: return
                latestProductMap[snapshot.key!!] = products
                refreshRecyclerViewPost()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //nothing
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //nothing
            }

            override fun onCancelled(error: DatabaseError) {
                //nothing
            }

        })

        val vegetableRef = ref.getReference("dataReports/Vegetables")
        vegetableRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val products = snapshot.getValue(com.example.farmlinkapp.helper.TableRow::class.java) ?: return
                latestProductMapVegetable[snapshot.key!!] = products
                refreshRecyclerViewPostVegetable()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val products = snapshot.getValue(com.example.farmlinkapp.helper.TableRow::class.java) ?: return
                latestProductMapVegetable[snapshot.key!!] = products
                refreshRecyclerViewPostVegetable()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //nothing
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //nothing
            }

            override fun onCancelled(error: DatabaseError) {
                //nothing
            }

        })
    }
    private val latestProductMap = HashMap<String, com.example.farmlinkapp.helper.TableRow>()
    private val latestProductMapVegetable = HashMap<String, com.example.farmlinkapp.helper.TableRow>()

    fun refreshRecyclerViewPost(){
        adapterCrops.clear()
        latestProductMap.values.forEach {
            adapterCrops.add(BuyFragment.GetProductStats(it))
        }
    }

    fun refreshRecyclerViewPostVegetable(){
        adapterVegetable.clear()
        latestProductMapVegetable.values.forEach {
            adapterVegetable.add(BuyFragment.GetProductStatsVegetable(it))
        }
    }

    class GetProductStats(val product: com.example.farmlinkapp.helper.TableRow): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val binding = ProductStatBinding.bind(viewHolder.itemView)

            val supplyKg = product.quantity.toString()

            binding.productName.text = product.name
            binding.supplyNumber.text = "${supplyKg}kg"

            val supply = product.quantity

            if (supply == 0.0 || supply == null){
                binding.status.text = "No data available"
            } else {
                if (supply < 400.0) {
                    binding.status.text = "Under-supply"
                    binding.statusImg.setImageResource(R.drawable.undersupply)
                } else if (supply > 400.0 && supply < 700.0) {
                    binding.status.text = "Neutral"
                    binding.statusImg.setImageResource(R.drawable.neutral)
                } else if (supply > 700.0) {
                    binding.status.text = "Oversupply"
                    binding.statusImg.setImageResource(R.drawable.oversupply)
                }
            }
        }
        override fun getLayout(): Int {
            return R.layout.product_stat
        }
    }

    class GetProductStatsVegetable(val product: com.example.farmlinkapp.helper.TableRow): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val binding = ProductStatBinding.bind(viewHolder.itemView)

            val supplyKg = product.quantity.toString()

            binding.productName.text = product.name
            binding.supplyNumber.text = "${supplyKg}kg"

            val supply = product.quantity

            if (supply == 0.0 || supply == null){
                binding.status.text = "No data available"
            } else {
                if (supply < 400.0) {
                    binding.status.text = "Under-supply"
                    binding.statusImg.setImageResource(R.drawable.undersupply)
                } else if (supply > 400.0 && supply < 700.0) {
                    binding.status.text = "Neutral"
                    binding.statusImg.setImageResource(R.drawable.neutral)
                } else if (supply > 700.0) {
                    binding.status.text = "Oversupply"
                    binding.statusImg.setImageResource(R.drawable.oversupply)
                }
            }
        }
        override fun getLayout(): Int {
            return R.layout.product_stat
        }
    }

    private val adapterCrops = GroupAdapter<GroupieViewHolder>()
    private val adapterVegetable = GroupAdapter<GroupieViewHolder>()

}
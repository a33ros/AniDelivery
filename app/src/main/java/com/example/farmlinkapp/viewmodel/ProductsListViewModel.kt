package com.example.farmlinkapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmlinkapp.data.Product
import com.example.farmlinkapp.util.Resource
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
): ViewModel() {

    private val _cropsList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val cropsList: StateFlow<Resource<List<Product>>> = _cropsList

    private val _vegetableList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val vegetableList: StateFlow<Resource<List<Product>>> = _vegetableList

    private val _fruitsList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val fruitsList: StateFlow<Resource<List<Product>>> = _fruitsList

    private val _spicesList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val spicesList: StateFlow<Resource<List<Product>>> = _spicesList

    private val _seedsList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val seedsList: StateFlow<Resource<List<Product>>> = _seedsList

    private val _fertilizersList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val fertilizersList: StateFlow<Resource<List<Product>>> = _fertilizersList

    private val _herbalsList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val herbalsList: StateFlow<Resource<List<Product>>> = _herbalsList

    private val _dryFoodsList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val dryFoodsList: StateFlow<Resource<List<Product>>> = _dryFoodsList

    private val _plantsList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val plantsList: StateFlow<Resource<List<Product>>> = _plantsList

    private val _grassList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val grassList: StateFlow<Resource<List<Product>>> = _grassList

    private val _othersList = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val othersList: StateFlow<Resource<List<Product>>> = _othersList

    init {
        fetchCrops()
        fetchVegetables()
        fetchFruits()
        fetchPlants()
        fetchFertilizers()
        fetchDryFoods()
        fetchGrass()
        fetchHerbals()
        fetchOthers()
        fetchSeeds()
        fetchSpices()
    }

    fun fetchCrops() {
        viewModelScope.launch {
            _cropsList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Crops").get().addOnSuccessListener { result ->
            val crops = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _cropsList.emit(Resource.Success(crops))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _cropsList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchVegetables() {
        viewModelScope.launch {
            _vegetableList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Vegetables").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _vegetableList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _vegetableList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchFruits() {
        viewModelScope.launch {
            _fruitsList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Fruits").get().addOnSuccessListener { result ->
            val fruits = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _fruitsList.emit(Resource.Success(fruits))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _fruitsList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchHerbals() {
        viewModelScope.launch {
            _herbalsList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Herbals").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _herbalsList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _herbalsList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchSpices() {
        viewModelScope.launch {
            _spicesList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Spices").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _spicesList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _spicesList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchDryFoods() {
        viewModelScope.launch {
            _dryFoodsList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Dry Foods").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _dryFoodsList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _dryFoodsList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchSeeds() {
        viewModelScope.launch {
            _seedsList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Seeds").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _seedsList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _seedsList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchFertilizers() {
        viewModelScope.launch {
            _fertilizersList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Fertilizers").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _fertilizersList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _fertilizersList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchPlants() {
        viewModelScope.launch {
            _plantsList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Plants").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _plantsList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _plantsList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchGrass() {
        viewModelScope.launch {
            _grassList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Grass").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _grassList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _grassList.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchOthers() {
        viewModelScope.launch {
            _othersList.emit(Resource.Loading())
        }
        firestore.collection("Products").whereEqualTo("productType", "Others").get().addOnSuccessListener { result ->
            val vegetables = result.toObjects(Product::class.java)
            viewModelScope.launch {
                _othersList.emit(Resource.Success(vegetables))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _othersList.emit(Resource.Error(it.message.toString()))
            }
        }
    }
}
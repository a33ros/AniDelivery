package com.example.farmlinkapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.farmlinkapp.R
import com.example.farmlinkapp.databinding.ActivityMainMenuBinding
import com.example.farmlinkapp.util.Resource
import com.example.farmlinkapp.viewmodel.CartViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainMenu : AppCompatActivity() {

    val binding by lazy {
        ActivityMainMenuBinding.inflate(layoutInflater)
    }

    val viewModel by viewModels<CartViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        verifyIfUserIsLoggedIn()

        val navController = findNavController(R.id.mainMenuNavigationFragment)
        binding.bottomNavigationMenuMain.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment || destination.id == R.id.messageFragment ||
                    destination.id == R.id.profileFragment) {
                binding.bottomNavigationMenuMain.visibility = View.VISIBLE
            } else {
                binding.bottomNavigationMenuMain.visibility = View.GONE
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.cartProducts.collectLatest {
                when(it){
                    is Resource.Success -> {
                        val count = it.data?.size?: 0
                        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationMenuMain)
                        bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                            number = count
                            backgroundColor = resources.getColor(R.color.base_yellow)
                        }
                    }
                    else -> Unit
                }
            }
        }

    }

    private fun verifyIfUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, LoginRegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or (Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
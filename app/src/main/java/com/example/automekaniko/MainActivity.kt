package com.example.automekaniko

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.automekaniko.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0) // We handle internal padding manually
            
            // Apply bottom padding to the bottomBar specifically for system nav
            binding.bottomBar.setPadding(
                binding.bottomBar.paddingLeft,
                binding.bottomBar.paddingTop,
                binding.bottomBar.paddingRight,
                systemBars.bottom
            )
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Sync bottom bar (manual handling for now since we use custom TextViews)
        binding.hometxt.setOnClickListener {
            navController.popBackStack(R.id.homeFragment, false)
        }
        
        binding.settingtxt.setOnClickListener {
            // Future settings fragment
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.guidesFragment, R.id.obdFragment -> {
                    binding.bottomBar.visibility = View.VISIBLE
                    updateBottomBarHighlight(destination.id)
                }
                else -> {
                    binding.bottomBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateBottomBarHighlight(destinationId: Int) {
        val activeColor = 0xFFe02020.toInt()
        val inactiveColor = 0xFF555555.toInt()
        
        binding.hometxt.setTextColor(if (destinationId == R.id.homeFragment) activeColor else inactiveColor)
        // Add more highlights as needed
    }
}

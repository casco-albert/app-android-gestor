package com.example.myapplication

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 Toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        // 🔥 NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController

        // 🔥 Configuración global (Drawer + Top level)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_inicio,
                R.id.btnCliente,
                R.id.btnPedido,
                R.id.btnCobro
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.appBarMain.toolbar.navigationIcon?.setTint(getColor(android.R.color.black))

        // 🔥 Drawer
        binding.navView?.setupWithNavController(navController)

        // 🔥 Bottom Navigation
        binding.appBarMain.contentMain.bottomNavView?.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val navView: NavigationView? = findViewById(R.id.nav_view)

        if (navView == null) {
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        return when (item.itemId) {
            R.id.btnNuevaCarga -> {
                navController.navigate(R.id.btnNuevaCarga)
                true
            }
            R.id.btnConfig -> {
                navController.navigate(R.id.btnConfig)
                true
            }
            R.id.btnNuevoPedido -> {
                navController.navigate(R.id.btnNuevoPedido)
                true
            }
            R.id.btnCobroCliente -> {
                navController.navigate(R.id.btnCobroCliente)
                true
            }
            R.id.btnHistorialCobro -> {
                navController.navigate(R.id.btnHistorialCobro)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
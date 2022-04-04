package com.example.radaapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.radaapplication.databinding.ActivityHomeBinding
import com.example.radaapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

private const val TAG = "MainActivity"
const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var progress : ProgressBar
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var txt : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        
       
                binding = ActivityHomeBinding.inflate(layoutInflater)
                setContentView(binding.root)


        setSupportActionBar(binding.appBarHome.toolbar)

        // variable got the new method of startActivityForResult function
        val getAction = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            // get new map data from the data
        }

        binding.appBarHome.fab.setOnClickListener { view ->
            Snackbar.make(view, "Adding a new marker", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            val intent = Intent(this@HomeActivity, CreateMapsActivity::class.java)
            intent.putExtra("EXTRA_MAP_TITLE", " Add New Threat Point")
            getAction.launch(intent)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_map, R.id.nav_setting), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener { menuItem ->
//            logoutDialog()
//            true
//        }
        val headerView : View = navView.getHeaderView(0)
        txt = headerView.findViewById(R.id.currentUser)



        val userID = auth.currentUser?.uid.toString()
        val userRef = db.collection("User")
        val query = userRef.whereEqualTo("userID" , userID)

        query.get().addOnSuccessListener { snapshot ->
            snapshot?.forEach {

                val user = it.toObject(User::class.java)
                val currentUserName : String = user.name
                txt.text = currentUserName

            }
        }
            .addOnFailureListener { exception->
                Log.w(TAG ,"Error getting current user name $exception")
            }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_sginout -> {
           logoutDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    private fun logoutDialog() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(applicationContext, MainActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay)
        finish()
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    }
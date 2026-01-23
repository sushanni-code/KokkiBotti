package com.example.kokkibotti

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

// Pääaktiviteetti, joka toimii sovelluksen sisäänkäyntinä
class MainActivity : AppCompatActivity() {

    // Määrittää ActionBarin ja navigaation asetukset
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        // Asetetaan käyttäjän valitsema teema ennen käyttöliittymän luontia
        ThemeManager.applyTheme(this)

        // Kutsutaan AppCompatActivityn onCreate-metodia
        super.onCreate(savedInstanceState)

        // Asetetaan aktiviteetin layout
        setContentView(R.layout.activity_main)

        // Haetaan toolbar layoutista ja asetetaan se ActionBariksi
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Haetaan DrawerLayout (sivupaneeli)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        // Haetaan alareunan navigaatiopalkki
        val bottomNavView: BottomNavigationView = findViewById(R.id.nav_view)

        // Haetaan navigaatiovalikko drawerista
        val drawerNavView: NavigationView = findViewById(R.id.drawer_nav_view)

        // Haetaan NavHostFragment, joka sisältää navigointifragmentit
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // Haetaan NavController, joka hallitsee navigaatiota
        val navController: NavController = navHostFragment.navController

        // Määritellään pääkohteet (top-level destinations),
        // joissa ei näytetä "takaisin"-nuolta vaan drawer-ikoni
        val topLevelDestinations = setOf(
            R.id.navigation_recipes,
            R.id.navigation_planner,
            R.id.navigation_inspiration
        )

        // Luodaan AppBarConfiguration drawerin ja pääkohteiden kanssa
        appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)

        // Yhdistetään ActionBar NavControlleriin ja DrawerLayoutiin
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Yhdistetään alareunan navigaatio NavControlleriin
        bottomNavView.setupWithNavController(navController)

        // Yhdistetään drawer-navigaatio NavControlleriin
        drawerNavView.setupWithNavController(navController)
    }

    // Mahdollistaa yläpalkin takaisin-nuolen (<-)
    // ja hamburger-valikon (☰) oikean toiminnan
    override fun onSupportNavigateUp(): Boolean {
        // Haetaan NavController uudelleen
        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .navController

        // Hoidetaan navigointi tai fallback oletuskäytökseen
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}

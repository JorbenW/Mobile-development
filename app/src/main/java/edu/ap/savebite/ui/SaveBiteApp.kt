package edu.ap.savebite.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.ap.savebite.R
import edu.ap.savebite.data.Role
import edu.ap.savebite.ui.register.RegisterScreen
import edu.ap.savebite.ui.theme.Cream
import edu.ap.savebite.ui.theme.Ink
import edu.ap.savebite.ui.theme.Mustard

enum class SaveBiteScreen(@StringRes val title: Int, val icon: ImageVector? = null) {
    Login(R.string.screen_login),
    Register(R.string.screen_register),
    Discover(R.string.screen_discover, Icons.Filled.Search),
    Map(R.string.screen_map, Icons.Filled.Place),
    Orders(R.string.screen_orders, Icons.Filled.ShoppingCart),
    Favorites(R.string.screen_favorites, Icons.Filled.Favorite),
    MyBags(R.string.screen_my_bags, Icons.AutoMirrored.Filled.List),
    MyStore(R.string.screen_my_store, Icons.Filled.Home)
}

private val customerTabs = listOf(
    SaveBiteScreen.Discover, SaveBiteScreen.Map, SaveBiteScreen.Orders, SaveBiteScreen.Favorites
)
private val merchantTabs = listOf(SaveBiteScreen.MyBags, SaveBiteScreen.MyStore)

@Composable
fun SaveBiteApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = SaveBiteScreen.valueOf(
        backStackEntry?.destination?.route ?: SaveBiteScreen.Login.name
    )
    val tabs = when (current) {
        in customerTabs -> customerTabs
        in merchantTabs -> merchantTabs
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            if (tabs.isNotEmpty()) {
                SaveBiteTopBar(
                    title = stringResource(current.title),
                    onLogout = {
                        // Clear the whole back stack so Back cannot return to the app
                        navController.navigate(SaveBiteScreen.Login.name) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (tabs.isNotEmpty()) {
                SaveBiteBottomBar(
                    tabs = tabs,
                    current = current,
                    onTabClick = { tab ->
                        navController.navigate(tab.name) {
                            popUpTo(tabs.first().name)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SaveBiteScreen.Login.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(SaveBiteScreen.Login.name) {
                // shortcut: demo buttons until real login in step 6
                PlaceholderScreen(SaveBiteScreen.Login) {
                    Button(onClick = { navController.goToHome(SaveBiteScreen.Discover) }) {
                        Text(stringResource(R.string.demo_customer))
                    }
                    Button(onClick = { navController.goToHome(SaveBiteScreen.MyBags) }) {
                        Text(stringResource(R.string.demo_merchant))
                    }
                    Button(onClick = { navController.navigate(SaveBiteScreen.Register.name) }) {
                        Text(stringResource(R.string.screen_register))
                    }
                }
            }
            composable(SaveBiteScreen.Register.name) {
                RegisterScreen(
                    onBack = { navController.navigateUp() },
                    onRegistered = { role ->
                        navController.goToHome(
                            if (role == Role.CUSTOMER) SaveBiteScreen.Discover else SaveBiteScreen.MyStore
                        )
                    }
                )
            }
            (SaveBiteScreen.entries - SaveBiteScreen.Login - SaveBiteScreen.Register).forEach { screen ->
                composable(screen.name) { PlaceholderScreen(screen) }
            }
        }
    }
}

/** Opens the start screen of a role and removes Login from the back stack. */
private fun NavHostController.goToHome(home: SaveBiteScreen) {
    navigate(home.name) {
        popUpTo(SaveBiteScreen.Login.name) { inclusive = true }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveBiteTopBar(title: String, onLogout: () -> Unit) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        actions = {
            OutlinedIconButton(
                onClick = onLogout,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(2.dp, Ink),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.logout)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
    )
}

@Composable
private fun SaveBiteBottomBar(
    tabs: List<SaveBiteScreen>,
    current: SaveBiteScreen,
    onTabClick: (SaveBiteScreen) -> Unit
) {
    Box(Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
        NavigationBar(
            containerColor = Ink,
            windowInsets = WindowInsets(0),
            modifier = Modifier.clip(RoundedCornerShape(20.dp))
        ) {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = tab == current,
                    onClick = { onTabClick(tab) },
                    icon = { Icon(tab.icon!!, contentDescription = null, Modifier.size(22.dp)) },
                    label = { Text(stringResource(tab.title)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Ink,
                        selectedTextColor = Mustard,
                        indicatorColor = Mustard,
                        unselectedIconColor = Cream,
                        unselectedTextColor = Cream
                    )
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    screen: SaveBiteScreen,
    content: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(screen.title), style = MaterialTheme.typography.headlineMedium)
        content()
    }
}

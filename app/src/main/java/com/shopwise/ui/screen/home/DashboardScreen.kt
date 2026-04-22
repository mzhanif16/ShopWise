package com.shopwise.ui.screen.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shopwise.core.UserPreferences
import com.shopwise.ui.screen.onboarding.GemmaViewModel
import com.shopwise.ui.theme.PrimaryColor
import compose.icons.EvaIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.fill.Book
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val gemmaViewModel: GemmaViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()

    // Init model saat pertama kali dashboard dibuka
    LaunchedEffect(Unit) {
        dashboardViewModel.initSelectedModel()
    }

    // Tampilkan Toast saat model siap
    LaunchedEffect(dashboardViewModel.isModelReady) {
        if (dashboardViewModel.isModelReady) {
            Toast.makeText(context, "Brain Intelligence is Active!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(gemmaViewModel, userPreferences, dashboardViewModel)
        },
        bottomBar = {
            DashboardBottomBar(
                selectedIndex = pagerState.currentPage,
                onItemSelected = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0 -> HomeScreen(navController)
                    1 -> ProfileScreen()
                }
            }

            // Overlay Keterangan Inisialisasi Model
//            InitializationOverlay(dashboardViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    gemmaViewModel: GemmaViewModel, 
    userPreferences: UserPreferences,
    dashboardViewModel: DashboardViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    val userData = remember { userPreferences.getUserData() }
    val selectedModelId = userData["selectedModel"] as? String ?: "4B"
    
    val downloadedModels = gemmaViewModel.models.values.filter { it.isDownloaded }

    CenterAlignedTopAppBar(
        navigationIcon = {
            Box {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = EvaIcons.Fill.Book,
                        contentDescription = "Switch Model",
                        tint = PrimaryColor
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    if (downloadedModels.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No models downloaded", color = Color.Gray, fontSize = 14.sp) },
                            onClick = { showMenu = false },
                            enabled = false
                        )
                    } else {
                        Text(
                            "Select Active Model",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        downloadedModels.forEach { model ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            model.name,
                                            color = if (model.id == selectedModelId) PrimaryColor else Color.Black,
                                            fontWeight = if (model.id == selectedModelId) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (model.id == selectedModelId) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(PrimaryColor)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    userPreferences.saveUserData(
                                        fullName = userData["fullName"] as? String ?: "",
                                        birthDate = userData["birthDate"] as? String ?: "",
                                        gender = userData["gender"] as? String ?: "",
                                        height = userData["height"] as? String ?: "",
                                        weight = userData["weight"] as? String ?: "",
                                        allergies = userData["allergies"] as? Set<String> ?: emptySet(),
                                        selectedModel = model.id
                                    )
                                    showMenu = false
                                    // Re-init model saat ganti pilihan di dropdown
                                    dashboardViewModel.initSelectedModel()
                                }
                            )
                        }
                    }
                }
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ShopWise",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    fontSize = 18.sp
                )
                if (dashboardViewModel.isModelReady) {
                    Text(
                        text = "Brain Active",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Profile */ }) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun DashboardBottomBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.height(100.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 13.dp)
        ) {
            NavigationBarItem(
                selected = selectedIndex == 0,
                onClick = { onItemSelected(0) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    selectedTextColor = PrimaryColor,
                    indicatorColor = Color(0xFFE0F2F1),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            NavigationBarItem(
                selected = selectedIndex == 1,
                onClick = { onItemSelected(1) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("Profile") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    selectedTextColor = PrimaryColor,
                    indicatorColor = Color(0xFFE0F2F1),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

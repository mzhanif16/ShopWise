package com.shopwise.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shopwise.ui.screen.onboarding.GemmaViewModel
import com.shopwise.ui.theme.PrimaryColor
import compose.icons.EvaIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.fill.Options2
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val gemmaViewModel: GemmaViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()

    // Sync status dan init model saat masuk atau ganti tab
    LaunchedEffect(pagerState.currentPage) {
        gemmaViewModel.checkModelsStatus()
        if (pagerState.currentPage == 0) {
            dashboardViewModel.initSelectedModel()
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(gemmaViewModel, dashboardViewModel)
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
                    0 -> HomeScreen(navController, dashboardViewModel)
                    1 -> ModelSetupScreen(dashboardViewModel)
                    2 -> ProfileScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    gemmaViewModel: GemmaViewModel, 
    dashboardViewModel: DashboardViewModel
) {
    val selectedModelId = dashboardViewModel.selectedModel
    val modelState = gemmaViewModel.models[selectedModelId]
    val isReady = dashboardViewModel.isModelReady

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ShopWise",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    fontSize = 18.sp
                )
                if (isReady) {
                    Text(
                        text = "Brain Active",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                } else if (dashboardViewModel.isModelInitializing) {
                    Text(
                        text = "Initializing...",
                        fontSize = 10.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }else{
                    Text(
                        text = "Failed to load model",
                        fontSize = 10.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        actions = {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = modelState?.name ?: "Gemma $selectedModelId",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    fontSize = 14.sp
                )
                if (modelState?.isDownloaded == false) {
                    Text(
                        text = "Not Downloaded",
                        fontSize = 9.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                } else if (!isReady && !dashboardViewModel.isModelInitializing) {
                    Text(
                        text = "Ready to load",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
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
                    indicatorColor = PrimaryColor.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            NavigationBarItem(
                selected = selectedIndex == 1,
                onClick = { onItemSelected(1) },
                icon = { Icon(EvaIcons.Fill.Options2, contentDescription = "Intelligence", modifier = Modifier.size(24.dp)) },
                label = { Text("Model", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryColor,
                    selectedTextColor = PrimaryColor,
                    indicatorColor = PrimaryColor.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            NavigationBarItem(
                selected = selectedIndex == 2,
                onClick = { onItemSelected(2) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
                label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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

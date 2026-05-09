package com.shopwise.ui.screen.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shopwise.R
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
    var showBackendMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    fontSize = 18.sp
                )
                if (isReady) {
                    Text(
                        text = stringResource(R.string.brain_active),
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                } else if (dashboardViewModel.isModelInitializing) {
                    Text(
                        text = stringResource(R.string.initializing),
                        fontSize = 10.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.failed_load_model),
                        fontSize = 10.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        actions = {
            Box {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable { showBackendMenu = true }
                ) {
                    Text(
                        text = modelState?.name ?: "Gemma $selectedModelId",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Backend: ${dashboardViewModel.currentBackend}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    if (modelState?.isDownloaded == false) {
                        Text(
                            text = stringResource(R.string.not_downloaded),
                            fontSize = 9.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (isReady && !dashboardViewModel.isModelInitializing) {
                        Text(
                            text = stringResource(R.string.ready_to_load),
                            fontSize = 9.sp,
                            color = PrimaryColor
                        )
                    }else if (!isReady && !dashboardViewModel.isModelInitializing) {
                        Text(
                            text = stringResource(R.string.choose_backend),
                            fontSize = 9.sp,
                            color = Color.Red
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showBackendMenu,
                    onDismissRequest = { showBackendMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Use CPU") },
                        onClick = {
                            showBackendMenu = false
                            dashboardViewModel.updateBackend("CPU")
                        },
                        trailingIcon = {
                            if (dashboardViewModel.currentBackend == "CPU") {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Use GPU (Faster)") },
                        onClick = {
                            showBackendMenu = false
                            dashboardViewModel.updateBackend("GPU")
                        },
                        trailingIcon = {
                            if (dashboardViewModel.currentBackend == "GPU") {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                            }
                        }
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
                label = { Text(stringResource(R.string.nav_home)) },
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
                label = { Text(stringResource(R.string.nav_model), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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
                label = { Text(stringResource(R.string.nav_profile), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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

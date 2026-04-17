package com.shopwise.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shopwise.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            DashboardTopBar()
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "ShopWise",
                fontWeight = FontWeight.Bold,
                color = PrimaryColor,
                fontSize = 20.sp
            )
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
        modifier = Modifier.height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 20.dp)
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

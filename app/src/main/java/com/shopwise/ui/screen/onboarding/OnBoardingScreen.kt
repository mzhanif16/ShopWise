package com.shopwise.ui.screen.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shopwise.core.UserPreferences
import com.shopwise.ui.navigation.Routes
import com.shopwise.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@Composable
fun OnBoardingScreen(navController: NavController) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // BackHandler untuk navigasi balik
    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    // Hoisting State Page 1
    var fullName by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("172") }
    var weight by rememberSaveable { mutableStateOf("64") }
    var selectedGender by rememberSaveable { mutableStateOf("Female") }

    // Hoisting State Page 2
    var allergySearch by rememberSaveable { mutableStateOf("") }
    var selectedAllergies by remember { mutableStateOf(setOf("Peanuts", "Dairy")) }

    // Hoisting State Page 3
    var selectedModel by rememberSaveable { mutableStateOf("4B") }

    val scrollState1 = rememberScrollState()
    val scrollState2 = rememberScrollState()
    val scrollState3 = rememberScrollState()

    val currentScrollState = when (pagerState.currentPage) {
        0 -> scrollState1
        1 -> scrollState2
        else -> scrollState3
    }

    val showBottomSection by remember {
        derivedStateOf {
            currentScrollState.value >= (currentScrollState.maxValue - 2).coerceAtLeast(0)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(48.dp)) {
                    if (pagerState.currentPage > 0) {
                        IconButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                StepIndicator(currentStep = pagerState.currentPage, totalSteps = 3)
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        containerColor = Color.White,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> OnBoardingPage1(
                        scrollState = scrollState1,
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        birthDate = birthDate,
                        onBirthDateChange = { birthDate = it },
                        height = height,
                        onHeightChange = { height = it },
                        weight = weight,
                        onWeightChange = { weight = it },
                        selectedGender = selectedGender,
                        onGenderChange = { selectedGender = it }
                    )
                    1 -> OnBoardingPage2(
                        scrollState = scrollState2,
                        searchQuery = allergySearch,
                        onSearchChange = { allergySearch = it },
                        selectedAllergies = selectedAllergies,
                        onAllergyToggle = { allergy ->
                            selectedAllergies = if (selectedAllergies.contains(allergy)) {
                                selectedAllergies - allergy
                            } else {
                                selectedAllergies + allergy
                            }
                        }
                    )
                    2 -> OnBoardingPage3(
                        scrollState = scrollState3,
                        selectedModel = selectedModel,
                        onModelSelected = { selectedModel = it }
                    )
                }
            }

            AnimatedVisibility(
                visible = showBottomSection,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp, top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                userPreferences.saveUserData(
                                    fullName = fullName,
                                    birthDate = birthDate,
                                    gender = selectedGender,
                                    height = height,
                                    weight = weight,
                                    allergies = selectedAllergies,
                                    selectedModel = selectedModel
                                )
                                userPreferences.setOnboarded(true)
                                /**
                                 * Navigate to dashboard
                                 */
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.ONBOARDING) {
                                        inclusive = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val buttonText = if (pagerState.currentPage < 2) "Continue" else "Get Started"
                            Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "STEP ${pagerState.currentPage + 1} OF 3",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index == currentStep) PrimaryColor else Color(0xFFE0E0E0))
            )
        }
    }
}

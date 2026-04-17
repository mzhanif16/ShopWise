package com.shopwise.ui.screen.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopwise.common.utils.CustomTextField
import com.shopwise.common.utils.SectionLabel
import com.shopwise.ui.theme.PrimaryColor


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnBoardingPage2(
    scrollState: ScrollState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedAllergies: Set<String>,
    onAllergyToggle: (String) -> Unit
) {
    val commonAllergens = listOf(
        "Peanuts" to Icons.Default.Info,
        "Eggs" to Icons.Default.Info,
        "Dairy" to Icons.Default.Info,
        "Gluten" to Icons.Default.Info,
        "Shellfish" to Icons.Default.Info,
        "Soy" to Icons.Default.Info,
        "Tree Nuts" to Icons.Default.Info
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "PERSONALIZATION",
            color = Color(0xFF3277D8),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Text(
            text = "Allergy Profile",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Configure your clinical safeguards. We'll cross-reference these during every scan.",
            color = Color.Gray,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        SectionLabel("SEARCH OR ADD CUSTOM")

        // Custom search dengan penambahan alergi kustom
        Column {
            CustomTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = "Search allergens (e.g. Sesame)",
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                }
            )

            // Suggestion Box untuk Add Custom
            AnimatedVisibility(
                visible = searchQuery.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable {
                            onAllergyToggle(searchQuery)
                            onSearchChange("")
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Add \"")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(searchQuery)
                                }
                                append("\" to profile")
                            },
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Common Clinical Triggers",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gabungkan common allergens dan custom yang dipilih
        val customSelected = selectedAllergies.filter { name -> commonAllergens.none { it.first == name } }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commonAllergens.forEach { (name, icon) ->
                val isSelected = selectedAllergies.contains(name)
                AllergenChip(
                    name = name,
                    icon = icon,
                    isSelected = isSelected,
                    onClick = { onAllergyToggle(name) }
                )
            }

            customSelected.forEach { name ->
                AllergenChip(
                    name = name,
                    icon = Icons.Default.Info,
                    isSelected = true,
                    onClick = { onAllergyToggle(name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        InsightBox()

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun AllergenChip(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFB3261E) else Color.White,
        shadowElevation = if (isSelected) 0.dp else 2.dp,
        modifier = Modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun InsightBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF90CAF9)) // Light blue color
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF01579B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "GEMMA INSIGHT",
                    color = Color(0xFF01579B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Based on your profile, I'll prioritize identifying \"Cross-Contamination\" warnings in snack aisle products.",
                    color = Color(0xFF01579B),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
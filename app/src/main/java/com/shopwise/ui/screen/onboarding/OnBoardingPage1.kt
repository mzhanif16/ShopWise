package com.shopwise.ui.screen.onboarding

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopwise.common.utils.CustomTextField
import com.shopwise.common.utils.SectionLabel
import com.shopwise.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingPage1(
    scrollState: ScrollState,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    selectedGender: String,
    onGenderChange: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                        onBirthDateChange(sdf.format(Date(millis)))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = PrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showHeightDialog) {
        SelectionDialog(
            title = "Select Height",
            unit = "cm",
            currentValue = height.toIntOrNull() ?: 170,
            range = 100..250,
            onDismiss = { showHeightDialog = false },
            onValueSelected = {
                onHeightChange(it.toString())
                showHeightDialog = false
            }
        )
    }

    if (showWeightDialog) {
        SelectionDialog(
            title = "Select Weight",
            unit = "kg",
            currentValue = weight.toIntOrNull() ?: 60,
            range = 30..200,
            onDismiss = { showWeightDialog = false },
            onValueSelected = {
                onWeightChange(it.toString())
                showWeightDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 32.sp)) {
                    append("Tell us about\n")
                }
                withStyle(style = SpanStyle(color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 32.sp)) {
                    append("yourself")
                }
            },
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This helps our clinical AI tailor health insights and allergen warnings specifically for you.",
            color = Color.Gray,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        SectionLabel("FULL NAME")
        CustomTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            placeholder = "e.g. Julianne Smith"
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("GENDER")
        GenderSelector(
            selectedGender = selectedGender,
            onGenderSelected = onGenderChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("BIRTH DATE")
        CustomTextField(
            value = birthDate,
            onValueChange = { },
            placeholder = "March 24, 1995",
            enabled = false,
            modifier = Modifier.clickable { showDatePicker = true },
            trailingIcon = {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.Gray)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCard(
                label = "HEIGHT",
                value = height,
                unit = "cm",
                modifier = Modifier.weight(1f).clickable { showHeightDialog = true }
            )
            InfoCard(
                label = "WEIGHT",
                value = weight,
                unit = "kg",
                modifier = Modifier.weight(1f).clickable { showWeightDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        InfoBox()

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun SelectionDialog(
    title: String,
    unit: String,
    currentValue: Int,
    range: IntRange,
    onDismiss: () -> Unit,
    onValueSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentValue - range.first).coerceAtLeast(0))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(range.toList()) { value ->
                        Text(
                            text = "$value $unit",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onValueSelected(value) }
                                .padding(vertical = 12.dp),
                            fontSize = 18.sp,
                            fontWeight = if (value == currentValue) FontWeight.Bold else FontWeight.Normal,
                            color = if (value == currentValue) PrimaryColor else Color.Black
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun InfoBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F4FD), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF3277D8),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "We encrypt this data locally. It's only used to cross-reference with ingredients during your scans.",
                color = Color(0xFF3277D8),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



@Composable
fun InfoCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF7F7F7))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = unit, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(7) { index ->
                    val height = when (index) {
                        3 -> 28.dp
                        2, 4 -> 20.dp
                        else -> 14.dp
                    }
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(height)
                            .background(if (index == 3) PrimaryColor else Color(0xFFD0D0D0), RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFFF7F7F7), RoundedCornerShape(32.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Female", "Male").forEach { gender ->
            val isSelected = selectedGender == gender
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onGenderSelected(gender) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gender,
                    color = if (isSelected) PrimaryColor else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

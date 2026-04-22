package com.shopwise.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopwise.core.UserPreferences
import com.shopwise.ui.theme.PrimaryColor

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val userData = remember { userPreferences.getUserData() }

    var fullName by remember { mutableStateOf(userData["fullName"] as? String ?: "") }
    var birthDate by remember { mutableStateOf(userData["birthDate"] as? String ?: "") }
    var selectedGender by remember { mutableStateOf(userData["gender"] as? String ?: "Female") }
    var height by remember { mutableStateOf(userData["height"] as? String ?: "") }
    var weight by remember { mutableStateOf(userData["weight"] as? String ?: "") }
    val selectedModel = remember { userData["selectedModel"] as? String ?: "4B" }
    
    val allergies = remember { 
        val set = userData["allergies"] as? Set<String> ?: emptySet()
        set.toMutableStateList()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newAllergyText by remember { mutableStateOf("") }

    // Helper function untuk simpan semua data ke SharedPreferences
    val saveChanges = {
        userPreferences.saveUserData(
            fullName = fullName,
            birthDate = birthDate,
            gender = selectedGender,
            height = height,
            weight = weight,
            allergies = allergies.toSet(),
            selectedModel = selectedModel
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Image Section
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryColor)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Form Fields
        ProfileSectionLabel("FULL NAME")
        ProfileTextField(value = fullName, onValueChange = { 
            fullName = it 
            saveChanges()
        })

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSectionLabel("GENDER")
        ProfileGenderSelector(selectedGender = selectedGender, onGenderSelected = { 
            selectedGender = it 
            saveChanges()
        })

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSectionLabel("BIRTH DATE")
        ProfileTextField(
            value = birthDate,
            onValueChange = { 
                birthDate = it 
                saveChanges()
            },
            leadingIcon = Icons.Default.Info,
            trailingIcon = Icons.Default.Info
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                ProfileSectionLabel("HEIGHT (CM)")
                ProfileTextField(value = height, onValueChange = { 
                    height = it 
                    saveChanges()
                }, trailingIcon = Icons.Default.Info)
            }
            Column(modifier = Modifier.weight(1f)) {
                ProfileSectionLabel("WEIGHT (KG)")
                ProfileTextField(value = weight, onValueChange = { 
                    weight = it 
                    saveChanges()
                }, trailingIcon = Icons.Default.Info)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Allergies Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ALLERGIES & SENSITIVITIES",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "+ ADD NEW",
                color = PrimaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable { showAddDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Allergies Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allergies.forEach { allergy ->
                AllergyChip(name = allergy, onDelete = { 
                    allergies.remove(allergy)
                    saveChanges()
                })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bottom Info Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFBBDEFB).copy(alpha = 0.7f))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Gemma notes: Keeping your weight and allergies updated helps us provide more accurate safety insights during your scans.",
                    color = Color(0xFF0D47A1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Dialog Tambah Alergi
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Allergy") },
            text = {
                OutlinedTextField(
                    value = newAllergyText,
                    onValueChange = { newAllergyText = it },
                    placeholder = { Text("e.g. Seafood, Eggs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newAllergyText.isNotBlank()) {
                            allergies.add(newAllergyText.trim())
                            saveChanges()
                            newAllergyText = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add", color = PrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7F7F7)
    ) {
        // Karena ini Profile, biasanya TextField harusnya bisa diketik
        // Tapi di desain awal pakai Text, saya ubah ke BasicTextField atau TextField agar interaktif
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) Text("Enter value", color = Color.LightGray)
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Icon(trailingIcon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        )
    }
}

@Composable
fun ProfileGenderSelector(selectedGender: String, onGenderSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Female", "Male").forEach { gender ->
            val isSelected = selectedGender == gender
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
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

@Composable
fun AllergyChip(name: String, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left indicator line
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (name == "Lactose") Color(0xFF1E88E5) else Color(0xFFB3261E))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

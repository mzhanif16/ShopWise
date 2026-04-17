package com.shopwise.common.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopwise.ui.theme.PrimaryColor


@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = PrimaryColor,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

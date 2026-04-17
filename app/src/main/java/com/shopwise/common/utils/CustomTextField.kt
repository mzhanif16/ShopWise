package com.shopwise.common.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFFF7F7F7), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(text = placeholder, color = Color.Gray, fontSize = 16.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            trailingIcon?.invoke()
        }
    }
}
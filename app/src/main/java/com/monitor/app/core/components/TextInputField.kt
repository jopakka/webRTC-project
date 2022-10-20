package com.monitor.app.core.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun TextInputField(label: String = "", placeholder: String = "", onChange: (text: String) -> Unit) {
    var mText by remember { mutableStateOf(TextFieldValue("")) }
    OutlinedTextField(
        value = mText,
        onValueChange = { newText ->
            mText = newText
            onChange(newText.text)
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        placeholder = { Text(placeholder) },
        label = { Text(label) }
    )
}
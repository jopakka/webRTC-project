package com.monitor.app.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextInputField(label: String = "", placeholder: String = "", onChange: (text: String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var mText by remember { mutableStateOf(TextFieldValue("")) }
    OutlinedTextField(
        value = mText,
        onValueChange = { newText ->
            mText = newText
            onChange(newText.text)
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        placeholder = { Text(placeholder) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(1f)
    )
}
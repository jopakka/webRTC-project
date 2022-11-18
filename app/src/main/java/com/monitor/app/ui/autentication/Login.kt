package com.monitor.app.ui.autentication

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.monitor.app.R
import com.monitor.app.core.components.*


@Composable
fun LoginScreen(auth: FirebaseAuth, onLogin: (userId: String) -> Unit) {

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    val isEmailValid by derivedStateOf {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val isPasswordValid by derivedStateOf {
        password.length > 4
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var isPasswordVisible by remember {
        mutableStateOf(false)
    }

    val toastText = stringResource(id = R.string.error_invalid_email_or_password)

    val doLogin = {
        if (isEmailValid && isPasswordValid) {
            isLoading = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("AUTH", "user: ${auth.currentUser?.email}")
                        onLogin(auth.currentUser?.uid!!)
                    } else {
                        Log.w("AUTH", "${it.exception}")
                        Toast.makeText(
                            context,
                            toastText,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colors.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
//        Text(
//            text = "Hi",
//            fontFamily = FontFamily.SansSerif,
//            fontWeight = FontWeight.Bold,
//            fontStyle = FontStyle.Italic,
//            fontSize = 32.sp,
//            modifier = Modifier.padding(top = 16.dp)
//        )

        Title(text = stringResource(R.string.app_name))

        MCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(all = 10.dp)
            ) {
                TextInputField(
                    value = email,
                    onChange = { email = it },

                    label = stringResource(id = R.string.email_address),
                    placeholder = stringResource(id = R.string.placeholder_email),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = email.isNotEmpty() && !isEmailValid,
                    trailingIcon = {
                        if (email.isNotBlank()) {
                            IconButton(onClick = { email = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear email"
                                )
                            }
                        }
                    },
                )

                TextInputField(value = password,
                    onChange = { password = it },
                    label = stringResource(id = R.string.password),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            doLogin()
                        }
                    ),
                    isError = password.isNotEmpty() && !isPasswordValid,
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password visibility"
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )

                MButton(
                    text = "Login",
                    onClick = doLogin,
                    enabled = !isLoading && isEmailValid && isPasswordValid,
                    loading = isLoading,
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MTextButton("Forgot password")
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MTextButton("Register")
                }

//                MButton(
//                    onClick = {},
//                    enabled = true,
//                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
//                    text = "Register",
//                )
            }
        }
    }
}


//@Composable
//fun DefaultPreview() {
//    HomeSecuritySystemTheme {
//        LoginScreen(Firebase.auth)
//    }
//}

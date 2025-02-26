package com.mehmetbaloglu.mychatapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mehmetbaloglu.mychatapp.navigation.AppScreens
import com.mehmetbaloglu.mychatapp.ui.viewmodels.LogInViewModel

@Composable
fun LogInScreen(
    navController: NavController = rememberNavController()
) {
    val logInViewModel: LogInViewModel = hiltViewModel()
    val logInState by logInViewModel.logInState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(logInState) {
        Log.d("TAG_screen", "logInState değişti: data=${logInState.data}, message=${logInState.message}")
        when {
            logInState.data != null -> {
                Toast.makeText(context, logInState.data, Toast.LENGTH_LONG).show()
                navController.navigate(AppScreens.ChatListScreen.name) {
                    popUpTo(AppScreens.LoginScreen.name) { inclusive = true }
                }
                logInViewModel.clearMessage()
            }
            logInState.message != null -> {
                formError = logInState.message
                logInViewModel.clearMessage()
            }
        }
    }

    Scaffold { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; if (formError != null) formError = null },
                    label = { Text("E-posta") },
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = formError != null && email.isBlank()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; if (formError != null) formError = null },
                    label = { Text("Şifre") },
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = formError != null && password.isBlank()
                )
                formError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                Button(
                    onClick = {
                        formError = when {
                            email.isBlank() || password.isBlank() -> "Tüm alanları doldurun!"
                            !email.contains("@") || !email.contains(".") -> "Geçerli bir e-posta adresi girin!"
                            else -> {
                                logInViewModel.logIn(email, password)
                                null // Navigasyonu LaunchedEffect'e bırakıyoruz
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 12.dp),
                    enabled = !logInState.isLoading
                ) {
                    Text("Giriş Yap")
                }
                TextButton(
                    onClick = { navController.navigate(AppScreens.RegisterScreen.name) },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Hesabın yok mu? Kayıt Ol")
                }
                if (logInState.isLoading) {
                    Log.d("TAG", "logInState.isLoading: ${logInState.isLoading}")
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}
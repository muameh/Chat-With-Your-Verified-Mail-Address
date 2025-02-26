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
import com.mehmetbaloglu.mychatapp.navigation.AppScreens
import com.mehmetbaloglu.mychatapp.ui.viewmodels.RegisterViewModel

@Composable
fun RegisterScreen(
    navController: NavController
) {
    val registerViewModel: RegisterViewModel = hiltViewModel()
    val registerState by registerViewModel.registerState.collectAsState()
    val context = LocalContext.current

    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showVerificationCheck by remember { mutableStateOf(false) }

    LaunchedEffect(registerState) {
        Log.d(
            "TAG_screen",
            "registerState değişti: data=${registerState.data}, message=${registerState.message}"
        )
        when {
            registerState.data?.contains("doğrulandı") == true -> {
                Toast.makeText(context, registerState.data, Toast.LENGTH_LONG).show()
                navController.navigate(AppScreens.ChatListScreen.name)
                registerViewModel.clearMessage()
            }

            registerState.data != null -> {
                Toast.makeText(context, registerState.data, Toast.LENGTH_LONG).show()
                showVerificationCheck = true
                registerViewModel.clearMessage()
            }

            registerState.message != null -> {
                formError = registerState.message
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
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Kullanıcı Adı") },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    isError = formError != null && userName.isBlank()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    isError = formError != null && email.isBlank()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Şifre") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    isError = formError != null && (password.isBlank() || password.length < 6)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Şifreyi Onayla") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    isError = formError != null && (confirmPassword.isBlank() || confirmPassword != password)
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
                            userName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> "Tüm alanları doldurun!"
                            !email.contains("@") || !email.contains(".") -> "Geçerli bir e-posta adresi girin!"
                            password != confirmPassword -> "Şifreler uyuşmuyor!"
                            password.length < 6 -> "Şifre en az 6 karakter olmalıdır!"
                            else -> {
                                registerViewModel.registerUser(email, password)
                                null
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 12.dp),
                    enabled = !registerState.isLoading
                ) {
                    Text("Kayıt Ol")
                }
                if (showVerificationCheck) {
                    Button(
                        onClick = { registerViewModel.checkEmailVerification() },
                        modifier = Modifier.padding(top = 12.dp),
                        enabled = !registerState.isLoading
                    ) {
                        Text("Doğrulamayı Kontrol Et")
                    }
                }
                if (registerState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}
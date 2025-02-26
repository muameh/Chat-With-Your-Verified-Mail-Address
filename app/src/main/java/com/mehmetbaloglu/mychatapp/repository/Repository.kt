package com.mehmetbaloglu.mychatapp.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.mehmetbaloglu.mychatapp.models.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class Repository @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    fun registerUser(email: String, password: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource(isLoading = true))  // 📌 Kayıt işlemi başladı

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                try {
                    user.sendEmailVerification().await()
                    emit(
                        Resource(
                            data = "E-posta doğrulama e-postası gönderildi. Lütfen e-postanızı kontrol edin.",
                            isLoading = false
                        )
                    )
                    Log.d("TAG", "registerUser: Doğrulama e-postası gönderildi - ${user.email}")
                } catch (e: Exception) {
                    emit(Resource(message = "Email doğrulama hatası: ${e.localizedMessage}"))
                    Log.d("TAG", "registerUser: ${e.localizedMessage}")
                }
            } else {
                emit(Resource(message = "Kullanıcı oluşturulamadı!"))
                Log.d("Tag", "registerUser: Kullanıcı oluşturulamadı!")
            }

        } catch (e: Exception) {
            emit(Resource(message = "Hata: ${e.localizedMessage}"))
            Log.d("TAG", "registerUser: ${e.localizedMessage}")// 📌 Hata mesajı
        }
    }

    fun checkEmailVerification(): Flow<Resource<String>> = flow {
        emit(Resource(isLoading = true))
        val user = auth.currentUser
        if (user != null) {
            user.reload().await() // Kullanıcı durumunu güncelle
            if (user.isEmailVerified) {
                emit(Resource(data = "E-posta doğrulandı! Kayıt başarılı.", isLoading = false))
            } else {
                emit(Resource(message = "E-posta henüz doğrulanmadı.", isLoading = false))
            }
        } else {
            emit(Resource(message = "Kullanıcı bulunamadı!", isLoading = false))
        }
    }

    fun logIn(email: String, password: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource(isLoading = true))
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                if (user.isEmailVerified) {
                    emit(Resource(data = "Giriş başarılı!", isLoading = false))
                    Log.d("TAG", "logIn: Giriş başarılı - ${user.email}")
                } else {
                    emit(
                        Resource(
                            message = "E-posta henüz doğrulanmadı. Lütfen e-postanızı doğrulayın.",
                            isLoading = false
                        )
                    )
                    Log.d("TAG", "logIn: E-posta doğrulanmadı - ${user.email}")
                }
            } else {
                emit(Resource(message = "Giriş başarısız!", isLoading = false))
                Log.d("TAG", "logIn: Giriş başarısız!")
            }
        } catch (e: Exception) {
            emit(Resource(message = e.localizedMessage, isLoading = false))
            Log.d("TAG", "logIn: ${e.localizedMessage}")
        }
    }
}

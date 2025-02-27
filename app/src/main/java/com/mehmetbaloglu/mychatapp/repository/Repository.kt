package com.mehmetbaloglu.mychatapp.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mehmetbaloglu.mychatapp.models.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class Repository @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun registerUser(email: String, password: String, userName: String): Flow<Resource<String>> = flow {
        emit(Resource(isLoading = true))
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                try {
                    // Kullanıcı bilgilerini veritabanına kaydet
                    database.reference.child("users").child(user.uid).setValue(
                        mapOf(
                            "userName" to userName,
                            "email" to email
                        )
                    ).await()
                    Log.d("xxRepository", "registerUser: userName=$userName, email=$email kaydedildi")
                } catch (e: Exception) {
                    emit(Resource(message = "Kullanıcı bilgileri kaydedilemedi: ${e.localizedMessage}", isLoading = false))
                    Log.e("xxRepository", "registerUser: Failed to save user data - ${e.localizedMessage}")
                    return@flow
                }

                try {
                    user.sendEmailVerification().await()
                    emit(
                        Resource(
                            data = "E-posta doğrulama e-postası gönderildi. Lütfen e-postanızı kontrol edin.",
                            isLoading = false
                        )
                    )
                    Log.d("xxRepository", "registerUser: Doğrulama e-postası gönderildi - ${user.email}, userName=$userName")
                } catch (e: Exception) {
                    emit(Resource(message = "Email doğrulama hatası: ${e.localizedMessage}", isLoading = false))
                    Log.e("xxRepository", "registerUser: Email verification failed - ${e.localizedMessage}")
                }
            } else {
                emit(Resource(message = "Kullanıcı oluşturulamadı!", isLoading = false))
                Log.e("xxRepository", "registerUser: Kullanıcı oluşturulamadı!")
            }
        } catch (e: Exception) {
            emit(Resource(message = "Kayıt hatası: ${e.localizedMessage}", isLoading = false))
            Log.e("xxRepository", "registerUser: ${e.localizedMessage}")
        }
    }.flowOn(Dispatchers.IO)

    fun checkEmailVerification(): Flow<Resource<String>> = flow {
        emit(Resource(isLoading = true))
        try {
            val user = auth.currentUser
            if (user != null) {
                user.reload().await() // Kullanıcı durumunu güncelle
                if (user.isEmailVerified) {
                    emit(Resource(data = "E-posta doğrulandı! Kayıt başarılı.", isLoading = false))
                    Log.d("xxRepository", "checkEmailVerification: E-posta doğrulandı - ${user.email}")
                } else {
                    emit(Resource(message = "E-posta henüz doğrulanmadı.", isLoading = false))
                    Log.d("xxRepository", "checkEmailVerification: E-posta doğrulanmadı - ${user.email}")
                }
            } else {
                emit(Resource(message = "Kullanıcı bulunamadı!", isLoading = false))
                Log.e("xxRepository", "checkEmailVerification: Kullanıcı bulunamadı!")
            }
        } catch (e: Exception) {
            emit(Resource(message = "Doğrulama kontrolü hatası: ${e.localizedMessage}", isLoading = false))
            Log.e("xxRepository", "checkEmailVerification: ${e.localizedMessage}")
        }
    }.flowOn(Dispatchers.IO)

    fun logIn(email: String, password: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource(isLoading = true))
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                if (user.isEmailVerified) {
                    emit(Resource(data = "Giriş başarılı!", isLoading = false))
                    Log.d("xxRepository", "logIn: Giriş başarılı - ${user.email}")
                } else {
                    emit(
                        Resource(
                            message = "E-posta henüz doğrulanmadı. Lütfen e-postanızı doğrulayın.",
                            isLoading = false
                        )
                    )
                    Log.d("xxRepository", "logIn: E-posta doğrulanmadı - ${user.email}")
                }
            } else {
                emit(Resource(message = "Giriş başarısız!", isLoading = false))
                Log.e("xxRepository", "logIn: Giriş başarısız!")
            }
        } catch (e: Exception) {
            emit(Resource(message = "Giriş hatası: ${e.localizedMessage}", isLoading = false))
            Log.e("xxRepository", "logIn: ${e.localizedMessage}")
        }
    }.flowOn(Dispatchers.IO)
}

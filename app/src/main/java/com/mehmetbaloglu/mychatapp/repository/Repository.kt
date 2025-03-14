package com.mehmetbaloglu.mychatapp.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mehmetbaloglu.mychatapp.models.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class Repository @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun registerUser(email: String, password: String, userName: String): Flow<Resource<String>> =
        flow {
            emit(Resource(isLoading = true))
            try {
                Log.d("xxRepository", "Kayıt başlıyor: email=$email")
                val authResult = withTimeout(15000L) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                }
                val user = authResult.user

                if (user != null) {
                    Log.d("xxRepository", "Kullanıcı oluşturuldu: uid=${user.uid}")

                    // Veritabanı yazma işlemi
                    val ref = database.reference.child("users").child(user.uid)
                    try {
                        ref.setValue(
                            mapOf(
                                "userName" to userName,
                                "email" to email
                            )
                        ).await()
                        Log.d("xxRepository", "Kullanıcı bilgileri kaydedildi")
                    } catch (e: Exception) {
                        Log.e(
                            "xxRepository",
                            "Veritabanına yazma hatası: ${e.localizedMessage}, cause=${e.cause}"
                        )
                        emit(
                            Resource(
                                message = "Kullanıcı oluşturuldu ancak veritabanına kaydedilemedi: ${e.localizedMessage}",
                                isLoading = false
                            )
                        )
                        return@flow
                    }

                    // Doğrulama emaili gönderimi (isteğe bağlı, başarısızlık akışı durdurmaz)
                    try {
                        Log.d("xxRepository", "Doğrulama emaili gönderimi başlıyor")
                        withTimeout(15000L) {
                            user.sendEmailVerification().await()
                        }
                        Log.d("xxRepository", "Doğrulama maili gönderildi: ${user.email}")
                        emit(
                            Resource(
                                data = "Kayıt başarılı! Doğrulama emaili gönderildi.",
                                isLoading = false
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "xxRepository",
                            "Doğrulama maili gönderilemedi: ${e.localizedMessage}, cause=${e.cause}"
                        )
                        emit(
                            Resource(
                                data = "Kayıt başarılı! Ancak doğrulama maili gönderilemedi.",
                                isLoading = false
                            )
                        )
                    }
                } else {
                    Log.e("xxRepository", "Kullanıcı null geldi!")
                    emit(Resource(message = "Kullanıcı oluşturulamadı!", isLoading = false))
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("xxRepository", "Zaman aşımı: İşlem tamamlanamadı")
                emit(
                    Resource(
                        message = "Zaman aşımı: Kayıt işlemi tamamlanamadı",
                        isLoading = false
                    )
                )
            } catch (e: Exception) {
                Log.e("xxRepository", "Genel kayıt hatası: ${e.localizedMessage}, cause=${e.cause}")
                emit(Resource(message = "Kayıt hatası: ${e.localizedMessage}", isLoading = false))
            }
        }.flowOn(Dispatchers.IO)

    fun checkEmailVerification(): Flow<Resource<String>> = flow {
        emit(Resource(isLoading = true))
        try {
            val user = auth.currentUser
            if (user != null) {
                withTimeout(15000L) {
                    user.reload().await()
                }
                Log.d(
                    "xxRepository",
                    "Kullanıcı durumu güncellendi: isEmailVerified=${user.isEmailVerified}"
                )
                if (user.isEmailVerified) {
                    emit(Resource(data = "E-posta doğrulandı!", isLoading = false))
                } else {
                    emit(Resource(message = "E-posta henüz doğrulanmadı.", isLoading = false))
                }
            } else {
                Log.e("xxRepository", "Kullanıcı null!")
                emit(Resource(message = "Kullanıcı bulunamadı!", isLoading = false))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("xxRepository", "Zaman aşımı: Doğrulama kontrolü başarısız")
            emit(Resource(message = "Zaman aşımı: Doğrulama kontrolü başarısız", isLoading = false))
        } catch (e: Exception) {
            Log.e("xxRepository", "Doğrulama kontrol hatası: ${e.localizedMessage}")
            emit(Resource(message = "Doğrulama hatası: ${e.localizedMessage}", isLoading = false))
        }
    }.flowOn(Dispatchers.IO)

    fun logIn(email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource(isLoading = true))
        try {
            Log.d("xxRepository", "Giriş deneniyor: email=$email")
            val authResult = withTimeout(15000L) {
                auth.signInWithEmailAndPassword(email, password).await()
            }
            val user = authResult.user
            if (user != null) {
                Log.d("xxRepository", "Kullanıcı var Mail doğrulaması kontrol ediliyor: uid=${user.uid}")
                if (user.isEmailVerified) {
                    emit(Resource(data = "Hoşgeldiniz!", isLoading = false))
                } else {
                    // Email doğrulanmamışsa logout yap ve hata mesajı döndür
                    auth.signOut()
                    Log.d("xxRepository", "Email doğrulanmamış: uid=${user.uid}")
                    emit(Resource(message = "Lütfen önce email adresinizi doğrulayın!", isLoading = false))
                }
            } else {
                Log.e("xxRepository", "Giriş başarısız, kullanıcı null!")
                emit(Resource(message = "Giriş başarısız!", isLoading = false))
            }
        } catch (e: Exception) {
            Log.e("xxRepository", "Giriş hatası: ${e.localizedMessage}")
            emit(Resource(message = e.localizedMessage, isLoading = false))
        }
    }.flowOn(Dispatchers.IO)
}
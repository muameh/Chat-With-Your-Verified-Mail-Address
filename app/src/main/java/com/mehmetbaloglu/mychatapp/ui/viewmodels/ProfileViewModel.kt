package com.mehmetbaloglu.mychatapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _profileState.value = ProfileState(isLoading = false, error = "Kullanıcı oturumu açık değil")
            Log.e("xxProfileViewModel", "No user logged in")
            return
        }

        _profileState.value = ProfileState(isLoading = true, email = currentUser.email)

        val userId = currentUser.uid
        database.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("userName").getValue(String::class.java)
                    _profileState.value = ProfileState(
                        isLoading = false,
                        userName = userName,
                        email = currentUser.email
                    )
                    Log.d("xxProfileViewModel", "Profile loaded: userName=$userName, email=${currentUser.email}")
                }

                override fun onCancelled(error: DatabaseError) {
                    _profileState.value = ProfileState(
                        isLoading = false,
                        email = currentUser.email,
                        error = "Profil bilgileri yüklenemedi: ${error.message}"
                    )
                    Log.e("xxProfileViewModel", "Failed to load profile: ${error.message}")
                }
            })
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                Log.d("xxProfileViewModel", "User signed out successfully")
            } catch (e: Exception) {
                Log.e("xxProfileViewModel", "Sign out error: ${e.localizedMessage}")
            }
        }
    }
}

data class ProfileState(
    val isLoading: Boolean = true,
    val userName: String? = null,
    val email: String? = null,
    val error: String? = null
)
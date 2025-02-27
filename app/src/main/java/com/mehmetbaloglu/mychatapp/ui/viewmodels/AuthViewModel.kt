package com.mehmetbaloglu.mychatapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _authStateFlow = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val authStateFlow: StateFlow<FirebaseUser?> get() = _authStateFlow

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _authStateFlow.value = firebaseAuth.currentUser
        Log.d("xxAuthViewModel", "Auth state changed, currentUser: ${firebaseAuth.currentUser?.uid}")
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        Log.d("xxAuthViewModel", "Auth listener removed")
    }
}

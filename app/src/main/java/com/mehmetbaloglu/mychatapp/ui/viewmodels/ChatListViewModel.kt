package com.mehmetbaloglu.mychatapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mehmetbaloglu.mychatapp.models.Resource
import com.mehmetbaloglu.mychatapp.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    private val _homeState = MutableStateFlow<Resource<String>>(Resource())
    val homeState: StateFlow<Resource<String>> = _homeState

    // Çıkış yapma fonksiyonu
    fun logOut() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
                _homeState.value = Resource(data = "Çıkış yapıldı!", isLoading = false)
                Log.d("xxHomeViewModel", "User logged out successfully")
            } catch (e: Exception) {
                _homeState.value = Resource(message = "Çıkış hatası: ${e.localizedMessage}", isLoading = false)
                Log.e("xxHomeViewModel", "Logout error: ${e.localizedMessage}")
            }
        }
    }

    fun clearMessage() {
        _homeState.value = _homeState.value.copy(message = null)
    }
}
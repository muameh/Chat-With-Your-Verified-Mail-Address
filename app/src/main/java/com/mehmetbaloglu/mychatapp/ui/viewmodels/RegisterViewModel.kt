package com.mehmetbaloglu.mychatapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetbaloglu.mychatapp.models.Resource
import com.mehmetbaloglu.mychatapp.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _registerState = MutableStateFlow<Resource<String>>(Resource())
    val registerState: StateFlow<Resource<String>> = _registerState

    fun registerUser(email: String, password: String, userName:String) {
        viewModelScope.launch {
            repository.registerUser(email, password, userName).collect { result ->
                _registerState.value = result
                Log.d("TAG_viewmodel_registerState", "registerUser: ${result.message}")
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            repository.checkEmailVerification().collect { result ->
                _registerState.value = result
                Log.d("TAG_viewmodel_registerState", "checkEmailVerification: ${result.message}")
            }
        }
    }

    fun clearMessage() {
        _registerState.value = _registerState.value.copy(message = null)
    }


}
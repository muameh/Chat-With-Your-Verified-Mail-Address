package com.mehmetbaloglu.mychatapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetbaloglu.mychatapp.models.Resource
import com.mehmetbaloglu.mychatapp.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _logInState = MutableStateFlow<Resource<String>>(Resource())
    val logInState: StateFlow<Resource<String>> = _logInState

    fun logIn(email: String, password: String) {
        viewModelScope.launch {
            repository.logIn(email, password).collect { result ->
                _logInState.value = result
                Log.d("TAG_viewmodel_logInState", "logIn: ${result.message ?: result.data}")
            }
        }
    }

    fun clearMessage() {
        _logInState.value = _logInState.value.copy(message = null)
    }
}
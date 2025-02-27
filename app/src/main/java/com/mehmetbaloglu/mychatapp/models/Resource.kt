package com.mehmetbaloglu.mychatapp.models

data class Resource<out T>(
    val data: T? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)


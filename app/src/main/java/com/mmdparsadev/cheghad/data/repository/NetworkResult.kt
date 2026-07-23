package com.mmdparsadev.cheghad.data.repository

sealed class NetworkResult<out T> {
    data class Success<out T>(val Data: T) : NetworkResult<T>()
    data class Error(val MessageResId: Int, val Exception: Exception? = null) : NetworkResult<Nothing>()
}

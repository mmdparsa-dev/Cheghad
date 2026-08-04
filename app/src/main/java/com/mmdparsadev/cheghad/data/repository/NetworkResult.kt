package com.mmdparsadev.cheghad.data.repository

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T, val isFresh: Boolean = true) : NetworkResult<T>()
    data class Error(val messageResId: Int, val exception: Exception? = null) : NetworkResult<Nothing>()
}

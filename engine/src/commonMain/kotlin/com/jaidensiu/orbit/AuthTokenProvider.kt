package com.jaidensiu.orbit

fun interface AuthTokenProvider {
    fun currentToken(): String?
}

package com.stefanbratanov.sofiasupermarketsapi.common

import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun getTrustAllCerts() : SSLSocketFactory {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray()
        }

        override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
        override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
    })

    return try {
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        sslContext.socketFactory
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("Failed to create a SSL socket factory", e)
    } catch (e: KeyManagementException) {
        throw RuntimeException("Failed to create a SSL socket factory", e)
    }
}
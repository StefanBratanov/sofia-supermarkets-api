package com.stefata.sofiasupermarketsapi.interfaces

interface CdnUploader {

    fun upload(key: String, url: String): String

}
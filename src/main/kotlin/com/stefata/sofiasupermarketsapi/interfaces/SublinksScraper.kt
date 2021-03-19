package com.stefata.sofiasupermarketsapi.interfaces

import java.net.URL

interface SublinksScraper {

    fun getSublinks(): List<URL>

}
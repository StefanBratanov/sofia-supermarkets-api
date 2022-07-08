package com.stefanbratanov.sofiasupermarketsapi.interfaces

import java.net.URL

interface SublinksScraper {

    fun getSublinks(): List<URL>
}

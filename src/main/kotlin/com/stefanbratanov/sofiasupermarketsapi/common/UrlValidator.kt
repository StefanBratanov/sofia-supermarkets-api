package com.stefanbratanov.sofiasupermarketsapi.common

import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

class UrlValidator {

  fun isValid(url: String): Boolean {
    return try {
      URL(url).toURI()
      true
    } catch (e: MalformedURLException) {
      false
    } catch (e: URISyntaxException) {
      false
    }
  }
}

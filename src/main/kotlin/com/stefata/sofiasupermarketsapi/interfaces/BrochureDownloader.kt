package com.stefata.sofiasupermarketsapi.interfaces

import java.nio.file.Path

interface BrochureDownloader {

    fun download(): Path

}
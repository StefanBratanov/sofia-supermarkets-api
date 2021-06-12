package com.stefata.sofiasupermarketsapi.interfaces

import java.nio.file.Path
import java.time.LocalDate

interface BrochureDownloader {

    fun download(): Pair<Path, LocalDate?>

}
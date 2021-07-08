package com.stefata.sofiasupermarketsapi.interfaces

import java.nio.file.Path
import java.time.LocalDate

interface BrochureDownloader {

    fun download(): List<Brochure>

    data class Brochure(val path: Path, val validUntil: LocalDate?)

}
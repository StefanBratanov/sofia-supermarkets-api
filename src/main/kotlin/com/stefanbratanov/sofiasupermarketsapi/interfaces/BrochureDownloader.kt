package com.stefanbratanov.sofiasupermarketsapi.interfaces

import java.nio.file.Path
import java.time.LocalDate

interface BrochureDownloader {

    fun download(): List<Brochure>

    data class Brochure(val path: Path, val validFrom: LocalDate?, val validUntil: LocalDate?)

}
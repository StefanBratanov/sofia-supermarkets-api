package com.stefata.sofiasupermarketsapi.interfaces

import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.pdfbox.pdmodel.PDDocument
import java.nio.file.Path

interface PdfProductsExtractor {

    fun extract(pdf: Path): List<Product>

    fun getPDocument(pdf: Path): PDDocument {
        return PDDocument.load(pdf.toFile())
    }
}
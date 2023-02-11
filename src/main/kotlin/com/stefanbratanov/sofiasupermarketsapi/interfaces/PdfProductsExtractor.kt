package com.stefanbratanov.sofiasupermarketsapi.interfaces

import com.stefanbratanov.sofiasupermarketsapi.model.Product
import java.nio.file.Path
import org.apache.pdfbox.pdmodel.PDDocument

interface PdfProductsExtractor {

  fun extract(pdf: Path): List<Product>

  fun getPDocument(pdf: Path): PDDocument {
    return PDDocument.load(pdf.toFile())
  }
}

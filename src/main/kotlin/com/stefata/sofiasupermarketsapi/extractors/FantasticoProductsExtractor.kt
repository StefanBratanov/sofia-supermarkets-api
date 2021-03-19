package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.normalizePrice
import com.stefata.sofiasupermarketsapi.interfaces.PdfProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import com.stefata.sofiasupermarketsapi.pdf.PdfPageProductsExtractor
import com.stefata.sofiasupermarketsapi.pdf.ProductSection
import com.stefata.sofiasupermarketsapi.pdf.ProductSection.*
import com.stefata.sofiasupermarketsapi.pdf.TextWithCoordinates
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.apache.logging.log4j.util.Strings
import org.springframework.stereotype.Component
import java.nio.file.Path

@Log
@Component("Fantastico")
class FantasticoProductsExtractor : PdfProductsExtractor {

    private val regexesToIgnore = listOf(
        "www\\.fantastico\\.bg".toRegex(RegexOption.IGNORE_CASE),
        "ОФЕРТА ЗА ПЕРИОДА".toRegex(RegexOption.IGNORE_CASE),
        "fantastico\\.stores".toRegex(RegexOption.IGNORE_CASE),
        "Продуктите се продават в количества".toRegex(RegexOption.IGNORE_CASE),
        "си запазва правото на промяна".toRegex(RegexOption.IGNORE_CASE),
        "(Промоцията|акцията) е валидна".toRegex(RegexOption.IGNORE_CASE),
        "за магазините.*цени".toRegex(RegexOption.IGNORE_CASE)
    )

    private val productSectionResolver: Map<ProductSection, (String) -> Boolean> = mapOf(
        OLD_PRICE to { text -> text.matches("\\d{1,2}\\.\\d{2}".toRegex()) },
        NEW_PRICE to { text -> text.matches("\\d{3,4}".toRegex()) },
        DISCOUNT to { text -> text.matches("-?\\d{1,2}%".toRegex()) },
        CURRENCY to { text -> text.contains("лв|") },
        QUANTITY to { text -> text.contains("\\d+\\s*(мл|г|л|бр|см)".toRegex(RegexOption.IGNORE_CASE)) },
        NAME to { true }
    )

    override fun extract(pdf: Path): List<Product> {

        log.info("Processing Fantastico PDF: {}", pdf.fileName)

        val pdfDoc = getPDocument(pdf)
        val initialCenterPredicate: (TextWithCoordinates) -> Boolean = {
            it.text?.contains("лв|") == true
        }
        val pageProductsExtractor =
            PdfPageProductsExtractor(pdfDoc, regexesToIgnore, initialCenterPredicate, productSectionResolver)

        val products = generateSequence(1, { it + 1 }).take(pdfDoc.numberOfPages).flatMap { pageNumber ->
            log.info("Processing page {}/{}", pageNumber, pdfDoc.numberOfPages)
            pageProductsExtractor.getProductTextsWithSections(pageNumber).mapNotNull {
                val name = getName(it)
                val oldPrice = it.firstOrNull { sectionAndText ->
                    sectionAndText.first == OLD_PRICE
                }?.second?.text
                val newPrice = it.first { sectionAndText ->
                    sectionAndText.first == NEW_PRICE
                }.second.text?.replace("^0(?=\\d+)".toRegex(), "")
                val quantity = it.filter { sectionAndText ->
                    sectionAndText.first == QUANTITY
                }.joinToString(separator = " ") { sectionAndText ->
                    sectionAndText.second.text.toString()
                }.takeUnless { text ->
                    Strings.isBlank(text)
                }

                Product(
                    name = normalizeSpace(name),
                    price = normalizePrice(newPrice)?.div(100),
                    oldPrice = normalizePrice(oldPrice),
                    quantity = normalizeSpace(quantity)
                ).takeIf {
                    StringUtils.isNotBlank(name)
                }
            }
        }.toList()

        pdfDoc.close()

        return products
    }

    private fun getName(cluster: List<Pair<ProductSection, TextWithCoordinates>>): String {
        return cluster.filter {
            it.first == NAME
        }.filter {
            it.second.font?.name?.contains("Officina") == false
        }.joinToString(" ") {
            it.second.text.toString()
        }
    }

}
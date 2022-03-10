package com.stefanbratanov.sofiasupermarketsapi.extractors

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.normalizePrice
import com.stefanbratanov.sofiasupermarketsapi.common.removeDuplicateSubstrings
import com.stefanbratanov.sofiasupermarketsapi.interfaces.PdfProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import com.stefanbratanov.sofiasupermarketsapi.pdf.PdfPageProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.pdf.ProductSection
import com.stefanbratanov.sofiasupermarketsapi.pdf.ProductSection.*
import com.stefanbratanov.sofiasupermarketsapi.pdf.TextWithCoordinates
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.apache.logging.log4j.util.Strings
import org.springframework.stereotype.Component
import java.nio.file.Path

@Log
@Component("Fantastico")
class FantasticoProductsExtractor : PdfProductsExtractor {

    private val myriadProRegex = "myriadpro".toRegex(RegexOption.IGNORE_CASE)
    private val officinaSansRegex = "officinasans".toRegex(RegexOption.IGNORE_CASE)
    private val bebasNeueRegex = "bebasneuecyrillic".toRegex(RegexOption.IGNORE_CASE)

    private val regexesToIgnore = listOf(
        "www\\.fantastico\\.bg".toRegex(RegexOption.IGNORE_CASE),
        "ОФЕРТА ЗА ПЕРИОДА".toRegex(RegexOption.IGNORE_CASE),
        "fantastico\\.stores".toRegex(RegexOption.IGNORE_CASE),
        "Продуктите се продават в количества".toRegex(RegexOption.IGNORE_CASE),
        "си запазва правото на промяна".toRegex(RegexOption.IGNORE_CASE),
        "(Промоцията|акцията) е валидна".toRegex(RegexOption.IGNORE_CASE),
        "за магазините.*цени".toRegex(RegexOption.IGNORE_CASE),
        "Купи бира с марка".toRegex(RegexOption.IGNORE_CASE),
        ".*Легенда Цени.*".toRegex(RegexOption.IGNORE_CASE),
        ".*Уважаеми клиенти.*".toRegex(RegexOption.IGNORE_CASE),
        ".*наличността на промоциите.*".toRegex(RegexOption.IGNORE_CASE),
        ".*(ЧЕРВЕНА|СИНЯ|ЗЕЛЕНА) ЦЕНА.*".toRegex(RegexOption.IGNORE_CASE),
        "(.*\\s+)?Ф\\d+\\s+.*".toRegex(RegexOption.IGNORE_CASE),
        ".*Работно време.*".toRegex(RegexOption.IGNORE_CASE),
        ".*Във всички обекти на верига Фантастико.*".toRegex(RegexOption.IGNORE_CASE),
        "Според потребителската класация".toRegex(RegexOption.IGNORE_CASE)
    )

    private val fontsToKeep = listOf(
        myriadProRegex,
        officinaSansRegex,
        bebasNeueRegex
    )

    private val regexesToRemove = listOf(
        "стр\\.\\s*\\d+(-\\d+)?".toRegex(RegexOption.IGNORE_CASE),
        "\\*\\s*с включен".toRegex(RegexOption.IGNORE_CASE),
        "амбалаж 0(,|\\.)\\d+\\s*лв".toRegex(RegexOption.IGNORE_CASE),
        "\\*+".toRegex(RegexOption.IGNORE_CASE),
        ",\\s*\$".toRegex(RegexOption.IGNORE_CASE),
        "➥+".toRegex(RegexOption.IGNORE_CASE),
        "количествата са лимитирани".toRegex(RegexOption.IGNORE_CASE)
    )

    private val productSectionResolver: Map<ProductSection, (TextWithCoordinates) -> Boolean> = mapOf(
        OLD_PRICE to { twc -> twc.text!!.matches("\\d{1,2}\\.\\d{2}".toRegex()) },
        NEW_PRICE_LEGACY to { twc -> twc.text!!.matches("\\d{3,4}(\\*?)".toRegex()) },
        NEW_PRICE to { twc -> twc.text!!.matches("\\d{1,2}(\\*?)".toRegex()) &&  twc.font?.name?.contains(bebasNeueRegex) == true},
        DISCOUNT to { twc -> twc.text!!.matches("-?\\d{1,2}%".toRegex()) },
        CURRENCY to { twc -> twc.text!!.contains("лв|") },
        QUANTITY to { twc -> twc.text!!.contains("\\d+\\s*(мл|г|л|бр|см)".toRegex(RegexOption.IGNORE_CASE)) },
        NAME to { twc -> twc.font?.name?.contains(myriadProRegex) == true },
        UNKNOWN to { true }
    )

    override fun extract(pdf: Path): List<Product> {

        log.info("Processing Fantastico PDF: {}", pdf.fileName)

        val pdfDoc = getPDocument(pdf)
        val initialCenterPredicate: (TextWithCoordinates) -> Boolean = {
            it.text?.contains("лв|") == true
        }
        val pageProductsExtractor =
            PdfPageProductsExtractor(
                pdfDoc,
                regexesToIgnore,
                fontsToKeep,
                initialCenterPredicate,
                productSectionResolver
            )

        val products = generateSequence(1) { it + 1 }.take(pdfDoc.numberOfPages).flatMap { pageNumber ->
            log.info("Processing page {}/{}", pageNumber, pdfDoc.numberOfPages)
            pageProductsExtractor.getProductTextsWithSections(pageNumber).mapNotNull {
                val name = getName(it)
                val oldPrice = it.firstOrNull { sectionAndText ->
                    sectionAndText.first == OLD_PRICE
                }?.second?.text
                val newPrice = it.firstOrNull { sectionAndText ->
                    sectionAndText.first == NEW_PRICE_LEGACY || sectionAndText.first == NEW_PRICE
                }?.second?.text?.replace("^0(?=\\d+)".toRegex(), "")
                val quantity = it.filter { sectionAndText ->
                    sectionAndText.first == QUANTITY
                }.joinToString(separator = " ") { sectionAndText ->
                    sectionAndText.second.text.toString()
                }.takeUnless { text ->
                    Strings.isBlank(text)
                }.let { text ->
                    regexesToRemove.fold(text) { q, toRemove -> q?.replace(toRemove, "") }
                }.let { text ->
                    removeDuplicateSubstrings(text)
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
            it.second.font?.name?.contains(myriadProRegex) == true
        }.joinToString(" ") {
            it.second.text.toString()
        }.let {
            regexesToRemove.fold(it) { name, toRemove -> name.replace(toRemove, "") }
        }
    }

}
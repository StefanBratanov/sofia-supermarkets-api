package com.stefata.sofiasupermarketsapi

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors

class SofiaSupermarketsApiApplicationTests {

    @Test
    fun contextLoads() {

        val pdfFile =
            Paths.get("C:\\Users\\StefanPC\\IdeaProjects\\sofia-supermarkets-api\\src\\test\\resources\\BILLA Bulgaria - BG_weekly_leaflet_04.03.2021-10.03.2021_CW09__WEB.pdf")

        val pdf = PDDocument.load(pdfFile.toFile())

        val pdfStripper = PDFTextStripper()

        pdfStripper.startPage = 7
        pdfStripper.endPage = 7

        val text = pdfStripper.getText(pdf)

        val splitRegex = "(?=-\\d{1,2}%)".toRegex()

        val lines = text.split(splitRegex).stream()
            .map { it.split(System.lineSeparator()).stream().collect(Collectors.joining()) }
            .collect(Collectors.toList())

        println(lines.size)

        Files.writeString(
            pdfFile.resolveSibling("text.txt"),
            lines.stream().collect(Collectors.joining(System.lineSeparator())),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )

        //println(value)


    }

}

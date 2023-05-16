package com.example.Translator.controller;

import com.example.Translator.report.Report;
import com.example.Translator.service.ReportService;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/translator")
@RequiredArgsConstructor
@Slf4j
public class ReportToPdfController {
    private final ReportService reportService;

    @GetMapping(value = "/report-to-pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> reportToPdf() throws Exception {
        log.info("Generating report");
        Report report = reportService.report();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        log.info("Converting to PDF");

        document.add(new Paragraph("1. Number of words: " + report.wordsNumber()));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("2. Words of different lengths:"));
        document.add(new Paragraph("2. 1. Number of words of different lengths in polish"));
        document.add(new Paragraph("(no of letters: no of words of this length):"));
        for (Map.Entry<Integer, Long> entry : report.wordsLength().get("POL").entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
        }
        document.add(new Paragraph(" "));
        document.add(new Paragraph("2. 2. Number of words of different lengths in english"));
        document.add(new Paragraph("(no of letters: no of words of this length):"));
        for (Map.Entry<Integer, Long> entry : report.wordsLength().get("ENG").entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
        }
        document.add(new Paragraph(" "));

        document.add(new Paragraph("3. Average length of words in diff. languages: "));
        document.add(new Paragraph("Polish: " + report.averageLength().get("POL")));
        document.add(new Paragraph("English: " + report.averageLength().get("ENG")));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("4. Number of unknown words: " + report.unknownWordsNumber()));

        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.setContentDispositionFormData("report.pdf", "report.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }

}

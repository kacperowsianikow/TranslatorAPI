package com.example.Translator.controller;

import com.example.Translator.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/translator")
@RequiredArgsConstructor
@Slf4j
public class ReportToPdfController {
    private final ReportService reportService;

    @GetMapping(value = "/report-to-pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> reportToPdf() throws Exception {
        log.info("Generating report");
        byte[] reportBytes = reportService.generateReportInPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.setContentDispositionFormData("report.pdf", "report.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

}

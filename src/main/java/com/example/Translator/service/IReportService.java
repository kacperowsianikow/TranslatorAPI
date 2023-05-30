package com.example.Translator.service;

import com.example.Translator.dto.ReportDTO;

public interface IReportService {
    ReportDTO report();
    byte[] generateReportInPdf() throws Exception;

}

package com.davidp799.patcotoday.utils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class ConvertPDF {
    private String fileName, fileDir;
    public ConvertPDF(String fileDir, String fileName) {
        setFileName(fileName);
        setFileDir(fileDir);
    }
    public String getText() {
        try {
            PdfReader reader = new PdfReader(fileDir + fileName);
            int n = reader.getNumberOfPages();
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < n; i++) {
                output.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim()).append("\n");
            }
            reader.close();
            return output.toString();
        } catch (Exception e) {
            return "Error found is : \n" + e;
        }
    }
    public String getFileName() {
        return fileName;
    }
    public String getFileDir() {
        return fileDir;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }
}

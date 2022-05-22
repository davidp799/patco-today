package com.davidp799.patcotoday;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class ConvertPDF {
    private String fileName, fileDir;
    public ConvertPDF(String fileDir, String fileName) {
        setFileName(fileName);
        setFileDir(fileDir);
    }
    /**
     * Function which extracts text from given pdf file
     * @return output String form of pdf text data
     */
    public String getText() {
        try {
            PdfReader reader = new PdfReader(fileDir + fileName);
            int n = reader.getNumberOfPages();
            String output = "";
            for (int i = 0; i < n; i++) {
                output = output + PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + "\n";
            }
            reader.close();
            return output;
        } catch (Exception e) {
            return "Error found is : \n" + e;
        }
    }
    /* Accessor for file name */
    public String getFileName() {
        return fileName;
    }
    /* Accessor for file directory */
    public String getFileDir() {
        return fileDir;
    }
    /* Modifier for file name  */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /* Modifier for file directory */
    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }
}

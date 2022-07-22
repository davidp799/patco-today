package com.davidp799.patcotoday.utils;

import android.content.Context;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class JpgToPdf {
    private final String inputFile; private final String outputFile;
    private final Context context;
    private final String directoryPath = "/data/data/com.davidp799.patcotoday/files/data/special/";


    public JpgToPdf(Context context, String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.context = context;
        convertToPdf();
    }

    public void convertToPdf() {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(directoryPath + outputFile)); //  Change pdf's name.
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }

        document.open();

        Image image = null;  // Change image's name and extension.
        try {
            image = Image.getInstance(directoryPath + inputFile);
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }

        assert image != null;
        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
        image.scalePercent(scaler);
        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

        try {
            document.add(image);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        document.close();
    }

    public String getInputFile() {
        return inputFile;
    }
    public String getOutputFile() {
        return outputFile;
    }

    public Context getContext() {
        return context;
    }
}
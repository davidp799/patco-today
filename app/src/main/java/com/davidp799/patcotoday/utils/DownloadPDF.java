package com.davidp799.patcotoday.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadPDF {
    public DownloadPDF(String link, String filePath, String fileName) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(link);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("- E: Server ResponseCode = " + connection.getResponseCode() + " ResponseMessage=" + connection.getResponseMessage());
            }
            // download the file
            input = connection.getInputStream();
            System.out.println("- destinationFilePath = " + filePath+fileName);
            new File(filePath+fileName).createNewFile();
            output = new FileOutputStream(filePath+fileName);

            byte[] data = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connection != null) connection.disconnect();
        }
    }
}
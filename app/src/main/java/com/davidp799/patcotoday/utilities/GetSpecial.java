package com.davidp799.patcotoday.utilities;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Calendar;

public class GetSpecial {
    private Document doc;
    private String text, url;

    public GetSpecial(Document doc) {
        setDoc(doc);
        setData();
    }
    public Document getDoc() {
        return doc;
    }
    public void setDoc(Document doc) {
        this.doc = doc;
    }
    /* Accessor for special schedule status */
    public boolean getStatus() {
        /* Initialize current day and month */
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        /* Initialize HTML elements to search through */
        Element table = doc.body().getElementsByTag("table").first();
        Element tbody = table.getElementsByTag("tbody").first();
        /* Move to HTML element containing special schedule announcements */
        for (Element tr : tbody.getElementsByTag("tr")) {
            for (Element td : tr.getElementsByTag("td")) {
                for (Element p : td.getElementsByTag("p")) {
                    if (p.text().contains(", " + (month + 1) + "/" + day)) {
                        return true;
                    } else if (p.text().contains("(" + (month + 1) + "/" + day + ")")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /* Modifier for url and text data */
    public void setData() {
        /* Initialize current day and month */
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        /* Initialize HTML elements to search through */
        Element table = doc.body().getElementsByTag("table").first();
        Element tbody = table.getElementsByTag("tbody").first();
        /* Move to HTML element containing special schedule announcements */
        for (Element tr : tbody.getElementsByTag("tr")) {
            for (Element td : tr.getElementsByTag("td")) {
                for (Element p : td.getElementsByTag("p")) {
                    if (p.text().contains(", " + (month + 1) + "/" + day)) {
                        for (Element a : p.getElementsByTag("a")) {
                            this.url = a.attr("href");
                            this.text = a.text();
                        }
                    } else if (p.text().contains("(" + (month + 1) + "/" + day + ")")) {
                        for (Element a : p.getElementsByTag("a")) {
                            this.url = a.attr("href");
                            this.text = a.text();
                        }
                    }
                }
            }
        }
    }
    /* Accessor for url string data */
    public String getUrl() {
        return this.url;
    }
    /* Accessor for text string data */
    public String getText() {
        return this.text;
    }
}

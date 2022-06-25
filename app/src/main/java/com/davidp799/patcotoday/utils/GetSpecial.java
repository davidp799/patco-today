package com.davidp799.patcotoday.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Calendar;

public class GetSpecial {
    private Document doc;
    private ArrayList<String> text, url;

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
        /* Initialize lists for urls and titles */
        ArrayList<String> url = new ArrayList<>();
        ArrayList<String> text = new ArrayList<>();
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
                            String urlDir = a.attr("href");
                            url.add("http://www.ridepatco.org" + urlDir.replace("..", ""));
                            String[] split = p.text().split(" \\[");
                            text.add(split[0]);
                        }
                    } else if (p.text().contains("(" + (month + 1) + "/" + day + ")")) {
                        for (Element a : p.getElementsByTag("a")) {
                            String urlDir = a.attr("href");
                            url.add("http://www.ridepatco.org" + urlDir.replace("..", ""));
                            String[] split = p.text().split(" \\[");
                            text.add(split[0]);
                        }
                    }
                }
            }
        }
        this.url = url;
        this.text = text;
    }
    /* Accessor for url string data */
    public ArrayList<String> getUrl() {
        return this.url;
    }
    /* Accessor for text string data */
    public ArrayList<String> getText() {
        return this.text;
    }
}

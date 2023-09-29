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
    public void setDoc(Document doc) {
        this.doc = doc;
    }
    public void setData() {
        try {
            ArrayList<String> url = new ArrayList<>();
            ArrayList<String> text = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            Element tableElement = doc.body().getElementsByTag("table").first();
            assert tableElement != null;
            Element tbodyElement = tableElement.getElementsByTag("tbody").first();
            assert tbodyElement != null;
            Element tdElement = tbodyElement.getElementsByTag("tr").get(3);
            for (Element p : tdElement.getElementsByTag("p")) {
                if (p.text().contains(", " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH))) {
                    for (Element a : p.getElementsByTag("a")) {
                        String urlDir = a.attr("href");
                        url.add("http://www.ridepatco.org" + urlDir.replace("..", ""));
                        String[] split = p.text().split(" \\[");
                        text.add(split[0]);
                    }
                } else if (p.text().contains("(" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + ")")) {
                    for (Element a : p.getElementsByTag("a")) {
                        String urlDir = a.attr("href");
                        url.add("http://www.ridepatco.org" + urlDir.replace("..", ""));
                        String[] split = p.text().split(" \\[");
                        text.add(split[0]);
                    }
                }
            }
            this.url = url;
            this.text = text;
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public ArrayList<String> getUrl() {
        return this.url;
    }
    public ArrayList<String> getText() {
        return this.text;
    }
}

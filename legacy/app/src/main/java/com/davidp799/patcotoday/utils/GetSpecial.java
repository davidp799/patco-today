package com.davidp799.patcotoday.utils;

import android.util.Log;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

public class GetSpecial {
    private static final String TAG = "GetSpecial"; // Tag for logcat
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
            // Regex for date in format YYYY-MM-DD or similar
            Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
            Elements links = doc.select("a[href$=.pdf]");
            for (Element link : links) {
                String href = link.absUrl("href");
                if (href.isEmpty()) href = "http://www.ridepatco.org" + link.attr("href").replace("..", "");
                Matcher matcher = datePattern.matcher(href);
                if (matcher.find()) {
                    // Only consider links inside a <ul> that follows a heading with "special"
                    Element ul = link.parent();
                    int levels = 0;
                    boolean foundSpecialSection = false;
                    while (ul != null && levels < 5) {
                        if (ul.tagName().equals("ul")) {
                            // Look for previous sibling heading with "special"
                            Element prev = ul.previousElementSibling();
                            while (prev != null) {
                                if (prev.tagName().matches("h1|h2|h3|h4|h5|h6") && prev.text().toLowerCase().contains("special")) {
                                    foundSpecialSection = true;
                                    break;
                                }
                                prev = prev.previousElementSibling();
                            }
                            break;
                        }
                        ul = ul.parent();
                        levels++;
                    }
                    if (foundSpecialSection) {
                        url.add(href);
                        text.add(link.text());
                    }
                }
            }
            this.url = url;
            this.text = text;
        } catch (Exception e){
            e.printStackTrace();
            this.url = new ArrayList<>();
            this.text = new ArrayList<>();
            Log.e(TAG, "Exception in setData: " + e.getMessage(), e);
        }
    }

    public ArrayList<String> getUrl() {
        return this.url;
    }
    public ArrayList<String> getText() {
        return this.text;
    }
}

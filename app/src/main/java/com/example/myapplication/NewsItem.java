package com.example.myapplication;

import android.net.Uri;
import android.text.format.DateUtils;

import java.util.Date;
public class NewsItem {

    private long datetime;
    private String headline;
    private Uri image;
    private String url;

    private String summary;
    private String source;

    public NewsItem(long datetime, String headline, Uri image, String url, String summary, String source) {
        this.datetime = datetime;
        this.headline = headline;
        this.image = image;
        this.url = url;
        this.summary = summary;
        this.source = source;
    }

    public long getDatetime() {
        return datetime;
    }

    //calculating the time difference in hours
    public CharSequence getHoursPassed() {
        long now = System.currentTimeMillis();
        return DateUtils.getRelativeTimeSpanString(datetime * 1000, now, DateUtils.HOUR_IN_MILLIS);
    }

    public String getHeadline() {
        return headline;
    }

    public Uri getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }

    public String getSummary() {
        return summary;
    }

    public String getSource() {
        return source;
    }

    public String toString(){
        return "NewsItem{" +
                "datetime= " + datetime +
                "headline= " + headline +
                "image= " + image +
                "url= " + url +
                "summary= " + summary +
                "source= " + source;
    }

}

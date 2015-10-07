package edu.sjsu.qi.mytube;

import com.google.api.client.util.DateTime;

import java.math.BigInteger;
import java.util.Date;

/**
 * Created by qi on 10/5/15.
 */
public class VideoItem {

    private String id;
    private String title;
    private BigInteger views;
    private DateTime pub_date;
    private String thumbnailURL;
    private boolean favorite;

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigInteger getViews() {
        return views;
    }

    public void setViews(BigInteger views) {
        this.views = views;
    }

    public DateTime getPub_date() {
        return pub_date;
    }

    public void setPub_date(DateTime pub_date) {
        this.pub_date = pub_date;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

}

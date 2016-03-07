package com.example.xyzreader.data.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

import com.example.xyzreader.data.ItemsContract;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amrendra Kumar on 08/03/16.
 */
public class Article implements Parcelable {

    @SerializedName("id")
    int id;
    @SerializedName("photo")
    String photoUrl;
    @SerializedName("thumb")
    String thumbUrl;
    @SerializedName("aspect_ratio")
    double aspectRatio;
    @SerializedName("author")
    String author;
    @SerializedName("title")
    String title;
    @SerializedName("published_date")
    String publishDate;
    @SerializedName("body")
    String body;

    public Article() {

    }

    protected Article(Parcel in) {
        id = in.readInt();
        photoUrl = in.readString();
        thumbUrl = in.readString();
        aspectRatio = in.readDouble();
        author = in.readString();
        title = in.readString();
        publishDate = in.readString();
        body = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(photoUrl);
        dest.writeString(thumbUrl);
        dest.writeDouble(aspectRatio);
        dest.writeString(author);
        dest.writeString(title);
        dest.writeString(publishDate);
        dest.writeString(body);
    }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        Time time = new Time();
        values.put(ItemsContract.Items.SERVER_ID, Integer.toString(id));
        values.put(ItemsContract.Items.AUTHOR, author);
        values.put(ItemsContract.Items.TITLE, title);
        values.put(ItemsContract.Items.BODY, body);
        values.put(ItemsContract.Items.THUMB_URL, thumbUrl);
        values.put(ItemsContract.Items.PHOTO_URL, photoUrl);
        values.put(ItemsContract.Items.ASPECT_RATIO, Double.toString(aspectRatio));
        time.parse3339(publishDate);
        values.put(ItemsContract.Items.PUBLISHED_DATE, time.toMillis(false));
        return values;
    }
}

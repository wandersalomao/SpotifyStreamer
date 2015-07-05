package com.example.wandersalomao.spotifystreamer.artists.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author wandersalomao
 * @since 01/07/2015
 *
 * This class is a model used to store information of an artist
 */
public class SpotifyArtist implements Parcelable {

    public static final String ARTIST_KEY = "artist";

    private String spotifyId;
    private String name;
    private String thumbnailImageUrl;

    public SpotifyArtist(String spotifyId, String name, String thumbnailImageUrl) {
        this.spotifyId = spotifyId;
        this.name = name;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public String getName() {
        return name;
    }

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(spotifyId);
        dest.writeString(name);
        dest.writeString(thumbnailImageUrl);
    }

    /**
     * Retrieving SpotifyArtist data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of the object CREATOR
     */
    private SpotifyArtist(Parcel in){
        this.spotifyId = in.readString();
        this.name = in.readString();
        this.thumbnailImageUrl = in.readString();
    }

    public static final Parcelable.Creator<SpotifyArtist> CREATOR = new Parcelable.Creator<SpotifyArtist>() {

        @Override
        public SpotifyArtist createFromParcel(Parcel source) {
            return new SpotifyArtist(source);
        }

        @Override
        public SpotifyArtist[] newArray(int size) {
            return new SpotifyArtist[size];
        }
    };

}

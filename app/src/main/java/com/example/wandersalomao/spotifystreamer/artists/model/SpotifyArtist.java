package com.example.wandersalomao.spotifystreamer.artists.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class is a model used to store information of an artist
 *
 * @author wandersalomao
 * @since 01/07/2015
 *
 */
public class SpotifyArtist implements Parcelable {

    // this is a constant used as a key to pass information (e.g using Intents)
    public static final String ARTIST_KEY = "artist";

    private String spotifyId;
    private String name;
    private String thumbnailImageUrl;

    /**
     * This is the public constructor that will be used to create a new SpotifyArtist
     *
     * @param spotifyId         The spotifyId of this artist
     * @param name              The artist name
     * @param thumbnailImageUrl The artist image
     */
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
     *
     * @param in    The Parcel object which will be used to create a new SpotifyArtist
     */
    private SpotifyArtist(Parcel in){
        this.spotifyId = in.readString();
        this.name = in.readString();
        this.thumbnailImageUrl = in.readString();
    }

    /**
     * Static property used to create a new SpotifyArtist using an existing Parcel object
     */
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

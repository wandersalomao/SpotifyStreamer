package com.example.wandersalomao.spotifystreamer.tracks.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author wandersalomao
 * @since 01/07/2015
 *
 * This class is a model used to store information of a track
 */
public class SpotifyTrack implements Parcelable {

    private String trackName;
    private String albumName;
    private String thumbnailImageUrl;
    private String previewUrl;

    public SpotifyTrack(String trackName, String albumName, String thumbnailImageUrl, String previewUrl) {
        this.trackName = trackName;
        this.albumName = albumName;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.previewUrl = previewUrl;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(thumbnailImageUrl);
        dest.writeString(previewUrl);
    }

    /**
     * Retrieving SpotifyTrack data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of the object CREATOR
     */
    private SpotifyTrack(Parcel in){
        this.trackName = in.readString();
        this.albumName = in.readString();
        this.thumbnailImageUrl = in.readString();
        this.previewUrl = in.readString();
    }

    public static final Parcelable.Creator<SpotifyTrack> CREATOR = new Parcelable.Creator<SpotifyTrack>() {

        @Override
        public SpotifyTrack createFromParcel(Parcel source) {
            return new SpotifyTrack(source);
        }

        @Override
        public SpotifyTrack[] newArray(int size) {
            return new SpotifyTrack[size];
        }
    };

}

package com.example.wandersalomao.spotifystreamer.artists.model;

/**
 * This class is a model used to store information from an artist
 */
public class SpotifyArtist {

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
}

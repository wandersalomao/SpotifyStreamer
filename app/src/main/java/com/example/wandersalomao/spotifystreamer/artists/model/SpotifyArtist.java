package com.example.wandersalomao.spotifystreamer.artists.model;

/**
 * @author wandersalomao
 * @since 01/07/2015
 *
 * This class is a model used to store information of an artist
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

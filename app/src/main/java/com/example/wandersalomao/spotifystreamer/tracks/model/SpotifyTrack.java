package com.example.wandersalomao.spotifystreamer.tracks.model;

/**
 * @author wandersalomao
 * @since 01/07/2015
 *
 * This class is a model used to store information of a track
 */
public class SpotifyTrack {

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
}

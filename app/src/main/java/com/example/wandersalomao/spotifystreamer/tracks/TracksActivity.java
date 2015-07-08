package com.example.wandersalomao.spotifystreamer.tracks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;

/**
 * This is the activity class for the activity_tracks.xml layout
 */
public class TracksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            // setting the artist name as subtitle in the action bar
            Intent intent = getIntent();
            if (intent != null) {
                SpotifyArtist artist = intent.getParcelableExtra(SpotifyArtist.ARTIST_KEY);
                actionBar.setSubtitle(artist.getName());
            }
        }
    }
}

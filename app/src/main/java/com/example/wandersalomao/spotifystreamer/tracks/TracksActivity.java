package com.example.wandersalomao.spotifystreamer.tracks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;

public class TracksActivity extends AppCompatActivity {

    private final String LOG_TAG = TracksActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.logo);
            actionBar.setTitle(getString(R.string.title_activity_tracks));

            Intent intent = getIntent();
            if (intent != null) {
                SpotifyArtist artist = intent.getParcelableExtra(SpotifyArtist.ARTIST_KEY);
                actionBar.setSubtitle(artist.getName());
            }

            //if (intent != null && intent.hasExtra(Intent.EXTRA_SHORTCUT_NAME)) {
              //  actionBar.setSubtitle(intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));
            //}
        }

    }

}

package com.example.wandersalomao.spotifystreamer.tracks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.player.PlayerActivity;
import com.example.wandersalomao.spotifystreamer.tracks.model.SpotifyTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the activity class for the activity_tracks.xml layout
 */
public class TracksActivity extends AppCompatActivity implements TracksFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        // getting the artist from the intent
        SpotifyArtist artist = getIntent().getParcelableExtra(SpotifyArtist.ARTIST_KEY);

        if (savedInstanceState == null) {

            // Create the Tracks Fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(SpotifyArtist.ARTIST_KEY, artist);

            TracksFragment fragment = new TracksFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artists_top_tracks_container, fragment)
                    .commit();
        }

        // update the action bar subtitle
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && artist != null) {

            // setting the artist name as subtitle in the action bar
            actionBar.setSubtitle(artist.getName());
        }
    }

    /**
     * This is a callback method that is called when the user selects a track. This method will be
     * called only when running the application on a phone. If running on a tablet the Artist
     * activity will handle it.
     *
     * @param tracks     The list of tracks for the current artist
     * @param position   The current position of the selected track
     * @param artistName The name of the selected artist
     */
    @Override
    public void onTrackSelected(List<SpotifyTrack> tracks, int position, String artistName) {

        // create a new Intent associated to the TracksActivity
        // pass the artist with the intent so that we can use it to retrieve the top 10 tracks
        Intent intent = new Intent(this, PlayerActivity.class);

        intent.putParcelableArrayListExtra(SpotifyTrack.TRACKS_KEY, new ArrayList<Parcelable>(tracks));
        intent.putExtra(SpotifyTrack.CURRENT_TRACK_POSITION, position);
        intent.putExtra(SpotifyArtist.ARTIST_NAME_KEY, artistName);

        startActivity(intent);
    }
}

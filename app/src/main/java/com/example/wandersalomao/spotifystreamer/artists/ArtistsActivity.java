package com.example.wandersalomao.spotifystreamer.artists;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.player.PlayerActivity;
import com.example.wandersalomao.spotifystreamer.player.PlayerFragment;
import com.example.wandersalomao.spotifystreamer.tracks.TracksActivity;
import com.example.wandersalomao.spotifystreamer.tracks.TracksFragment;
import com.example.wandersalomao.spotifystreamer.tracks.model.SpotifyTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the activity class for the activity_artists.xml layout
 */
public class ArtistsActivity extends AppCompatActivity implements ArtistsFragment.Callback,
        TracksFragment.Callback {

    // this constant is used to identify the track fragment
    public static final String TOP_TRACKS_FRAGMENT_TAG = "top_tracks_fragment";

    // this is a private field used to identify if we running in two pane mode or single pane mode
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artists);

        if (findViewById(R.id.artists_top_tracks_container) != null) {

            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.artists_top_tracks_container, new TracksFragment(), TOP_TRACKS_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    /**
     * This is a callback method that is called when the user selects an artist
     *
     * @param selectedArtist The artist selected by the user
     */
    @Override
    public void onArtistSelected(SpotifyArtist selectedArtist) {

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(SpotifyArtist.ARTIST_KEY, selectedArtist);

            TracksFragment fragment = new TracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artists_top_tracks_container, fragment, TOP_TRACKS_FRAGMENT_TAG)
                    .commit();
        } else {

            // create a new Intent associated to the TracksActivity
            // pass the artist with the intent so that we can use it to retrieve the top 10 tracks
            Intent intent = new Intent(this, TracksActivity.class)
                    .putExtra(SpotifyArtist.ARTIST_KEY, selectedArtist);

            startActivity(intent);
        }
    }

    /**
     * This is a callback method that is called when the user selects a track. This method will be
     * called only when running the application on a tablet. If running on a phone the Tracks
     * activity will handle it.
     *
     * @param tracks     The list of tracks for the current artist
     * @param position   The current position of the selected track
     * @param artistName The name of the selected artist
     */
    @Override
    public void onTrackSelected(List<SpotifyTrack> tracks, int position, String artistName) {
        // In two-pane mode, show the detail view in this activity by
        // adding or replacing the detail fragment using a
        // fragment transaction.
        Bundle args = new Bundle();
        args.putParcelableArrayList(SpotifyTrack.TRACKS_KEY, new ArrayList<Parcelable>(tracks));
        args.putInt(SpotifyTrack.CURRENT_TRACK_POSITION, position);
        args.putString(SpotifyArtist.ARTIST_NAME_KEY, artistName);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment.show(fragmentManager, PlayerActivity.DIALOG_PLAYER_FRAGMENT_TAG);
    }

    /**
     * This is an util method used to indicate if the application is running on a tablet or not
     *
     * @return {boolean} indicates if the application is running on tablet or not
     */
    public boolean isTablet() {
        return mTwoPane;
    }

}

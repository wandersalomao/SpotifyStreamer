package com.example.wandersalomao.spotifystreamer.player;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.tracks.model.SpotifyTrack;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    public static final String DIALOG_PLAYER_FRAGMENT_TAG = "dialog_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // get the player fragment
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment dialogFragment = (DialogFragment) fm.findFragmentByTag(DIALOG_PLAYER_FRAGMENT_TAG);

        // if the fragment does not exist yet we create it
        if (dialogFragment == null) {

            Intent intent = getIntent();
            Bundle args = new Bundle();

            // get the data from the intent
            List<SpotifyTrack> tracks = intent.getParcelableArrayListExtra(SpotifyTrack.TRACKS_KEY);
            String currentArtistName = intent.getStringExtra(SpotifyArtist.ARTIST_NAME_KEY);
            int currentPosition = intent.getIntExtra(SpotifyTrack.CURRENT_TRACK_POSITION,0);

            // set the data to  the arguments
            args.putParcelableArrayList(SpotifyTrack.TRACKS_KEY, new ArrayList<Parcelable>(tracks));
            args.putInt(SpotifyTrack.CURRENT_TRACK_POSITION, currentPosition);
            args.putString(SpotifyArtist.ARTIST_NAME_KEY, currentArtistName);

            //Create Dialog as Fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            PlayerFragment fragment = new PlayerFragment();
            fragment.setArguments(args);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, fragment, DIALOG_PLAYER_FRAGMENT_TAG).commit();
        }
    }
}

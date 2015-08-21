package com.example.wandersalomao.spotifystreamer.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.tracks.model.SpotifyTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The player fragment. It extends from Dialog Fragment so that it can be used on phones as a fullscreen
 * view or on tables as a dialog view
 */
public class PlayerFragment extends DialogFragment {

    private TextView artistName, albumName, trackName, duration, currentTime;
    private ImageView albumImage;
    private SeekBar seekBar;
    private ImageButton previousTrack, playPauseTrack, nextTrack;

    private String currentArtistName;
    private int currentPosition;
    private List<SpotifyTrack> tracks;

    // service life cycle
    private MusicService musicSrv;
    private boolean isInit = false;
    private boolean isPaused = false;
    private boolean isBound=false;
    private Handler progressHandler;

    /**
     * This runnable field will run as soon as the song is playing and it will update
     * the progress views on the UI
     */
    private Runnable progressRunnable = new Runnable() {

        @Override
        public void run() {

            if (musicSrv != null) {
                if (musicSrv.isSongPlaying()) {

                    int progress = musicSrv.getProgress();
                    int durationTime = musicSrv.getDuration();

                    //set seekbar progress
                    seekBar.setProgress(progress);

                    //update the currentTime textView
                    currentTime.setText(getReadableTime(progress));

                    if (seekBar.getMax() != durationTime) {
                        seekBar.setMax(durationTime);
                        duration.setText(getReadableTime(durationTime));
                    }
                }

                if (musicSrv.isSongCompleted()) {
                    isInit = false;
                    playPauseTrack.setImageResource(android.R.drawable.ic_media_play);
                }
            }

            progressHandler.postDelayed(this, 100);
        }
    };

    /**
     * This is the service connection. It's used to connect and disconnect the service
     */
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;

            // get service
            musicSrv = binder.getService();
            isBound = true;

            if (!isInit) {
                // start playing a new track
                startPlayingNewTrack();
            }
            progressHandler = new Handler();
            progressHandler.post(progressRunnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // the fragment's style
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    /**
     * This method will save information of the current state of this fragment based on some events,
     * for example when the user rotates the device
     *
     * @param outState  the state object that will be used to save information of the current state
     */
    @Override
    public void onSaveInstanceState (Bundle outState) {

        outState.putString(SpotifyArtist.ARTIST_NAME_KEY, currentArtistName);
        outState.putParcelableArrayList(SpotifyTrack.TRACKS_KEY, new ArrayList<Parcelable>(tracks));
        outState.putInt(SpotifyTrack.CURRENT_TRACK_POSITION, currentPosition);
        outState.putBoolean("isBound", isBound);
        outState.putBoolean("isInit", isInit);
        outState.putBoolean("isPaused", isPaused);

        super.onSaveInstanceState(outState);
    }

    /**
     * We start and bind the service when this fragment is started
     */
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * We unbind the service and remove the callbacks when this fragment is stopped
     */
    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(musicConnection);
        isBound = false;
        progressHandler.removeCallbacks(progressRunnable);
    }

    /**
     * This method will create the view
     *
     * @param inflater           The layout inflater that will be used to inflate this view
     * @param container          The view container
     * @param savedInstanceState The savedInstanceState object containing previously saved data
     * @return the view created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        // if we don't have a previously saved state
        if(savedInstanceState == null) {

            Bundle args = getArguments();
            tracks = args.getParcelableArrayList(SpotifyTrack.TRACKS_KEY);
            currentArtistName = args.getString(SpotifyArtist.ARTIST_NAME_KEY);
            currentPosition = args.getInt(SpotifyTrack.CURRENT_TRACK_POSITION);

        } else {
            // in this case there is a previsouly saved state
            tracks = savedInstanceState.getParcelableArrayList(SpotifyTrack.TRACKS_KEY);
            currentArtistName = savedInstanceState.getString(SpotifyArtist.ARTIST_NAME_KEY);
            currentPosition = savedInstanceState.getInt(SpotifyTrack.CURRENT_TRACK_POSITION);
            isInit = savedInstanceState.getBoolean("isInit");
            isBound = savedInstanceState.getBoolean("isBound");
            isPaused = savedInstanceState.getBoolean("isPaused");
        }

        // initialize the views
        initializeViews(rootView);

        // update the screen based on the current track selected by the user
        updateViewsForCurrentTrack();

        // when the user clicks on play or pause
        playPauseTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if the song is playing we pause it
                if (musicSrv.isSongPlaying()) {
                    musicSrv.pause();
                    playPauseTrack.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    playPauseTrack.setImageResource(android.R.drawable.ic_media_pause);

                    // if the song is completed and the user clicked on play we start it again
                    if (musicSrv.isSongCompleted()) {
                        startPlayingNewTrack();
                    } else { // if the song is paused we play it
                        musicSrv.start();
                    }
                }
            }
        });

        // if the user clicked on next track
        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });

        // if the user clicked on previous track
        this.previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousSong();
            }
        });

        return rootView;
    }

    /**
     * This method is executed when the user clicks on the next button
     */
    public void playNextSong() {
        setNextSongPosition();
        updateViewsForCurrentTrack();
        startPlayingNewTrack();
    }

    /**
     * This method is executed when the user clicks on the previous button
     */
    public void playPreviousSong() {
        setPreviousSongPosition();
        updateViewsForCurrentTrack();
        startPlayingNewTrack();
    }

    /**
     * This method will start playing the current track from the beginning
     */
    public void startPlayingNewTrack() {
        musicSrv.setPreviewUrl(getCurrentTrack().getPreviewUrl());
        musicSrv.playSong();
        isInit = true;
        isPaused = false;
    }

    /**
     * This method will adjust the field currentPosition to the next song
     */
    private void setNextSongPosition() {

        if (currentPosition == tracks.size()-1) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
    }

    /**
     * This method will adjust the field currentPosition to the previous song
     */
    private void setPreviousSongPosition() {
        if (currentPosition == 0) {
            currentPosition = tracks.size()-1;
        } else {
            currentPosition--;
        }
    }

    /**
     * This method will update the UI views based on the current song being played.
     */
    public void updateViewsForCurrentTrack() {

        SpotifyTrack currentTrack = getCurrentTrack();

        artistName.setText(currentArtistName);
        albumName.setText(currentTrack.getAlbumName());
        trackName.setText(currentTrack.getTrackName());
        playPauseTrack.setImageResource(android.R.drawable.ic_media_pause);

        // if the current track has a thumbnail image we load it
        if (!currentTrack.getThumbnailImageUrl().isEmpty()) {
            // load the track image
            Picasso.with(getActivity())
                    .load(currentTrack.getThumbnailImageUrl())
                    .fit()
                    .into(albumImage);
        }
    }

    /**
     * This method will initialize the view based on their ids
     *
     * @param rootView  The root view that contains the subviews that will be initialized
     */
    private void initializeViews(View rootView){

        // initialize the views
        this.artistName = (TextView) rootView.findViewById(R.id.artistName);
        this.albumName = (TextView) rootView.findViewById(R.id.albumName);
        this.trackName = (TextView) rootView.findViewById(R.id.trackName);
        this.currentTime = (TextView) rootView.findViewById(R.id.currentTime);
        this.duration = (TextView) rootView.findViewById(R.id.duration);
        this.albumImage = (ImageView) rootView.findViewById(R.id.albumImage);
        this.seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        this.previousTrack = (ImageButton) rootView.findViewById(R.id.previousTrack);
        this.playPauseTrack = (ImageButton) rootView.findViewById(R.id.playPauseTrack);
        this.nextTrack = (ImageButton) rootView.findViewById(R.id.nextTrack);
    }

    /**
     * This method will format the give time in milliseconds to something like m:ss
     *
     * @param milliseconds  The root view that contains the subviews that will be initialized
     * @return String       The formatted time in String
     */
    private String getReadableTime(double milliseconds) {

        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes((long) milliseconds),
                TimeUnit.MILLISECONDS.toSeconds((long) milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) milliseconds))
        );
    }

    /**
     * Returns the current track based on the currentPosition field
     *
     * @return {SpotifyTrack}
     */
    private SpotifyTrack getCurrentTrack() {
        return tracks.get(currentPosition);
    }
}
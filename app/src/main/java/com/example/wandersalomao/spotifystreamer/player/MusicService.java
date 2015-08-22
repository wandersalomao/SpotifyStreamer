package com.example.wandersalomao.spotifystreamer.player;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * A service that will handle the music selected by the user
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final String LOG_TAG = MusicService.class.getSimpleName();

    private final IBinder musicBinder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private String previewUrl;
    private boolean isSongCompleted;
    private boolean isSongPlaying;
    public int progress;
    public int duration;
    private Handler progressHandler;

    // default constructor
    public MusicService() {}

    /* getters and setters */

    public int getProgress() {
        return progress;
    }

    public int getDuration() {
        return duration;
    }

    public void setPreviewUrl(String previewUrl){
        this.previewUrl = previewUrl;
    }

    public boolean isSongPlaying() {
        return isSongPlaying;
    }

    public boolean isSongCompleted(){
        return isSongCompleted;
    }

    /* bind and unbind the service */

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * Method called when the media player is ready to start playing
     *
     * @param mp    The media player
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        isSongPlaying = true;
        watchProgress();
    }

    /**
     * This method is called when a track finishes playing
     *
     * @param mp   The media player
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        isSongCompleted = true;
        isSongPlaying = false;
        progressHandler.removeCallbacks(mediaPlayerProgressRunnable);
        mediaPlayer.release();
        mediaPlayer = null;
    }

    /**
     * This method is called in case we have any errors
     *
     * @param mp    the media player
     * @param what  the error code
     * @param extra extra info
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Error on Media Player. " +
                "Please check your internet connection and try again later", Toast.LENGTH_LONG).show();
        mp.reset();
        return true;
    }

    /**
     * This method is called to start playing a new song. It will create the media player and start
     * playing the song
     */
    public void playSong(){
        progress = 0;
        isSongPlaying = false;
        isSongCompleted = false;

        // if there is a media player we release it
        if (mediaPlayer != null){
            mediaPlayer.release();
        }

        // instantiate the media player and set the audio type to be Stream
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //set the data source
        try {
            mediaPlayer.setDataSource(previewUrl);
        } catch(Exception e) {
            Log.e(LOG_TAG, "Error setting data source", e);
        }

        // set the listeners
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

        // Async method that when ready will call onPrepare()
        mediaPlayer.prepareAsync();
    }

    /**
     * This method is called to pause a song that is current playing
     */
    public void pause(){
        if (mediaPlayer!=null) {
            mediaPlayer.pause();
            isSongPlaying = false;
        }
    }

    /**
     * This method is called to start playing a song that was paused
     */
    public void start(){
        mediaPlayer.start();
        watchProgress();
        isSongPlaying = true;
    }

    /**
     * This method is called to reset the player and start playing the song again
     */
    public void reset(){
        if (mediaPlayer!=null) {
            mediaPlayer.reset();
            isSongPlaying = false;
        }
    }

    /**
     * This method is called to stop playing the current song
     */
    public void stop(){
        mediaPlayer.stop();
        isSongPlaying = false;
    }

    /**
     * This method will set the media player progress to the given progress selected by the user.
     *
     * @param progress The progress user has selected
     */
    public void seekTo(int progress){
        mediaPlayer.seekTo(progress);
    }

    /**
     * This method is called to release the service. It removes the callbacks and the media player
     */
    public void release(){
        progressHandler.removeCallbacks(mediaPlayerProgressRunnable);
        progress = 0;
        mediaPlayer.release();
        isSongPlaying = false;
    }

    /**
     * This method is called to watch the process of the song being played. It will use the
     * progressHandler to update the progress of the song
     */
    private void watchProgress() {
        if (progressHandler == null) {
            progressHandler = new Handler();
        }
        progressHandler.postDelayed(mediaPlayerProgressRunnable, 100);
    }

    /**
     * Internal class used as the service binder
     */
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    /**
     * This runnable field will update the progress of the song being played
     */
    private Runnable mediaPlayerProgressRunnable = new Runnable() {
        public void run() {

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {

                // get current position
                progress = mediaPlayer.getCurrentPosition();
                duration = mediaPlayer.getDuration();
                duration = duration > 0 ? duration : 0;

                //repeat yourself that again in 100 miliseconds
                progressHandler.postDelayed(this, 100);
            }
        }
    };

}

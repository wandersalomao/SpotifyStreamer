package com.example.wandersalomao.spotifystreamer.tracks;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.tracks.adapter.TrackAdapter;
import com.example.wandersalomao.spotifystreamer.tracks.model.SpotifyTrack;
import com.example.wandersalomao.spotifystreamer.util.ConnectivityUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class TracksFragment extends Fragment {

    private final String LOG_TAG = TracksFragment.class.getSimpleName();

    // string used as key to save the current state of this fragment
    private final String CURRENT_LIST_KEY = "currentListKey";

    // this is the adapter for this fragment
    private TrackAdapter mTrackAdapter;

    // this variable stores the current artist the user has selected
    private SpotifyArtist currentArtist;

    private ProgressBar mProgressBar;

    public TracksFragment() {}

    /**
     * This method will create the view
     *
     * @param inflater              The layout inflater that will be used to inflate this view
     * @param container             The view container
     * @param savedInstanceState    The savedInstanceState object containing previously saved data
     * @return                      the view created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.tracksProgressBar);

        // if we don' have a previously saved state
        if(savedInstanceState == null || !savedInstanceState.containsKey(CURRENT_LIST_KEY)) {

            mTrackAdapter = new TrackAdapter(getActivity(), new ArrayList<SpotifyTrack>());

            // Inspect the intent for artist id data.
            Intent intent = getActivity().getIntent();
            if (intent != null) {

                currentArtist = intent.getParcelableExtra(SpotifyArtist.ARTIST_KEY);

                if (ConnectivityUtil.isNetworkAvailable(getActivity())) {
                    mProgressBar.setVisibility(View.VISIBLE);

                    FetchTracksTask task = new FetchTracksTask();
                    task.execute(currentArtist.getSpotifyId());
                } else {
                    displayMessage(getString(R.string.error_connection));
                }

            } else {
                Log.e(LOG_TAG, "Could not find the artist id");
            }
        } else {

            // in this case there is a previsouly saved state so instead of calling the remote
            // service again we retrieve the tracks using the savedInstanceState object
            List<SpotifyTrack> list = savedInstanceState.getParcelableArrayList(CURRENT_LIST_KEY);
            currentArtist = savedInstanceState.getParcelable(SpotifyArtist.ARTIST_KEY);

            mTrackAdapter = new TrackAdapter(getActivity(), new ArrayList<SpotifyTrack>());
            mTrackAdapter.addAll(list);
        }

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listView_tracks);
        listView.setAdapter(mTrackAdapter);

        // set the emptyView template to be used when the list view is empty
        listView.setEmptyView(rootView.findViewById(R.id.empty));

        // if the current artist has a thumbnail image we load it as the background image
        if (!currentArtist.getThumbnailImageUrl().isEmpty()) {
            // load the artist image as background
            ImageView artistBackground = (ImageView) rootView.findViewById(R.id.artist_image);

            Picasso.with(getActivity())
                    .load(currentArtist.getThumbnailImageUrl())
                    .into(artistBackground);
        }

        return rootView;
    }

    /**
     * This method will save information of the current state of this fragment based on some events,
     * for example when the user rotates the device
     *
     * @param outState  the state object that will be used to save information of the current state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // we save the list of tracks and the current artist selected
        outState.putParcelableArrayList(CURRENT_LIST_KEY, new ArrayList<Parcelable>(mTrackAdapter.getTracks()));
        outState.putParcelable(SpotifyArtist.ARTIST_KEY, this.currentArtist);
        super.onSaveInstanceState(outState);
    }

    /**
     * This method will display a message to the user
     *
     * @param message   The message that will be displayed to the user
     */
    public void displayMessage(String message) {
        // show a message to the user
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.error);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(false);

        // Add the buttons
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.cancel();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * This is an internal class that will be used to download the top tracks of an artist in
     * background
     *
     * @author wsalomao
     * @since 01/07/2015
     *
     */
    public class FetchTracksTask extends AsyncTask<String, Void, List<SpotifyTrack>> {

        @Override
        protected List<SpotifyTrack> doInBackground(String... params) {

            // check if the artist id was passed as parameter
            if (params.length == 0) {
                return null;
            }

            // Spotify API
            SpotifyApi api = new SpotifyApi();

            // artist name used to filter
            final String artistId = params[0];

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String country = prefs.getString(
                    getString(R.string.pref_country_key),
                    getString(R.string.pref_country_default));

            // pass the country
            Map<String, Object> options = new HashMap<>();
            options.put("country", country);

            final List<SpotifyTrack> topTracks = new ArrayList<>();

            try {

                Tracks tracks = api.getService().getArtistTopTrack(artistId, options);

                if (!tracks.tracks.isEmpty()) {

                    for (Track track : tracks.tracks) {

                        String thumbnailUrl = "";

                        if (track.album.images.size() > 0) {
                            Image image = track.album.images.get(0);
                            thumbnailUrl = image.url;
                        }

                        topTracks.add(new SpotifyTrack(track.name,
                                track.album.name,
                                thumbnailUrl,
                                track.preview_url));
                    }
                }

            } catch (final Exception e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayMessage(getString(R.string.error_executing_api));
                    }
                });
            }

            return topTracks;
        }

        @Override
        protected void onPostExecute(List<SpotifyTrack> spotifyTracks) {
            String emptyMessage = "";

            // here we clear the adapter if no artists were returned
            if (spotifyTracks == null || spotifyTracks.isEmpty()) {

                // if not artists were found we configure the message that will be shown to the user
                emptyMessage = getString(R.string.no_tracks_found, currentArtist.getName());
                mTrackAdapter.clear();
            } else {
                mTrackAdapter.addAll(spotifyTracks);
            }

            if (mProgressBar != null)
                mProgressBar.setVisibility(View.GONE);

            if (getView() != null) {
                TextView emptyView = (TextView) getView().findViewById(R.id.empty_text_message);
                emptyView.setText(emptyMessage);
            } else {
                Log.w(LOG_TAG, "Was not able to retrieve the empty view layout");
            }

        }
    }

}

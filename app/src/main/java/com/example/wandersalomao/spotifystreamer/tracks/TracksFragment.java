package com.example.wandersalomao.spotifystreamer.tracks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.tracks.adapter.TrackAdapter;
import com.example.wandersalomao.spotifystreamer.tracks.model.SpotifyTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class TracksFragment extends Fragment {

    private final String LOG_TAG = TracksFragment.class.getSimpleName();
    private final String CURRENT_LIST_KEY = "currentListKey";

    private TrackAdapter mTrackAdapter;
    private SpotifyArtist currentArtist;

    public TracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);

        if(savedInstanceState == null || !savedInstanceState.containsKey(CURRENT_LIST_KEY)) {

            mTrackAdapter = new TrackAdapter(getActivity(), new ArrayList<SpotifyTrack>());

            // Inspect the intent for artist id data.
            Intent intent = getActivity().getIntent();
            if (intent != null) {

                currentArtist = intent.getParcelableExtra(SpotifyArtist.ARTIST_KEY);

                FetchTracksTask task = new FetchTracksTask();
                task.execute(currentArtist.getSpotifyId());

            } else {
                Log.e(LOG_TAG, "Could not find the artist id");
            }
        } else {
            List<SpotifyTrack> list = savedInstanceState.getParcelableArrayList(CURRENT_LIST_KEY);
            currentArtist = (SpotifyArtist) savedInstanceState.getParcelable(SpotifyArtist.ARTIST_KEY);

            mTrackAdapter = new TrackAdapter(getActivity(), new ArrayList<SpotifyTrack>());
            mTrackAdapter.addAll(list);
        }

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listView_tracks);
        listView.setAdapter(mTrackAdapter);

        // load the artist image as background
        ImageView artistBackground = (ImageView) rootView.findViewById(R.id.artist_image);

        Picasso.with(getActivity())
                .load(currentArtist.getThumbnailImageUrl())
                .into(artistBackground);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(CURRENT_LIST_KEY, new ArrayList<Parcelable>(mTrackAdapter.getTracks()));
        outState.putParcelable(SpotifyArtist.ARTIST_KEY, this.currentArtist);
        super.onSaveInstanceState(outState);
    }

    /**
     * @author wsalomao
     * @since 01/07/2015
     *
     * This is an internal class that will be used to download the tracks of an artist in
     * background
     */
    public class FetchTracksTask extends AsyncTask<String, Void, List<SpotifyTrack>> {

        private final String LOG_TAG = FetchTracksTask.class.getSimpleName();

        @Override
        protected List<SpotifyTrack> doInBackground(String... params) {

            // check if the artist id was passed as parameter
            if (params.length == 0) {
                return null;
            }

            final List<SpotifyTrack> listOfTracks = new ArrayList<>();

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

            api.getService().getArtistTopTrack(artistId, options, new Callback<Tracks>() {

                @Override
                public void success(Tracks tracks, Response response) {

                    for (Track track : tracks.tracks) {

                        String thumbnailUrl = "";

                        if (track.album.images.size() > 0) {
                            Image image = track.album.images.get(0);
                            thumbnailUrl = image.url;
                        }

                        listOfTracks.add(new SpotifyTrack(track.name,
                                track.album.name,
                                thumbnailUrl,
                                track.preview_url));
                    }

                    mTrackAdapter.clear();
                    mTrackAdapter.addAll(listOfTracks);

                }

                @Override
                public void failure(RetrofitError error) {

                    Log.e(LOG_TAG, error.getMessage());
                }

            });

            return listOfTracks;
        }

        @Override
        protected void onPostExecute(List<SpotifyTrack> tracks) {
            mTrackAdapter.clear();
            mTrackAdapter.addAll(tracks);
        }
    }

}

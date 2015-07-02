package com.example.wandersalomao.spotifystreamer.artists;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.adapter.ArtistAdapter;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.tracks.TracksActivity;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistsFragment extends Fragment {

    private final String LOG_TAG = ArtistsFragment.class.getSimpleName();

    private ArtistAdapter mArtistAdapter;

    public ArtistsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mArtistAdapter = new ArtistAdapter(getActivity(), new ArrayList<SpotifyArtist>());

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listView_artists);
        listView.setAdapter(mArtistAdapter);

        // adding the listener to call the Tracks Activity when the user clicks on an artist
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // get the artist
                SpotifyArtist artist = mArtistAdapter.getItem(position);

                // create a new Intent associated to the TracksActivity
                // pass the artist id with the intent so that we can use it to retrieve the top 10 tracks
                Intent intent = new Intent(getActivity(), TracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist.getSpotifyId());
                startActivity(intent);
            }
        });

        EditText inputSearch = (EditText) rootView.findViewById(R.id.inputSearch);

        /**
         * Enabling Search Filter
         * */
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                FetchArtistsTask task = new FetchArtistsTask();
                task.execute(cs.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        return rootView;
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, List<SpotifyArtist>> {

        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected List<SpotifyArtist> doInBackground(String... params) {

            // check if the artist name was passed as parameter
            if (params.length == 0) {
                return null;
            }

            // artist name used to filter
            final String artistName = params[0];

            final List<SpotifyArtist> artists = new ArrayList<>();

            // Spotify API
            SpotifyApi api = new SpotifyApi();

            // search artists using the given name
            api.getService().searchArtists(artistName, new Callback<ArtistsPager>() {

                @Override
                public void success(ArtistsPager artistsPager, Response response) {

                    // if no artists are found we shoud a message to the user
                    if (artistsPager.artists.items.size() <= 0) {

                        String message = getString(R.string.no_artists_found, artistName);
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                    } else {
                        for (Artist artist : artistsPager.artists.items) {

                            // the Spotify API says that the image field returns
                            // images of the artist in various sizes, widest first.
                            // for a better performance we load the smallest
                            // which means the last one in this array
                            String thumbnailUrl = "";

                            if (artist.images.size() > 0) {
                                Image image = artist.images.get(artist.images.size()-1);
                                thumbnailUrl = image.url;
                            }

                            artists.add(new SpotifyArtist(artist.id, artist.name, thumbnailUrl));
                        }

                        mArtistAdapter.clear();
                        mArtistAdapter.addAll(artists);

                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(LOG_TAG, error.getMessage());
                }
            });

            return artists;
        }

        @Override
        protected void onPostExecute(List<SpotifyArtist> spotifyArtists) {

            if (spotifyArtists == null || spotifyArtists.isEmpty()) {
                mArtistAdapter.clear();
            }

        }
    }

}

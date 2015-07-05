package com.example.wandersalomao.spotifystreamer.artists;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.Settings.SettingsActivity;
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
public class ArtistsFragment extends Fragment implements SearchView.OnQueryTextListener {

    private final String LOG_TAG = ArtistsFragment.class.getSimpleName();

    private ArtistAdapter mArtistAdapter;
    private SearchView mSearchView;

    public ArtistsFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this.getActivity(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // this will enable the Options Menus to appear on the Action Bar
        setHasOptionsMenu(true);

        mArtistAdapter = new ArtistAdapter(getActivity(), new ArrayList<SpotifyArtist>());

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listView_artists);
        listView.setAdapter(mArtistAdapter);
        listView.setEmptyView(rootView.findViewById(R.id.empty));

        // adding the listener to call the Tracks Activity when the user clicks on an artist
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // get the artist
                SpotifyArtist artist = mArtistAdapter.getItem(position);

                // create a new Intent associated to the TracksActivity
                // pass the artist id with the intent so that we can use it to retrieve the top 10 tracks
                Intent intent = new Intent(getActivity(), TracksActivity.class)
                        .putExtra(SpotifyArtist.ARTIST_KEY, artist);

                startActivity(intent);
            }
        });

        //EditText inputSearch = (EditText) rootView.findViewById(R.id.inputSearch);

        /**
         * Enabling Search Filter
         * */
/*
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
                Log.v(LOG_TAG, "Test");
            }
        });

*/
        return rootView;
    }

    public boolean onQueryTextChange(String newText) {
        FetchArtistsTask task = new FetchArtistsTask();
        task.execute(newText);

        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        // this will hide the keyboard
        mSearchView.clearFocus();
        return true;
    }

    public boolean onClose() {
        return false;
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, List<SpotifyArtist>> {

        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected List<SpotifyArtist> doInBackground(String... params) {

            // check if the artist name was passed as parameter
            if (params.length == 0 || params[0].isEmpty()) {
                return null;
            }

            // artist name used to filter
            final String artistName = params[0];

            final List<SpotifyArtist> artists = new ArrayList<>();

            final TextView emptyView = (TextView) getView().findViewById(R.id.empty_text_message);

            // Spotify API
            SpotifyApi api = new SpotifyApi();

            // search artists using the given name
            api.getService().searchArtists(artistName, new Callback<ArtistsPager>() {

                @Override
                public void success(ArtistsPager artistsPager, Response response) {

                    // if no artists are found we shoud a message to the user
                    if (artistsPager.artists.items.size() <= 0) {

                        emptyView.setText(getString(R.string.no_artists_found, artistName));

                        //String message = getString(R.string.no_artists_found, artistName);
                        //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                    } else {
                        emptyView.setText("");

                        for (Artist artist : artistsPager.artists.items) {

                            // the Spotify API says that the image field returns
                            // images of the artist in various sizes, widest first.
                            // for a better performance we load the smallest
                            // which means the last one in this array
                            String thumbnailUrl = "";

                            if (artist.images.size() > 0) {
                                //Image image = artist.images.get(artist.images.size()-1);
                                Image image = artist.images.get(0);
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

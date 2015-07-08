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
 * A placeholder fragment containing a simple view. This fragment also implements the
 * SearchView.OnQueryTextListener interface that will be used to handle the searchView widget
 */
public class ArtistsFragment extends Fragment implements SearchView.OnQueryTextListener {

    private ArtistAdapter mArtistAdapter;
    private SearchView mSearchView;

    /* Public constructor */
    public ArtistsFragment() {}

    /**
     *  Inflate the menu; this adds items to the action bar if it is present.
     *
     * @param menu      The current menu
     * @param inflater  The MenuInfraler used to inflate the menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);

        // initializing the search view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        mSearchView.setOnQueryTextListener(this);
    }

    /**
     * Handle action bar item clicks here. The action bar will automatically handle clicks on the
     * Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
     *
     * @param item  The menu item selected
     * @return      boolean value
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // if user has selected the settings option we start the Settings Activity
        if (id == R.id.action_settings) {
            startActivity(new Intent(this.getActivity(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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

        // this will enable the Options Menus to appear on the Action Bar
        setHasOptionsMenu(true);

        mArtistAdapter = new ArtistAdapter(getActivity(), new ArrayList<SpotifyArtist>());

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listView_artists);
        listView.setAdapter(mArtistAdapter);

        // set the emptyView template to be used when the list view is empty
        listView.setEmptyView(rootView.findViewById(R.id.empty));

        // adding the listener to call the Tracks Activity when the user clicks on an artist
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // get the artist
                SpotifyArtist artist = mArtistAdapter.getItem(position);

                // create a new Intent associated to the TracksActivity
                // pass the artist with the intent so that we can use it to retrieve the top 10 tracks
                Intent intent = new Intent(getActivity(), TracksActivity.class)
                        .putExtra(SpotifyArtist.ARTIST_KEY, artist);

                startActivity(intent);
            }
        });

        return rootView;
    }

    /**
     * This method is called every time a user types on the searchView widget. We're searching for
     * artists using this method so that we can show partial results to the user
     *
     * @param newText   The text entered by the user
     * @return          a boolean value
     */
    public boolean onQueryTextChange(String newText) {
        // every time the user changes the text we execute the search
        FetchArtistsTask task = new FetchArtistsTask();
        task.execute(newText);

        return true;
    }

    /**
     * This method will be called when the user clicks on the search button on his keyboard to
     * submit the search.
     *
     * @param query The text user has typed
     * @return      a boolean value
     */
    public boolean onQueryTextSubmit(String query) {
        // remove the focus from the searchView widget so that the keyboard will be hidden
        mSearchView.clearFocus();
        return true;
    }

    /**
     * This class will asynchronously search for artists based on the text entered by the user
     *
     * @author wandersalomao
     */
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

            //final TextView emptyView = (TextView) getView().findViewById(R.id.empty_text_message);

            // Spotify API
            SpotifyApi api = new SpotifyApi();

            // search artists using the given name
            api.getService().searchArtists(artistName, new Callback<ArtistsPager>() {

                @Override
                public void success(ArtistsPager artistsPager, Response response) {

                    String emptyMessage = "";

                    // if artists were found
                    if (artistsPager.artists.items.size() > 0) {

                        // for each artist we create a SpotifyArtist object
                        for (Artist artist : artistsPager.artists.items) {

                            String thumbnailUrl = "";

                            if (artist.images.size() > 0) {
                                Image image = artist.images.get(0);
                                thumbnailUrl = image.url;
                            }

                            artists.add(new SpotifyArtist(artist.id, artist.name, thumbnailUrl));
                        }

                        mArtistAdapter.clear();
                        mArtistAdapter.addAll(artists);

                    } else {
                        // if not artists were found we configure the message that will be shown to the user
                        emptyMessage = getString(R.string.no_artists_found, artistName);
                    }

                    if (getView() != null) {
                        TextView emptyView = (TextView) getView().findViewById(R.id.empty_text_message);
                        emptyView.setText(emptyMessage);
                    } else {
                        Log.w(LOG_TAG, "Was not able to retrieve the empty view layout");
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

            // here we clear the adapter if no artists were returned
            if (spotifyArtists == null || spotifyArtists.isEmpty()) {
                mArtistAdapter.clear();
            }

        }
    }

}

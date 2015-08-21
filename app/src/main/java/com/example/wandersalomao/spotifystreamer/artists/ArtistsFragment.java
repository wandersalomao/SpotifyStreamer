package com.example.wandersalomao.spotifystreamer.artists;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.Settings.SettingsActivity;
import com.example.wandersalomao.spotifystreamer.artists.adapter.ArtistAdapter;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.util.ConnectivityUtil;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

/**
 * A placeholder fragment containing a simple view. This fragment also implements the
 * SearchView.OnQueryTextListener interface that will be used to handle the searchView widget
 */
public class ArtistsFragment extends Fragment implements SearchView.OnQueryTextListener {

    // string used as key to save the current state of this fragment
    private final String CURRENT_LIST_KEY = "currentListKey";

    private ArtistAdapter mArtistAdapter;
    private SearchView mSearchView;
    private ProgressBar mProgressBar;

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

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        // this will enable the Options Menus to appear on the Action Bar
        setHasOptionsMenu(true);

        // initially the adapter contains en empty list
        mArtistAdapter = new ArtistAdapter(getActivity(), new ArrayList<SpotifyArtist>());

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.artistsProgressBar);

        // if we have a previously saved state
        if(savedInstanceState != null && savedInstanceState.containsKey(CURRENT_LIST_KEY)) {
            List<SpotifyArtist> list = savedInstanceState.getParcelableArrayList(CURRENT_LIST_KEY);
            mArtistAdapter.addAll(list);
        }

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listView_artists);
        listView.setAdapter(mArtistAdapter);

        // set the emptyView template to be used when the list view is empty
        listView.setEmptyView(rootView.findViewById(R.id.empty));

        // adding the listener to call the Tracks Activity when the user clicks on an artist
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // get the selected artist and call the callback method
                SpotifyArtist selectedArtist = mArtistAdapter.getItem(position);
                Callback callback = (Callback) getActivity();

                callback.onArtistSelected(selectedArtist);
            }
        });

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
        // we save the list of artists
        outState.putParcelableArrayList(CURRENT_LIST_KEY, new ArrayList<Parcelable>(mArtistAdapter.getArtists()));
        super.onSaveInstanceState(outState);
    }

    /**
     * This method is called every time a user types on the searchView widget. We're searching for
     * artists using this method so that we can show partial results to the user
     *
     * @param newText   The text entered by the user
     * @return          a boolean value
     */
    public boolean onQueryTextChange(String newText) {

        if (ConnectivityUtil.isNetworkAvailable(getActivity())) {

            // clean the top tracks if running on tablets
            ArtistsActivity activity = (ArtistsActivity)getActivity();
            if (activity.isTablet()) {
                activity.onArtistSelected(null);
            }

            // every time the user changes the text we execute the search
            mProgressBar.setVisibility(View.VISIBLE);
            FetchArtistsTask task = new FetchArtistsTask();
            task.execute(newText);

        } else {

            displayMessage(getString(R.string.error_connection));
        }

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
     * This class will asynchronously search for artists based on the text entered by the user
     *
     * @author wandersalomao
     */
    public class FetchArtistsTask extends AsyncTask<String, Void, List<SpotifyArtist>> {

        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        private String mArtistName; // used to filter artists

        @Override
        protected List<SpotifyArtist> doInBackground(String... params) {

            // check if the artist name was passed as parameter
            if (params.length == 0 || params[0].isEmpty()) {
                return null;
            }

            // artist name used to filter
            final String artistName = params[0];
            final List<SpotifyArtist> artists = new ArrayList<>();

            mArtistName = artistName;

            // Spotify API
            SpotifyApi api = new SpotifyApi();

            try {

                // get the list of artists
                ArtistsPager artistsPager = api.getService().searchArtists(artistName);

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
                }

            } catch (final Exception e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayMessage(getString(R.string.error_executing_api));
                    }
                });
            }

            return artists;
        }

        @Override
        protected void onPostExecute(List<SpotifyArtist> spotifyArtists) {

            String emptyMessage = "";

            // here we clear the adapter if no artists were returned
            if (spotifyArtists == null || spotifyArtists.isEmpty()) {

                // if not artists were found we configure the message that will be shown to the user
                emptyMessage = getString(R.string.no_artists_found, mArtistName);
                mArtistAdapter.clear();
            } else {
                mArtistAdapter.addAll(spotifyArtists);
            }

            if (mProgressBar != null)
                mProgressBar.setVisibility(View.GONE);

            if (getView() != null && mArtistName != null) {
                TextView emptyView = (TextView) getView().findViewById(R.id.empty_text_message);
                emptyView.setText(emptyMessage);
            } else {
                Log.w(LOG_TAG, "Was not able to retrieve the empty view layout");
            }

        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {

        /**
         * ArtistsFragmentCallback for when an item has been selected.
         */
        public void onArtistSelected(SpotifyArtist selectedArtist);
    }


}

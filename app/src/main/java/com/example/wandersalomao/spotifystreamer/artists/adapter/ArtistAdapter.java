package com.example.wandersalomao.spotifystreamer.artists.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.artists.model.SpotifyArtist;
import com.example.wandersalomao.spotifystreamer.picasso.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ArtistAdapter extends ArrayAdapter<SpotifyArtist> {

    private static final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context        The current context. Used to inflate the layout file.
     * @param spotifyArtists A List of spotifyArtists objects to display in a list
     */
    public ArtistAdapter(Activity context, List<SpotifyArtist> spotifyArtists) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, spotifyArtists);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Gets the AndroidFlavor object from the ArrayAdapter at the appropriate position
        SpotifyArtist artist = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }

        if (!artist.getThumbnailImageUrl().isEmpty()) {
            ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.list_item_icon);


            Picasso.with(getContext())
                    .load(artist.getThumbnailImageUrl())
                    .resize(60, 60)
                    .centerCrop()
                    .transform(new RoundedTransformation(30,0))
                    .into(thumbnailView);

            /*Picasso.with(getContext())
                    .load(artist.getThumbnailImageUrl())
                    .resize(50, 50)
                    .centerCrop()
                    .into(thumbnailView);*/
        }

        TextView artistNameView = (TextView) convertView.findViewById(R.id.list_item_artist_name);
        artistNameView.setText(artist.getName());

        return convertView;
    }
}

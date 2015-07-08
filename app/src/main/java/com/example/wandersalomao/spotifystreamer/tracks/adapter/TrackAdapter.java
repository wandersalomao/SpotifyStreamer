package com.example.wandersalomao.spotifystreamer.tracks.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wandersalomao.spotifystreamer.R;
import com.example.wandersalomao.spotifystreamer.tracks.model.SpotifyTrack;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * This class is the adapter used to create listview items using the SpotifyTrack object
 */
public class TrackAdapter extends ArrayAdapter<SpotifyTrack> {

    // internal property used to store the list of tracks of a given artist
    private List<SpotifyTrack> tracks;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context        The current context. Used to inflate the layout file.
     * @param spotifyTracks  A List of spotifyTracks objects to display in a list
     */
    public TrackAdapter(Activity context, List<SpotifyTrack> spotifyTracks) {

        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, spotifyTracks);
        this.tracks = spotifyTracks;
    }

    public List<SpotifyTrack> getTracks() {
        return this.tracks;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Gets the SpotifyTrack object from the ArrayAdapter at the appropriate position
        SpotifyTrack track = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
        }

        // if the track contains a thumbnail image we set the icon view using Picasso
        if (!track.getThumbnailImageUrl().isEmpty()) {
            ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.list_item_icon);

            Picasso.with(getContext())
                    .load(track.getThumbnailImageUrl())
                    .resize(80, 80)
                    .centerCrop()
                    .into(thumbnailView);
        }

        // set the track name
        TextView trackNameView = (TextView) convertView.findViewById(R.id.list_item_track_name);
        trackNameView.setText(track.getTrackName());

        // set the album name
        TextView albumNameView = (TextView) convertView.findViewById(R.id.list_item_album_name);
        albumNameView.setText(track.getAlbumName());

        return convertView;
    }
}

package femi.com.ng.core.activities;

/**
 * Created by femi on 22/08/2014.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import femi.com.ng.core.adapters.GridAdapter;
import femi.com.ng.kure.musicplayer.activities.ActivityListSongs;
import femi.com.ng.kure.musicplayer.activities.ActivityMaster;
import femi.com.ng.kure.musicplayer.kMP;
import femi.com.ng.kure.musicplayer.model.Song;
import femi.com.ng.R;

import java.util.List;


/**
 * Shows a menu with all the albums of all the artists
 * on SongList, allowing the user to choose one of
 * them and going to a specific artist menu.
 *
 */
public class ArtistActivity extends ActivityMaster
        implements AdapterView.OnItemClickListener {

    /**
     * All the possible items the user can select on this menu.
     *
     * Will be initialized with default values on `onCreate`.
     */
    public static List<Song> items;

    /**
     * List that will be populated with all the items.
     *
     * Look for it inside the res/layout xml files.
     */
    GridView gView;

    public String category= GridAdapter.ARTIST;

    /**
     * Called when the activity is created for the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.orimi_grid_view_layout);
        // If we got an extra with a title, we'll apply it
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();


        // This enables the "Up" button on the top Action Bar
        // Note that it returns to the parent Activity, specified
        // on `AndroidManifest`
        com.actionbarsherlock.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // List to be populated with items
        gView = (GridView)findViewById(R.id.grid_view);

        items = kMP.songs.getAllArtists();
       // if(category.equals(GENRE)) items = kMP.songs.getAllGenres();

        // Adapter that will convert from Strings to List Items
       // final ArrayAdapter<String> adapter = new ArrayAdapter<String>
              //  (this, android.R.layout.simple_list_item_1, items);
        final GridAdapter adapter=new GridAdapter(this, items, category);
        // Filling teh list with all the items
        gView.setAdapter(adapter);

        gView.setOnItemClickListener(this);
        gView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                //SongAdapter a=(SongAdapter)g.getAdapter();
                if (scrollState != 0) {
                    adapter.isScrolling = true;
                }
                else {
                    adapter.isScrolling = false;
                    adapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {

            }
        });
    }

    /**
     * Will react to the user selecting an item.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // We can only handle the user choice from now on
        // if we've successfully scanned the songs from the
        // device.
        if (! kMP.songs.isInitialized())
            return;

        Song selectedAlbumSong = items.get(position);

        kMP.musicList = kMP.songs.getSongsByAlbum(selectedAlbumSong.getAlbum());

        Intent intent = new Intent(this, ActivityListSongs.class);

        intent.putExtra("title", selectedAlbumSong.getAlbum());

        startActivity(intent);
    }

    /**
     * When destroying the Activity.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Need to clear all the items otherwise
        // they'll keep adding up.
        items.clear();
    }

}


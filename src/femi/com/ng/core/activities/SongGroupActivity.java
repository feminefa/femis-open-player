package femi.com.ng.core.activities;

/**
 * Created by femi on 22/08/2014.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import femi.com.ng.core.adapters.GridAdapter;
import femi.com.ng.kure.musicplayer.activities.ActivityMaster;
import femi.com.ng.kure.musicplayer.kMP;
import femi.com.ng.kure.musicplayer.model.Song;
import femi.com.ng.R;
import com.actionbarsherlock.widget.SearchView;

import java.util.ArrayList;
import java.util.List;


/**
 * Shows a menu with all the albums of all the artists
 * on SongList, allowing the user to choose one of
 * them and going to a specific artist menu.
 *
 */
public class SongGroupActivity extends ActivityMaster
        implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {


    //List<Song> tmp;
    /**
     * All the possible items the user can select on this menu.
     *
     * Will be initialized with default values on `onCreate`.
     */
    public List<Song> items;
    GridAdapter adapter;

    /**
     * List that will be populated with all the items.
     *
     * Look for it inside the res/layout xml files.
     */
    GridView gView;
    List<Song> tmp=new ArrayList<Song>();
    public String category=GridAdapter.ALBUM;
    String filterBy;
    String value;

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

        if (bundle != null) {

            String t=(String) bundle.get("type");
            if(t!=null && t.length()>0) {
                category=t;
                this.setTitle(t);
            }
            String title=(String) bundle.get("title");
            if(title!=null && title.length()>0) {
                this.setTitle(title);
            }
            if(bundle.get("filter_by")!=null) {
                filterBy=(String)bundle.get("filter_by");
            }
            if(bundle.get("value")!=null) {
                value=(String)bundle.get("value");
            }
        }
        // This enables the "Up" button on the top Action Bar
        // Note that it returns to the parent Activity, specified
        // on `AndroidManifest`
        com.actionbarsherlock.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // List to be populated with items
        gView = (GridView)findViewById(R.id.grid_view);
        items=new ArrayList<Song>();
        items=reset();
      // tmp=items;
        // Adapter that will convert from Strings to List Items
       // final ArrayAdapter<String> adapter = new ArrayAdapter<String>
              //  (this, android.R.layout.simple_list_item_1, items);
        if(filterBy!=null) {
            if(filterBy.equals(GridAdapter.ARTIST)) {
                items = copy(kMP.songs.getSongAlbumsByArtist(value));
            }
        }else{
            if(category.equals(GridAdapter.ARTIST)) {
                items= copy(kMP.songs.getAllArtists());
            }
            if(category.equals(GridAdapter.ALBUM)) {
               items= copy(kMP.songs.getAllAlbums());
            }
            if(category.equals(GridAdapter.GENRE)) {
                items= copy(kMP.songs.getAllGenres());
            }


        }
        adapter = new GridAdapter(this, items, category);
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
        GridAdapter ga=(GridAdapter)gView.getAdapter();
        Song selectedSong = ga.getData().get(position);
        if(category.equals(GridAdapter.ALBUM)) {

            Intent intent = new Intent(this, SongsActivity.class);

            intent.putExtra("title", getString(R.string.album)+": "+selectedSong.getAlbum());
           // intent.putExtra("album_id", selectedSong.getAlbumId());
            intent.putExtra("filter_by",GridAdapter.ALBUM);
            intent.putExtra("value", ""+selectedSong.getAlbumId());
            startActivity(intent);
            return;
        }
        if(category.equals(GridAdapter.ARTIST)) {

            Intent intent = new Intent(this, SongSubGroupActivity.class);
            intent.putExtra("type", GridAdapter.ALBUM);
            intent.putExtra("title", getString(R.string.artist)+": "+selectedSong.getArtist());
           // intent.putExtra("artist", selectedSong.getArtist());
            intent.putExtra("filter_by",GridAdapter.ARTIST);
            intent.putExtra("value", selectedSong.getArtist());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        if(category.equals(GridAdapter.GENRE)) {
            Intent intent = new Intent(this, SongsActivity.class);

            intent.putExtra("title",getString(R.string.genre)+": "+selectedSong.getGenre());
            intent.putExtra("filter_by",GridAdapter.GENRE);
            intent.putExtra("value", selectedSong.getGenre());

            startActivity(intent);
            return;
        }




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
    public void doNothing(View v) {
        return;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.orimi_search_menu, menu);
        SearchView searchView = (com.actionbarsherlock.widget.SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(this);
       // searchView.setIconified(false);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // if (getParentActivityIntent() == null) {
                onBackPressed();
                //} else {
                //  NavUtils.navigateUpFromSameTask(this);
                // }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
       if(tmp==null) tmp=new ArrayList<Song>();
        else tmp.clear();
        String lookIn="";
       // tmp=copy(items);
       // items.clear();

        //.write(category+" "+query+" "+size);
        for(Song s:items) {
            //Song s=tmp.get(i);
            String search="";
            if(category.equals(GridAdapter.ALBUM)) {
                search=s.getAlbum();
            }
            if(category.equals(GridAdapter.ARTIST)) {
               search=s.getArtist();
            }
            if(category.equals(GridAdapter.GENRE)) {
               search=s.getGenre();
            }
            if(search.toLowerCase().contains(query.toLowerCase())) {
                tmp.add(s);
            }
        }
        //adapter.notifyDataSetChanged();
       gView.setAdapter( new GridAdapter(this, tmp, category));
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //mAdapter.getFilter().filter(newText.trim());
        if(newText.trim().length()==0) {
           //items=reset();
            gView.setAdapter( new GridAdapter(this, items, category));
        }else onQueryTextSubmit(newText);
        return false;
    }
    private List<Song> copy(List<Song> from) {
        int size=from.size();
        List<Song> tos=new ArrayList<Song>(size);
        //Logger.write(""+size);
        for(int i=0; i<size; i++) {
            tos.add(from.get(i));
        }
        return tos;
    }
    private List<Song> reset() {
        if(category.equals(GridAdapter.ALBUM)) {
            //items = kMP.songs.getAllAlbums();
            return copy(kMP.songs.getAllAlbums());
        }
        if(category.equals(GridAdapter.ARTIST)) {
            return  copy(kMP.songs.getAllArtists());
        }
        if(category.equals(GridAdapter.GENRE)) {
            return copy(kMP.songs.getAllGenres());
        }
        return new ArrayList<Song>();
    }

}


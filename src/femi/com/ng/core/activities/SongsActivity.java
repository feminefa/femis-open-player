package femi.com.ng.core.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import femi.com.ng.core.adapters.GridAdapter;
import femi.com.ng.core.adapters.SongAdapter;
import femi.com.ng.kure.musicplayer.activities.ActivityMaster;
import femi.com.ng.kure.musicplayer.kMP;
import femi.com.ng.kure.musicplayer.model.Playlist;
import femi.com.ng.kure.musicplayer.model.Song;
import femi.com.ng.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Shows a predefined list of songs, letting the user select
 * them to play.
 *
 * @note This class is a mess because, to decide which songs to
 *       display, it uses the member `kMP.musicList`.
 */
public class SongsActivity extends ActivityMaster implements OnItemClickListener, OnItemLongClickListener, SearchView.OnQueryTextListener {

    /**
     * List of songs that will be shown to the user.
     */
    private ListView songListView;
    private AlertDialog myDialog;
    private  List<Song> items=new ArrayList<Song>();
    boolean showSearch;

   // Long albumId=null;
    String filterBy;
    String value;
    public String category=null;
    SongAdapter songAdapter;
    boolean isFirstLoad;
    @Override
    protected void onCreate(Bundle popcorn) {
        super.onCreate(popcorn);
        isFirstLoad=true;
        setContentView(R.layout.activity_list_songs);

        // Let's fill ourselves with all the songs
        // available on the device.
        songListView = (ListView)findViewById(R.id.activity_list_songs_list);

        // We'll get warned when the user clicks on an item.
        songListView.setOnItemClickListener(this);

        // If we got an extra with a title, we'll apply it
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
           if(bundle.get("title")!=null) this.setTitle((String) bundle.get("title"));
            if(bundle.get("showSearch")!=null) {
                showSearch=(Boolean)bundle.get("showSearch");
            }
            if(bundle.get("filter_by")!=null) {
                filterBy=(String)bundle.get("filter_by");
            }
            if(bundle.get("value")!=null) {
                value=(String)bundle.get("value");
            }
            if(bundle.get("type")!=null) {
                category=(String)bundle.get("type");
            }
        }

        // Connects the song list to an adapter
        // (thing that creates several Layouts from the song list)
        if(filterBy!=null) {
            if(filterBy.equals(GridAdapter.ALBUM)) {
                items = copy(kMP.songs.getSongsByAlbumId(Long.parseLong(value)));

            }
            if(filterBy.equals(GridAdapter.GENRE)){
              ////  Logger.write(value);
               // Logger.write(""+kMP.songs.getSongsByGenre(value).size());
                items = copy(kMP.songs.getSongsByGenre(value));
            }
            if(filterBy.equals(GridAdapter.PLAYLIST)){
                ////  Logger.write(value);
                // Logger.write(""+kMP.songs.getSongsByGenre(value).size());
                items = copy(kMP.songs.getSongsByPlaylist(value));
            }
        }else {
           /// items = copy(kMP.songs.getSongsByAlbumId(albumId));
            if(category!=null && category.equals(GridAdapter.PLAYLIST)) {
                items=copy(kMP.songs.getPlaylistAsSongs());
            }else {
                if ((kMP.musicList != null) && (kMP.musicList.size() != 0)) {
                    items = copy(kMP.musicList);
                }
            }
        }

        /**
         * Set action to add new if we r on the playlist
         */
        if(category!=null && category.equals(GridAdapter.PLAYLIST)) {
            ((TextView)findViewById(R.id.songlist_action_top_text)).setText(R.string.new_playlist);
            ((ImageView)findViewById(R.id.songlist_action_top_button)).setImageResource(R.drawable.ic_add);
        }
       songAdapter = new SongAdapter(SongsActivity.this, items, category);
        //For smooth scroll, do not load album art when scrolling
        //songAdapter.isScrolling = false;
        songListView.setOnScrollListener(songAdapter.getOnScrollListener());
        songListView.setAdapter(songAdapter);
        // This enables the "Up" button on the top Action Bar
        // Note that it returns to the parent Activity, specified
        // on `AndroidManifest`
        LinearLayout playAllBut = (LinearLayout) findViewById(R.id.play_all_button);


             playAllBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(songAdapter.getData().size()==0) {
                        return;
                    }
                    if (category!=null && category.equals(GridAdapter.PLAYLIST)) {
                        showAddPlayListDialog(null);
                        return;
                    }
                    kMP.nowPlayingList = (ArrayList<Song>) songAdapter.getData();
                    kMP.musicService.setList(kMP.nowPlayingList);
                    kMP.musicService.currentSongPosition=0;
                    kMP.musicService.playSong();
                    MainActivity.updateView(true);

                }
            });

        if(items.size()==0) {
            if(category== null) playAllBut.setVisibility(View.GONE);

                TextView tv = new TextView(this);
                tv.setText("Empty Collection");
                tv.setPadding(20, 20, 20, 20);
                tv.setTextAppearance(this.getApplicationContext(), R.style.boldWhiteText);
                addContentView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // If we press and hold on a Song, let's add to the current
        // playing queue.
        songListView.setOnItemLongClickListener(this);


    }

    /**
     * When the user selects an item from our list, we'll start playing.
     *
     * We'll play the current list, starting from the song the user
     * just selected.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // We'll play the current song list
       // kMP.startMusicService(this);
        if(category!=null && category.equals(GridAdapter.PLAYLIST)) {
            SongAdapter sa=(SongAdapter)songListView.getAdapter();
            Song selectedSong = sa.getData().get(position);

            Intent intent = new Intent(this, SubSongActivity.class);

            intent.putExtra("title", getString(R.string.playlist)+": "+selectedSong.getPlayList().getName());
           // intent.putExtra("type", GridAdapter.);
            // intent.putExtra("album_id", selectedSong.getAlbumId());
            intent.putExtra("filter_by", GridAdapter.PLAYLIST);
            intent.putExtra("value", ""+selectedSong.getPlayList().getName());
            startActivity(intent);
            return;
        }
        showOptions(position);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // if (getParentActivityIntent() == null) {
                onBackPressed();
                // } else {
                // NavUtils.navigateUpFromSameTask(this);
                //  }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    protected void onStart() {
        super.onStart();

        kMP.startMusicService(this);
    }

    private void showOptions(final int songId) {

        myDialog = new AlertDialog.Builder(this).create();
        //myDialog.setTitle("Select...");

        final ListView listview=new ListView(this);
        listview.setBackgroundColor(getResources().getColor(R.color.black));
       final TextView editText = new TextView(this);
        editText.setTextAppearance(this, R.style.boldWhiteText);
        editText.setPadding(10, 10, 5, 10);
        //editText.setTextSize(18);
        // editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.discoverseed_larg1, 0, 0, 0);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText);
        layout.addView(listview);

       // editText.setPadding(0, 10, 0, 10);
        layout.setBackgroundColor(getResources().getColor(R.color.orange));
       editText.setText(kMP.musicList.get(songId).getTitle());

        List<String> data  = new ArrayList<String>();
        data.add(getString(R.string.play_only));
        data.add(getString(R.string.add_to_now_playing));
        data.add(getString(R.string.add_to_playlist));
        if(filterBy!=null && filterBy.equals(GridAdapter.PLAYLIST)) {
            data.add(getString(R.string.remove_from_playlist));
        }
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        listview.setAdapter(adapter);
        myDialog.setView(layout);
        myDialog.setButton( DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
         listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int index, long arg3) {
                    // TODO Auto-generated method stub
                        TextView tv=(TextView)arg1;
                        SongAdapter sa=(SongAdapter)songListView.getAdapter();

                    if(tv.getText().equals(getString(R.string.play_only))) {
                        ArrayList<Song> t=new ArrayList<Song>();
                        //kMP.nowPlayingList = (ArrayList<Song>) items;
                       t.add(sa.getData().get(songId));
                        kMP.nowPlayingList = t;
                        kMP.musicService.setList(kMP.nowPlayingList);
                        //  kMP.startMusicService(getApplicationContext());
                        kMP.musicService
                                .setSong(0);
                        kMP.musicService.playSong();
                    }
                    if(tv.getText().equals(getString(R.string.add_to_now_playing))) {
                        if( kMP.nowPlayingList==null) {
                            kMP.nowPlayingList=new ArrayList<Song>();
                        }
                        kMP.nowPlayingList.add(sa.getData().get(songId));
                        kMP.musicService.setList(kMP.nowPlayingList);
                        Toast.makeText(SongsActivity.this,
                                getString(R.string.song_added_to_now_playing),
                                Toast.LENGTH_SHORT).show();
                        //  kMP.startMusicService(getApplicationContext());
                      //  kMP.musicService
                            //    .setSong(0);
                        //kMP.musicService.playSong();
                    }
                    if(tv.getText().equals(getString(R.string.add_to_playlist))) {
                        ArrayList<Song> s= new ArrayList<Song>();
                        s.add(songAdapter.getData().get(songId));
                        myDialog.dismiss();
                        showAddPlayListDialog(s);
                    }
                    if(tv.getText().equals(getString(R.string.remove_from_playlist))) {
                        //ArrayList<Song> s= new ArrayList<Song>();
                        //s.add(songAdapter.getData().get(songId));
                        Song s=songAdapter.getData().get(songId);
                        s.setPlayList(kMP.songs.getPlayListFromName(value));
                        kMP.songs.removeFromPlayList(SongsActivity.this, s, "external");
                        songAdapter.setData(kMP.songs.getSongsByPlaylist(value));
                        songAdapter.notifyDataSetChanged();
                        myDialog.dismiss();

                       // showAddPlayListDialog(s);
                    }
                    MainActivity.updateView( true);
                        myDialog.dismiss();
                    }
            });

        myDialog.show();

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.orimi_search_menu, menu);
        com.actionbarsherlock.widget.SearchView searchView = (com.actionbarsherlock.widget.SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(this);
        if(showSearch) {
            searchView.setIconified(false);
        }
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        List<Song> tmp=new ArrayList<Song>();//copy(kMP.musicList);
        String lookIn="";

       // items.clear();

        //.write(category+" "+query+" "+size);
        for(Song s:items) {
            if(category==null)   lookIn=s.getTitle();
            else {
                if(category.equals(GridAdapter.PLAYLIST)) lookIn = s.getPlayList().getName();
            }
            if(lookIn.toLowerCase().contains(query.toLowerCase())) {
                tmp.add(s);
            }
        }
        //((SongAdapter)songListView.getAdapter()).notifyDataSetChanged();
        songListView.setAdapter( new SongAdapter(this, tmp, category));
        return true;
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
    @Override
    public boolean onQueryTextChange(String newText) {
        //mAdapter.getFilter().filter(newText.trim());
        if(newText.trim().length()==0) {
           // items=copy(kMP.musicList);
            songListView.setAdapter(new SongAdapter(this, items, category));
        }else onQueryTextSubmit(newText);
        return false;
    }
    public void showAddPlayListDialog(final ArrayList<Song> songsToAdd) {
       final AlertDialog plDialog= new AlertDialog.Builder(this).create();
        //myDialog.setTitle("Select...");
        View v=getLayoutInflater().inflate(R.layout.orimi_playlist_dialog, null);
        TextView titleTV=(TextView)v.findViewById(R.id.playlist_diolog_title);



        final ListView listview=(ListView)v.findViewById(R.id.playlist_listview);
        listview.setBackgroundColor(getResources().getColor(R.color.black));
        final EditText editText = (EditText)v.findViewById(R.id.playlist_title_edittext);

        listview.setFastScrollEnabled(true);
       // listview.setSmoothScrollbarEnabled(true);
       // listview.setScrollBarDefaultDelayBeforeFade(10000);


        if(songsToAdd==null) {
            titleTV.setText("Create New Play List");
            listview.setVisibility(View.GONE);
        }
        else{
            titleTV.setText("Select a Play List");
        }

        Button createPlayListButton=(Button)v.findViewById(R.id.playlist_add_button);

       // b.setText("Add +");
        //editText.setTextSize(18);
        // editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.discoverseed_larg1, 0, 0, 0);


        // editText.setPadding(0, 10, 0, 10);
      //  layout.setBackgroundColor(getResources().getColor(R.color.orange));
       // editText.setText(kMP.musicList.get(songId).getTitle());
        final List<Song> songs= kMP.songs.getPlaylistAsSongs();
        final ArrayList<String> data = new ArrayList<String>();//String[songs.size()];//{getString(R.string.play_only), getString(R.string.add_to_now_playing),getString(R.string.add_to_playlist)};

        for(Song song:songs) {
            data.add(song.getPlayList().getName());
        }
        createPlayListButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final String text =editText.getText().toString();
                if(text.trim().length()==0) {
                    MainActivity.createAlertDialog(SongsActivity.this,"Error", "You must provide a play list title").show();
                    return;
                }
                for(String name:data) {
                    if(name.toLowerCase().equals(text.toLowerCase())) {
                        MainActivity.createAlertDialog(SongsActivity.this,"Error", "A play list with the same title already exists").show();
                        return;
                    }
                }
                ArrayList<Song> add=new ArrayList<Song>();
               // if(songsToAdd==null) add=new ArrayList<Song>();
                //else  add=songsToAdd;
                kMP.songs.newPlaylist(SongsActivity.this.getApplicationContext(), "external", text, add);
              //  data.add(text);
                if(songsToAdd==null) {
                   /* items=copy(kMP.songs.getPlaylistAsSongs());
                    songAdapter.setData(items);
                    songAdapter.notifyDataSetChanged();*/
                    plDialog.dismiss();
                    MainActivity.playLists=kMP.songs.getPlaylistAsSongs(true);
                    items= copy(MainActivity.playLists);
                    songAdapter.setData(items);
                    songAdapter.notifyDataSetChanged();
                }else{
                    editText.setText("");
                    hideKeyBoard(editText);
                    List<Song> songz= kMP.songs.getPlaylistAsSongs(true);
                    data.clear();// = new ArrayList<String>();//String[songs.size()];//{getString(R.string.play_only), getString(R.string.add_to_now_playing),getString(R.string.add_to_playlist)};
                    for(Song song:songz) {
                        data.add(song.getPlayList().getName());
                    }
                    ArrayAdapter<String> adapter= new ArrayAdapter<String>(SongsActivity.this, android.R.layout.simple_list_item_1, data) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView tv=(TextView)super.getView(position, convertView, parent);
                            tv.setBackgroundColor(getResources().getColor(R.color.black));
                            if(data.get(position).equals(text)) {
                                //tv.setTextAppearance(SongsActivity.this, R.style.boldWhiteText);
                                tv.setBackgroundColor(getResources().getColor(R.color.orange_transparent));
                                return  tv;
                            }
                            return tv;
                        }
                    };
                    listview.setAdapter(adapter);
                    listview.setSelection(adapter.getCount() - 1);
                }

             }
        });
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        listview.setAdapter(adapter);
        plDialog.setView(v);
        plDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {
                // TODO Auto-generated method stub
                Song s= MainActivity.playLists.get(arg2);
               // Logger.write(songsToAdd.get(0).getTitle());
                kMP.songs.addSongToPlaylist(SongsActivity.this, arg2, songsToAdd.get(0), "external");
                Toast.makeText(SongsActivity.this,
                        getString(R.string.song_added_to_playlist),
                        Toast.LENGTH_SHORT).show();
                plDialog.dismiss();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideKeyBoard(editText);
            }
        }, 200);

        plDialog.show();

    }
    private  void hideKeyBoard(View v) {
        v.clearFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {
        if(category!=null && category.equals(GridAdapter.PLAYLIST)) {
         // SimpleListDialog sld =null;
            final Playlist playlist=songAdapter.getData().get(i).getPlayList();
            String title=getString(R.string.playlist)+" :: "+playlist.getName();
            List<String> options = new ArrayList<String>();
            options.add(getString(R.string.delete)) ;
            options.add(getString(R.string.clear));
            OnItemClickListener oic=new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String selected=((TextView)view).getText().toString();
                    if(selected.equals(getString(R.string.delete))) {
                       kMP.songs.deletePlayList(SongsActivity.this, "external", playlist);
                       // kMP.songs.deletePlayList(SongsActivity.this, "external", playlist.getID());
                    }
                    if(selected.equals(getString(R.string.clear))) {
                       // kMP.songs.deletePlayList(SongsActivity.this, "external", playlist);
                        //kMP.songs.newPlaylist(SongsActivity.this, "external", playlist.getName(), new ArrayList<Song>());
                        kMP.songs.clearPlayList(SongsActivity.this, "external", playlist);
                    }
                    myDialog.dismiss();

                    MainActivity.playLists=kMP.songs.getPlaylistAsSongs(true);
                    //Logger.write(""+MainActivity.playLists.size());
                    items= copy(MainActivity.playLists);
                   // songListView.setAdapter(new SongAdapter(SongsActivity.this, items, GridAdapter.PLAYLIST));
                   // SongAdapter sa=(SongAdapter)((ListView)view).set
                   songAdapter.setData(items);
                   songAdapter.notifyDataSetChanged();

                }
            };
            //myDialog=
            myDialog=SimpleListDialog.getInstance(this, title, options, oic);
            myDialog.show();
            return true ;
        }
        return false;
    }
   static class SimpleListDialog {

        public SimpleListDialog() {
            //super(getApplicationContext());

        }
        public static AlertDialog getInstance(Context c, String title, List<String> items, OnItemClickListener oic) {

            final TextView editText = new TextView(c);
            editText.setTextAppearance(c, R.style.boldWhiteText);
            editText.setPadding(10, 10, 5, 10);
            editText.setText(title);

            final ListView listview=new ListView(c) ;
            listview.setBackgroundColor(c.getResources().getColor(R.color.black));


            ArrayAdapter<String> aa=new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, items) ;
            listview.setAdapter(aa);
            listview.setOnItemClickListener(oic);

            LinearLayout layout = new LinearLayout(c);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(c.getResources().getColor(R.color.orange));
            layout.addView(editText);
            layout.addView(listview);
            AlertDialog alertDialog=new AlertDialog.Builder(c).create();
            alertDialog.setView(layout);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            return alertDialog;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(category!=null && category.equals(GridAdapter.PLAYLIST) && !isFirstLoad) {
            songAdapter.setData(kMP.songs.getPlaylistAsSongs(true));
            songAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstLoad=false;
    }

}

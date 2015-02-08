package femi.com.ng.core.activities;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;

import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
//import com.facebook.AppEventsLogger;
import org.json.JSONArray;
import org.json.JSONObject;


import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import femi.com.ng.core.adapters.GridAdapter;
import femi.com.ng.kure.musicplayer.model.Song;
import femi.com.ng.kure.musicplayer.services.ServicePlayMusic;
import femi.com.ng.R;
import femi.com.ng.core.adapters.SongAdapter;
import femi.com.ng.core.libs.Orin;
import femi.com.ng.kure.musicplayer.NotificationMusic;
import femi.com.ng.kure.musicplayer.kMP;
import femi.com.ng.kure.musicplayer.activities.ActivityMaster;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;

public class MainActivity   extends ActivityMaster implements ViewPager.OnPageChangeListener, SeekBar.OnSeekBarChangeListener {
    public DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    // public static final ArrayList<String> items = new ArrayList<String>();

    public static boolean LoginFirstTime = true;

    public static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public static final String AMAZON_URL = "http://yaply.com.ng:8082/amazon.php";
    static final char[] APP_KEY = "ssfsdfsdfiwo4ow03".toCharArray();
    public static final String MY_PREF = "my_open_player_preferences";
    public static final String MY_TAGS = "my_tags", REPEAT = "repeat", SHUFFLE = "shuffle", LAST_PLAYED_SONG = "last_played_song", NOW_PLAYING_SONGS = "now_playing", LAST_HOME_VIEW = "last_home_view";

    public static int MAX_TAGS_IN_HISTORY = 5;
    public static SharedPreferences orimiPreferences;
    public static String USERNAME = "orimi_username", USERBALANCE = "orimi_balance", EMAIL = "orimi_email", KEY = "orimi_key";
    public static ListView sliderListView;
    private static ViewPager viewPager;
    public static List<Song> nowPlayingList = new ArrayList<Song>();
    public static Orin lastSongPlayed = null, currentSongPlaying = null;
    public static ListView nowPlayingLV;
    public static LruCache<String, Bitmap> memoryCache;
    public static ImageView iView;
    public static List<Song> albums = new ArrayList<Song>();
    public static List<Song> artists = new ArrayList<Song>();
    public static List<Song> genres = new ArrayList<Song>(), playLists = new ArrayList<Song>();
    public static SeekBar progressBar;
    private static Timer timer = null;
    private BroadcastReceiver broadcastReceiver;
    TextView durationTextView, positionTextView, textView, userNameField, userEmailField, userBalanceField;
    ImageView playButton, nextButton, prevButton, repeatBut, shuffleBut, playListBut;
    static SherlockFragmentActivity mainActivity;
    static String FBAID = "1523505251230623";
    public static boolean checkedForUpdate = false;
    ScrollView homeView;

    //DIRECTORIES
    public static File DIR_ROOT = new File("FemisOpenPLayer"), DIR_CHAT_HISTORY = new File("chat_history"),
            DIR_CACHE = new File("cache"), DIR_HTML_CACHE = new File("html"), DIR_DOWNLOADS = new File("downloads"), DIR_MEDIA = new File("media");

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mainActivity = this;
       /* try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "femi.com.ng",              PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Logger.write("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/

        /**
         * Check if app update is available from the app online version file fop.org/versions.txt
         */
        UpdateAppActivity.checkForUpdate(this, "http://fop.org/versions.txt");
        /**
         * Disables the permanent action menu button and enables the more button on the action bar
         * Thanks to a miracle worker on stackoverflow
         */
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            // presumably, not relevant
        }
        broadcastReceiver = new StartedPlaying();
        String rootPath = DIR_ROOT.getPath();

        orimiPreferences = getSharedPreferences(MainActivity.MY_PREF, MODE_PRIVATE);


        DIR_CACHE = new File(getCacheDir().getAbsolutePath() + File.separator + DIR_CACHE.getName());
        DIR_CACHE.mkdirs();
        if (isSDMounted(true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            DIR_HTML_CACHE = new File(getExternalCacheDir().getAbsolutePath() + File.separator + DIR_HTML_CACHE.getName());
        } else {
            DIR_HTML_CACHE = new File(getCacheDir().getAbsolutePath() + File.separator + DIR_HTML_CACHE.getName());
        }
        DIR_HTML_CACHE.mkdirs();
        DIR_DOWNLOADS = new File(rootPath + File.separator + DIR_DOWNLOADS.getName());
        DIR_DOWNLOADS.mkdirs();
        DIR_CHAT_HISTORY = new File(rootPath + File.separator + DIR_CHAT_HISTORY.getName());
        DIR_CHAT_HISTORY.mkdirs();
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };

        //super.onCreate(seventhSonOfASeventhSon);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // GENERAL PROGRAM INFO
        kMP.applicationName = "Orimi Player";
        kMP.packageName = "<unknown>";
        kMP.versionName = "<unknown>";
        kMP.versionCode = -1;
        kMP.firstInstalledTime = -1;
        kMP.lastUpdatedTime = -1;

        //load the settings
        kMP.settings.load(this);
        // if (kMP.mainMenuHasNowPlayingItem) {
        // items.add(getString(R.string.menu_main_now_playing));
        // }
        // Initializing the main program logic.
        kMP.initialize(this);
        scanSongs(false);

        setContentView(R.layout.orimi_activity_main);
        setTitle("");
        userEmailField = (TextView) findViewById(R.id.user_email_tv);
        userNameField = (TextView) findViewById(R.id.user_name_tv);
        userBalanceField = (TextView) findViewById(R.id.user_balance_tv);
        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);


        //mDrawerList = (ListView) findViewById(R.id.left_drawer_list_view);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // getActionBar().setTitle(mTitle);
                // invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // if(mDrawerLayout!=null) {
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // }

        durationTextView = (TextView) findViewById(R.id.home_current_song_duration);
        positionTextView = (TextView) findViewById(R.id.home_current_song_position);
        playButton = (ImageView) findViewById(R.id.home_play_but);
        nextButton = (ImageView) findViewById(R.id.home_next_but);
        prevButton = (ImageView) findViewById(R.id.home_prev_but);
        repeatBut = (ImageView) findViewById(R.id.home_repeat_but);
        shuffleBut = (ImageView) findViewById(R.id.home_shuffle_but);
        playListBut = (ImageView) findViewById(R.id.home_play_list_but);

        playButton.setOnClickListener(playButtonAction("play"));
        nextButton.setOnClickListener(playButtonAction("next"));
        prevButton.setOnClickListener(playButtonAction("prev"));
        repeatBut.setOnClickListener(playButtonAction("repeat"));
        shuffleBut.setOnClickListener(playButtonAction("shuffle"));
        playListBut.setOnClickListener(playButtonAction("togglePlayList"));


        textView = (TextView) findViewById(R.id.home_now_play_title);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView.setSingleLine(true);
        // startSlidingAnimation(textView, 20000);
        textView.setMarqueeRepeatLimit(50);
        textView.setSelected(true);

        progressBar = (SeekBar) findViewById(R.id.home_progress_bar);
        //progressBar.setIndeterminate(false);
        // findViewById(android.R.drawable.ic_)
        progressBar.setOnSeekBarChangeListener(this);
        homeView = (ScrollView) getLayoutInflater().inflate(R.layout.orimi_home_view_layout, null);



        sliderListView = (ListView) findViewById(R.id.left_drawer_list_view);

        String[] optionStrings = new String[]{
               getString(R.string.all_songs), getString(R.string.menu_music_albums), getString(R.string.menu_music_playlists),
               getString(R.string.menu_music_artists), getString(R.string.menu_music_genres),
               getString(R.string.music_store)

        };
        final Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/DeckerB.TTF");
        // TextView tv = (TextView) findViewById(R.id.CustomFontText);
        //tv.setTypeface(tf);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, optionStrings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // TODO Auto-generated method stub
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTextAppearance(this.getContext(), R.style.boldBlackText);
                v.setTextSize(22f);
                v.setTextColor(getResources().getColor(R.color.black));
                v.setTypeface(tf);

                return v;
            }
        };

        sliderListView.setAdapter(adapter);

        sliderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Intent nowPlayingIntent;
                TextView tv = (TextView) arg1;
                if (tv.getText().equals(getString(R.string.all_songs))) {
                    kMP.musicList = kMP.songs.songs;
                    //Logger.write(""+kMP.songs.songs.size());
                    startActivity(new Intent(MainActivity.this, SongsActivity.class));
                    return;
                }
                if (tv.getText().equals(getString(R.string.menu_music_albums))) {
                    if (kMP.musicList == null || kMP.musicList.size() == 0) {
                        kMP.musicList = kMP.songs.songs;
                    }
                    //Logger.write(""+kMP.songs.songs.size());

                    nowPlayingIntent = new Intent(MainActivity.this, SongGroupActivity.class);
                    nowPlayingIntent.putExtra("type", GridAdapter.ALBUM);
                    nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    MainActivity.this.startActivity(nowPlayingIntent);
                }
                if (tv.getText().equals(getString(R.string.menu_music_artists))) {
                    if (kMP.musicList == null || kMP.musicList.size() == 0) {
                        kMP.musicList = kMP.songs.songs;
                    }

                    nowPlayingIntent = new Intent(MainActivity.this, SongGroupActivity.class);
                    nowPlayingIntent.putExtra("type", GridAdapter.ARTIST);
                    //  nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(nowPlayingIntent);
                    return;
                }
                if (tv.getText().equals(getString(R.string.menu_music_genres))) {
                    if (kMP.musicList == null || kMP.musicList.size() == 0) {
                        kMP.musicList = kMP.songs.songs;
                    }

                    Intent genreIntent = new Intent(MainActivity.this, SongGroupActivity.class);
                    genreIntent.putExtra("type", GridAdapter.GENRE);
                    // genreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(genreIntent);
                    return;
                }
                if (tv.getText().equals(getString(R.string.menu_music_playlists))) {
                    if (kMP.musicList == null || kMP.musicList.size() == 0) {
                        kMP.musicList = kMP.songs.songs;
                    }
                    //Logger.write(""+kMP.songs.songs.size());
                    Intent playlistIntent = new Intent(MainActivity.this, SongsActivity.class);
                    playlistIntent.putExtra("type", GridAdapter.PLAYLIST);
                    playlistIntent.putExtra("title", GridAdapter.PLAYLIST);
                    // playlistIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(playlistIntent);
                    return;
                }
                if (tv.getText().equals(getString(R.string.music_store))) {
                    Intent storeIntent = new Intent(MainActivity.this, AmazonActivity.class);
                    startActivity(storeIntent);
                    return;
                }

            }

        });

        //xupdatePlayModeButtons();

				 /* populate now playing list view from shared pref */
        nowPlayingList = new ArrayList<Song>();
        try {
            currentSongPlaying = new Orin(new JSONObject(orimiPreferences.getString(LAST_PLAYED_SONG, null)));
        } catch (Exception jse) {

        }

        //initialize the view pager
        viewPager = (ViewPager) findViewById(R.id.pager);
        //set the page listener that triggers the floating button states
        viewPager.setOnPageChangeListener(this);
        nowPlayingLV = new ListView(getApplicationContext());
        SongAdapter sa = new SongAdapter(MainActivity.this, kMP.nowPlayingList, "");
        nowPlayingLV.setAdapter(sa);

        nowPlayingLV.setOnScrollListener(sa.getOnScrollListener());


        iView = new ImageView(getApplicationContext());


        iView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        List<View> l = new ArrayList<View>();
        l.add(nowPlayingLV);
        l.add(iView);
        nowPlayingLV.setFastScrollEnabled(true);
        nowPlayingLV.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        //  kMP.musicService.setList(kMP.nowPlayingList);

                        kMP.musicService.setSong(arg2);
                        kMP.musicService.playSong();
                        // kMP.musicService.toggleRepeat();
                        //kMP.musicService.notifyCurrentSong();
                    }
                }
        );

        viewPager.setAdapter(new HomeViewPagerAdapter(this, l));
        //set the viewpager to the album art page if nowPlayingList is empty
        if(kMP.nowPlayingList==null || kMP.nowPlayingList.size()==0) {
            viewPager.setCurrentItem(1, true);
        }
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub
                //photoNum.setText((arg0+1)+"/"+bitmapHolders.size());
                if (arg0 == 0) {
                    playListBut.setImageResource(R.drawable.ic_list_on);
                } else playListBut.setImageResource(R.drawable.ic_list_off);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#E67300")));
        //mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        return super.onCreateView(name, context, attrs);
    }

    public void showHideDrawer(View v) {
        // android.view.MenuItem menuItem=(android.view.MenuItem)findViewById(android.R.id.home);
        mDrawerLayout.openDrawer(8388611);
        //mDrawerLayout.openDrawer((View)menuItem);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(Gravity.CENTER);
    }

    void scanSongs(boolean forceScan) {

        // Loading all the songs from the device on a different thread.
        // We'll only actually do it if they weren't loaded already
        //
        // See the implementation right at the end of this class.
        if ((forceScan) || (!kMP.songs.isInitialized())) {

            Toast.makeText(this,
                    getString(R.string.menu_main_scanning),
                    Toast.LENGTH_LONG).show();

            new ScanSongs().execute();
        }
    }

    class ScanSongs extends AsyncTask<String, Integer, String> {

        /**
         * The action we'll do in the background.
         */
        @Override
        protected String doInBackground(String... params) {

            try {
                // Will scan all songs on the device
                kMP.songs.scanSongs(MainActivity.this, "external");
                while (kMP.musicService == null) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {

                    }
                }
                return MainActivity.this.getString(R.string.menu_main_scanning_ok);
            } catch (Exception e) {
                Log.e("Couldn't execute background task", e.toString());
                e.printStackTrace();
                return MainActivity.this.getString(R.string.menu_main_scanning_not_ok);
            }

        }

        /**
         * Called once the background processing is done.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            updateNowPlaying();
            if (orimiPreferences.getBoolean(SHUFFLE, false)) {
                try {
                    kMP.musicService.toggleShuffle();

                } catch (NullPointerException npe2) {
                }
            }
            updatePlayModeButtons();
           /* if(orimiPreferences.getBoolean(REPEAT, false)) {
                try {
                   // kMP.musicService.toggleRepeat();
                    kMP.settings.set("repeat_list", true);
                }catch (NullPointerException npe) {

                }
            }else{
                kMP.settings.set("repeat_list", false);
            }*/

            Toast.makeText(MainActivity.this,
                    result,
                    Toast.LENGTH_LONG).show();
        }

        @SuppressLint("NewApi")
        public void execute() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                execute("");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    void updateNowPlaying() {
        String nowPlayingString = orimiPreferences.getString(NOW_PLAYING_SONGS, null);
        if (nowPlayingString != null) {
            //Logger.write(nowPlayingString);
            try {
                JSONArray ja = new JSONArray(nowPlayingString);
                for (int sp = 0; sp < ja.length(); sp++) {
                    JSONObject jo = new JSONObject(ja.getString(sp));
                    Song orin = kMP.songs.getSongById(jo.getInt("id"));//new Orin(jo.getInt("id"), jo.getString("path"));
                    orin.setAlbumId(jo.getInt("album_id"));
                    //Logger.write(orin.getTitle());
                    nowPlayingList.add(orin);
                }
                //  SongAdapter sa=new SongAdapter(this, nowPlayingList, "");
                //nowPlayingLV.setAdapter(sa);

                kMP.nowPlayingList = (ArrayList<Song>) nowPlayingList;
                kMP.musicService.setList(kMP.nowPlayingList);
            } catch (Exception e) {

            }
            updateView(true);
        }
    }

    public void doNothing(View v) {
        return;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        //AppEventsLogger.deactivateApp(this, FBAID);
        // stopService(intent);
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    static public boolean isSDMounted(boolean requireWriteAccess) {
        //TODO: After fix the bug,  add "if (VERBOSE)" before logging errors.
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        String directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }

    public static void vibrate(Context c) {
        vibrate(c, 100);
    }

    public static void vibrate(Context c, long duration) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c);
        long[] pattern = {duration, duration};
        mBuilder.setVibrate(pattern);

       /*if(ring) {
    	  // mBuilder.setStyle(new NotificationCompat.InboxStyle());
         Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(alarmSound == null){
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if(alarmSound == null){
                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }

        mBuilder.setSound(alarmSound);
       }*/


        final Notification note = mBuilder.build();

        //note.setLatestEventInfo(this, title, msg, contentIntent);
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Activity.NOTIFICATION_SERVICE);

        notificationManager.notify(Integer.parseInt("" + Math.round(Math.random() * 10000)), note);
    }

    public static AlertDialog.Builder createAlertDialog(Context c, String title, String message) {
        return new AlertDialog.Builder(c).setTitle(title).setMessage(message).setPositiveButton(c.getString(R.string.ok), null);

    }

    class HomeViewPagerAdapter extends PagerAdapter {

        private SherlockFragmentActivity activity;
        private List<View> views;
        private LayoutInflater inflater;

        //  private ImageView btnDelete;
        // constructor
        public HomeViewPagerAdapter(SherlockFragmentActivity a, List<View> bitmaps) {
            this.activity = a;
            this.views = bitmaps;
        }

        @Override
        public int getCount() {
            return this.views.size();
        }


        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (object);
        }

        @SuppressWarnings("unused")
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            //ViewHolder holder = new ViewHolder();
            LayoutInflater li = activity.getLayoutInflater();
            View vv = li.inflate(R.layout.orimi_player_home_view, null);
            LinearLayout ll = (LinearLayout) vv.findViewById(R.id.play_list_holder);
            View v = views.get(position);

            if (v instanceof ListView) {
                //  if (kMP.nowPlayingList != null) Logger.write("not playing " + kMP.nowPlayingList.size());
                ListView lv = (ListView) v;
                ll.setVerticalGravity(Gravity.TOP);
                ((SongAdapter) lv.getAdapter()).notifyDataSetChanged();
                ll.addView(lv);
                ((ViewPager) container).addView(ll);

            }
            if (v instanceof ImageView) {
                ImageView iv;
                //if(v==null) {
                //iv = (ImageView)vv.findViewById(R.id.home_album_art);
                //}else{
                iv = (ImageView) v;
                //}
                iv.setPadding(50, 50, 50, 50);
                iv.setScaleType(ScaleType.FIT_CENTER);
                if (currentSongPlaying == null) {
                    iv.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.big_logo));
                }
                ll.addView(iv);
                ((ViewPager) container).addView(ll);
            }

            return ll;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((RelativeLayout) object);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar


        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.orimi_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Bundle b = new Bundle();
        Intent nowPlayingIntent;// = new Intent(this, SongGroupActivity.class);
        switch (item.getItemId()) {
            case android.R.id.home:
                // mDrawerLayout.openDrawer(Gravity.START);
                if (mDrawerLayout.isDrawerOpen(Gravity.START)) mDrawerLayout.closeDrawer(Gravity.START);
                else mDrawerLayout.openDrawer(Gravity.START);
                return true;
            case R.id.action_search:
                kMP.musicList = kMP.songs.songs;
                Intent i = new Intent(MainActivity.this, SongsActivity.class);
                i.putExtra("showSearch", true);
                //  i.putExtra("type", GridAdapter.);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(i);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
           /* case R.id.action_songs:
                kMP.musicList = kMP.songs.songs;
                //Logger.write(""+kMP.songs.songs.size());
                startActivity(new Intent(MainActivity.this, SongsActivity.class));
                return true;
            case R.id.action_albums:
                if (kMP.musicList == null || kMP.musicList.size() == 0) {
                    kMP.musicList = kMP.songs.songs;
                }
                //Logger.write(""+kMP.songs.songs.size());

                nowPlayingIntent = new Intent(this, SongGroupActivity.class);
                nowPlayingIntent.putExtra("type", GridAdapter.ALBUM);
                nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                MainActivity.this.startActivity(nowPlayingIntent);
                return true;
            case R.id.action_artists:
                if (kMP.musicList == null || kMP.musicList.size() == 0) {
                    kMP.musicList = kMP.songs.songs;
                }

                nowPlayingIntent = new Intent(this, SongGroupActivity.class);
                nowPlayingIntent.putExtra("type", GridAdapter.ARTIST);
                //  nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(nowPlayingIntent);
                return true;
            case R.id.action_genre:
                if (kMP.musicList == null || kMP.musicList.size() == 0) {
                    kMP.musicList = kMP.songs.songs;
                }

                Intent genreIntent = new Intent(this, SongGroupActivity.class);
                genreIntent.putExtra("type", GridAdapter.GENRE);
                // genreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(genreIntent);
                return true;
            case R.id.action_play_list:
                if (kMP.musicList == null || kMP.musicList.size() == 0) {
                    kMP.musicList = kMP.songs.songs;
                }
                //Logger.write(""+kMP.songs.songs.size());
                Intent playlistIntent = new Intent(this, SongsActivity.class);
                playlistIntent.putExtra("type", GridAdapter.PLAYLIST);
                playlistIntent.putExtra("title", GridAdapter.PLAYLIST);
                // playlistIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(playlistIntent);
                return true;
                */
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Bitmap addBitmapToMemoryCache(String key, Bitmap bitmap) {
        Bitmap imagebm = getBitmapFromMemCache(key);
        if (imagebm == null) {
            memoryCache.put(key, bitmap);
            return bitmap;
        } else return imagebm;
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    protected void onStart() {
        super.onStart();

        kMP.startMusicService(this);
    }

    /*
        @Override
        protected void onStop() {
            super.onStop();
            kMP.stopMusicService(this);
        }
    */
    @Override
    protected void onStop() {
        super.onStop();
        //  unregisterReceiver(broadcastReceiver);
        //items.clear();

        // Cancell all thrown Notifications
        // NotificationMusic.cancelAll(this);

        // kMP.stopMusicService(this);
        //  kMP.stopMusicService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if(kMP.musicService==null) {
        kMP.startMusicService(this);
        // }
        userEmailField.setText(MainActivity.orimiPreferences.getString(MainActivity.EMAIL, "Click to add account"));
        userNameField.setText(MainActivity.orimiPreferences.getString(MainActivity.USERNAME, "No account"));
        userBalanceField.setText(MainActivity.orimiPreferences.getString(MainActivity.USERBALANCE, ""));
        try {
            registerReceiver(broadcastReceiver, new IntentFilter(ServicePlayMusic.BROADCAST_ACTION));
            if (kMP.musicService.isPlaying()) {
                //Intent intent = new Intent(ServicePlayMusic.BROADCAST_EXTRA_PLAYING);
                kMP.musicService.broadcastState(ServicePlayMusic.BROADCAST_EXTRA_PLAYING);
            }
            if (kMP.musicService.isPaused()) {
                kMP.musicService.broadcastState(ServicePlayMusic.BROADCAST_EXTRA_PAUSED);
            }
            updatePlayModeButtons();
        } catch (Exception e) {

        }
        //AppEventsLogger.activateApp(this, FBAID);
        // AppEventsLogger.activateApp(this);
        //registerReceiver(broadcastReceiver, new IntentFilter(ServicePlayMusic.BROADCAST_ACTION));


        //updateView(this);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (isFinishing()) {
            //  Logger.write("FEMI");
            try {
                NotificationMusic.cancelAll(this);
                kMP.musicService.pausePlayer();//.stopMusicPlayer();
                kMP.stopMusicService(this);
                kMP.destroy();

            } catch (Exception e) {
            }
        }
        // kMP.stopMusicService(this);
        //Logger.write("FEMI");
    }

    public static void updateView(boolean updateData) {
        if (kMP.nowPlayingList != null) {
            // Logger.write("RESUMINGGGGGG " + kMP.musicService.currentSong.getAlbumArt());


            String path = (kMP.musicService == null || kMP.musicService.currentSong == null) ? null : kMP.musicService.currentSong.getAlbumArt();
            // kMP.musicService.set
            if (path != null && path.length() > 0) {
                iView.setImageDrawable(Drawable.createFromPath(kMP.musicService.currentSong.getAlbumArt()));
            } else {
                iView.setImageResource(R.drawable.big_logo2);
            }
            // nowPlayingLV.setAdapter(new SongAdapter(f, kMP.nowPlayingList));
            SongAdapter sa = (SongAdapter) nowPlayingLV.getAdapter();
            if (updateData) {
                sa.setData(kMP.nowPlayingList);
                ((HomeViewPagerAdapter) viewPager.getAdapter()).notifyDataSetChanged();
            }
            //if(kMP.musicService.isPlaying()) {
            if (path != null) sa.currentlyPlaying = kMP.musicService.currentSongPosition;
            // }
            sa.notifyDataSetChanged();
            if (updateData) {
                SharedPreferences.Editor e = orimiPreferences.edit();
                JSONArray ja = new JSONArray();
                for (Song s : kMP.nowPlayingList) {
                    JSONObject jo = new JSONObject();
                    try {
                        jo.put("id", s.getId());
                        jo.put("album_id", s.getAlbumId());
                        ja.put(jo.toString());
                    } catch (Exception ee) {
                    }
                }
                //Logger.write(ja.toString());
                e.putString(NOW_PLAYING_SONGS, ja.toString());
                e.commit();
            }

        }
    }

    private String getAlbumArt(String album_id) {
        Cursor cursor = null;
        String path = null;
        try {
            cursor = getContentResolver().query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID + "=?",
                    new String[]{album_id},
                    null
            );
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return path;
    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    public class StartedPlaying extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String state = b.getString(ServicePlayMusic.BROADCAST_EXTRA_STATE);
            // Logger.write();
            if (state == null) return;
            if (state.equals(ServicePlayMusic.BROADCAST_EXTRA_PLAYING)) {
                progressBar.setVisibility(View.VISIBLE);
                //  if(kMP.musicService.isPlaying())

                playButton.setImageResource(R.drawable.pause_but);
                updateTimerText();
                updateView(false);
                positionTextView.clearAnimation();

                Song s = kMP.musicService.currentSong;
                String str = s.getTitle() + " by " + s.getArtist() + ". Album: " + s.getAlbum() + ". " + s.getGenre();
                int len = str.length();
                for (int i = 0; i < len; i++) {
                    str += " ";
                }
                textView.setText(str + str.trim());
                //Logger.write(str);

            }
            if (state.equals(ServicePlayMusic.BROADCAST_EXTRA_PAUSED)) {
                playButton.setImageResource(R.drawable.play_but);
                startBlinkAnimation(positionTextView, 500);
                updateTimerText();
                progressBar.setMax(kMP.musicService.getDuration() / 1000);
                progressBar.setVisibility(View.VISIBLE);
                updateView(false);
                Song s = kMP.musicService.currentSong;
                String str = s.getTitle() + " by " + s.getArtist() + ". Album: " + s.getAlbum() + ". " + s.getGenre();
                int len = str.length();
                for (int i = 0; i < len; i++) {
                    str += " ";
                }
                textView.setText(str + str.trim());
                //  TextView textView=(TextView)findViewById(R.id.home_now_play_title);
                // textView.getAnimation().cancel();
            }
            if (state.equals(ServicePlayMusic.BROADCAST_EXTRA_UNPAUSED)) {
                // stopTimer();
                playButton.setImageResource(R.drawable.pause_but);
                positionTextView.clearAnimation();
                if (kMP.musicService.isPlaying()) progressBar.setMax(kMP.musicService.getDuration() / 1000);
            }
            if (state.equals(ServicePlayMusic.BROADCAST_EXTRA_COMPLETED)) {
                stopTimer();
                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setProgress(0);
                positionTextView.setText("0:00");
                playButton.setImageResource(R.drawable.play_but);
            }

            // progressBar.setProgress(kMP.musicService.getPosition());

            // String order = intent.getStringExtra(ServicePlayMusic.BROADCAST_EXTRA_GET_ORDER);
            // if(order.equals(ServicePlayMusic.BROADCAST_EXTRA_PLAYING)) {

            // }
        }
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public void updateTimerText() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            boolean timerUpdated = false;

            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //
                                if (kMP.musicService.isPlaying()) {
                                    if (!timerUpdated) {
                                        progressBar.setMax(kMP.musicService.getDuration() / 1000);
                                        durationTextView.setText(toMinute(kMP.musicService.getDuration()) + ":" + addZero(toSecond(kMP.musicService.getDuration())));

                                        timerUpdated = true;
                                    }
                                    progressBar.setProgress(kMP.musicService.getPosition() / 1000);
                                    // durationTextView.setText("jkj");
                                    positionTextView.setText(toMinute(kMP.musicService.getPosition()) + ":" + addZero(toSecond(kMP.musicService.getPosition())));

                                }
                                // Logger.write(kMP.musicService.getPosition()+"/ "+kMP.musicService.getDuration());
                               /* } else {
                                    timer.cancel();
                                    timer.purge();
                                    /*
                                    bufferingText.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            bufferingText.setText("");
                                        }
                                    });
                                }*/

                            } catch (Exception e) {
                            }
                        }
                    });
                } catch (Exception npe) {
                }
            }
        }, 0, 1000);
    }

    static int songPos = 0;

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        kMP.musicService.seekTo(songPos * 1000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        songPos = i;
       /*f(i>=seekBar.getMax()) {
           // progressBar.setVisibility(View.INVISIBLE);
            seekBar.setProgress(0);
            positionTextView.setText("0:00");
            return;
        }*/
        //
    }

    public void updatePlayModeButtons() {
        /*if (kMP.musicService != null && kMP.musicService.isRepeat()) {
            repeatBut.setImageResource(R.drawable.ic_menu_repeat_on);
        } else {
            repeatBut.setImageResource(R.drawable.ic_menu_repeat_off);
        }*/
        if (kMP.settings.get("repeat_list", false)) {
            repeatBut.setImageResource(R.drawable.ic_menu_repeat_on);
        } else {
            repeatBut.setImageResource(R.drawable.ic_menu_repeat_off);
        }
        if (kMP.musicService != null && kMP.musicService.isShuffle()) {
            shuffleBut.setImageResource(R.drawable.ic_menu_shuffle_on);
        } else {
            shuffleBut.setImageResource(R.drawable.ic_menu_shuffle_off);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public int toSecond(int milliseconds) {
        return (int) (milliseconds / 1000) % 60;
        //int minutes = (int) ((milliseconds / (1000*60)) % 60);
        //int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
    }

    public int toMinute(int milliseconds) {
        return (int) ((milliseconds / (1000 * 60)) % 60);
    }

    public int toHour(int milliseconds) {
        return (int) ((milliseconds / (1000 * 60 * 60)) % 24);
    }

    private String addZero(int i) {
        if (i < 10) return "0" + i;
        else return "" + i;
    }

    private OnClickListener playButtonAction(final String action) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (action.equals("play")) {

                    if (kMP.
                            musicService.isPlaying()) {
                        kMP.musicService.pausePlayer();
                        return;
                    } else {
                        if (kMP.musicService.isPaused()) {
                            kMP.musicService.unpausePlayer();
                        } else {
                            kMP.musicService.playSong();
                        }
                    }
                }
                if (action.equals("stop")) {
                    kMP.musicService.stopMusicPlayer();
                }
                if (action.equals("pause")) {
                    kMP.musicService.pausePlayer();
                }
                if (action.equals("unpause")) {
                    kMP.musicService.unpausePlayer();
                }
                if (action.equals("next")) {
                    kMP.musicService.next(false);
                    kMP.musicService.playSong();
                    return;
                }
                if (action.equals("prev")) {
                    if (kMP.musicService.getPosition() > 1000 * 5) {
                        kMP.musicService.seekTo(0);
                        kMP.musicService.playSong();
                        return;
                    }
                    kMP.musicService.previous(false);
                    kMP.musicService.playSong();
                }
                if (action.equals("repeat")) {

                    //kMP.musicService.toggleRepeat();
                    if (kMP.settings.get("repeat_list", false)) {
                        kMP.settings.set("repeat_list", false);
                    } else kMP.settings.set("repeat_list", true);
                    SharedPreferences.Editor e = orimiPreferences.edit();
                    e.putBoolean(MainActivity.REPEAT, kMP.musicService.isRepeat());
                    e.commit();
                    updatePlayModeButtons();
                }
                if (action.equals("shuffle")) {
                    kMP.musicService.toggleShuffle();
                    SharedPreferences.Editor e = orimiPreferences.edit();
                    e.putBoolean(MainActivity.SHUFFLE, kMP.musicService.isShuffle());
                    e.commit();
                    updatePlayModeButtons();
                }
                if (action.equals("togglePlayList")) {
                    if (viewPager.getCurrentItem() == 0)
                        viewPager.setCurrentItem(1);
                    else viewPager.setCurrentItem(0);
                }


            }
        };

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);

    }

    public void startSlidingAnimation(View v, int duration) {
        final Animation animation = new TranslateAnimation(400.0f, -400.0f, 0.0f, 0.0f);
        animation.setDuration(duration); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.RESTART); // Reverse animation at the end so the button will fade back in
        v.startAnimation(animation);
    }

    public void startBlinkAnimation(View v, int duration) {
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(duration); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        v.startAnimation(animation);
    }
}
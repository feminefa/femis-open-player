package femi.com.ng.core.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import femi.com.ng.kure.musicplayer.model.Song;
import femi.com.ng.R;
import femi.com.ng.core.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by femi on 23/08/2014.
 */
public class GridAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private LayoutInflater songInflater;
    private String uniq="lakdl";
    public boolean isScrolling=false;
    public static  final String ALBUM="Albums", ARTIST="Artists", GENRE="Genres", PLAYLIST="Play Lists";

    String type= ALBUM;

    List<Song> list=new ArrayList<Song>();
    // Constructor
    public GridAdapter(Context c, List<Song> l, String category){
        context = c;
        list=l;
        songInflater = LayoutInflater.from(c);
        this.type=category;
    }
    public void setData(List<Song> data) {
        list = data;
    }
    public List<Song> getData() {
        return list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    public long getAlbumId(int position) {
        return list.get(position).getAlbumId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Will map from a Song to a Song layout
        LinearLayout songLayout = (LinearLayout)songInflater.inflate(R.layout.orimi_grid_view_item_view,
                parent, false);
        TextView titleView  = (TextView)songLayout.findViewById(R.id.item_title);
        final ImageView albumArt=(ImageView)songLayout.findViewById(R.id.item_photo);

        final Song currentSong = list.get(position);
        // final String[] ALBUM_SUMMARY_PROJECTION = {  MediaStore.Audio.Albums.ALBUM_ART };

        String album_id="0";
       /* JSONObject jo=null;

        try{
            jo=new JSONObject(String.valueOf(currentSong.getAlbum()));
            album_id=""+jo.get("id");
        }catch (JSONException je) {
            return null;
        }*/
        album_id=""+currentSong.getAlbumId();
        final  String key=""+album_id+"_"+uniq;
        final Bitmap currentBitmap= MainActivity.getBitmapFromMemCache(key);
        if(currentBitmap!=null) {
            albumArt.setImageBitmap(currentBitmap);
            //make it opaque
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                albumArt.setAlpha(1f);
            else  albumArt.setAlpha(255);
        }else{
            //make it transparent
            albumArt.setImageResource(R.drawable.logo);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                albumArt.setAlpha(0.3f);
            else albumArt.setAlpha(100);
        }
        final String albumArtId=album_id;

        //Cursor cursor;
        if(!isScrolling && currentBitmap==null) {
            Cursor cursor=null;
            String path="";
            try {

                cursor = context.getApplicationContext().
                        getContentResolver().query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                        MediaStore.Audio.Albums._ID + "=?",
                        new String[]{albumArtId},
                        null
                );
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                   path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                }
            }finally {
                if(cursor!=null) {
                    cursor.close();
                    cursor=null;
                }
            }
            final String albumArtPath=path;
            new AsyncTask<String, Void, String>() {
                Bitmap img=null;


                @Override
                protected String doInBackground(String... strings) {
                    String path=strings[0];

                    if(path==null) {
                        list.get(position).setAlbumArt("");
                        return null;
                    }else list.get(position).setAlbumArt(path);


                    Bitmap bitmap=null;

                    Bitmap b=MainActivity.getBitmapFromMemCache(key);
                    if(b==null) {
                        try {
                            bitmap = BitmapFactory.decodeFile(path);
                            if (bitmap != null)
                                bitmap = MainActivity.addBitmapToMemoryCache(key, BitmapFactory.decodeFile(path));
                        }catch (NullPointerException npe) {

                        }

                    }
                    else bitmap=b;
                    img=bitmap;
                    return path;
                }

                @Override
                protected void onPostExecute(String path) {


                    albumArt.post(new Runnable() {
                        @Override
                        public void run() {
                            if (img != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                    albumArt.setAlpha(1f);
                                else  albumArt.setAlpha(255);
                                albumArt.setImageBitmap(img);
                            }

                        }
                    });
                }

                public void execute() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, albumArtPath);
                    }else{
                        execute(albumArtPath);
                    }
                }
            }.execute();


        }
        //String url=MainActivity.ORIMI_ALBUM_ART_URL+"?k="+new String(MainActivity.APP_KEY)+"&song="+currentSong.getHashCode();
        //Logger.write(url);
        //imL.displayImage(url, albumArt);

        String title="";
            if(type.equals(ALBUM)) {
                title=currentSong.getAlbum();
            }
            if(type.equals(ARTIST)) {
                title=currentSong.getArtist();
            }
            if(type.equals(GENRE)) {
                title=currentSong.getGenre();
            }
       // Logger.write(type);
        if (title.trim().length()==0)
            titleView.setText("<unknown>");
        else {
           titleView.setText(title);
        }
       // Logger.write(currentSong.getTitle());
        //String artist = currentSong.getArtist();
       // if (artist.trim().length()==0) {
            //artistView.setText("By <unknown>");
       // } else {
            //artistView.setText("By " + currentSong.getArtist());
       // }


        // Saving position as a tag.
        // Each Song layout has a onClick attribute,
        // which calls a function that plays a song
        // with that tag.
        songLayout.setTag(position);
        return songLayout;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}

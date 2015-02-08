package femi.com.ng.core.adapters;


import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import femi.com.ng.core.activities.MainActivity;
import femi.com.ng.kure.musicplayer.model.Song;
import femi.com.ng.R;
import femi.com.ng.core.libs.MyImageLoader;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public 	class SongAdapter extends BaseAdapter {

	private List<Song> songs;
	private LayoutInflater songInflater;
	private SherlockFragmentActivity activity;
    SherlockFragmentActivity context;
	MyImageLoader imL;
    public boolean isScrolling=false;
    public Integer currentlyPlaying=null;
    String uniq="adfas";
    private String category=null;

    public void setData(List<Song> data) {
        songs = data;
    }
    public List<Song> getData() {
        return songs;
    }

	public SongAdapter(SherlockFragmentActivity c, List<Song> theSongs, String type) {
		imL=new MyImageLoader(c);
        if(theSongs==null) theSongs=new ArrayList<Song>();
        songs = theSongs;
		songInflater = LayoutInflater.from(c);
        context=c;
       if(type!=null && type.length()>0) category=type;

	}
    /*public SongAdapter(SherlockFragmentActivity c, List<Song> theSongs) {
        imL=new MyImageLoader(c);
        if(theSongs==null) theSongs=new ArrayList<Song>();
        songs = theSongs;
        songInflater = LayoutInflater.from(c);
        context=c;
    }*/

    public SongAdapter(Context c, List<Song> theSongs, SherlockFragmentActivity sf) {
        imL=new MyImageLoader(c);
        songs = theSongs;
        songInflater = LayoutInflater.from(c);
        activity=sf;
    }

	@Override
	public int getCount() {
		return songs.size();
	}

	@Override
	public Object getItem(int position) {
		return songs.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
    @SuppressLint("InlinedApi")
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		// Will map from a Song to a Song layout
		LinearLayout songLayout = (LinearLayout)songInflater.inflate(R.layout.orimi_song_listview_item,
				                                                     parent, false);
		TextView titleView  = (TextView)songLayout.findViewById(R.id.item_song_title);
		TextView artistView = (TextView)songLayout.findViewById(R.id.item_song_artist);
		TextView dateTaggedView  = (TextView)songLayout.findViewById(R.id.item_song_date);
		final ImageView albumArt=(ImageView)songLayout.findViewById(R.id.item_album_art);

		final Song currentSong = songs.get(position);

        if(category!=null && category.equals(GridAdapter.PLAYLIST)) {
            albumArt.setImageResource(R.drawable.ic_list_black);
            albumArt.setScaleType(ImageView.ScaleType.FIT_XY);
            albumArt.setBackgroundColor(context.getResources().getColor(R.color.orange_transparent));
            albumArt.setPadding(15, 0, 15, 0);
            titleView.setText(currentSong.getPlayList().getName());
            artistView.setText(currentSong.getPlayList().getSongIds().size()+" Songs");
            return songLayout;
        }
        if(currentlyPlaying!=null) {
            if(position==currentlyPlaying) songLayout.setBackgroundColor(context.getResources().getColor(R.color.orange_transparent));
        }
      // final String[] ALBUM_SUMMARY_PROJECTION = {  MediaStore.Audio.Albums.ALBUM_ART };

        String album_id="0";
       /* JSONObject jo=null;

        try{
            jo=new JSONObject(String.valueOf(currentSong.getAlbum()));
            album_id=""+jo.get("id");
        }catch (JSONException je) {
            return null;
        }*/
        try {
            album_id = "" + currentSong.getAlbumId();
        }catch (NullPointerException npe) {
            album_id="";
        }
        final  String key=""+album_id+"_"+uniq;
        final Bitmap currentBitmap= MainActivity.getBitmapFromMemCache(key);
        if(currentBitmap!=null) {
            albumArt.setImageBitmap(currentBitmap);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                albumArt.setAlpha(1f);
            else  albumArt.setAlpha(255);
        }else{
            albumArt.setImageResource(R.drawable.logo);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                albumArt.setAlpha(0.3f);
            else  albumArt.setAlpha(100);
        }
        final String albumArtId=album_id;

        //Cursor cursor;
        if(!isScrolling && currentBitmap==null) {
            String path="";
            Cursor cursor=null;
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
            final String dPath=path;
            new AsyncTask <String, Void, String>() {
                Bitmap img=null;
                Cursor cursor=null;

               @Override
               protected String doInBackground(String... strings) {
                   String path=strings[0];
                   if(path==null) {
                       songs.get(position).setAlbumArt("");
                       return null;
                   }else songs.get(position).setAlbumArt(path);


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
               @Override
               protected void onCancelled() {
                   super.onCancelled();
                   if(cursor!=null) {
                       cursor.close();
                       cursor=null;
                   }
               }
               @Override
                protected void onCancelled(String s) {
                   super.onCancelled();
                   if(cursor!=null) {
                       cursor.close();
                       cursor=null;
                   }
               }
               public void execute() {
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                       executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dPath);
                   }else{
                       execute(dPath);
                   }
               }
           }.execute();


        }
		//String url=MainActivity.ORIMI_ALBUM_ART_URL+"?k="+new String(MainActivity.APP_KEY)+"&song="+currentSong.getHashCode();
		//Logger.write(url);
		//imL.displayImage(url, albumArt);

		String title = currentSong.getTitle();
		if (title.trim().length()==0)
			titleView.setText("<unknown>");
		else
			titleView.setText(currentSong.getTitle());

		String artist = currentSong.getArtist();
		if (artist.trim().length()==0)
			artistView.setText("By <unknown>");
		else
			artistView.setText("By "+currentSong.getArtist());
		
		
		// Saving position as a tag.
		// Each Song layout has a onClick attribute,
		// which calls a function that plays a song
		// with that tag.
		songLayout.setTag(position);
		return songLayout;
	}
   public AbsListView.OnScrollListener getOnScrollListener() {
       return  new AbsListView.OnScrollListener() {
           @Override
           public void onScrollStateChanged(AbsListView absListView, int scrollState) {
               if (scrollState != 0) {
                   isScrolling = true;
               } else {
                   isScrolling = false;
                   notifyDataSetChanged();
               }
           }

           @Override
           public void onScroll(AbsListView absListView, int i, int i2, int i3) {

           }
       };
    }
}

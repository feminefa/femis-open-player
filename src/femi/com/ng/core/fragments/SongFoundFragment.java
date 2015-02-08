package femi.com.ng.core.fragments;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import femi.com.ng.core.activities.MainActivity;
import femi.com.ng.R;
import femi.com.ng.core.libs.Logger;
import android.annotation.SuppressLint;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class SongFoundFragment extends SherlockFragment {
	String song="";
	MediaPlayer mediaPlayer;
	public JSONObject jo;
	String songURL;
	TextView bufferingText;
	String songDuration="";
	Handler handler=new Handler();
	Button listenBut;
	boolean addToMyTags=true;
	public void setSong(String s, boolean addToTaggedList) {
		// TODO Auto-generated constructor stub
		song=s;
		addToMyTags=addToTaggedList;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		setUserVisibleHint(false);
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if(!isVisibleToUser) {
			if(mediaPlayer!=null) {
				try{
					mediaPlayer.stop();
					mediaPlayer.release();
					 listenBut.setText("Listen");
					 listenBut.setEnabled(true);
                     bufferingText.post(new Runnable() {
                         @Override
                         public void run() {
                         	bufferingText.setText("");
                         }
                     });
                 
				}catch(Exception e) {}
			}
		}
	}
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		setUserVisibleHint(false);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v=inflater.inflate(R.layout.orimi_fragment_found, container, false);
		
		// TODO Auto-generated method stub
		//return super.onCreateView(inflater, container, savedInstanceState);
		
		try{
			jo=new JSONObject(song);
			String tagged= MainActivity.orimiPreferences.getString(MainActivity.MY_TAGS, null);
			JSONArray ja=new JSONArray();
			
			try{
				ja=new JSONArray(tagged);
			}catch(Exception e) {
			}
			//Logger.write(""+ja.length());
			if(addToMyTags) {
				if(ja.length()>=MainActivity.MAX_TAGS_IN_HISTORY) {
					int start=ja.length()-MainActivity.MAX_TAGS_IN_HISTORY+1;
					JSONArray tmpA=new JSONArray();
					for(int i=start; i<ja.length(); i++) {
						tmpA.put(ja.get(i));
					}
					ja=tmpA;
				}
				
				jo.put("time_tagged", System.currentTimeMillis()/1000);
				ja.put(jo.toString());
				
				Editor e=MainActivity.orimiPreferences.edit();
				e.putString(MainActivity.MY_TAGS, ja.toString());
				e.commit();
			}
			//songURL=MainActivity.ORIMI_SONG_URL+"?k="+new String(MainActivity.APP_KEY)+"&song="+jo.getString("hash");
			//songURL=MainActivity.ORIMI_SONG_URL+"?s="+jo.getInt("orimi_id");
			//songURL="http://104.131.222.236/mp3s/bba3477a5afc25840f6788cbc004efb1afdc102ec05057f1c406e6f052525c7c.mp3?lkodsjo=sds";
			
			Logger.write(songURL);
				}catch(Exception e) {
			Logger.write(e);
		}
		
		 
		ImageView albumArtIV=(ImageView)v.findViewById(R.id.found_album_art);
		//ProgressBar pBar=(ProgressBar)v.findViewById(R.id.found_progress_bar);
		 listenBut=(Button)v.findViewById(R.id.found_play_but);
		bufferingText=(TextView)v.findViewById(R.id.buffering_text);
		ImageView startTaggin=(ImageView)v.findViewById(R.id.found_start_taggin_but);
		startTaggin.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
				/*	FragmentManager fm=getSherlockActivity().getSupportFragmentManager();
					FragmentTransaction ft=fm.beginTransaction();
					ft.remove(SongFoundFragment.this);
					ft.commit();*/
					//startActivity(new Intent(getSherlockActivity(), TagSongActivity.class));
				}catch(Exception e) {
					
				}
			}
		});
		listenBut.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					if (mediaPlayer != null && mediaPlayer.isPlaying()) {
						mediaPlayer.pause();
						mediaPlayer.seekTo(0);
						return;					
					}
				}catch(Exception e) {
					try{
						mediaPlayer.reset();
					}catch(Exception z) {}
					mediaPlayer=null;
					
				}
				if(mediaPlayer!=null) {
					mediaPlayer.start();
					updateTimerText(mediaPlayer);
					listenBut.setText("Stop");
					return;
				}else{
					mediaPlayer=new MediaPlayer();
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				}
				
				MediaPlayer.OnPreparedListener mpPreparedListener=new MediaPlayer.OnPreparedListener() {
				    public void onPrepared(final MediaPlayer player) {
				    	//bufferingText.setText("Buffering...");
				    	int milliseconds=player.getDuration();
						int seconds = (int) (milliseconds / 1000) % 60 ;
						int minutes = (int) ((milliseconds / (1000*60)) % 60);
						int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
				    	songDuration=minutes+":"+addZero(seconds);
				    	player.start();
				    	listenBut.setEnabled(true);
				    	getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								bufferingText.setVisibility(View.VISIBLE);		
								bufferingText.setText("Playing...");	
								listenBut.setText("Stop");
							}
				    	});
				    	updateTimerText(player);
				    	
				    	
				    }
				};
				bufferingText.setText("Loading...");
				bufferingText.setVisibility(View.VISIBLE);
				MediaPlayer.OnErrorListener errorListener=new MediaPlayer.OnErrorListener() {
					
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						// TODO Auto-generated method stub
						
						getActivity().runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								mediaPlayer=null;
								bufferingText.setVisibility(View.INVISIBLE);
								listenBut.setEnabled(true);
								MainActivity.createAlertDialog(getActivity(), "Error", "Unable to connect. Please check your internet connection.").show();
								
								//MainActivity.createAlertDialog(getActivity(), "Error", "Unable to connect. Please check your internet connection.").show();
								
							}
						});
						return false;
					}
				};
				listenBut.setEnabled(false);
				new PlaySong(songURL, mpPreparedListener, errorListener );				
			}
		});
		
		try{
			//String url=MainActivity.ORIMI_ALBUM_ART_URL+"?k="+new String(MainActivity.APP_KEY)+"&song="+jo.getString("hash");
			//Logger.write(url);
			//new MyImageLoader(getSherlockActivity()).displayImage(url, albumArtIV);
			TextView sTitle = (TextView)v.findViewById(R.id.found_song_title);
			TextView sArtist = (TextView)v.findViewById(R.id.found_song_artist);
			sTitle.setText(jo.getString("title"));
			sArtist.setText("By "+jo.getString("artist"));
		}catch(Exception e) {
			
		}
		
		
		return v;
	}
	
	class PlaySong extends AsyncTask<String, Integer, String> {
		MediaPlayer.OnPreparedListener opl;
		MediaPlayer.OnErrorListener errorL;
		public PlaySong(String url, MediaPlayer.OnPreparedListener o, MediaPlayer.OnErrorListener e) {
			// TODO Auto-generated constructor stub
			opl=o;
			errorL=e;
			execute(url);
		}
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			try{
				mediaPlayer.setDataSource(params[0]);
				mediaPlayer.setOnErrorListener(errorL);
				mediaPlayer.setOnPreparedListener(opl);
				mediaPlayer.prepare(); // might take long! (for buffering, etc)
				mediaPlayer.start();
			}catch(IOException e) {
				//Logger.write(e);
				errorL.onError(mediaPlayer, 0, 0);
				return null;
			}catch (IllegalStateException ise) {
				// TODO: handle exception
			}
			
			return null;
		}
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
		}
		
		@SuppressLint("NewApi")
		public void execute(String url) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
	    	}else{
	    	   execute(url);
	    	}
		}
		
	}
	private String addZero(int i) {
		if(i<10) return "0"+i;
		else return ""+i;
	}
	public String getTimerText(final MediaPlayer mp) {
		int milliseconds=mp.getCurrentPosition();
		final int seconds = (int) (milliseconds / 1000) % 60 ;
		final int minutes = (int) ((milliseconds / (1000*60)) % 60);
		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		return minutes+":"+addZero(seconds)+"/"+songDuration;
	}
	
	public void updateTimerText(final MediaPlayer player) {
		final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            	try{
                SongFoundFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	try{
                        if (player != null && player.isPlaying()) {
                            bufferingText.post(new Runnable() {
                                @Override
                                public void run() {
                                	bufferingText.setText(getTimerText(player));
                                }
                            });
                        } else {
                            timer.cancel();
                            timer.purge();
                            listenBut.setText("Listen");
                            bufferingText.post(new Runnable() {
                                @Override
                                public void run() {
                                	bufferingText.setText("");
                                }
                            });
                        }
                    
                    	}catch(Exception e) {}
                    }
                });
            	}catch(Exception npe) {}
            }
        }, 0, 1000);	
	}
	

}

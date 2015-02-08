package femi.com.ng.kure.musicplayer.model;

import java.util.*;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import femi.com.ng.core.activities.MainActivity;
import femi.com.ng.core.libs.Logger;
/**
 * Edited line 182-185, 244
 *
 */
/**
 * Global interface to all the songs this application can see.
 *
 * Tasks:
 * - Scans for songs on the device
 *   (both internal and external memories)
 * - Has query functions to songs and their attributes.
 *
 * Thanks:
 *
 * - Showing me how to get a music's full PATH:
 *   http://stackoverflow.com/a/21333187
 *
 * - Teaching me the queries to get Playlists
 *   and their songs:
 *   http://stackoverflow.com/q/11292125
 */
public class SongList {

	/**
	 * Big list with all the Songs found.
	 */
	public ArrayList<Song> songs = new ArrayList<Song>();

	/**
	 * Big list with all the Playlists found.
	 */
	public ArrayList<Playlist> playlists = new ArrayList<Playlist>();

	/**
	 * Maps song's genre IDs to song's genre names.
	 * @note It's only available after calling `scanSongs`.
	 */
	private HashMap<String, String> genreIdToGenreNameMap;

    /**
     * Maps genre ID to song's id.
     * @note It's only available after calling `scanSongs`.
     */
    private HashMap<String, String> genreIdToSongIdMap;

	/**
	 * Maps song's IDs to song genre IDs.
	 * @note It's only available after calling `scanSongs`.
	 */
	private HashMap<String, String> songIdToGenreIdMap;

	/**
	 * Flag that tells if successfully scanned all songs.
	 */
	private boolean scannedSongs;

	/**
	 * Flag that tells if we're scanning songs right now.
	 */
	private boolean scanningSongs;

	/**
	 * Tells if we've successfully scanned all songs on
	 * the device.
	 *
	 * This will return `false` both while we're scanning
	 * for songs and if some error happened while scanning.
	 */
	public boolean isInitialized() {
		return scannedSongs;
	}

	/**
	 * Tells if we're currently scanning songs on the device.
	 */
	public boolean isScanning() {
		return scanningSongs;
	}

	/**
	 * Scans the device for songs.
	 *
	 * This function takes a lot of time to execute and
	 * blocks the program UI.
	 * So you should call it on a separate thread and
	 * query `isInitialized` when needed.
	 *
	 * Inside it, we make a lot of queries to the system's
	 * databases - getting songs, genres and playlists.
	 *
	 * @note If you call this function twice, it rescans
	 *       the songs, refreshing internal lists.
	 *       It doesn't add up songs.
	 *
	 * @param c         The current Activity's Context.
	 * @param fromWhere Where should we scan for songs.
	 *
	 * Accepted values to `fromWhere` are:
	 * - "internal" To scan for songs on the phone's memory.
	 * - "external" To scan for songs on the SD card.
	 * - "both"     To scan for songs anywhere.
	 */
	public void scanSongs(Context c, String fromWhere) {

		// This is a rather complex function that interacts with
		// the underlying Android database.
		// Grab some coffee and stick to the comments.

		// Not implemented yet.
		if (fromWhere == "both")
			throw new RuntimeException("Can't scan from both locations - not implemented");

		// Checking for flags so we don't get called twice
		// Fucking Java that doesn't allow local static variables.
		if (scanningSongs)
			return;
		scanningSongs = true;

		// The URIs that tells where we should scan for files.
		// There are separate URIs for music, genres and playlists. Go figure...
		//
		// Remember - internal is the phone memory, external is for the SD card.
		Uri musicUri = ((fromWhere == "internal") ?
				        MediaStore.Audio.Media.INTERNAL_CONTENT_URI:
				        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
		Uri genreUri = ((fromWhere == "internal") ?
				        MediaStore.Audio.Genres.INTERNAL_CONTENT_URI:
				        MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI);
		Uri playlistUri = ((fromWhere == "internal") ?
		        MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI:
		        MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI);

		// Gives us access to query for files on the system.
		ContentResolver resolver = c.getContentResolver();

		// We use this thing to iterate through the results
		// of a SQLite database query.
		Cursor cursor;

		// OK, this is where we start.
		//
		// First, before even touching the songs, we'll save all the
		// music genres (like "Rock", "Jazz" and such).
		// That's because Android doesn't allow getting a song genre
		// from the song file itself.
		//
		// To get the genres, we make queries to the system's SQLite
		// database. It involves genre IDs, music IDs and such.
		//
		// We're creating two maps:
		//
		// 1. Genre ID -> Genre Names
		// 2. Song ID -> Genre ID
		//
		// This way, we have a connection from a Song ID to a Genre Name.
		//
		// Then we finally get the songs!
		// We make queries to the database, getting all possible song
		// metadata - like artist, album and such.


		// These are the columns from the system databases.
		// They're the information I want to get from songs.
		String GENRE_ID      = MediaStore.Audio.Genres._ID;
		String GENRE_NAME    = MediaStore.Audio.Genres.NAME;
        String SONG_ID       = MediaStore.Audio.Media._ID;
		String SONG_TITLE    = MediaStore.Audio.Media.TITLE;
		String SONG_ARTIST   = MediaStore.Audio.Media.ARTIST;
		String SONG_ALBUM    = MediaStore.Audio.Media.ALBUM;
		String SONG_YEAR     = MediaStore.Audio.Media.YEAR;
		String SONG_TRACK_NO = MediaStore.Audio.Media.TRACK;
		String SONG_FILEPATH = MediaStore.Audio.Media.DATA;
		String SONG_DURATION = MediaStore.Audio.Media.DURATION;

        /**
        * I will also like to retrieve album id as wellso as get the album art
        */
        String SONG_ALBUM_ID    = MediaStore.Audio.Media.ALBUM_ID;
		// Creating the map  "Genre IDs" -> "Genre Names"
		genreIdToGenreNameMap = new HashMap<String, String>();

		// This is what we'll ask of the genres
		String[] genreColumns = {
				GENRE_ID,
				GENRE_NAME
		};

		// Actually querying the genres database
        cursor = resolver.query(genreUri, genreColumns, null, null, null);

		// Iterating through the results and filling the map.
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            genreIdToGenreNameMap.put(cursor.getString(0), cursor.getString(1));

        cursor.close();

        // Map from Songs IDs to Genre IDs
        songIdToGenreIdMap = new HashMap<String, String>();

        genreIdToSongIdMap = new HashMap<String, String>();

        // UPDATE URI HERE
    	if (fromWhere == "both")
    		throw new RuntimeException("Can't scan from both locations - not implemented");

    	// For each genre, we'll query the databases to get
    	// all songs's IDs that have it as a genre.
    	for (String genreID : genreIdToGenreNameMap.keySet()) {

        	Uri uri = MediaStore.Audio.Genres.Members.getContentUri(fromWhere,
        			                                                Long.parseLong(genreID));

        	cursor = resolver.query(uri, new String[] { SONG_ID }, null, null, null);

        	// Iterating through the results, populating the map
        	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

        		long currentSongID = cursor.getLong(cursor.getColumnIndex(SONG_ID));

        		songIdToGenreIdMap.put(Long.toString(currentSongID), genreID);
                genreIdToSongIdMap.put(genreIdToGenreNameMap.get(genreID), Long.toString(currentSongID));
        	}
        	cursor.close();
        }

    	// Finished getting the Genres.
    	// Let's go get dem songzz.

		// Columns I'll retrieve from the song table
		String[] columns = {
				SONG_ID,
				SONG_TITLE,
				SONG_ARTIST,
				SONG_ALBUM,
				SONG_YEAR,
				SONG_TRACK_NO,
				SONG_FILEPATH,
				SONG_DURATION,
                SONG_ALBUM_ID, // ADDED TO LIST OF COLUMNS RETRIEVED
		};

		// Thing that limits results to only show music files.
		//
		// It's a SQL "WHERE" clause - it becomes `WHERE IS_MUSIC=1`.
		//
		// (note: using `IS_MUSIC!=0` takes a fuckload of time)
		final String musicsOnly = MediaStore.Audio.Media.IS_MUSIC + "=1";

		// Actually querying the system
		cursor = resolver.query(musicUri, columns, musicsOnly, null, null);

		if (cursor != null && cursor.moveToFirst())
		{
			// NOTE: I tried to use MediaMetadataRetriever, but it was too slow.
			//       Even with 10 songs, it took like 13 seconds,
			//       No way I'm releasing it this way - I have like 4.260 songs!

			do {

				Song song = new Song(cursor.getInt(cursor.getColumnIndex(SONG_ID)),
						             cursor.getString(cursor.getColumnIndex(SONG_FILEPATH)));
                // Creating a song from the values on the row
                /**
                 * Convert song.album to JSONString containing album_id and album name
                 * I did not want to tamper with the Song class
                 */
               /* JSONObject jo= new JSONObject();
                try {
                    jo.put("id", (cursor.getInt(cursor.getColumnIndex(SONG_ALBUM_ID))));
                    jo.put("name", (cursor.getString(cursor.getColumnIndex(SONG_ALBUM))));
                }catch (Exception e) {

                }*/

				song.setTitle      (cursor.getString(cursor.getColumnIndex(SONG_TITLE)));
				song.setArtist     (cursor.getString(cursor.getColumnIndex(SONG_ARTIST)));
				//song.setAlbum      (cursor.getString(cursor.getColumnIndex(SONG_ALBUM)));
                song.setAlbum      (cursor.getString(cursor.getColumnIndex(SONG_ALBUM)));
				song.setYear       (cursor.getInt   (cursor.getColumnIndex(SONG_YEAR)));
				song.setTrackNumber(cursor.getInt   (cursor.getColumnIndex(SONG_TRACK_NO)));
				song.setDuration   (cursor.getInt   (cursor.getColumnIndex(SONG_DURATION)));
                song.setAlbumId(cursor.getInt   (cursor.getColumnIndex(SONG_ALBUM_ID)));
				// Using the previously created genre maps
				// to fill the current song genre.
				String currentGenreID   = songIdToGenreIdMap.get(Long.toString(song.getId()));
				String currentGenreName = genreIdToGenreNameMap.get(currentGenreID);

                if(currentGenreID!=null) {
                     song.setGenre(currentGenreName);
                    // Logger.write(currentGenreName);
                   // song.setGenre();
                }
				// Adding the song to the global list
				songs.add(song);
			}
			while (cursor.moveToNext());
		}
		else
		{
			// What do I do if I can't find any songs?
		}
		cursor.close();

		// Alright, now I'll get all the Playlists.
		// First I grab all playlist IDs and Names and then for each
		// one of those, getting all songs inside them.

		// As you know, the columns for the database.
		String PLAYLIST_ID      = MediaStore.Audio.Playlists._ID;
		String PLAYLIST_NAME    = MediaStore.Audio.Playlists.NAME;
		String PLAYLIST_SONG_ID = MediaStore.Audio.Playlists.Members.AUDIO_ID;

		// This is what I'll get for all playlists.
		String[] playlistColumns = {
				PLAYLIST_ID,
				PLAYLIST_NAME
		};

		// The actual query - takes a while.
		cursor = resolver.query(playlistUri, playlistColumns, null, null, null);

		// Going through all playlists, creating my class and populating
		// it with all the song IDs they have.
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			Playlist playlist = new Playlist(cursor.getLong(cursor.getColumnIndex(PLAYLIST_ID)),
			                                 cursor.getString(cursor.getColumnIndex(PLAYLIST_NAME)));

			// For each playlist, get all song IDs
			Uri currentUri = MediaStore.Audio.Playlists.Members.getContentUri(fromWhere, playlist.getID());

			Cursor cursor2 = resolver.query(currentUri,
			                                new String[] { PLAYLIST_SONG_ID },
			                                musicsOnly,
			                                null, null);

			// Adding each song's ID to it
			for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2.moveToNext())
				playlist.add(cursor2.getLong(cursor2.getColumnIndex(PLAYLIST_SONG_ID)));

			playlists.add(playlist);
			cursor2.close();
		}

		// Finally, let's sort the song list alphabetically
		// based on the song title.
		Collections.sort(songs, new Comparator<Song>() {
			public int compare(Song a, Song b)
			{
				return a.getTitle().compareTo(b.getTitle());
			}
		});

		scannedSongs  = true;
		scanningSongs = false;
	}

	public void destroy() {
		songs.clear();
	}

	/**
	 * Returns an alphabetically sorted list with all the
	 * artists of the scanned songs.
	 *
	 * @note This method might take a while depending on how
	 *       many songs you have.
	 */
	public ArrayList<String> getArtists() {

		ArrayList<String> artists = new ArrayList<String>();
        Map<String, Song> tmp=new HashMap<String, Song>();

        for (Song song : songs) {
			String artist = song.getArtist();

			if ((artist != null) && (! artists.contains(artist))) {
                artists.add(artist);
                tmp.put(artist, song);
            }
		}

		// Making them alphabetically sorted
		Collections.sort(artists);
        MainActivity.artists.clear();
        for(String s:artists) {
            //Logger.write(s);
            MainActivity.artists.add(tmp.get(s));
        }
		return artists;
	}
    public List<Song> getAllArtists() {
        if(MainActivity.artists.size()==0) getArtists();
        return MainActivity.artists;
    }
	/**
	 * Returns an alphabetically sorted list with all the
	 * albums of the scanned songs.
	 *
	 * @note This method might take a while depending on how
	 *       many songs you have.
	 */
    public List<Song> getAllAlbums() {
        if(MainActivity.albums.size()==0)  getAlbums();
      //  Logger.write(""+songs.size());
        return MainActivity.albums;
    }
	public ArrayList<String> getAlbums() {

		ArrayList<String> albums = new ArrayList<String>();
        Map<String, Song> tmpAlbums=new HashMap<String, Song>();

		for (Song song : songs) {
			String album = song.getAlbum();

			if ((album != null) && (! albums.contains(album))) {
                albums.add(album);
               tmpAlbums.put(album, song);
            }
		}


		// Making them alphabetically sorted
		Collections.sort(albums);
        MainActivity.albums.clear();
        for(String s:albums) {
           // Logger.write(s);
            MainActivity.albums.add(tmpAlbums.get(s));
        }

		return albums;
	}

    public List<Song> getAllGenres () {
        if(MainActivity.genres.size()==0)  getGenres();
        //  Logger.write(""+songs.size());
        return MainActivity.genres;

    }
	/**
	 * Returns an alphabetically sorted list with all
	 * existing genres on the scanned songs.
	 */
	public ArrayList<String> getGenres() {

		ArrayList<String> genres = new ArrayList<String>();


        for (String genre : genreIdToGenreNameMap.values()) {
            genres.add(genre);
        }
        //songIdToGenreIdMap
		Collections.sort(genres);

        for(String g:genres) {
           //Logger.write("Num "+genreIdToSongIdMap.size());
            if(genreIdToSongIdMap.get(g)==null) continue;
           // Logger.write(g);

            MainActivity.genres.add(getSongById(Long.parseLong(genreIdToSongIdMap.get(g))));
        }

		return genres;
	}

	/**
	 * Returns a list with all years your songs have.
	 *
	 * @note It is a list of Strings. To access the
	 *       years, do a `Integer.parseInt(string)`.
	 */
	public ArrayList<String> getYears() {

		ArrayList<String> years = new ArrayList<String>();

		for (Song song : songs) {
			String year = Integer.toString(song.getYear());

			if ((Integer.parseInt(year) > 0) && (! years.contains(year)))
				years.add(year);
		}

		// Making them alphabetically sorted
		Collections.sort(years);

		return years;
	}

	/**
	 * Returns a list of Songs belonging to a specified artist.
	 */
	public ArrayList<Song> getSongsByArtist(String desiredArtist) {
		ArrayList<Song> songsByArtist = new ArrayList<Song>();

		for (Song song : songs) {
			String currentArtist = song.getArtist();

			if (currentArtist.equals(desiredArtist))
				songsByArtist.add(song);
		}

		// Sorting resulting list by Album
		Collections.sort(songsByArtist, new Comparator<Song>() {
			public int compare(Song a, Song b)
			{
				return a.getAlbum().compareTo(b.getAlbum());
			}
		});

		return songsByArtist;
	}

	/**
	 * Returns a list of album names belonging to a specified artist.
	 */
	public ArrayList<String> getAlbumsByArtist(String desiredArtist) {
		ArrayList<String> albumsByArtist = new ArrayList<String>();

		for (Song song : songs) {
			String currentArtist = song.getArtist();
			String currentAlbum  = song.getAlbum();

			if (currentArtist.equals(desiredArtist))
				if (! albumsByArtist.contains(currentAlbum))
					albumsByArtist.add(currentAlbum);
		}

		// Sorting alphabetically
		Collections.sort(albumsByArtist);

		return albumsByArtist;
	}

    public ArrayList<Song> getSongAlbumsByArtist(String desiredArtist) {
        ArrayList<String> albumsByArtist = new ArrayList<String>();
        Map<String, Song> s=new HashMap<String, Song>();
        ArrayList<Song> artistAlbums = new ArrayList<Song>();
        for (Song song : songs) {
            String currentArtist = song.getArtist();
            String currentAlbum  = song.getAlbum();

            if (currentArtist.equals(desiredArtist))
                if (! albumsByArtist.contains(currentAlbum)) {
                    albumsByArtist.add(currentAlbum);
                    s.put(currentAlbum, song);
                }
        }

        // Sorting alphabetically
        Collections.sort(albumsByArtist);
        for(String ss:albumsByArtist) {
            artistAlbums.add(s.get(ss));
        }

        return artistAlbums;
    }

	/**
	 * Returns a new list with all songs.
	 *
	 * @note This is different than accessing `songs` directly
	 *       because it duplicates it - you can then mess with
	 *       it without worrying about changing the original.
	 */
	public ArrayList<Song> getSongs() {
		ArrayList<Song> list = new ArrayList<Song>();

		for (Song song : songs)
			list.add(song);

		return list;
	}
    public ArrayList<Song> getSongsByAlbumId(Long desiredAlbum) {
        ArrayList<Song> songsByAlbum = new ArrayList<Song>();
        for (Song song : songs) {
            Long currentAlbum = song.getAlbumId();
            if (currentAlbum==desiredAlbum)
                songsByAlbum.add(song);
        }
        return songsByAlbum;
    }

    /**
	 * Returns a list of Songs belonging to a specified album.
	 */
	public ArrayList<Song> getSongsByAlbum(String desiredAlbum) {
		ArrayList<Song> songsByAlbum = new ArrayList<Song>();
		for (Song song : songs) {
			String currentAlbum = song.getAlbum();
			if (currentAlbum.equals(desiredAlbum))
				songsByAlbum.add(song);
		}
		return songsByAlbum;
	}

	/**
	 * Returns a list with all songs that have the same `genre.`
	 */
	public ArrayList<Song> getSongsByGenre(String genreName) {

		ArrayList<Song> currentSongs = new ArrayList<Song>();

		for (Song song : songs) {
			String currentSongGenre = song.getGenre();
            //Logger.write(song.getGenre());
			if (currentSongGenre.equals(genreName))
				currentSongs.add(song);
		}

		return currentSongs;
	}

	/**
	 * Returns a list with all songs composed at `year`.
	 */
	public ArrayList<Song> getSongsByYear(int year) {

		ArrayList<Song> currentSongs = new ArrayList<Song>();

		for (Song song : songs) {

			int currentYear = song.getYear();

			if (currentYear == year)
				currentSongs.add(song);
		}

		return currentSongs;
	}

	public ArrayList<String> getPlaylistNames() {

		ArrayList<String> names = new ArrayList<String>();

		for (Playlist playlist : playlists)
			names.add(playlist.getName());

        Collections.sort(names);
		return names;
	}
    public List<Song> getPlaylistAsSongs() {
        return getPlaylistAsSongs(true);
    }
    public List<Song> getPlaylistAsSongs(boolean reset) {
        if(!reset && MainActivity.playLists.size()>0) return MainActivity.playLists;
        List<Song> playLists = new ArrayList<Song>();
        //Collections.sor
        for (Playlist playlist : playlists) {
           ArrayList<Long> sid= playlist.getSongIds();
            Song s=null;
            if(sid==null || sid.size()==0) {
               s=new Song(0,null);
            }else {
                   s=new Song(sid.get(0),null);
            }
            s.setPlayList(playlist);
            playLists.add(s);
           // names.add(playlist.getName());
        }
        MainActivity.playLists=playLists;
        return playLists;
    }

	public Song getSongById(long id) {

		Song currentSong = null;

		for (Song song : songs)
			if (song.getId() == id) {
				currentSong = song;
				break;
			}

		return currentSong;
	}

	public ArrayList<Song> getSongsByPlaylist(String playlistName) {

		ArrayList<Long> songIDs = null;

		for (Playlist playlist : playlists)
			if (playlist.getName().equals(playlistName)) {
				songIDs = playlist.getSongIds();
				break;
			}

		ArrayList<Song> currentSongs = new ArrayList<Song>();

		for (Long songID : songIDs)
			currentSongs.add(getSongById(songID));

		return currentSongs;
	}
    public Playlist getPlayListFromName(String playlistName) {
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(playlistName)) {
                return playlist;
            }
        }
        return null;
    }
    /**
     * Clears a Playlist
     *
     * @param Activity c Activity
     *  @param int id playlist id
     */
    public int clearPlayList(Context c, String fromWhere, Playlist pl) {
        ContentResolver resolver = c.getContentResolver();

        Uri currentUri = MediaStore.Audio.Playlists.Members.getContentUri(fromWhere, pl.getID());
       // final String musicsOnly = MediaStore.Audio.Media.IS_MUSIC + "=1";
        try{
            int num=resolver.delete(currentUri,MediaStore.Audio.Media.IS_MUSIC + "=?", new String[] { ""+1 } );
            Logger.write(""+num);
            return num;
        }catch (Exception e) {
            int num=resolver.delete(currentUri,null, null);
            return num;
        }

    }
/**
 * Deletes a Playlist
 *
 * @param Activity c Activity
 *  @param int id playlist id
 */
    public int deletePlayList(Context c, String fromWhere, Playlist pl) {
        ContentResolver resolver = c.getContentResolver();

        Uri playlistUri = ((fromWhere == "internal") ?
                MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI:
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI);
        int num=resolver.delete(playlistUri, MediaStore.Audio.Playlists._ID+"=?", new String[] {""+pl.getID()});
        playlists.remove(pl);
        return num;
    }
	/**
	 * Creates a new Playlist.
	 *
	 * @param c          Activity on which we're creating.
	 * @param fromWhere  "internal" or "external".
	 * @param name       Playlist name.
	 * @param songsToAdd List of song IDs to place on it.
	 */
	public Playlist newPlaylist(Context c, String fromWhere, String name, ArrayList<Song> songsToAdd) {

		ContentResolver resolver = c.getContentResolver();

		Uri playlistUri = ((fromWhere == "internal") ?
		        MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI:
		        MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI);

		// CHECK IF PLAYLIST EXISTS!

		// Setting the new playlists' values
		ContentValues values = new ContentValues();
		values.put(MediaStore.Audio.Playlists.NAME, name);
		values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());

		// Actually inserting the new playlist.
		Uri newPlaylistUri = resolver.insert(playlistUri, values);
        Logger.write(newPlaylistUri.toString());
		// Getting the new Playlist ID
		String PLAYLIST_ID      = MediaStore.Audio.Playlists._ID;
		String PLAYLIST_NAME    = MediaStore.Audio.Playlists.NAME;

		// This is what I'll get for all playlists.
		String[] playlistColumns = {
				PLAYLIST_ID,
				PLAYLIST_NAME
		};

		// The actual query - takes a while.
		Cursor cursor = resolver.query(playlistUri, playlistColumns, null, null, null);

		long playlistID = 0;

		// Going through all playlists, creating my class and populating
		// it with all the song IDs they have.
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
			if (name.equals(cursor.getString(cursor.getColumnIndex(PLAYLIST_NAME))))
				playlistID = cursor.getLong(cursor.getColumnIndex(PLAYLIST_ID));

		// Now, to it's songs
		Uri songUri = Uri.withAppendedPath(newPlaylistUri, MediaStore.Audio.Playlists.Members.CONTENT_DIRECTORY);
		int songOrder = 1;

		for (Song song : songsToAdd) {

			ContentValues songValues = new ContentValues();

			songValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID,   song.getId());
			songValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, songOrder);

			resolver.insert(songUri, songValues);
			songOrder++;
		}

		// Finally, we're updating our internal list of Playlists
		 Playlist newPlaylist = new Playlist(playlistID, name);

		for (Song song : songsToAdd)
			newPlaylist.add(song.getId());

		playlists.add(newPlaylist);
        return  newPlaylist;
	}
    public void addSongToPlaylist(Context c, int idOnPlaylist, Song song, String fromWhere) {
        Playlist p=playlists.get(idOnPlaylist);
        ContentResolver resolver = c.getContentResolver();
         Uri playlistUri = MediaStore.Audio.Playlists.Members.getContentUri(fromWhere, p.getID());
       // Uri songUri = Uri.withAppendedPath(playlistUri, MediaStore.Audio.Playlists.Members.CONTENT_DIRECTORY);

        // int songOrder = 1;

            ContentValues songValues = new ContentValues();

            songValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID,   song.getId());
           songValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, p.getSongIds().size()+1);

           Uri u = resolver.insert(playlistUri, songValues);

       // Logger.write(u.toString());
      //  Logger.write(idOnPlaylist+">"+p.getName()+">"+song.getTitle());
        playlists.get(idOnPlaylist).add(song.getId());
       // playlists.add(idOnPlaylist, p);
       // return newPlaylist;
            //songOrder++;



      /*

        Uri playlistUri = ((fromWhere == "internal") ?
                android.provider.MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI:
                android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI);
       // Uri newPlaylistUri = resolver.query(playlistUri, values);
        Cursor c= resolver.query(playlistUri, values);
        Uri songUri = Uri.withAppendedPath(newPlaylistUri, MediaStore.Audio.Playlists.Members.CONTENT_DIRECTORY);
        int songOrder = 1;

        for (Song song : songsToAdd) {

            ContentValues songValues = new ContentValues();

            songValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID,   song.getId());
            songValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, songOrder);

            resolver.insert(songUri, songValues);
            songOrder++;
        }*/
    }
    public void removeFromPlayList(Context c, Song song, String fromWhere) {
        Playlist p=song.getPlayList();
        ContentResolver resolver = c.getContentResolver();
        Uri playlistUri = MediaStore.Audio.Playlists.Members.getContentUri(fromWhere, p.getID());
        // Uri songUri = Uri.withAppendedPath(playlistUri, MediaStore.Audio.Playlists.Members.CONTENT_DIRECTORY);

        // int songOrder = 1;
        resolver.delete(playlistUri, MediaStore.Audio.Playlists.Members.AUDIO_ID+" = ?", new String[]{ ""+song.getId()});
        int i=0;
        for(Playlist pl:playlists) {
            if(pl.getID()==p.getID()) {
                Logger.write(pl.getName());
               playlists.get(i).remove(song.getId());
            }
            i++;
        }


        // Logger.write(u.toString());
       // Logger.write(idOnPlaylist+">"+p.getName()+">"+song.getTitle());
       // playlists.get(idOnPlaylist).add(song.getId());
    }
}

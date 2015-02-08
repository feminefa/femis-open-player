package femi.com.ng.core.libs;

import org.json.JSONObject;

import femi.com.ng.kure.musicplayer.model.Song;


/**
 * Represents a single audio file on the Android system.
 *
 * It's a simple data container, filled with setters/getters.
 *
 * Only mandatory fields are:
 * - id (which is a unique Android identified for a media file
 *       anywhere on the system)
 * - filePath (full path for the file on the filesystem).
 */
public class Orin extends Song {

	private long id;
	private String filePath, hashCode, jsonString;
	private Long dateTagged;
    boolean explicit=false;
    String orimi_type="orimi_songs";
    double price = 0.00;
    String releaseDate;
	

	/**
	 * Creates a new Song, with specified `songID` and `filePath`.
	 *
	 * @note It's a unique Android identifier for a media file
	 *       anywhere on the system.
	 */
	public Orin() {
		super(0, null);
	}
    public static Orin createFromJSON(JSONObject jo) {
        Orin  orin=new Orin();
        try {
            orin.setId(jo.getLong("song_id"));
        }catch (Exception e) {}

        try {
            orin.setTitle(jo.getString("title"));
        }catch (Exception e) {}
        try {
            orin.setArtist(jo.getString("artist"));
        }catch (Exception e) {}
        try {
            orin.setAlbum(jo.getString("album"));
        }catch (Exception e) {}
        try {
            orin.setAlbumArt(jo.getString("album_art_url"));
        }catch (Exception e) {}
        try {
            orin.setPrice(jo.getDouble("price"));
        }catch (Exception e) {}
        try {
            orin.setOrimiType(jo.getString("type"));
        }catch (Exception e) {}
        try {
            orin.setTrackNumber(jo.getInt("track_number"));
        }catch (Exception e) {}
        try {
            orin.setReleaseDate(jo.getString("year"));
        }catch (Exception e) {}


        return  orin;
    }
	public Orin(JSONObject jo) {
		super(0, null);
		try{
			this.id=jo.getInt("id");
			this.filePath=jo.getString("path");
		}catch(Exception e) {
			
		}
	}
	public Orin(long id, String filePath) {
		super(id, filePath);
		this.id        = id;
		this.filePath  = filePath;
	}

	/**
	 * Identifier for the song on the Android system.
	 * (so we can locate the file anywhere)
	 */
	public String getJSONString() {
		return jsonString;
	}
	public void setJSONString(String code) {
		jsonString=code;
	}
	public String getHashCode() {
		return hashCode;
	}
	public void setHashCode(String code) {
		hashCode=code;
	}

    public double getPrice() {
        return price;
    }
    public void setPrice(double code) {
        price=code;
    }

    public long getId() {
        return id;
    }
    public void setId(long code) {
        id=code;
    }

	public Long getDateTagged() {
		return dateTagged;
	}
    public boolean isExplicit() { return explicit; }
    public void isExplicit(boolean expl) { explicit=expl; }
    public String getOrimiType() {
        return orimi_type;
    }
    public void setOrimiType(String type) {
       orimi_type=type;
    }
	public boolean isOrimiAlbum() {
        return (orimi_type.equals("orimi_albums"));
    }
    public void setReleaseDate(String type) {
        releaseDate=type;
    }
    public String getReleaseDate() {
        return releaseDate;
    }
	
	public void setDateTagged(Long dateT) {
		dateTagged=dateT;
	}
}

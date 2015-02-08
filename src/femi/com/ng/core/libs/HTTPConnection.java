package femi.com.ng.core.libs;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Map;
import java.util.TimerTask;

import org.apache.http.Header;

import femi.com.ng.core.activities.MainActivity;
import android.annotation.SuppressLint;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.loopj.android.http.*;

public class HTTPConnection extends Thread { //AsyncTask<String, Void, String>  {
	String TAG = "HTTPConnection";
	AjaxCallback callback=null;
	private int responseCode=0;
	String url="";
	private String responseString="";
	SherlockActivity mActivity=null;
	public Map<String, ?> postParameters=null;
	public static AsyncHttpClient client = new AsyncHttpClient();
	File cacheDir=null;
	//private boolean cache=false;
	private boolean loadFromCache=false;
	int socketTimeout=30000;
	PersistentCookieStore pcs=null;
	 
	/*public HTTPConnection( AjaxCallback cb) {
		// TODO Auto-generated constructor stub
		callback=cb;
		mActivity=a;
		cacheDir=MainActivity.DIR_HTML_CACHE;
		if(a!=null) {
			
			 pcs= new PersistentCookieStore(mActivity);
		}
		// UnhandledExceptionHandler.setUnhandledException(Thread.currentThread());
		
	}*/
	public HTTPConnection(AjaxCallback cb) {
		// TODO Auto-generated constructor stub
		callback=cb;
		cacheDir= MainActivity.DIR_HTML_CACHE;
		loadFromCache=true;
		//mActivity=a;
		//pcs=a.myCookieStore;
		// UnhandledExceptionHandler.setUnhandledException(Thread.currentThread());
		
	}
	public HTTPConnection() {
		
	}
	public String getResponseString() {
		return responseString;
	}
	public int getResponseCode() {
		return responseCode;
	}
	@Override
	public synchronized void  run() {
		// TODO Auto-gesuper.run();
		boolean interupt=false;
		
		while(!isInterrupted() && !interupt) {
		 try {
        		 connect(url);
         //  //Log.e("SMS url", url);
           
           
         } catch (IOException e) {
           //return getString(R.string.connection_error);
         	//
        	// Log.e(TAG, "CONNECTION ERROR: "+responseCode, e);
         }
		 /*if(mActivity!=null) {
		 mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					callback.run(responseCode, responseString);
				}
			});
		 }else{
			
			 callback.run(responseCode, responseString);
		 }*/
		 interupt=true;
		 interrupt();
		}
	}
	
	public HTTPConnection enableCaching(File cacheDir, boolean loadFromCache) {
		this.cacheDir=cacheDir;
		this.loadFromCache=loadFromCache;
		return this;
	}
	public HTTPConnection enableCaching(boolean loadFromCache) {
		this.cacheDir=MainActivity.DIR_HTML_CACHE;
		this.loadFromCache=loadFromCache;
		return this;
	}
	public HTTPConnection disableCaching() {
		this.loadFromCache=false;
		return this;
	}
	/** Initiates the fetch operation. */
	
    public  synchronized void connect(String urlString) throws IOException {
        Logger.write(urlString);
        InputStream stream = null;
        String str ="";
        final File cF;
        if(cacheDir!=null) {
        	cF=new File(cacheDir, ""+urlString.hashCode());
        	cF.deleteOnExit();
        }else{
        	cF=null;
        }
       // final File cF=File.createTempFile("YAPLY_", null, cacheDir);
       if((cF != null && cF.exists()) && loadFromCache && pcs==null) {
			stream =  new FileInputStream(cF);
			char[] buffer=new char[1024];
			InputStreamReader  isr = new InputStreamReader(stream);
			
			responseString="";
			int len;
			while ((len=isr.read(buffer))>0) {
				if(isInterrupted()) break;
				responseString+=new String(buffer, 0, len)	;	
				 
			}
			//_timer.cancel();
			responseCode=200;
			 callback.run(responseCode, responseString);
			 return;
		}

        try {
        	
           // stream = downloadUrl(urlString);
           
           // str = readIt(stream, null);
           // Log.e("LENT", ""+str.length());
        	AsyncHttpResponseHandler respHandler=new AsyncHttpResponseHandler() {
        		@Override
        		 public void onFailure(int statusCode, Header[] header, byte[] response,
        				Throwable arg3) {
        			// TODO Auto-generated method stub
        			//super.onFailure(statusCode, header, response, arg3);
        			// Log.e("ERRRROR", arg3.getMessage(), arg3);
        			//responseCode=statusCode;
        			//responseString=new String(response);
                    Logger.write("Code: "+statusCode);
        			callback.run(statusCode, "");
        		}
        		@Override
        		public void onProgress(int bytesWritten, int totalSize) {
        			// TODO Auto-generated method stub
        			//super.onProgress(bytesWritten, totalSize);
        			 //Log.e(TAG,bytesWritten+"/"+totalSize);
        		}
        		@Override
        		public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        			// TODO Auto-generated method stub
        			super.onSuccess(statusCode, headers, responseBody);
        			responseCode=statusCode;
        			responseString=new String(responseBody);
        			//if(c) {
        				if(cF.exists()) cF.delete();
        				try{
	        				FileOutputStream fio=new FileOutputStream(cF);
	        				fio.write(responseString.getBytes());
        				}catch(Exception e) {}
        			//}
        			 callback.run(responseCode, responseString);
        			
        		}
        	};
        	//client.addHeader(urlString, str);
        	
        	client.setTimeout(socketTimeout);
            client.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        	if(pcs!=null) {
        	//	Log.e("USING COOKIE", "YES");
        		client.setCookieStore(pcs);
        	}else Log.e("USING COOKIE", "NO");
		//	_timer.schedule(new Timeout(this), socketTimeout);
        	if(postParameters!=null) {
        		RequestParams rq=new RequestParams();
        		 for (Map.Entry<String, ?> entry : postParameters.entrySet()) {  
    		    	 if(entry.getValue() instanceof String) {
    		    		  rq.put(entry.getKey(), entry.getValue());
    		    	 }
    		    	 if(entry.getValue() instanceof InputStream ){
    		    		 rq.put(entry.getKey(), (InputStream)entry.getValue(), "file.jpg", "image/jpg");
    		    	 }
    		    	 if(entry.getValue() instanceof File ){
    		    		 rq.put(entry.getKey(), (File)entry.getValue());
    		    	 }
    		    	
    		      }
        		//Log.e("REQUEST PARAM", rq.toString());
        		client.post(urlString, rq, respHandler);
        	}else{
        		client.get(urlString, respHandler);
        	}
        	
        	
       } catch(Exception e) {
    	  Log.e("ERRRROR",  e.getMessage(), e);
       }finally {
           if (stream != null) {
               stream.close();
            }
        }
       
       // return str;
    }

/*
	public InputStream downloadUrl(String urlString) throws IOException {
		
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setReadTimeout(60*1000 );// milliseconds 
        conn.setConnectTimeout(20000 );// milliseconds 
       
        conn.setDoInput(true);
        conn.setDoOutput(true);
        // Start the query
        if (postParameters != null) {
        	
        	String paramString="";
        	 String crlf = "\r\n";
			  String twoHyphens = "--";
			  String boundary =  "*****";
			  List<File> files=new ArrayList<File>();
			  List<String> fParamNames=new ArrayList<String>();
        	  for (Map.Entry<String, ?> entry : postParameters .entrySet()) { 
        		  if(entry.getValue() instanceof File) {
        			files.add((File)entry.getValue());
        			fParamNames.add(entry.getKey()) ;
        		  }else{
	        		  if(paramString.length()>0) paramString+="&";
	        		  paramString+=entry.getKey()+"="+entry.getValue().toString(); 
        		  }
        	  }
        	  if(files.size()==0) {
        		 
	            conn.setRequestMethod("POST");
	            conn.setFixedLengthStreamingMode(
	            		paramString.getBytes().length);
	            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	          //send the POST out
	            PrintWriter out = new PrintWriter(conn.getOutputStream());
	            out.print(paramString);
	            out.close();
        	  }else{
        		 
        		  conn.setRequestProperty("Connection", "Keep-Alive");
    			  conn.setRequestProperty("Cache-Control", "no-cache");
    			  conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
    			  DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
    			  outputStream.writeBytes(twoHyphens + boundary + crlf);
    			  
    			  for(int i=0; i<files.size(); i++) {
    				  outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fParamNames.get(i) + "\";filename=\"" + files.get(i).getAbsolutePath() + "\"" + crlf);
        			  outputStream.writeBytes(crlf);
	    			 
	    			  InputStream is = new FileInputStream(files.get(i));
	    			  int bytesAvailable=is.available();
	    			  byte[] buffer=new byte[Math.min(bytesAvailable, (1024*1024))];
	    			  int read=0;
	    			  while((read=is.read(buffer)) > 0) {
	    				  outputStream.write(buffer, 0, read);
	    				 // //Log.e("WRITEN BYTE", new String(buffer));
	    			  }
	    			  outputStream.writeBytes(crlf);
	    			  outputStream.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
	    			  is.close();
	    			}
    			 // request.flush();
    			 // is.close();
        	  }
        	  
            
        }else{
        	Log.e("IT IS", "GEt");
        	 conn.setRequestMethod("GET");
        	conn.connect();
        }
        responseCode=conn.getResponseCode();
      
        InputStream stream = conn.getInputStream();
        return stream;

    }
*/
    /** Reads an InputStream and converts it to a String.
     * @param stream InputStream containing HTML from targeted site.
     * @param len Length of string that this method returns.
     * @return String concatenated according to len parameter.
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
   /* private static String readIt(InputStream stream, Integer len) throws IOException, UnsupportedEncodingException {
        //Reader reader = null;
       // reader = new InputStreamReader(stream, "UTF-8");
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        if(len==null) {
        	byte[] buffer=new byte[1024];
        	int n=0;
        	String read=reader.readLine();
        //	String mLine = reader.readLine();
            while(read != null)
            {
            	// process line
            	sb.append(read);
            	read = reader.readLine();
            }

            reader.close();
        	/*while((n=stream.read(buffer))>0) {
        		read+=new String(buffer, 0, n);
        	}*/
/*        	Log.e("Stream len", ""+sb.toString().length());
        	//return read;
        	return sb.toString();
        }
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
*/
public class HTTPResponseCallBack implements AjaxCallback {
	public void run(int code, String response) {
		//callback(code, response);
	}
	
}
@SuppressLint("NewApi")
public void load(String url) {
	load(url, null);
	/*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		 
      	this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
  	}else{
  		this.execute(url);
  	}*/
}
public void load(String url, Map<String, Object> postParams) {
	this.url=url;
	postParameters=postParams;
	
	this.start();
	/*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		 
      	this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
  	}else{
  		this.execute(url);
  	}*/
}
public void syncLoad(String url) {
	syncLoad(url, null);
	/*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		 
      	this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
  	}else{
  		this.execute(url);
  	}*/
}
public void syncLoad(String url, Map<String, Object> postParams) {
	this.url=url;
	postParameters=postParams;
	this.run();
	/*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		 
      	this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
  	}else{
  		this.execute(url);
  	}*/
}
class Timeout extends TimerTask {
   // private int timeOut=10000;
	private HTTPConnection connection;
    public Timeout(HTTPConnection c) {
        //this.timeOut=timeOut;
    	connection=c;
    }

    @Override
    public void run() {
        Log.e(TAG,"Timed out while downloading.");
        connection.interrupt();
        connection=null;
    }
};
public interface AjaxCallback {
	public void run(int code, String response);
}
}

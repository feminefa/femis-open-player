package femi.com.ng.core.libs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

import femi.com.ng.core.activities.MainActivity;

import android.util.Log;

public abstract class Logger {
	public static void write(String fileName, String text) {
		Log.e("ERORORORORO", text);
		if(fileName.length()==0) fileName="Log";
		 String rootPath= MainActivity.DIR_ROOT.getPath();
		 MainActivity.DIR_CACHE=new File(rootPath+File.separator+MainActivity.DIR_CACHE.getName()+File.separator+"log");
		 MainActivity.DIR_CACHE.mkdirs();
		 File file=new File(MainActivity.DIR_CACHE, fileName+".txt");
		 try{
			 //if(file.exists()) file.delete();
		 file.createNewFile();
		 OutputStream os=new FileOutputStream(file);
		 os.write(text.getBytes());
		 os.close();
		 }catch(Exception e) {
			 
		 }
	 }
	 public static void write(String tag, Exception pv) {
		 StackTraceElement[] elements=pv.getStackTrace();
			String string=""+new Date().toString()+"\n";//pv.getCause().getMessage()+"\n";		
			for(int i=0; i<elements.length; i++) {
				string+=elements[i].getLineNumber()+"->"+elements[i].getMethodName()+"->"+elements[i].getClassName()+"->"+elements[i].getFileName()+"\n";
			}			
			//write(tag, string);
			Log.e(tag, pv.getMessage(), pv);
	 }
	 public static void write(Exception pv) {
		 write("ERROR", pv);
	 }
	 public static void write(String pv) {
		 write("", pv);
	 }
	
}

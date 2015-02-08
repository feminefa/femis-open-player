package femi.com.ng.core.libs;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import femi.com.ng.core.activities.MainActivity;
import femi.com.ng.R;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;

public class MyImageLoader extends ImageLoader {
	

	 public  ImageLoaderConfiguration config;
	 static Context activity;
	public MyImageLoader(Context a) {
		activity=a;
		L.disableLogging();
		ImageLoader.getInstance();		
		
		
		 String root = Environment.getExternalStorageDirectory().getAbsolutePath();
    	 File DIR_ROOT= MainActivity.DIR_ROOT;
    	
    	 String rootPath=DIR_ROOT.getPath();
    	// DIR_CACHE=new File(rootPath+File.separator+DIR_CACHE.getName());
    	
		
		//cache_dir=new File(DIR_ROOT,MainActivity.DIR_CACHE.getName()).getAbsolutePath();
	//	Log.e("cache directory", cacheDir.getAbsolutePath());
		 config = new ImageLoaderConfiguration.Builder(activity)
		 
		 // You can pass your own memory cache implementation
		.discCache(new UnlimitedDiscCache(getImageCacheDir())) // You can pass your own disc cache implementation
		.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
		.build();
		 this.init();
	}
	public static Bitmap getCachedImage(MainActivity a, String url) {
		File cachFile = new File(getImageCacheDir()+File.separator+url.hashCode());
		if(cachFile.exists()) {
			return BitmapFactory.decodeFile(cachFile.getAbsolutePath());
		}else return null;
	}
    static Builder options = new DisplayImageOptions.Builder();


	public static ImageLoader roundCorner(MainActivity a, int radius) {
		ImageLoader iLoader=MyImageLoader.getInstance();

        options.showImageForEmptyUri(R.drawable.big_logo)
                .showImageOnFail(R.drawable.big_logo)
                .showStubImage(R.drawable.big_logo)
                .cacheInMemory(false)
                .cacheOnDisc(true)
                .delayBeforeLoading(2)
                .displayer(new FadeInBitmapDisplayer(3))
                .build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(a.getApplicationContext())
		.discCache(new UnlimitedDiscCache(getImageCacheDir())) // You can pass your own disc cache implementation
		.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
		.build();		
		iLoader.init(config);
		return iLoader;
	}
	public static File getImageCacheDir() {
		File cacheDir = new File(MainActivity.DIR_CACHE.getName());
		File imageCacheDir = new File(cacheDir, "images");
		if(!imageCacheDir.exists()) imageCacheDir.mkdir();
		return imageCacheDir;
	}
	public  void init() {
		this.init(config);
	}
	public Listener getListener() {
		return new Listener();
	}
	public class Listener extends SimpleImageLoadingListener {
	 final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
	@Override
	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		// Empty implementation
		File cachFile = new File(getImageCacheDir()+File.separator+imageUri.hashCode());
		
		DiscCacheUtil.removeFromCache(imageUri, MyImageLoader.this.getDiscCache());
		if(cachFile.exists()) {
			cachFile.delete();
		}
		cachFile.deleteOnExit();
	}
	
	
	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		if (loadedImage != null) {
			ImageView imageView = (ImageView) view;
			  boolean firstDisplay = !displayedImages.contains(imageUri);
			//if(loadedImage==null) Log.e("LODING","IMAGE NOT LOADED");
			if (firstDisplay) {
				//FadeInBitmapDisplayer.animate(imageView, 500);
				//imageView.setImageBitmap(loadedImage);
				   displayedImages.add(imageUri);
			}
			//Log.e("IMAGE URI LOADED", imageUri.hashCode()+"=>"+imageUri); 
		}
	}
	}
	public void displayImage(String photoPath, ImageView imageView) {
		this.displayImage(photoPath, imageView, getDisplayOptions(), getListener());
	}
	public static DisplayImageOptions getDisplayOptions() {
		return getDisplayOptions(null);
	}
	public static DisplayImageOptions getDisplayOptions(Integer roundness) {
        Builder b = new DisplayImageOptions.Builder();
        if(roundness==null) {
                    b.showImageForEmptyUri(R.drawable.big_logo)
                    .showImageOnFail(R.drawable.big_logo)
                    .showStubImage(R.drawable.big_logo)
                    .cacheInMemory(false)
                    .cacheOnDisc(true)
                    .delayBeforeLoading(0);
            b.displayer(new FadeInBitmapDisplayer(3));
        }else {
            b=options;
        }
		return b.build();
	}
}

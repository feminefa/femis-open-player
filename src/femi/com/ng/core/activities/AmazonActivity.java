package femi.com.ng.core.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import femi.com.ng.R;
import femi.com.ng.core.libs.Logger;

/**
 * Created by femi on 08/09/2014.
 */
public class AmazonActivity extends SherlockFragmentActivity {
    WebView browser;
    ProgressDialog pd;
    String url ="http://myorimi.com:8081/amazon.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orimi_amazon_layout);
        pd=new ProgressDialog(this);
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");
        browser=(WebView)findViewById(R.id.store_webview);
        WebViewClient wvc=new StoreBrowser();

        browser.setWebViewClient(wvc);
        open();
    }
    public void open(){
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        browser.setBackgroundColor(0x00000000);
        //  url=MainActivity.AMAZON_URL+"?w="+browser.getHeight()+"&h="+browser.getWidth();
        browser.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                url+=(browser.getWidth()-20)+"&h="+(browser.getHeight()-10);
                browser.loadUrl(url);
                Logger.write(url);
            }
        });


    }
    private class StoreBrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            pd.show();

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            pd.dismiss();
        }
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
}

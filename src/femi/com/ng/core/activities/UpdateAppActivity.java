package femi.com.ng.core.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import femi.com.ng.core.libs.HTTPConnection;
import femi.com.ng.R;
import femi.com.ng.core.libs.Logger;

/**
 * Created by femi on 07/09/2014.
 */
public class UpdateAppActivity  extends SherlockFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();

        setContentView(R.layout.orimi_update_required);
        Button updateBut=(Button)findViewById(R.id.updateBut);
        Button closebut=(Button)findViewById(R.id.closeBut);
        updateBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bundle.getString("url")));
                startActivity(browserIntent);
            }
        });
        closebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UpdateAppActivity.this.finish();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
       // MainActivity.mainActivity.finish();
        UpdateAppActivity.this.finish();
    }

    public static void checkForUpdate(final  SherlockFragmentActivity c, String url) {

        new HTTPConnection(new HTTPConnection.AjaxCallback() {
            @Override
            public void run(int code, String response) {
                String download_url="";
               // code=200;
               // response="#download_url=http://orimi.com.ng/download\n1.2 required";
                Logger.write(response);
                if(code==200) return;
                if(code==200) {
                    String[] s=response.split("\n");
                    for(String line:s) {
                        line=line.trim();
                        if(line.length()==0) continue;
                        if(line.startsWith("#download_url")) {
                            String[] m=line.split("=");
                            download_url=m[1];
                        }

                        if(line.startsWith("#")) continue;
                        line=line.replace(" ", "\t");
                        String[] str=line.split("\t");

                        try {
                            PackageInfo info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
                           // Logger.write("Femi: "+Double.parseDouble(str[0]) +" - "+Double.parseDouble(info.versionName));
                            if(Double.parseDouble(str[0]) > Double.parseDouble(info.versionName)) {
                                try {
                                    if (str[1].equals("required")) {
                                        Intent intent = new Intent(c, UpdateAppActivity.class);
                                        Bundle b = new Bundle();
                                        b.putString("url", download_url);
                                        intent.putExtras(b);
                                        intent.setAction(Intent.ACTION_MAIN);
                                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                        c.startActivity(intent);
                                        MainActivity.mainActivity.finish();
                                        return;
                                    }else showUpateAlert(c, download_url);
                                }catch (Exception e){
                                    showUpateAlert(c, download_url);
                                }
                            }

                        }catch (Exception e) {
                            Logger.write(e);
                        }
                        break;
                    }
                }
            }
        }).disableCaching().load(url);

    }
    public static void showUpateAlert(final SherlockFragmentActivity c, String dUrl) {
       final  OnClick onClick=new OnClick(c, dUrl);
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.createAlertDialog(c, "Update Available", "A new version of "+c.getString(R.string.app_name)+" exists.")
                        .setPositiveButton("Update now", onClick)
                        .setNegativeButton("Remind me later", onClick).create().show();
                ;
            }
        });


    }
    static class OnClick implements DialogInterface.OnClickListener {
        Context context;
        String downloadUrl;
        OnClick(Context c, String dUrl) {
            context=c;
            downloadUrl=dUrl;
        }
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if(i==DialogInterface.BUTTON_POSITIVE) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                context.startActivity(browserIntent);
                dialogInterface.dismiss();
            }
            if(i==DialogInterface.BUTTON_NEGATIVE) {
                dialogInterface.dismiss();
            }
        }
    }

}

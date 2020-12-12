package com.icche.rudf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.blog.library.UpdateChecker;
import com.google.android.material.navigation.NavigationView;


public class About extends AppCompatActivity {

    String fbapp = "fb://group/49880688703";
    String fburl = "https://www.facebook.com/groups/bfdf.ru/";
    String pageApp = "fb://page/169680089735915";
    String pageurl = "https://www.facebook.com/rubfdf/";
    private Context liContext = null;

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isNetworkStatusAvialable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                return netInfos.isConnected();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 22) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_about);
        liContext = this.getApplicationContext();

    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        *//*MenuItem item = menu.findItem(R.id.nav_about);
        item.setVisible(false);*//*
        return true;
    }*/
  /* @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         MenuItem menuItem = menu.findItem(R.id.refresh);
         menuItem.setVisible(false);
         return true;
     }*/

   /*  @Override
   public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_about_app:
                Intent h = new Intent(About.this, AboutApp.class);
                startActivity(h);
                return true;
            case R.id.pro:
                Intent p = new Intent(About.this, ProfileActivity.class);
                startActivity(p);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

}

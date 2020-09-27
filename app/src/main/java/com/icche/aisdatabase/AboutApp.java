package com.icche.aisdatabase;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blog.library.AppUtils;

public class AboutApp extends AppCompatActivity {
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 22) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_about_app);
        TextView textView = findViewById(R.id.version);
        textView.setText("Current version: " + AppUtils.getVersionName(this));
        Button button = findViewById(R.id.feedback);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/email");
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.feedbackMail)});
                intent.putExtra(Intent.EXTRA_SUBJECT, (getString(R.string.feedbacksub)));
                intent.putExtra(Intent.EXTRA_TEXT,(getString(R.string.msg_feedback)));
                startActivity(Intent.createChooser(intent, (getString(R.string.feedTitle))));
            }
        });
    }

}

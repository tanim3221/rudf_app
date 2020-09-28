package com.icche.aisdatabase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DisplaySQLiteDataActivity extends AppCompatActivity {

    SQLiteHelper sqLiteHelper;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;
    ListAdapter listAdapter;
    ListView LISTVIEW;

    ArrayList<String> ID_Array;
    ArrayList<String> TITLE_Array;
    ArrayList<String> MESSAGE_Array;
    ArrayList<String> TIME_Array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        LISTVIEW = (ListView) findViewById(R.id.listView1);

        ID_Array = new ArrayList<String>();
        TIME_Array = new ArrayList<String>();
        TITLE_Array = new ArrayList<String>();
        MESSAGE_Array = new ArrayList<String>();

        sqLiteHelper = new SQLiteHelper(this);

    }

    @Override
    protected void onResume() {

        ShowSQLiteDBdata();

        super.onResume();
    }

    private void ShowSQLiteDBdata() {

        sqLiteDatabase = sqLiteHelper.getWritableDatabase();

        cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_NAME + "", null);

        ID_Array.clear();
        TITLE_Array.clear();
        MESSAGE_Array.clear();
        TIME_Array.clear();

        if (cursor.moveToFirst()) {
            do {

                ID_Array.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_ID)));
                TITLE_Array.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_1_Title)));
                MESSAGE_Array.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_2_Message)));
                TIME_Array.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_3_Time)));


            } while (cursor.moveToNext());
        }

        listAdapter = new com.icche.aisdatabase.ListAdapter(DisplaySQLiteDataActivity.this,
                ID_Array,
                TITLE_Array,
                MESSAGE_Array,
                TIME_Array
        );

        LISTVIEW.setAdapter(listAdapter);

        cursor.close();
    }
}

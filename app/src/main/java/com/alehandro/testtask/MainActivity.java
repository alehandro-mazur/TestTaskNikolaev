package com.alehandro.testtask;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //database variables
    private SQLiteDatabase database;
    private Cursor cursor;

    //constants
    private final String DATABASE_NAME = "TaskDataBase";
    private final String TABLE_NAME = "numbered";
    private final String DEFAULT_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + " (id INTEGER,first_name VARCHAR,last_name VARCHAR);";
    private final String TEST_TASK_LOG = "MyTask";
    private final int DEFAULT_MAX_ELEMENTS_IN_MEMORY = 50;
    private final int MAX_ITEMS_AMOUNT = 500;


    //table variables
    private String firstName;
    private String lastName;
    private int id;

    //list variables
    private ArrayList<String> items;
    private ListView listView;
    private ArrayAdapter<String> myAdapter;
    private boolean islistAlreadyShowed;
    private boolean isNotified;

    //fetch variables
    private boolean isInitialized;
    private ProgressDialog dialog;
    private FetchTask fetchTask;
    private int fetchedRange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize dialog
        dialog = new ProgressDialog(this);
        dialog.setTitle("TestTask");
        dialog.setMessage("Loading...Please wait");

        fetchedRange = 0;

        listView = (ListView) findViewById(R.id.lvItems);

        items = new ArrayList<>();

        fetchData();



        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                int lastItem = firstVisibleItem + visibleItemCount;

                //end of list has been reached
                if (lastItem == totalItemCount & visibleItemCount > 1 & isNotified) {
                    fetchData();
                    Log.d(TEST_TASK_LOG, "fetch from end of list");
                }
            }
        });


    }


    private void fetchData() {

        if (fetchedRange < MAX_ITEMS_AMOUNT) {
            fetchTask = new FetchTask();
            fetchTask.execute();
        } else {
            Toast.makeText(this, "You have reached end of list", Toast.LENGTH_SHORT);
        }
    }

    private void fillDatabase() {
        for (int i = 0; i < MAX_ITEMS_AMOUNT; i++) {
            firstName = "first_name " + i;
            lastName = " last_name " + i;
            id = i;

            database.execSQL("INSERT INTO " + TABLE_NAME + " VALUES('" + id + "', '" + firstName + "', " +
                    "'" + lastName + "' );");
        }
        isInitialized = true;
    }

    private void getDataFromDB() {



        cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " LIMIT " + fetchedRange + ", "
                + DEFAULT_MAX_ELEMENTS_IN_MEMORY, null);

        while (cursor.moveToNext()) {
            items.add(cursor.getString(1) + " " + cursor.getString(2));
        }

        fetchedRange = fetchedRange + DEFAULT_MAX_ELEMENTS_IN_MEMORY;
    }

    private void showList() {
        if (myAdapter == null) {
            myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                    android.R.id.text1, items);
        }
        listView.setAdapter(myAdapter);
        islistAlreadyShowed = true;
        isNotified=true;

    }


    private class FetchTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
            isNotified=false;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!isInitialized) {
                //create database
                database = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
                //create table
                database.execSQL(DEFAULT_TABLE);
                fillDatabase();
                isInitialized = true;
            }
            getDataFromDB();

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (!islistAlreadyShowed) {
                showList();
            } else {
                myAdapter.notifyDataSetChanged();
                Log.d(TEST_TASK_LOG,"adapter is notified");
                isNotified=true;
            }
        }
    }
}








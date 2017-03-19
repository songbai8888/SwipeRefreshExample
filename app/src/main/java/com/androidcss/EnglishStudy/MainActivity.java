package com.androidcss.EnglishStudy;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static SortedMap<Integer,String> type_name=new TreeMap<Integer, String>();
    public static final int READ_TIMEOUT = 30000;
    public static boolean isUpdated = false;
    private RecyclerView mRVEnglishContent;
    private LinearLayoutManager mLayoutManager;
    private AdapterEnglishContent mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private String save_listdata = "";
    private String data_dir = "";
    private File saveDir;
    public static List<DataEnglishContent> data;
    public static int currentSelected = 0;
    private static int currentPage = 1;
    private static int totalPages = 0;
    private static String prevUrl = "";
    private static String nextUrl = "";
    private static int lastVisibleItem = 0;
    private static int refreshState = 0; //0: new data, 1: more data
    private static List<Integer> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        type_name.put(1, "VOA Specials");
        type_name.put(2, "VOA Corresponds");
        type_name.put(3, "VOA Readings");

        mLayoutManager = new LinearLayoutManager(this);
        mRVEnglishContent = (RecyclerView)findViewById(R.id.fishPriceList);
        data=new ArrayList<>();
        ids=new ArrayList<>();
        mAdapter = new AdapterEnglishContent(MainActivity.this, data);
        mRVEnglishContent.setAdapter(mAdapter);
        mRVEnglishContent.setLayoutManager(mLayoutManager);
        mRVEnglishContent.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
        save_listdata = getApplicationInfo().dataDir + "/save.dat";

        data_dir = getApplicationInfo().dataDir + "/mp3";
        saveDir = createDiretory(data_dir);

        mRVEnglishContent.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!checkInternetConenction()){
                    mSwipeRefreshLayout.setRefreshing(false);
                    return ;
                }
                //Log.i("Hello", String.valueOf(lastVisibleItem) + ":" + String.valueOf(mAdapter.getItemCount()));
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == mAdapter.getItemCount()) {
                    refreshState = 1;
                    //Log.i("Hello", "Need more data...." + MainActivity.currentPage);
                    new AsyncFetch().execute();
                }
                else {
                    refreshState = 0;
                    //Log.i("Hello", "00000...." + MainActivity.currentPage);
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });

        // Swipe Refresh Layout
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swifeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncFetch().execute();
            }


        });

        //Make call to AsyncTask
        if(checkInternetConenction()){
            new AsyncFetch().execute();
        }else {
            loadFromLocal();
        }

    }

    private void loadFromLocal(){
        try {
            readFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for(DataEnglishContent mdata: data){
            if(ids.contains(mdata.id)){
                continue;
            }else{
                ids.add(mdata.id);
            }
        }
        mAdapter.data = data;
        mRVEnglishContent.setAdapter(mAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private class AsyncFetch extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
           /* pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();*/

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                if(refreshState == 0){
                    url = new URL("http://192.168.0.103:8000/englishpapercontent/");
                    //Log.i("Hello", "New data ");
                }
                else{
                    url = new URL(nextUrl);
                    //Log.i("Hello", nextUrl);
                }


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return ("unsuccessful");
            }

            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoOutput to true as we recieve data from json file
                //Android 4.0 does't not need this for get method.
                //conn.setDoOutput(true);

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return ("unsuccessful");
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return ("unsuccessful");
            }
            finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            //pdLoading.dismiss();

            if(result.equals("unsuccessful")){
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Server is not reached! Please try again later.", Toast.LENGTH_SHORT).show();
                loadFromLocal();
                return;
            }
            try {
                JSONObject jObject = new JSONObject(result);
                MainActivity.totalPages = jObject.getInt("count");
                MainActivity.prevUrl = jObject.getString("previous");
                String tempUrl = jObject.getString("next");

                String str[] = tempUrl.split("=");
                String returnValue = str[str.length - 1];

                if(returnValue.equals("") || returnValue.equals("null")){
                    return;
                }
                int temppos = Integer.parseInt(returnValue) - 1;
                if(MainActivity.currentPage  <= temppos){
                    MainActivity.currentPage = temppos;
                    MainActivity.nextUrl = jObject.getString("next");
                }

                JSONArray jArray = jObject.getJSONArray("results");

                // Extract data from json and store into ArrayList as class objects
                for(int i=0;i<jArray.length();i++){
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    JSONObject json_data = jArray.getJSONObject(i);
                    DataEnglishContent englishContentData = new DataEnglishContent();
                    englishContentData.english_paper_title = json_data.getString("english_paper_title");
                    englishContentData.english_paper_content =json_data.getString("english_paper_content");
                    englishContentData.english_paper_media =json_data.getString("english_paper_media");
                    englishContentData.english_paper_dir = type_name.get(json_data.getInt("english_type_id"));
                    englishContentData.english_type_id = json_data.getInt("english_type_id");
                    //englishContentData.english_paper_date = dateFormat.parse(json_data.getString("english_paper_date"));
                    englishContentData.id = json_data.getInt("id");
                    if(isMp3Downloaded(englishContentData.id + ".mp3")){
                        englishContentData.is_downloaded = true;
                    }else{
                        englishContentData.is_downloaded = false;
                    }
                    if(!ids.contains(json_data.getInt("id") )){
                        data.add(englishContentData);
                        ids.add(json_data.getInt("id"));
                    }

                }

                // Setup and Handover data to recyclerview
                Collections.sort(data, new Comparator<DataEnglishContent>() {
                    @Override
                    public int compare(DataEnglishContent z1, DataEnglishContent z2) {
                        if(z1.english_paper_date == null || z2.english_paper_date == null)
                            return 0;
                        if (z2.english_paper_date.compareTo(z1.english_paper_date) <= 0)
                            return 1;
                        if (z2.english_paper_date.compareTo(z1.english_paper_date) > 0)
                            return -1;
                        return 0;
                    }
                });

                isUpdated = true;
                mAdapter.data = data;
                mRVEnglishContent.setAdapter(mAdapter);
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mRVEnglishContent.scrollToPosition(lastVisibleItem);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                this.startActivity(myIntent);
                return true;
            case R.id.menu_help:
                Toast.makeText(this, "You have selected Help Menu", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_about:
                Toast.makeText(this, "You have selected About Menu", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Hello","ResumeResumeResumeResumeResumeResumeResume");

        if(!checkInternetConenction()){
            File folder = new File(save_listdata);
            if (folder.exists()) {
                try {
                    readFromFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        mAdapter.data = data;
        mRVEnglishContent.setAdapter(mAdapter);
        mRVEnglishContent.scrollToPosition(lastVisibleItem);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Hello","PausePausePausePausePausePause");

        if(data.size()>0 && isUpdated){
            try {
                writeToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeToFile() throws IOException {
        File saveDir = new File(getApplicationInfo().dataDir);
        saveDir.mkdirs();
        File outputFile = new File(saveDir,"save.dat");
        FileOutputStream fos = new FileOutputStream(outputFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(data);
        oos.close();
    }


    private void readFromFile() throws IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream(save_listdata);
        ObjectInputStream ois = new ObjectInputStream(fis);
        data.clear();
        data = (List<DataEnglishContent>) ois.readObject();
        ois.close();
    }

    public boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            //Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
            //Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

    private File createDiretory(String dir){
        File folder = new File(dir);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (!success) {
            Toast.makeText(this, "Create directory failed:" + dir, Toast.LENGTH_LONG).show();
        }
        return folder;
    }

    private boolean isMp3Downloaded(String fileName){
        File mp3file = new File(saveDir, fileName);
        if (mp3file.exists()) {
            return true;

        }else{
            return false;
        }

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing EnglishFY")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}

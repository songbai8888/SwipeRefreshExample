package com.androidcss.EnglishStudy;


import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.media.MediaPlayer;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;
import android.app.ProgressDialog;
import android.graphics.Bitmap;

import android.net.ConnectivityManager;
import android.os.Message;

import static android.view.View.INVISIBLE;

public class StudyActivity extends AppCompatActivity implements MediaPlayer.OnErrorListener{
    private static final int  MEGABYTE = 1024 * 1024;
    private String data_dir = "";
    private File saveDir;
    private Button b1,b2,b3,b4;
    private ImageView iv;
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private int current_media = 0;
    private boolean is_localfile = false;
    private LinearLayout statusLayout1, statusLayout2, statusLayout3;

    private double startTime = 0;
    private double finalTime = 0;

    private Handler myHandler = new Handler();;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private WebView webView;
    private TextView tx1,tx2,tx3;

    private ProgressDialog progressDialog;

    private BroadcastReceiver mReceiver;
    private IntentFilter filter ;


    public static int oneTimeOnly = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_study);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        statusLayout1 = (LinearLayout)findViewById(R.id.status_1);
        statusLayout2 = (LinearLayout)findViewById(R.id.status_2);
        statusLayout3 = (LinearLayout)findViewById(R.id.status_3);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        current_media = MainActivity.currentSelected;

        b1 = (Button) findViewById(R.id.button);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button)findViewById(R.id.button3);
        b4 = (Button)findViewById(R.id.button4);

        webView = (WebView)findViewById(R.id.webView);
        tx1 = (TextView)findViewById(R.id.textView2);
        tx2 = (TextView)findViewById(R.id.textView3);

        data_dir = getApplicationInfo().dataDir + "/mp3";
        saveDir = createDiretory(data_dir);

        if(getDownloadConfiguration()){
            //Toast.makeText(getApplicationContext(), "Need to download firstly", Toast.LENGTH_SHORT).show();
            downloadMP3();
        }

        this.setTitle(MainActivity.data.get(MainActivity.currentSelected).english_paper_title);
        webView.loadDataWithBaseURL("", MainActivity.data.get(MainActivity.currentSelected).english_paper_content, "text/html", "utf-8", "");

        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setWakeMode(this.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            //mediaPlayer.setDataSource(parseUrl(MainActivity.data.get(MainActivity.currentSelected).english_paper_media).toASCIIString());
            setPlayerMediaFile(parseUrl(MainActivity.data.get(MainActivity.currentSelected).english_paper_media).toASCIIString(), MainActivity.data.get(MainActivity.currentSelected).id);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    isPrepared = true;
                    mp.start();
                    b3.performClick();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    SharedPreferences spOptions;
                    spOptions = getSharedPreferences("settings", 0);

                    boolean checked = spOptions.getBoolean("checkbox", false);

                    if(checked){
                        try {
                            mediaPlayer.stop();
                            mediaPlayer.seekTo(0);
                            isPrepared = false;
                            webView.loadDataWithBaseURL("", MainActivity.data.get(current_media).english_paper_content, "text/html", "utf-8", "");
                            //mediaPlayer.setDataSource(parseUrl(getNextPaper()).toASCIIString());
                            String url = parseUrl(getNextPaper()).toASCIIString();
                            setPlayerMediaFile(url, MainActivity.data.get(current_media).id);
                            StudyActivity.this.setTitle(MainActivity.data.get(current_media).english_paper_title);
                            //Toast.makeText(getApplicationContext(), "Waiting for next", Toast.LENGTH_SHORT).show();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        //Toast.makeText(getApplicationContext(), "Play finished", Toast.LENGTH_SHORT).show();
                        b2.performClick();
                    }

                }
            });
            mediaPlayer.setOnErrorListener(this);

        }
        catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        seekbar = (SeekBar)findViewById(R.id.seekBar);
        seekbar.setClickable(false);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                if(fromUser){
                    finalTime = mediaPlayer.getDuration();
                    startTime = mediaPlayer.getCurrentPosition();
                    mediaPlayer.seekTo((int)finalTime * progress / 100);
                }

            }
        });

        b2.setEnabled(false);

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPrepared){
                    if(!checkInternetConenction()){
                        //if file downloaded
                        String fileName = MainActivity.data.get(current_media).id + ".mp3";   // -> http://maven.apache.org/maven-1.x/maven.pdf

                        File mp3file = new File(saveDir, fileName);
                        boolean success = true;
                        if (!mp3file.exists()) {
                            Toast.makeText(getApplicationContext(), "You didn't download mp3 yet!", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        mediaPlayer.stop();
                        mediaPlayer.seekTo(0);

                        seekbar.setProgress(0);
                        isPrepared = false;
                        webView.loadDataWithBaseURL("", MainActivity.data.get(current_media).english_paper_content, "text/html", "utf-8", "");
                        try {
                            //mediaPlayer.setDataSource(data_dir + fileName);
                            setPlayerMediaFile("", MainActivity.data.get(current_media).id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        StudyActivity.this.setTitle(MainActivity.data.get(current_media).english_paper_title);
                    }

                    return;
                }

                mediaPlayer.start();

                finalTime = mediaPlayer.getDuration();
                startTime = mediaPlayer.getCurrentPosition();

                if (oneTimeOnly == 0) {
                    seekbar.setMax((int) finalTime);
                    oneTimeOnly = 1;
                }

                tx2.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        finalTime)))
                );

                tx1.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        startTime)))
                );

                seekbar.setProgress((int)startTime);
                myHandler.postDelayed(UpdateSongTime,100);
                b2.setEnabled(true);
                b3.setEnabled(false);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Pausing sound",Toast.LENGTH_SHORT).show();
                        mediaPlayer.pause();
                b2.setEnabled(false);
                b3.setEnabled(true);
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp+forwardTime)<=finalTime){
                    startTime = startTime + forwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    //Toast.makeText(getApplicationContext(),"You have Jumped forward 5 seconds",Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(getApplicationContext(),"Cannot jump forward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp-backwardTime)>0){
                    startTime = startTime - backwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    //Toast.makeText(getApplicationContext(),"You have Jumped backward 5 seconds",Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(getApplicationContext(),"Cannot jump backward 5 seconds",Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }
                }
            }
        };
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            tx1.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            if(mediaPlayer.isPlaying()){
                webView.findAllAsync(tx1.getText().toString());
                try {
                    for (Method m : WebView.class.getDeclaredMethods()) {
                        if (m.getName().equals("setFindIsUp")) {
                            m.setAccessible(true);
                            m.invoke((webView), true);
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            seekbar.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };

    public URI parseUrl(String surl) throws Exception
    {
        if(surl.length() == 0 || surl == null){
            return null;
        }
        URL u = new URL(surl);
        return new URI(u.getProtocol(), u.getAuthority(), u.getPath(), u.getQuery(), u.getRef());
    }

    //checkInternetConenction();
    //downloadImage("http://www.tutorialspoint.com/green/images/logo.png");
    private void downloadImage(final String urlStr, final File directory) {
        progressDialog = ProgressDialog.show(this, "", "Downloading mp3 file...");
        final String urlstr = urlStr;


        new Thread() {
            public void run() {

                Message msg = Message.obtain();
                msg.what = 1;

                try {

                    URI su = parseUrl(urlStr);
                    URL url = new URL(su.toASCIIString());

                    //InputStream inputStream = urlConnection.getInputStream();
                    InputStream inputStream = openHttpConnection(su.toASCIIString());
                    FileOutputStream fileOutputStream = new FileOutputStream(directory);

                    byte[] buffer = new byte[MEGABYTE];
                    int bufferLength = 0;
                    while((bufferLength = inputStream.read(buffer))>0 ){
                        fileOutputStream.write(buffer, 0, bufferLength);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    Bundle b= new Bundle();
                    b.putInt("len", MEGABYTE);
                    msg.setData(b);
                    inputStream.close();
                }catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                messageHandler.sendMessage(msg);
            }
        }.start();
    }

    private InputStream openHttpConnection(String urlStr) {
        InputStream in = null;
        int resCode = -1;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }

            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            resCode = httpConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //ImageView img = (ImageView) findViewById(R.id.imageView);
            //img.setImageBitmap((Bitmap) (msg.getData().getParcelable("bitmap")));
            Toast.makeText(getApplicationContext(), "Download ok", Toast.LENGTH_SHORT).show();
            MainActivity.data.get(current_media).is_downloaded = true;
            progressDialog.dismiss();
            setPlayerMediaFile("", MainActivity.data.get(current_media).id);
        }
    };

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
            Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        //Toast.makeText(this, "what:" + what + " Extra:" + extra, Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_study, menu);
        SharedPreferences settings = getSharedPreferences("settings", 0);
        boolean isChecked = settings.getBoolean("checkbox", false);
        MenuItem item = menu.findItem(R.id.menu_autonext);
        item.setChecked(isChecked);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_autonext:
                item.setChecked(!item.isChecked());
                SharedPreferences settings = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("checkbox", item.isChecked());
                editor.commit();
                return true;
            case  android.R.id.home:
                //startActivity(new Intent(this, MainActivity.class));
                mediaPlayer.reset();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getNextPaper(){
        current_media = current_media + 1;
        if(current_media > MainActivity.data.size()){
            current_media = 0;
            return MainActivity.data.get(current_media).english_paper_media;
        }
        else {
            return MainActivity.data.get(current_media).english_paper_media;
        }
    }

    private boolean getDownloadConfiguration(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean download_switch = SP.getBoolean("download_switch",false);
        return download_switch;
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

    private void setPlayerMediaFile(String url, int pos){
        String fileName = pos + ".mp3";   // -> http://maven.apache.org/maven-1.x/maven.pdf
        oneTimeOnly = 0;
        File mp3file = new File(saveDir, fileName);
        try {
            if (mp3file.exists()) {
                setStatusbarVisibility(1);
                //Toast.makeText(getApplicationContext(), fileName + "  downloaded, Play local!", Toast.LENGTH_LONG).show();
                FileInputStream mp3 = new FileInputStream(data_dir + "/" + fileName);
                mediaPlayer.reset();
                mediaPlayer.seekTo(0);
                mediaPlayer.setDataSource(mp3.getFD());
                mediaPlayer.prepare();
                is_localfile = true;
            }else{
                if(url != null || url != "")
                {
                    setStatusbarVisibility(1);
                    is_localfile = false;
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepareAsync();
                }
                else{
                    setStatusbarVisibility(0);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setStatusbarVisibility(int status){
        if(status == 1){
            statusLayout1.setVisibility(View.VISIBLE);
            statusLayout2.setVisibility(View.VISIBLE);
            statusLayout3.setVisibility(View.VISIBLE);        }
        else{
            statusLayout1.setVisibility(View.GONE);
            statusLayout2.setVisibility(View.GONE);
            statusLayout3.setVisibility(View.GONE);
        }
    }

    private void downloadMP3(){
        String fileUrl =  MainActivity.data.get(current_media).english_paper_media;   // -> http://maven.apache.org/maven-1.x/maven.pdf
        String fileName = MainActivity.data.get(current_media).id + ".mp3";   // -> http://maven.apache.org/maven-1.x/maven.pdf

        File mp3file = new File(saveDir, fileName);
        if (mp3file.exists()) {
            //Toast.makeText(getApplicationContext(), fileName + "  downloaded, Play local!", Toast.LENGTH_SHORT).show();
            return;

        }
        try{
            mp3file.createNewFile();
            downloadImage(fileUrl,mp3file);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.reset();
        unregisterReceiver(mReceiver);
        //mediaPlayer.release();
        finish();
    }

}
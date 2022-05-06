package com.example.music;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private int songposition;
    private volatile boolean issongplaying;
    private  MediaPlayer mp;
    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSION_COUNT = 2;
    private static final int REQUEST_PERMISSION = 1234;
    private List<String>musicFileList;
    private boolean isMusicPlayerInt=false;
    private  File[] files;
    private File dir;
    private  String musicpath;
    private int  fileFoundCount;
    ListView listView;
    private  List<String> filelist;
    TextAdapter textAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isMusicPlayerInt) {

            musicpath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            dir = new File(musicpath);
            final ListView listView = findViewById(R.id.listviews);
            textAdapter = new TextAdapter();

            filelist = new ArrayList<>();
            final File[] files = dir.listFiles();
            for (File file : files) {
                final String path = file.getAbsolutePath();
                if (path.endsWith(".mp3")) {
                    filelist.add(path);
                }
            }
            textAdapter.setData(filelist);
            isMusicPlayerInt = true;
            listView.setAdapter(textAdapter);
            final SeekBar seekBar = findViewById(R.id.seekbar);
            final Button pause=findViewById(R.id.pause);
            final View playbackbutton=findViewById(R.id.playback);
            //Pause button instilization
            pause.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if(issongplaying)
                    {
                        mp.pause();
                        pause.setText("Play");
                    }
                    else
                    {
                        if(songposition==0)
                        {
                            playsong();
                        }
                        else
                        {
                        mp.start();
                        pause.setText("Pause");
                    }}
                    issongplaying=!issongplaying;
                }
            });
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(issongplaying)
                    {
                        mp.pause();
                        pause.setText("Play");
                    }
                    else
                    {
                        mp.start();
                        pause.setText("Pause");
                    }
                    issongplaying=!issongplaying;
                }
            });
            //ListView instillization
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                final TextView currentposition=findViewById(R.id.currentp);
                final  TextView durationtext=findViewById(R.id.duration);

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                    final String musicfilepath = filelist.get(i);
                    final int songduration = playMusic(musicfilepath);
                    final int songdurattext=songduration/1000;
                    seekBar.setMax(songduration);
                    playbackbutton.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);
                    durationtext.setText(String.valueOf(songdurattext/60)+":"+(String.valueOf(songdurattext%60)) );
                    new Thread() {

                        public void run() {

                            songposition = 0;
                            issongplaying=true;
                            while (songposition < songduration) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                              
                                if(issongplaying)
                                {
                                    issongplaying=true;
                                songposition+=1000;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        seekBar.setProgress(songposition);
                                        final int songpositiontext=songposition/1000;
                                        currentposition.setText(String.valueOf(songpositiontext/60)+":"+(String.valueOf(songpositiontext%60)));
                                    }
                                });
                            }}
mp.pause();
                            songposition=0;
                            currentposition.setText("0");
                            mp.seekTo(songposition);
                            pause.setText("Play");
                            issongplaying=false;
                            seekBar.setProgress(songposition);
                        }
                    }.start();
// seekbar instillization
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        int songprogress;

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                            songprogress = progress;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            songposition=songprogress;

                            mp.seekTo(songprogress);
                        }
                    });

                }
            });
        }
    }
    //permission



    // For Permission to excess  storage


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(PERMISSIONS, REQUEST_PERMISSION);
            return;
        }

        }
private void playsong()
{

}
    private boolean notPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionPtr = 0;
            while (permissionPtr < PERMISSION_COUNT) {
                if (checkSelfPermission(PERMISSIONS[permissionPtr]) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
                permissionPtr++;
            }

        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0) {
            if (notPermission()) {
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }
        }

    }


    //adding music path

    public int playMusic(String paths)
    {
        mp=new MediaPlayer();
        try {
            mp.setDataSource(paths);
            mp.prepare();
            mp.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return mp.getDuration();
    }
    //define all button and text
 
}
//Adapter formation
class TextAdapter extends BaseAdapter{
    private List<String> Data =new ArrayList<>();
    void setData(List<String> mData)
    {
        Data.clear();
        Data.addAll(mData);
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return Data.size();
    }

    @Override
    public String getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.item)));
        }
        ViewHolder holder=(ViewHolder) convertView.getTag();
        final String item=Data.get(i);
        holder.info.setText(item.substring(item.lastIndexOf('/')+1));
        return convertView;

    }

}

//ViewHolder

    class   ViewHolder
{
        TextView info;
        ViewHolder(TextView info) {
        this.info = info;
        }
        }
package com.example.greaper.concatvideo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RotateActivity extends AppCompatActivity {

    Video video;
    @BindView(R.id.vv_main)
    VideoView vvMain;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    private long countTime;
    private String outputPath;
    private int typeRotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate);
        ButterKnife.bind(this);
        this.video = getIntent().getParcelableExtra(Constants.ROTATE);
        this.typeRotate = getIntent().getIntExtra(Constants.TYPE_ROTATE, 90);
        try {
            FFmpeg.getInstance(this).loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Log.d("OKOKOK", "load sucess");
                    rotateVideo();
                }
            });

        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

    }

    private void rotateVideo() {
        String[] cmd = getComdRotateNotReencode();
        pbLoading.setVisibility(View.VISIBLE);
        countTime = System.currentTimeMillis();
        try {
            FFmpeg.getInstance(this).execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.d("OKOKtime ",  String.valueOf(System.currentTimeMillis() - countTime));
                    pbLoading.setVisibility(View.GONE);
                    vvMain.setVideoPath(outputPath);
                    vvMain.start();
                    Toast.makeText(RotateActivity.this, outputPath, Toast.LENGTH_SHORT).show();
                    super.onSuccess(message);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.d("OKOKOK", e.getMessage());
            e.printStackTrace();
        }
    }

    private String[] getCmdRotate() {
        String output = makeSubAppFolder(makeAppFolder("RotateVideo"), "Video") + "/VID_" + System.currentTimeMillis() + ".mp4";
        this.outputPath = output;
        ArrayList<String> listCmd = new ArrayList<>();
        listCmd.add("-i");
        listCmd.add(video.getPath());
        listCmd.add("-vf");
        listCmd.add("transpose=1");
        listCmd.add("-vcodec");
        listCmd.add("libx264");
        listCmd.add("-preset");
        listCmd.add("veryfast");
        listCmd.add("-acodec");
        listCmd.add("copy");
        listCmd.add(output);
        return listCmd.toArray(new String[listCmd.size()]);
    }

    private String[] getComdRotateNotReencode() {
        String output = makeSubAppFolder(makeAppFolder("RotateVideo"), "Video") + "/VID_" + System.currentTimeMillis() + ".mp4";
        this.outputPath = output;
        String realRotate = String.valueOf(typeRotate + 90);
        ArrayList<String> listCmd = new ArrayList<>();
        listCmd.add("-i");
        listCmd.add(video.getPath());
        listCmd.add("-map_metadata");
        listCmd.add("0");
        listCmd.add("-metadata:s:v");
        listCmd.add("rotate=" + realRotate);
        listCmd.add("-codec");
        listCmd.add("copy");
        listCmd.add(output);
        return listCmd.toArray(new String[listCmd.size()]);
    }

    public String makeSubAppFolder(String path, String subFolderName) {
        String subFolder = null;
        File file = new File(path + "/" + subFolderName);
        if (!file.exists()) {
            file.mkdirs();
            subFolder = file.getPath();
        } else {
            File folder = new File(path + "/" + subFolderName);
            subFolder = folder.getPath();
        }
        return subFolder;
    }

    public String makeAppFolder(String folderName) { //getExternalStorageDirectory
        String path = null;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + folderName);
        if (!file.exists()) {
            file.mkdirs();
            path = file.getPath();
        } else {
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + folderName);

            path = folder.getPath();
        }
        return path;
    }
}
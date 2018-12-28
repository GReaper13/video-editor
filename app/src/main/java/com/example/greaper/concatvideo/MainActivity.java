package com.example.greaper.concatvideo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements VideoAdapter.VideoListener {

    @BindView(R.id.rv_video)
    RecyclerView rvVideo;
    VideoAdapter videoAdapter;

    ArrayList<Video> listVideo;
    @BindView(R.id.btn_concat)
    Button btnConcat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
        allowPermission();
    }

    private void init() {
        videoAdapter = new VideoAdapter(this);
    }

    private void allowPermission() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (PermissionUtils.hasPermission(this, permissions)) {
            loadVideo(this);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, 66);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void loadVideo(Context context) {
        new AsyncTask<Void, Void, List<Video>>() {
            @Override
            protected List<Video> doInBackground(Void... voids) {
                List<Video> videos = new ArrayList<>();
                FileUtils.getVideoList(context, videos);
                return videos;
            }

            @Override
            protected void onPostExecute(List<Video> videos) {
                super.onPostExecute(videos);
                onVideoLoaded(videos);
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 66) {
            if (PermissionUtils.checkGranted(grantResults)) {
                loadVideo(this);
            } else {
                Toast.makeText(this, "Allow string", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void onVideoLoaded(List<Video> listVideo) {
        this.listVideo = (ArrayList<Video>) listVideo;
        videoAdapter.setVideoListener(this);
        videoAdapter.setListVideo(listVideo);
        rvVideo.setLayoutManager(new GridLayoutManager(this, 2));
        rvVideo.setAdapter(videoAdapter);
    }


    @Override
    public void onItemClick(Video video) {

    }

    public void concatClick(View view) {
        if (videoAdapter == null) {
            Toast.makeText(this, "Wait for load all videos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (videoAdapter.getVideoListSelect().size() < 2) {
            Toast.makeText(this, "Please select at least 2 video to concat", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Video> listVideoSelect = videoAdapter.getVideoListSelect();
        Intent intent = new Intent(MainActivity.this, ConcatActivity.class);
        intent.putParcelableArrayListExtra("video", listVideoSelect);
        startActivity(intent);
    }

    public void rotateClick(View view) {
        if (videoAdapter == null) {
            Toast.makeText(this, "Wait for load all videos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (videoAdapter.getVideoListSelect().size() != 1) {
            Toast.makeText(this, "Please select only 1 video", Toast.LENGTH_SHORT).show();
            return;
        }
        Video videoSelect = videoAdapter.getVideoListSelect().get(0);
        Intent rotateIntent = new Intent(MainActivity.this, RotateActivity.class);
        rotateIntent.putExtra(Constants.ROTATE, videoSelect);

        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_select_rotate);
        TextView txt90 = dialog.findViewById(R.id.txt_90);
        TextView txt180 = dialog.findViewById(R.id.txt_180);
        TextView txt270 = dialog.findViewById(R.id.txt_270);
        txt90.setOnClickListener(view1 -> {
            rotateIntent.putExtra(Constants.TYPE_ROTATE, 90);
            startActivity(rotateIntent);
            dialog.dismiss();
        });
        txt180.setOnClickListener(view1 -> {
            rotateIntent.putExtra(Constants.TYPE_ROTATE, 180);
            startActivity(rotateIntent);
            dialog.dismiss();
        });
        txt270.setOnClickListener(view1 -> {
            rotateIntent.putExtra(Constants.TYPE_ROTATE, 270);
            startActivity(rotateIntent);
            dialog.dismiss();
        });
        dialog.show();
    }

    public void cutClick(View view) {
        if (videoAdapter == null) {
            Toast.makeText(this, "Wait for load all videos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (videoAdapter.getVideoListSelect().size() != 1) {
            Toast.makeText(this, "Please select only 1 video", Toast.LENGTH_SHORT).show();
            return;
        }
        Video videoSelect = videoAdapter.getVideoListSelect().get(0);
        Intent rotateIntent = new Intent(MainActivity.this, CutActivity.class);
        rotateIntent.putExtra(Constants.CUT, videoSelect);
        startActivity(rotateIntent);
    }
}

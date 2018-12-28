package com.example.greaper.concatvideo;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class CutActivity extends AppCompatActivity implements CustomSeekBar.CustomSeekBarListener {

    @BindView(R.id.video_preview)
    VideoView videoPreview;
    @BindView(R.id.btn_play)
    ImageView btnPlay;
    @BindView(R.id.layout_video)
    RelativeLayout layoutVideo;
    @BindView(R.id.txt_start_time)
    TextView txtStartTime;
    @BindView(R.id.txt_current_duration)
    TextView txtCurrentDuration;
    @BindView(R.id.txt_end_time)
    TextView txtEndTime;
    @BindView(R.id.layout_time)
    RelativeLayout layoutTime;
    @BindView(R.id.sb_video)
    CustomSeekBar sbVideo;
    private Video video;
    @BindView(R.id.img_check)
    ImageView imgCheck;
    Disposable disposable;

    private static final int MIN_DURATION = 1000; // min duration for create a gif
    private int totalDuration;
    private String inputPath;

    private static final int TOTAL_FRAMES = 10;
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);
        ButterKnife.bind(this);

        this.video = getIntent().getParcelableExtra(Constants.CUT);
        totalDuration = video.getDuration();
        inputPath = video.getPath();
        videoPreview.setVideoPath(inputPath);
        sbVideo.setCustomSeekBarListener(this);
        sbVideo.setPlaying(false);
        sbVideo.setPercentMinWidth(Math.round(MIN_DURATION * 100f / totalDuration));
        String startTime = AppUtils.convertTime(0);
        txtStartTime.setText(startTime);
        String endTime = AppUtils.convertTime(video.getDuration());
        txtEndTime.setText(endTime);
        txtCurrentDuration.setText(endTime);
        retriever.setDataSource(inputPath);
    }


    @Override
    public void onSeekBarChange(int startPercent, int endPercent) {
        stopPlaying();
        updateTime(startPercent, endPercent, totalDuration);
    }

    private void stopPlaying() {
        if (disposable != null) {
            disposable.dispose();
        }
        videoPreview.pause();
        btnPlay.setVisibility(View.VISIBLE);
        sbVideo.setPlaying(false);
    }

    @Override
    public void clickLeftBlock() {

    }

    @Override
    public void clickRightBlock() {

    }

    @Override
    public void onSizeChangeComplete() {
        getAllFrame();
    }

    @Override
    public void onRunnerChange(float rateInVideo) {
        int currentDuration = Math.round(rateInVideo * totalDuration);
        videoPreview.seekTo(currentDuration);
//        if (currentDuration % 100 == 0) {
//            Bitmap frameBitmap = retriever.getFrameAtTime(currentDuration * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
//            imgCheck.setImageBitmap(frameBitmap);
//        }
    }

    public void updateTime(int startPercent, int endPercent, int totalDuration) {
        int startTime = startPercent * totalDuration / 100;
        String startTimeString = AppUtils.convertTime(startTime);
        int endTime = endPercent * totalDuration / 100;
        String endTimeString = AppUtils.convertTime(endTime);
        int duration = (endTime / 1000 - startTime / 1000) * 1000;
        String durationString = AppUtils.convertTime(duration);
        updateTime(startTimeString, endTimeString, durationString);
    }

    public void updateTime(String startTime, String endTime, String duration) {
        txtStartTime.setText(startTime);
        txtEndTime.setText(endTime);
        txtCurrentDuration.setText(duration);
    }

    private void getAllFrame() {
        List<Bitmap> listBitmap = new ArrayList<>();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(inputPath);
        for (int i = 0; i < TOTAL_FRAMES; i++) {
            Bitmap frameBitmap = retriever.getFrameAtTime(i * (totalDuration * 1000 / TOTAL_FRAMES) + 1000, MediaMetadataRetriever.OPTION_CLOSEST);
            listBitmap.add(frameBitmap);
        }
        sbVideo.setListBitmap(listBitmap);
    }

    @OnClick(R.id.btn_play)
    public void onViewClicked() {
        int currentStartTime = sbVideo.getRunnerPercent() * totalDuration / 100;
        int currentEndTime = AppUtils.stringToTime(txtEndTime.getText().toString());
        int currentDuration = currentEndTime - currentStartTime;
        videoPreview.start();
        videoPreview.seekTo(currentStartTime + 1000);
        btnPlay.setVisibility(View.GONE);
        sbVideo.setPlaying(true);
        sbVideo.setPositionBeforePlay();
        disposable = Observable.interval(50, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tick -> {
                    int currentTick = (int) (tick * 50);
                    float percent = (float)currentTick / currentDuration;
                    sbVideo.setPercentPositionRunner(percent);
                    Log.d("OKOKOK", currentTick + " " + percent);
                    if (percent >= 1) {
                        stopPlaying();
                        videoPreview.seekTo(100);
                    }
                });
    }
}

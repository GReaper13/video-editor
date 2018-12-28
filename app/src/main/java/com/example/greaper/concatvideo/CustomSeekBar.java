package com.example.greaper.concatvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CustomSeekBar extends View {

    private Paint grayPaint;
    private Paint borderPaint;
    private Paint yellowPaint;
    private Bitmap blockLeft;
    private Bitmap blockRight;
    private int width;
    private int height;
    private RectF leftBlockRect;
    private RectF rightBlockRect;
    private RectF runnerRect;
    private boolean clickBlockLeft = false;
    private boolean clickBlockRight = false;
    private boolean clickRunner = false;
    private float startX = 0;
    private int minWidth;
    private static final int MARGIN = 2;
    private static final int STROKE_WIDTH = 2;
    private float positionBeforePlay;
    private CustomSeekBarListener customSeekBarListener;
    private int percentMinWidth;
    private List<Bitmap> listBitmap;
    int widthPerBitmap;
    private static final int SIZE_RUNNER = 16;

    private boolean isPlaying;

    public CustomSeekBar(Context context) {
        super(context);
        init();
    }

    public CustomSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        grayPaint = new Paint();
        grayPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray));
        borderPaint = new Paint();
        borderPaint.setStrokeWidth(STROKE_WIDTH);
        int leftGradient = ContextCompat.getColor(getContext(), R.color.left_gradient);
        int rightGradient = ContextCompat.getColor(getContext(), R.color.right_gradient);
        borderPaint.setShader(new LinearGradient(0, 0, 0, getHeight(),  leftGradient, rightGradient, Shader.TileMode.MIRROR));
        yellowPaint = new Paint();
        yellowPaint.setColor(ContextCompat.getColor(getContext(), R.color.yellow_runner));
        blockLeft = BitmapFactory.decodeResource(getResources(), R.drawable.left_block);
        blockRight = BitmapFactory.decodeResource(getResources(), R.drawable.right_block);
        leftBlockRect = new RectF();
        rightBlockRect = new RectF();
        runnerRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        int totalRealWidth = width - (blockLeft.getWidth() + blockRight.getWidth());
        minWidth = totalRealWidth * percentMinWidth / 100;
        float widthBlock = (h * blockLeft.getWidth()) / blockLeft.getHeight();
        blockLeft = Bitmap.createScaledBitmap(blockLeft, (int)widthBlock, h, true);
        blockRight = Bitmap.createScaledBitmap(blockRight, (int)widthBlock, h, true);
        leftBlockRect.top = 0;
        leftBlockRect.left = 0;
        leftBlockRect.bottom = blockLeft.getHeight();
        leftBlockRect.right = blockLeft.getWidth();

        rightBlockRect.right = width;
        rightBlockRect.bottom = height;
        rightBlockRect.left = width - blockRight.getWidth();
        rightBlockRect.top = 0;

        runnerRect.left = blockLeft.getWidth();
        runnerRect.right = blockLeft.getWidth() + SIZE_RUNNER;
        runnerRect.top = MARGIN;
        runnerRect.bottom = height - MARGIN;

        customSeekBarListener.onSizeChangeComplete();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width > 0  && height > 0) {
            if (listBitmap != null) {
                for (int i = 0; i < listBitmap.size(); i++) {
                    canvas.drawBitmap(listBitmap.get(i), blockLeft.getWidth() + i * widthPerBitmap, STROKE_WIDTH, null);
                }
            }
            canvas.drawRect(blockLeft.getWidth(), leftBlockRect.top + MARGIN, leftBlockRect.right, rightBlockRect.bottom - MARGIN, grayPaint);
            canvas.drawRect(rightBlockRect.left, rightBlockRect.top + MARGIN, width - blockRight.getWidth(), rightBlockRect.bottom - MARGIN, grayPaint);
            canvas.drawBitmap(blockLeft, leftBlockRect.left, leftBlockRect.top, null);
            canvas.drawBitmap(blockRight, rightBlockRect.left, rightBlockRect.top, null);
            canvas.drawLine(leftBlockRect.right, MARGIN, rightBlockRect.left, MARGIN, borderPaint);
            canvas.drawLine(leftBlockRect.right, height - MARGIN, rightBlockRect.left, height - MARGIN, borderPaint);
            canvas.drawLine(runnerRect.left + SIZE_RUNNER / 2, MARGIN, runnerRect.left + SIZE_RUNNER / 2, height - MARGIN, yellowPaint);
        }
        super.onDraw(canvas);
    }

    public void setPositionBeforePlay() {
        positionBeforePlay = runnerRect.left + SIZE_RUNNER / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                blockClickEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                moveBlock(event);
                if (!clickRunner) {
                    int startPercent = getStartPercent();
                    int endPercent = getEndPercent();
                    customSeekBarListener.onSeekBarChange(startPercent, endPercent);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                updateRightBlock();
                updateLeftBlock();
                updateRunner();
                invalidate();
                break;
        }
        return true;
    }

    private void moveBlock(MotionEvent event) {
        if (clickBlockLeft) {
            if (leftBlockRect.left >= 0 && leftBlockRect.right <= rightBlockRect.left - minWidth) {
                leftBlockRect.left += event.getRawX() - startX;
                leftBlockRect.right += event.getRawX() - startX;
                startX = event.getRawX();
            }
            updateLeftBlock();
        }

        if (clickBlockRight) {
            if (rightBlockRect.left >= leftBlockRect.right + minWidth && rightBlockRect.right <= width) {
                rightBlockRect.left += event.getRawX() - startX;
                rightBlockRect.right += event.getRawX() - startX;
                startX = event.getRawX();
            }
            updateRightBlock();
        }

        if (clickRunner) {
            if (runnerRect.left >= leftBlockRect.right && runnerRect.right <= rightBlockRect.left) {
                Log.d("OKOKOK", "true");
                runnerRect.left += event.getRawX() - startX;
                runnerRect.right += event.getRawX() - startX;
                startX = event.getRawX();
                int totalRealWidth = width - 2 * blockLeft.getWidth();
                float positionRunner = runnerRect.left + SIZE_RUNNER / 2 ;
                customSeekBarListener.onRunnerChange(positionRunner / totalRealWidth);
            }
            updateRunner();
        }
    }

    private void updateRunner() {
        if (runnerRect.left < leftBlockRect.right) {
            runnerRect.left = leftBlockRect.right;
            runnerRect.right = leftBlockRect.right + SIZE_RUNNER;
            unclickRunner();
        } else if (runnerRect.right > rightBlockRect.left) {
            runnerRect.right = rightBlockRect.left;
            runnerRect.left = rightBlockRect.left - SIZE_RUNNER;
            unclickRunner();
        }
    }

    private void updateLeftBlock() {
        if (leftBlockRect.left < 0) {
            leftBlockRect.left = 0;
            leftBlockRect.right = blockLeft.getWidth();
            unClickBlock();
        } else if (leftBlockRect.right > rightBlockRect.left - minWidth) {
            leftBlockRect.right = rightBlockRect.left - minWidth;
            leftBlockRect.left = leftBlockRect.right - blockLeft.getWidth();
            unClickBlock();
        }
    }

    private void updateRightBlock() {
        if (rightBlockRect.right > width) {
            rightBlockRect.right = width;
            rightBlockRect.left = width - blockRight.getWidth();
            unClickBlock();
        } else if (rightBlockRect.left < leftBlockRect.right + minWidth) {
            rightBlockRect.left = leftBlockRect.right + minWidth;
            rightBlockRect.right = rightBlockRect.left + blockRight.getWidth();
            unClickBlock();
        }
    }

    private void blockClickEvent(MotionEvent event) {
        clickBlockLeft = leftBlockRect.contains(event.getX(), event.getY());
        if (clickBlockLeft) {
            startX = event.getRawX();
            customSeekBarListener.clickLeftBlock();
        }
        clickBlockRight = rightBlockRect.contains(event.getX(), event.getY());
        if (clickBlockRight) {
            startX = event.getRawX();
            customSeekBarListener.clickRightBlock();
        }
        clickRunner = runnerRect.contains(event.getX(), event.getY());
        if (clickRunner) {
            startX = event.getRawX();
        }
    }

    private void unClickBlock() {
        clickBlockLeft = false;
        clickBlockRight = false;
        startX = 0;
    }

    private void unclickRunner() {
        clickRunner = false;
        startX = 0;
    }

    public float getPositionRunner() {
        float currentWidth = rightBlockRect.left - leftBlockRect.right;
        float positionRunner = runnerRect.left + SIZE_RUNNER / 2 - leftBlockRect.right;
        return positionRunner / currentWidth;
    }

    public void setPercentPositionRunner(float percentPositionRunner) {
        if (isPlaying) {
            float currentWidth = rightBlockRect.left - positionBeforePlay;
            float positionRunner = percentPositionRunner * currentWidth + positionBeforePlay;
            runnerRect.left = positionRunner - SIZE_RUNNER / 2;
            runnerRect.right = positionRunner + SIZE_RUNNER / 2;
        }
        invalidate();
    }

    public void setCustomSeekBarListener(CustomSeekBarListener customSeekBarListener) {
        this.customSeekBarListener = customSeekBarListener;
    }

    private int getStartPercent() {
        int totalRealWidth = width - (blockLeft.getWidth() + blockRight.getWidth());
        float currentLeftWidth = leftBlockRect.right - blockLeft.getWidth();
        float percent = currentLeftWidth / totalRealWidth * 100;
        return Math.round(percent);
    }

    private int getEndPercent() {
        int totalRealWidth = width - (blockLeft.getWidth() + blockRight.getWidth());
        float currentRightWidth = rightBlockRect.left - blockLeft.getWidth();
        float percent = currentRightWidth / totalRealWidth * 100;
        return Math.round(percent);
    }

    public int getRunnerPercent() {
        int totalRealWidth = width - (blockLeft.getWidth() + blockRight.getWidth());
        float currentRunnerWidth = runnerRect.left + SIZE_RUNNER / 2 - blockLeft.getWidth();
        float percent = currentRunnerWidth / totalRealWidth * 100;
        return Math.round(percent);
    }

    public interface CustomSeekBarListener {
        void onSeekBarChange(int startPercent, int endPercent);
        void clickLeftBlock();
        void clickRightBlock();
        void onSizeChangeComplete();
        void onRunnerChange(float rateInVideo);
    }

    public void setPlaying(boolean playing) {
        if (!playing) {
            runnerRect.left = leftBlockRect.right;
            runnerRect.right = leftBlockRect.right + SIZE_RUNNER;
            invalidate();
        }
        isPlaying = playing;
    }

    public void setPercentMinWidth(int percentMinWidth) {
        this.percentMinWidth = percentMinWidth;
    }

    public void setListBitmap(List<Bitmap> listBitmapOld) {
        listBitmap = new ArrayList<>();
        int totalBitmap = listBitmapOld.size();
        float totalRealWidth = width - 2 * blockLeft.getWidth(); // total read width is max width of seek bar subtract two block rect
        widthPerBitmap = Math.round(totalRealWidth / totalBitmap);
        for (Bitmap bitmap : listBitmapOld) {
            Bitmap bitmapFrame = Bitmap.createScaledBitmap(bitmap, widthPerBitmap, height - 2 * STROKE_WIDTH, true);
            listBitmap.add(bitmapFrame);
        }
        invalidate();
    }
}

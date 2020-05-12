package com.zdww.video;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.zdww.video.util.DisplayUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    RelativeLayout videoRl;
    VideoView videoView;
    ImageView ivPlay;
    TextView tvTimeCurrent;
    TextView tvTimeTotal;
    ImageView ivVolume;
    SeekBar seekBar;
    SeekBar seekBarVolume;
    ImageView ivFull;

    LinearLayout popLl;
    SeekBar seekPercent;
    ImageView ivOperate;

    private int screenWidth, screenHeight;
    protected static final int UI_REFRESH = 2;

    private AudioManager audioManager;

    private boolean isFull = false;

    private boolean isAdjust = false;
    private int threshold = 54;
    float screenBrightness = 0;
    private float lastX = 0, lastY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initUI();
        setPlayerEvent();
        getPermission();
    }

    private Handler UIHandle = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (msg.what == UI_REFRESH) {
                //获取视频当前播放时间
                int currentPos = videoView.getCurrentPosition();
                //获取视频播放的总时间
                int totalDuration = videoView.getDuration();
                setTextViewTimeFormat(tvTimeCurrent, currentPos);
                setTextViewTimeFormat(tvTimeTotal, totalDuration);
                seekBar.setMax(totalDuration);
                seekBar.setProgress(currentPos);
                UIHandle.sendEmptyMessageDelayed(UI_REFRESH, 500);

            }
        }
    };

    /**
     * 时间格式化
     *
     * @param textView
     * @param millisecond
     */
    private void setTextViewTimeFormat(TextView textView, int millisecond) {
        int second = millisecond / 1000;
        int hh = second / 3600;
        int ss = second % 3600;
        int mm = second / 60;
        String str = null;
        if (hh != 0) {
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }

        textView.setText(str);
    }

    private void setPlayerEvent() {
        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    ivPlay.setImageResource(R.drawable.icon_start);
                    //暂停播放
                    videoView.pause();
                    //停止UI刷新
                    UIHandle.removeMessages(UI_REFRESH);
                } else {
                    ivPlay.setImageResource(R.drawable.icon_pause);
                    //开始播放
                    videoView.start();
                    //停止UI刷新
                    UIHandle.sendEmptyMessage(UI_REFRESH);
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setTextViewTimeFormat(tvTimeCurrent, progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //停止UI刷新
                UIHandle.removeMessages(UI_REFRESH);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                //另视频播放进度
                videoView.seekTo(progress);
                UIHandle.sendEmptyMessage(UI_REFRESH);
                setTextViewTimeFormat(tvTimeCurrent, progress);
            }
        });
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        ivFull.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onClick(View v) {
                if (isFull) {
                    ivFull.setImageResource(R.drawable.icon_minimize_press);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    ivFull.setImageResource(R.drawable.icon_maximize_press);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });
        /**
         * 控制viewview 的手势事件
         */

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float detlaX = x - lastX;
                        float detlaY = y - lastY;
                        float absDetlaX = Math.abs(detlaX);
                        float absDetlaY = Math.abs(detlaY);
                        if (absDetlaX > threshold && absDetlaY > threshold) {

                            if (absDetlaX < absDetlaY) {

                                isAdjust = true;
                            } else {
                                isAdjust = false;
                            }
                        } else if (absDetlaX < threshold && absDetlaY > threshold) {
                            isAdjust = true;
                        } else if (absDetlaX > threshold && absDetlaY < threshold) {
                            isAdjust = false;
                        }
                        if (isAdjust) {
                            if (x < screenWidth / 2) {
                                //调节亮度
                                if (detlaY > 0) {

                                    //降低亮度
                                } else {
                                    //增加亮度

                                }
                                changeBrightness(-detlaY);

                            } else {
                                //调节音量
                                if (detlaY > 0) {
                                    //降低音量
                                } else {
                                    //增加音量

                                }
                                changeVolume(-detlaY);
                            }

                        }
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        popLl.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 改变声音
     *
     * @param detlaY
     */
    private void changeVolume(float detlaY) {
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int index = (int) (detlaY / screenHeight * max * 3);
        int volume = Math.max(current + index, 0);
        ivOperate.setImageResource(R.drawable.icon_volume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        if (popLl.getVisibility() == View.GONE) {
            popLl.setVisibility(View.VISIBLE);
        }
        seekPercent.setProgress(volume);
        seekBarVolume.setProgress(volume);
    }

    /**
     * 改变亮度
     *
     * @param detlaY
     */
    private void changeBrightness(float detlaY) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        screenBrightness = attributes.screenBrightness;
        ivOperate.setImageResource(R.drawable.icon_brightness);
        float index = detlaY / screenHeight;
        screenBrightness += index;
        if (screenBrightness > 1.0f) {
            screenBrightness = 1.0f;
        }
        if (screenBrightness < 0.01f) {
            screenBrightness = 0.01f;
        }
        if (popLl.getVisibility() == View.GONE) {
            popLl.setVisibility(View.VISIBLE);
        }
        seekPercent.setProgress((int) (screenBrightness * 100));
        attributes.screenBrightness = screenBrightness;
        getWindow().setAttributes(attributes);
    }

    /**
     * 监听屏幕方向改变
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /**
         * 当屏幕方向为横屏
         */
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            ivVolume.setVisibility(View.VISIBLE);
            seekBarVolume.setVisibility(View.VISIBLE);
            isFull = true;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        /**
         * 当屏幕方向为竖屏
         */
        else {
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(this, 240));
            ivVolume.setVisibility(View.GONE);
            seekBarVolume.setVisibility(View.GONE);
            isFull = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止UI刷新
        UIHandle.removeMessages(UI_REFRESH);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //停止UI刷新
        UIHandle.removeMessages(UI_REFRESH);
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        videoRl = findViewById(R.id.video_rl);
        videoView = findViewById(R.id.videoview);
        ivPlay = findViewById(R.id.iv_play);
        tvTimeCurrent = findViewById(R.id.tv_time_current);
        tvTimeTotal = findViewById(R.id.tv_time_total);
        ivVolume = findViewById(R.id.iv_volume);
        seekBar = findViewById(R.id.seekBar);
        seekBarVolume = findViewById(R.id.seekBar_volume);
        ivFull = findViewById(R.id.iv_full);
        popLl = findViewById(R.id.ll_pop);
        seekPercent = findViewById(R.id.seekBar_percent);
        ivOperate = findViewById(R.id.iv_operate);

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        /**
         * 当前设备的最大音量
         */
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekBarVolume.setMax(maxVolume);
        seekBarVolume.setProgress(currentVolume);

    }

    /**
     * 设置屏幕大小
     *
     * @param width
     * @param height
     */
    private void setVideoViewScale(int width, int height) {
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        videoView.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams layoutParams1 = videoRl.getLayoutParams();
        layoutParams1.width = width;
        layoutParams1.height = height;
        videoRl.setLayoutParams(layoutParams1);
    }

    private void initVideo() {
        //播放本地视频
        //videoView.setVideoPath("");
        //播放网络视频
        videoView.setVideoURI(Uri.parse("http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4"));
//        //设置控制器 播放 暂停 快进
//        MediaController mediaController = new MediaController(this);
//        videoView.setMediaController(mediaController);
//        mediaController.setMediaPlayer(videoView);
        videoView.start();
        UIHandle.sendEmptyMessage(UI_REFRESH);
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            initVideo();
        }
    }

    private static final int NOT_NOTICE = 1001;
    AlertDialog mDialog;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {

            if (grantResults[0] == PERMISSION_GRANTED) {
                initVideo();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("申请存储权限")
                        .setMessage("应用要获取手机存储权限")
                        .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);//注意就是"package",不用改成自己的包名
                                intent.setData(uri);
                                startActivityForResult(intent, NOT_NOTICE);
                            }
                        });
                mDialog = builder.create();
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOT_NOTICE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                Toast.makeText(this, "您的权限申请失败", Toast.LENGTH_SHORT).show();
            } else {
                initVideo();
            }
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}

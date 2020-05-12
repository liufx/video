

# video
## 自定义VideoView
![Image text](https://github.com/liufx/video/blob/master/image/1.jpg)
![Image text](https://github.com/liufx/video/blob/master/image/2.jpg)
<video src="https://github.com/liufx/video/blob/master/image/20200512103723.mp4" controls="controls" width="500" height="300">您的浏览器不支持播放该视频！</video>


##关键方法

* 设置播放进度

```javascript

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

```
* 设置声音大小

```javascript

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

```
* 全屏设置

```javascript

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

```
* 改变声音

```javascript

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


```
* 改变亮度
```javascript
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


```


##有问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流

* 邮件: (547166147@qq.com)
* 微信: lfx19890801
* 简书: [@Smile_188](https://www.jianshu.com/u/cc3ecc8ef368)

##捐助开发者
在兴趣的驱动下,写一个`免费`的东西，有欣喜，也还有汗水，希望你喜欢我的作品，同时也能支持一下。


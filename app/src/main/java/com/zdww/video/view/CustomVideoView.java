package com.zdww.video.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;

/**
 * @Company： 中电万维
 * @ProjectName: video
 * @Package: com.zdww.video.view
 * @ClassName: CustomVideoView
 * @Description: java类作用描述
 * @Author: liufuxing
 * @CreateDate: 2020/4/2 10:43 AM
 * @UpdateUser: 更新者：
 * @UpdateDate: 2020/4/2 10:43 AM
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class CustomVideoView extends VideoView {
    int defaultWidth = 1920;
    int defaultHeight = 1080;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize(defaultWidth, widthMeasureSpec);
        int height = getDefaultSize(defaultHeight, heightMeasureSpec);
        setMeasuredDimension(width,height);
    }
}

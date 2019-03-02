package org.qxtx.idea.player.demo;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import java.util.Random;

import static android.graphics.Color.parseColor;

/**
 * 作者：laiyx
 * 创建日期：2017/12/15
 * 其它描述：属性动画类，构造函数中传入view对象
 * 备注1：内置的默认动画效果在低于1080p分辨率的情况下显示效果可能会较差，需要手动配置动画参数
 * 备注2：对于一些对属性值比较敏感的view，短时间内频繁的重复多次动画可能会导致view发生错误的变化，未找到合适的方法应付小于动画作用时间的过快调用
 *
 * 备注3：方法中的参数
 * @ parma propetyName 属性名称(String)
 * @ parma durationTime 持续时间(ms)
 * @ parma val 过程变化数值(Object/float/int)
 * @ param repeatCount：重复次数（第一次执行动画不属于重复次数）
 * @ param repeatType：重复形式（正序/倒序）
 * @ parma x x坐标值
 * @ parma y y坐标值
 * @ parma speed 速度（px/s）
 *
 * 安卓API提供的内置速率控制器：
 * AccelerateDecelerateInterpolator --- 在动画开始与结束的地方速率改变比较慢，在中间的时候加速（中间加速效果模型）
 * AccelerateInterpolator ------------- 在动画开始的地方速率改变比较慢，然后开始加速（“匀加速”效果模型）
 * AnticipateInterpolator ------------- 开始的时候向后然后向前甩（奇怪的模型）
 * AnticipateOvershootInterpolator ---- 开始的时候向后然后向前甩一定值后返回最后的值(莫名其妙的模型)
 * BounceInterpolator ----------------- 动画结束的时候弹起（物理弹跳模型）
 * CycleInterpolator ------------------ 动画循环播放特定的次数，速率改变沿着正弦曲线(莫名其妙的模型)
 * DecelerateInterpolator ------------- 在动画开始的地方快然后慢（“减速”效果模型）
 * LinearInterpolator ----------------- 以常量速率改变（匀速变化模型）
 * OvershootInterpolator -------------- 向前甩一定值后再回到原来位置（“超前”效果的模型）
 */
public class AnimationUtils {
    private final String TAG = "AnimationUtils内部";
    private final String NO_INIT = "启动前请先配置动画";
    private final String ERROR_NULL_OBJECT = "View工厂或状态值是空类";

    private ObjectAnimator mObjectAnimator; //属性动画类
    private ObjectFactory mObjectFactory; //属性工厂
    private static AnimatorSet animatorSet; //动画组合类
    private View mView;

    private float mStartPosX = 0f;
    private float mStartPosY = 0f; //记录控件原本的坐标

    private static View viewCheck = null; //检查连续两次是否为同一对象，仅用于抖动动画
    private static long time = 0; //检查同一对象连续执行动画的间隔时间， 仅用于抖动动画

    public AnimationUtils(View mView) {
        this.mView = mView;
        mObjectFactory = new ObjectFactory();
        animatorSet = new AnimatorSet();
    }



/*****************本类方法************************/
    //获得属性动画对象ObjectAnimator
    public ObjectAnimator getObjectAnimator() {
        return mObjectAnimator;
    }
    //强行结束当前动画
    public void endAnimation() {
        mObjectAnimator.removeAllListeners();
        mObjectAnimator.end();
    }
    //获得当前动画的状态
    public boolean isRunning() {
        if (mObjectAnimator == null) {
            return false;
        }
        return mObjectAnimator.isRunning();
    }
    public boolean isPaused() {
        return mObjectAnimator.isPaused();
    }
    public boolean isStarted() {
        return mObjectAnimator.isStarted();
    }
    //移除动画监听器
    public void removeAllListener() {
        mObjectAnimator.removeAllListeners();
    }
    public void removeAllUpdateListener() {
        mObjectAnimator.removeAllUpdateListeners();
    }





/*****************内置动画，已经配置好部分参数************************/
    /**
     * 磁贴跟随翻动
     * @ parma keyCode 键值
     */
    public void startTileRotation(int keyCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mView.setZ(1);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                startRotationY(400, 0f, -9f, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                startRotationX(400, 0f, 9f, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                startRotationY(400, 0f, 9f, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                startRotationX(400, 0f, -9f, 0f);
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            set("z", 100, 1, 0);
            startWithDelay(450);
        }
    }
    public void startTileRotation(int keyCode, float val) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mView.setZ(1);
            mView.requestLayout();
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:

                startRotationY(400, 0f, 0f - val, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                startRotationX(400, 0f, val, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                startRotationY(400, 0f, val, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                set("rotationX", 400, 0f, 0f - val, 0f);
                start();
                //startRotationX(400, 0f, -9f, 0f);
                break;
        }

        //恢复高度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            set("z", 100, 1, 0);
            startWithDelay(450);
        }
    }
    public void startTileRotation(int keyCode, int durationTime, float val) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mView.setZ(1);
            mView.requestLayout();
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                startRotationY(durationTime, 0f, 0f - val, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                startRotationX(durationTime, 0f, val, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                startRotationY(durationTime, 0f, val, 0f);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                startRotationX(durationTime, 0f, 0f - val, 0f);
                break;
        }

        //恢复高度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            set("z", 100, 1, 0);
            startWithDelay(450);
        }
    }

    /**
     * 伪随机RGB颜色变幻
     */
    public void startRandomColor() {
        int autoDuration = 5000;
        startRandom(autoDuration);
    }
    public void startRandomColor(int durationTime) {
        startRandom(durationTime);
    }
    public void startRandomColor(String propetyName, int durationTime) {
        startRandom(propetyName, durationTime);
    }
    public void startRandomColorOfBackground() {
        int autoDuration = 5000;
        String autoPropetyName = "backgroundColor";
        startRandom(autoPropetyName, autoDuration);
    }
    public void startRandomColorOfBackground(int durationTime) {
        String autoPropetyName = "backgroundColor";
        startRandom(autoPropetyName, durationTime);
    }

    /**
     * 颜色变化，目前只能实现纯色背景和字体颜色的变化
     */

    public void startColorful(String... val) {
        int autoDuration = 5000;
        String autoPropetyName = "color";

        //当不传入Object时自动填充
        if (val.length == 0) {
            val = new String[2];
            val[0] = "#ffffff";
            val[1] = "#2e8cf5";
        }
        
        autoColorful(autoPropetyName);
    }
    public void startColorful(String propetyName, String... val) {
        if ((!propetyName.equals("color")) && (!propetyName.equals("backgroundColor"))) {
            Log.e(TAG, "暂时没有这个动画功能");
            return ;
        }

        //当不传入Object时自动填充
        if (val.length == 0) {
            val = new String[2];
            val[0] = "#ffffff";
            val[1] = "#2e8cf5";
        }

        autoColorful(propetyName);
    }
    public void startColorful(int durationTime, String propetyName, Object... val) {
        if ((!propetyName.equals("color")) && (!propetyName.equals("backgroundColor"))) {
            Log.e(TAG, "暂时没有这个动画功能");
            return ;
        }

        //当不传入Object时自动填充
        if (val.length == 0) {
            val = new String[2];
            val[0] = "#ffffff";
            val[1] = "#2e8cf5";
        }

        setOfObject(propetyName, durationTime, (Object[])val);
        start();
    }

    /**
     * 抖动
     * 备注1：startShakeNow方法不建议在onCreate()中使用，可能会在全部控件坐标未分配时就调用造成动画不正确
     * 备注2：对于有连续数次触发条件的对象使用此类方法，有小概率会导致对象产生某个方向的位移
     * */
    public void startShakeX() {
        long autoTimeDelay = 600;
        //检查同一对象连续动画的间隔
        if (!canStartShake(autoTimeDelay)) {
            return ;
        }

        int autoRepeatCount = 3;
        String autoPropetyName = "x";
        int autoDuration = 100;
        int autoDelay = 200;
        int[] pos = new int[2];
        mView.getLocationOnScreen(pos);
        postDelay(autoRepeatCount, autoPropetyName, autoDuration, autoDelay, pos[0], pos[0] + 20, pos[0]);
    }
    public void startShakeY() {
        long autoTimeDelay = 600;
        //检查同一对象连续动画的间隔
        if (!canStartShake(autoTimeDelay)) {
            return ;
        }

        int autoRepeatCount = 3;
        String autoPropetyName = "y";
        int autoDuration = 100;
        int autoDelay = 200;
        int[] pos = new int[2];
        mView.getLocationOnScreen(pos);
        postDelay(autoRepeatCount, autoPropetyName, autoDuration, autoDelay, pos[1], pos[1] + 20, pos[1]);
    }
    public void startShakeX(int repeatCount) {
        long timeDelay = 100 * (repeatCount + 1) + 100;
        //检查同一对象连续动画的间隔
        if (!canStartShake(timeDelay)) {
            return ;
        }

        String autoPropetyName = "x";
        int autoDelay = 200;
        int autoDuration = 100;
        int[] pos = new int[2];
        mView.getLocationOnScreen(pos);
        postDelay(repeatCount, autoPropetyName, autoDuration, autoDelay, pos[0], pos[0] + 20, pos[0]);
    }
    public void startShakeY(int repeatCount) {
        long timeDelay = 100 * (repeatCount + 1) + 100;
        //检查同一对象连续动画的间隔
        if (!canStartShake(timeDelay)) {
            return ;
        }

        String autoPropetyName = "y";
        int autoDelay = 200;
        int autoDuration = 100;
        int[] pos = new int[2];
        mView.getLocationOnScreen(pos);
        postDelay(repeatCount, autoPropetyName, autoDuration, autoDelay, pos[1], pos[1] + 20, pos[1]);
    }
    public void startShakeXNow() {
        long autoTimeDelay = 600;
        //检查同一对象连续动画的间隔
        if (!canStartShake(autoTimeDelay)) {
            return ;
        }

        int autoRepeatCount = 3;
        String autoPropetyName = "x";
        postNow(autoRepeatCount, autoPropetyName);
    }
    public void startShakeYNow() {
        long autoTimeDelay = 600;
        //检查同一对象连续动画的间隔
        if (!canStartShake(autoTimeDelay)) {
            return ;
        }

        int autoRepeatCount = 3;
        String autoPropetyName = "y";
        postNow(autoRepeatCount, autoPropetyName);
    }
    public void startShakeXNow(int repeatCount) {
        long timeDelay = 100 * repeatCount + 100;
        //检查同一对象连续动画的间隔
        if (!canStartShake(timeDelay)) {
            return ;
        }

        String autoPropetyName = "x";
        postNow(repeatCount, autoPropetyName);
    }
    public void startShakeYNow(int repeatCount) {
        long timeDelay = 100 * repeatCount + 100;
        //检查同一对象连续动画的间隔
        if (!canStartShake(timeDelay)) {
            return ;
        }

        String autoPropetyName = "y";
        postNow(repeatCount, autoPropetyName);
    }
    public void startShake(int repeatCount, String propetyName, int durationTime, int delayTime, float... val) {
        long timeDelay = durationTime * (repeatCount + 1) + 100;
        //检查同一对象连续动画的间隔
        if (!canStartShake(timeDelay)) {
            return ;
        }

        postDelay(repeatCount, propetyName, durationTime, delayTime, val);
    }
	
	/**
	 * 透明度渐变
	 */
	 public void startAlphaShow() {
		int autoDuration = 700;
		set("alpha", autoDuration, 0f, 1f);
		start();
	 }
     public void startAlphaHide() {
        int autoDuration = 700;
        set("alpha", autoDuration, 1f, 0f);
        start();
     }
	 public void startAlpha(float... val) {
         if (val.length == 0) {
             val = new float[2];
             val[0] = 0f;
             val[1] = 1f;
         }

	 int autoDuration = 700;
		set("alpha", autoDuration, val);
		start();
	 }
	 public void startAlpha(int durationTime, float... val) {
         //当无val时，自动填补val值
         if (val.length == 0) {
             val = new float[2];
             val[0] = 0f;
             val[1] = 1f;
         }

		set("alpha", durationTime, (float[])val);
		start();
	 }

    /**
     * 呼吸
     */
    public void startBreathe() {
        int autoDuration = 1500;
        int autoRepeatCount = 4;
        set("alpha", autoDuration, 1f, 0.8f, 0.6f, 0.4f, 0.2f, 0.4f, 0.6f, 0.8f, 1f);
        startWithRepeat(autoRepeatCount, ValueAnimator.REVERSE);
    }
    public void startBreathe(int repeatCount) {
        int autoDuration = 1500;
        set("alpha", autoDuration, 1f, 0.2f, 1f);
        startWithRepeat(repeatCount, ValueAnimator.REVERSE);
    }
    public void startBreathe(int repeatCount, int durationTime) {
        set("alpha", durationTime, 1f, 0.2f, 1f);
        startWithRepeat(repeatCount, ValueAnimator.REVERSE);
    }
    public void startBreathe(int repeatCount, int repeatType, int durationTime, float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[3];
            val[0] = 1f;
            val[1] = 0.2f;
            val[2] = 1f;
        }

        set("alpha", durationTime, val);
        startWithRepeat(repeatCount, repeatType);
    }
	 
	 /**
	  * 中心缩放
	  */
     public void startScale(float... val) {
         //当无val时，自动填补val值
         if (val.length == 0) {
             val = new float[3];
             val[0] = 1f;
             val[1] = 1.1f;
             val[2] = 1f;
         }

         int autoDuration = 80;
         set("scaleX", autoDuration, val);
         start();

         ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mObjectFactory, "scaleY", val).setDuration(autoDuration);
         objectAnimator2.addListener(new Listener());
         objectAnimator2.start();
     }
     public void startScale(int durationTime, float... val) {
         //当无val时，自动填补val值
         if (val.length == 0) {
             val = new float[3];
             val[0] = 1f;
             val[1] = 1.1f;
             val[2] = 1f;
         }

         set("scaleX", durationTime, (float[])val);
         start();

         ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mObjectFactory, "scaleY", (float[])val).setDuration(durationTime);
         objectAnimator2.addListener(new Listener());
         objectAnimator2.start();
     }

    /**
     * 翻转
     */
    public void startRotation(int durationTime, float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[2];
            val[0] = 0f;
            val[1] = 360f;
        }

        set("rotation", durationTime, val);
        start();
    }
    public void startRotation(int repeatCount, int durationTime, float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[2];
            val[0] = 0f;
            val[1] = 360f;
        }

        set("rotation", durationTime, val);
        startWithRepeat(repeatCount, ValueAnimator.RESTART);
    }
    public void startRotationX(int durationTime, float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[3];
            val[0] = 0f;
            val[1] = 35f;
            val[2] = 0f;
        }

        set("rotationX", durationTime, val);
        start();
    }
    public void startRotationY(int durationTime, float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[3];
            val[0] = 0f;
            val[1] = 35f;
            val[2] = 0f;
        }

        set("rotationY", durationTime, val);
        start();
    }
    public void startRotation(String propetyName, int durationTime, Interpolator interpolator, float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[3];
            val[0] = 0f;
            val[1] = 35f;
            val[2] = 0f;
        }

        setWithInterpolator(propetyName, durationTime, interpolator, val);
        start();
    }


    /**
     * 闪烁
     */
    public void startFlicker() {
        int autoDuration = 500;
        int autoRepeat = 3;
        int startVlaue = mView.getVisibility() == View.VISIBLE ? View.VISIBLE : View.INVISIBLE;
        set("visibility", autoDuration, View.INVISIBLE, View.VISIBLE, startVlaue);
        startWithRepeat(autoRepeat, ValueAnimator.RESTART);
    }
    public void startFlicker(int repeatCount) {
        int autoDuration = 500;
        int startVlaue = mView.getVisibility() == View.VISIBLE ? View.VISIBLE : View.INVISIBLE;
        set("visibility", autoDuration, View.INVISIBLE, View.VISIBLE, startVlaue);
        startWithRepeat(repeatCount, ValueAnimator.RESTART);
    }
    public void startFlicker( int repeatCount, int durationTime) {
        int startVlaue = mView.getVisibility() == View.VISIBLE ? View.VISIBLE : View.INVISIBLE;
        set("visibility", durationTime, View.INVISIBLE, View.VISIBLE, startVlaue);
        startWithRepeat(repeatCount, ValueAnimator.RESTART);
    }

    /**
     * 物理弹跳模型（尤其适用物体下落的模型）
     */
    public void startBounceInterpolator(float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[2];
            val[0] = 0f;
            val[1] = 1080f - 130f; // 这里需要减小高度130px，不知道为什么用1080px会超出屏幕一点
        }

        String autoPreptyName = "y";
        int autoDuration = 2500;
        setWithInterpolator(autoPreptyName, autoDuration, new BounceInterpolator(), val);
        start();
    }
    public void startBounceInterpolator(String propertyName, float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[2];
            val[0] = 0f;
            val[1] = 1080f - 130f; // 这里需要减小高度130px，不知道为什么用1080px会超出屏幕一点
        }

        int autoDuration = 2500;
        setWithInterpolator(propertyName, autoDuration, new BounceInterpolator(), val);
        start();
    }
    public void startBounceInterpolator(String propertyName, int durationTime, float... val) {
        //当无val时，自动填补val值
        if (val.length == 0) {
            val = new float[2];
            val[0] = 0f;
            val[1] = 1080f - 130f; // 这里需要减小高度130px，不知道为什么用1080px会超出屏幕一点
        }

        setWithInterpolator(propertyName, durationTime, new BounceInterpolator(), val);
        start();
    }

    /**
     * 正向抛物线运动
     * @ param x 初始X坐标值
     * @ param y 初始Y坐标值
     * @ param speed 运动速度（px/秒）
     */
    public void startParabola() {
        int autoX = 0, autoY = 0; //默认坐标
        setCustom(600, new LinearInterpolator(), new ParabolaEvaluetor(), new AxisUpdateListener(), new PointF(autoX, autoY));
        start();
    }
    public void startParabola(int durationTime) {
        int autoX = 0, autoY = 0; //默认坐标
        setCustom(durationTime, new LinearInterpolator(), new ParabolaEvaluetor(), new AxisUpdateListener(), new PointF(autoX, autoY));
        start();
    }
    public void startParabola(int durationTime, float speed) {
        int autoX = 0, autoY = 0; //默认坐标
        setCustom(durationTime, new LinearInterpolator(), new ParabolaEvaluetor(speed), new AxisUpdateListener(), new PointF(autoX, autoY));
        start();
    }
    public void startParabola(int durationTime, float speed, float x, float y) {
        setCustom(durationTime, new LinearInterpolator(), new ParabolaEvaluetor(speed), new AxisUpdateListener(), new PointF(x, y));
        start();
    }


    /**
     * 简单的View形变动画
     */
    public void startViewShow() {
        startViewAlpha(0, 1, 500, 0f, 1f);
    }
    public void startViewHide() {
        startViewAlpha(0, 1, 500, 1f, 0f);
    }
    public void startViewShow(int repeatCount, int repeatMode) {
        startViewAlpha(repeatCount, repeatMode, 500, 0f, 1f);
    }
    public void startViewHide(int repeatCount, int repeatMode) {
        startViewAlpha(repeatCount, repeatMode, 500, 1f, 0f);
    }
    public void startViewAlpha(int durationTime, float startVal, float endVal) {
        startViewAlpha(0, 1, durationTime, startVal, endVal);
    }
    public void startViewAlpha(int repeatCount, int repeatMode, int durationTime, float startVal, float endVal) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(startVal, endVal);
        alphaAnimation.setDuration(durationTime);
        alphaAnimation.setRepeatCount(repeatCount);
        alphaAnimation.setRepeatMode(repeatMode);
        alphaAnimation.start();
    }

    public void startViewRotation() {
        startViewRotation(0, 1, 800, 0f, 360f);
    }
    public void startViewRotation(int repeatCount, int repeatMode) {
        startViewRotation(repeatCount, repeatMode, 800, 0f, 360f);
    }
    public void startViewRotation(int durationTime, float startVal, float endVal) {
        startViewRotation(0, 1, durationTime, startVal, endVal);
    }
    public void startViewRotation(int repeatCount, int repeatMode, int durationTime, float startVal, float endVal) {
        RotateAnimation rotateAnimation = new RotateAnimation(startVal, endVal);
        rotateAnimation.setDuration(durationTime);
        rotateAnimation.setRepeatCount(repeatCount);
        rotateAnimation.setRepeatMode(repeatMode);
        rotateAnimation.start();
    }

    public void startViewScale() {
        startViewScale(500, 0f, 1.5f, 0, 1.5f);
    }
    public void startViewScale(int repeatCount, int repeatMode) {
        long durationTime = 500;
        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1.5f, 0, 1.5f); // x&y放大倍数
        scaleAnimation.setDuration(durationTime);
        scaleAnimation.setRepeatCount(repeatCount);
        scaleAnimation.setRepeatMode(repeatMode);
        scaleAnimation.start();
    }
    public void startViewScale(int durationTime, float startX, float zoomX, float startY, float zoomY) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(startX, zoomX, startY, zoomY); // x&y放大倍数
        scaleAnimation.setDuration(durationTime);
        scaleAnimation.start();
    }

    public void startViewTranslation(int moveX, int moveY) {
        long autoDuration = 600;
        TranslateAnimation translateAnimation = new TranslateAnimation(0, moveX, 0, moveY);
        translateAnimation.setDuration(autoDuration);
        translateAnimation.start();
    }
    public void startViewTranslation(int durationTime, int startX, int moveX, int startY, int moveY) {
        startViewTranslation(durationTime, 0, 1, startX, moveX, startY, moveY);
    }
    public void startViewTranslation(int durationTime, int repeatCount, int repeatMode, int startX, int moveX, int startY, int moveY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(startX, moveX, startY, moveY);
        translateAnimation.setDuration(durationTime);
        translateAnimation.setRepeatCount(repeatCount);
        translateAnimation.setRepeatMode(repeatMode);
        translateAnimation.start();
    }






/**********************定制型动画，需要手动配置每一个参数**********************/
    /**
     * 简单的动画配置
     */
    public void start(String propetyName, int durationTime, int... val) {
        if ((mObjectFactory != null) && (val.length > 1)) {
            mObjectAnimator = ObjectAnimator
                    .ofInt(mObjectFactory, propetyName, val)
                    .setDuration(durationTime);
            start();
        } else {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }
    }
    public void start(String propetyName, int durationTime, float... val) {
        if ((mObjectFactory != null) && (val.length > 1)) {
            mObjectAnimator = ObjectAnimator
                    .ofFloat(mObjectFactory, propetyName, val)
                    .setDuration(durationTime);
            start();
        } else {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }
    }

    /**配置自定义动画
     * @param typeEvaluator 估值算法
     * @param updateListener 动画更新监听，配合估值算法完成自定义动画
     * @param val 对象
     */
    public AnimationUtils setCustom(int durationTime,
                                    Interpolator interpolator,
                                    TypeEvaluator typeEvaluator,
                                    ValueAnimator.AnimatorUpdateListener updateListener,
                                    Object... val) {
        if (val.length == 0) {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }

        mObjectAnimator = new ObjectAnimator();
        mObjectAnimator.setDuration(durationTime);
        mObjectAnimator.setObjectValues((Object[])val);
        mObjectAnimator.setInterpolator(interpolator); //速率控制
        mObjectAnimator.addUpdateListener(updateListener);
        mObjectAnimator.setEvaluator(typeEvaluator);

        return this;
    }


    /**配置普通动画
     * @ parma interpolator 动画的速率控制（实现各种速率效果，加速度，物理模型，三角函数等等速率变化逻辑，也可重写此类的逻辑）
     * @ param durationTime 动画持续时间（默认300ms）
     */
    public AnimationUtils set(String propertyName, int durationTime, int... val) {
        if ((mObjectFactory != null) && (val.length > 1)) {
            mObjectAnimator = ObjectAnimator.ofInt(mObjectFactory, propertyName, (int[])val).setDuration(durationTime);
        } else {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }
        return this;
    }
    public AnimationUtils set(String propertyName, int durationTime, float... val) {
        if ((mObjectFactory != null) && (val.length > 1)) {
            mObjectAnimator = ObjectAnimator.ofFloat(mObjectFactory, propertyName, val).setDuration(durationTime);
        } else {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }
        return this;
    }
    public AnimationUtils setOfObject(String propertyName, int durationTime, Object... val) {
        if ((mObjectFactory != null) && (val.length > 1)) {
            if (propertyName.equals("color") || propertyName.equals("backgroundColor")) {
                mObjectAnimator = ObjectAnimator.ofObject(mObjectFactory, propertyName, new ColorEvaluator(), (Object[])val).setDuration(durationTime);
            } else {
                Log.e(TAG, "这个方法目前只能实现颜色的动画");
            }
        } else {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }
        return this;
    }
    public AnimationUtils setWithInterpolator(String propertyName, int durationTime, Interpolator interpolator, int... val) {
        if ((mObjectFactory != null) && (val.length > 1)) {
            mObjectAnimator = ObjectAnimator.ofInt(mObjectFactory, propertyName, val).setDuration(durationTime);
            mObjectAnimator.setInterpolator(interpolator);
        } else {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }
        return this;
    }
    public AnimationUtils setWithInterpolator(String propertyName, int durationTime, Interpolator interpolator, float... val) {
        if ((mObjectFactory != null) && (val.length > 1)) {
            mObjectAnimator = ObjectAnimator.ofFloat(mObjectFactory, propertyName, val).setDuration(durationTime);
            mObjectAnimator.setInterpolator(interpolator);
        } else {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }
        return this;
    }
    public AnimationUtils setWithInterpolatorOfObject(String propertyName, int durationTime, Interpolator interpolator, Object... val) {
        if ((mObjectFactory != null) && (val.length > 1)) {
            if (propertyName.equals("color") || propertyName.equals("backgroundColor")) {
                mObjectAnimator.setInterpolator(interpolator);
                mObjectAnimator = ObjectAnimator.ofObject(mObjectFactory, propertyName, new ColorEvaluator(), (Object[])val).setDuration(durationTime);
            } else {
                Log.e(TAG, "这个方法目前只能实现颜色的动画");
            }
        } else {
            Log.e(TAG, ERROR_NULL_OBJECT);
        }
        return this;
    }

    /**
     * 动画启动
     */
    public void start() {
        if ((mObjectAnimator.getPropertyName() == null)
                && (mObjectAnimator.getDuration() == 0)
                && (mObjectAnimator.getValues() == null)) {
            Log.e(TAG, NO_INIT);
            return ;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mStartPosX = mView.getX();
                mStartPosY = mView.getY();
                //Log.e(TAG, "得到的坐标：" + mStartPosX + "&&" + mStartPosY);
            }
        }, 250);

        mObjectAnimator.addListener(new Listener());//不加的话会导致动画概率性不执行
        mObjectAnimator.start();
    }
    public void startWithDelay(long delayTime) {
        if ((mObjectAnimator.getPropertyName() == null)
                && (mObjectAnimator.getDuration() == 0)
                && (mObjectAnimator.getValues() == null)) {
            Log.e(TAG, NO_INIT);
            return ;
        }

        mObjectAnimator.setStartDelay(delayTime);
        start();
    }
    public void startWithRepeat(int repeatCount, int repeatMode) {
        if ((mObjectAnimator.getPropertyName() == null)
                && (mObjectAnimator.getDuration() == 0)
                && (mObjectAnimator.getValues() == null)) {
            Log.e(TAG, NO_INIT);
            return ;
        }

        mObjectAnimator.setRepeatCount(repeatCount);
        mObjectAnimator.setRepeatMode(repeatMode);
        start();
    }
    public void startWithDelayAndRepeat(long delayTime, int repeatCount, int repeatMode) {
        if ((mObjectAnimator.getPropertyName() == null)
                && (mObjectAnimator.getDuration() == 0)
                && (mObjectAnimator.getValues() == null)) {
            Log.e(TAG, NO_INIT);
            return ;
        }

        mObjectAnimator.setRepeatCount(repeatCount);
        mObjectAnimator.setRepeatMode(repeatMode);
        mObjectAnimator.setStartDelay(delayTime);
        start();
    }

    /**
     * 动画组合,逻辑整理有点混乱，需要重新整理
     */
    public void addAnimatorSet() {
        animatorSet.play(mObjectAnimator);
    }
    public void startAnimatorSet() {
        animatorSet.start();
    }
    public void playWith(AnimationUtils currentAnimationUtils) {
        Animator animator = currentAnimationUtils.getObjectAnimator();
        animatorSet.play(animator).with(mObjectAnimator);
    }
    public void playBefore(AnimationUtils currentAnimationUtils) {
        Animator animator = currentAnimationUtils.getObjectAnimator();
        animatorSet.play(mObjectAnimator).before(animator);
    }
    public void playAfter(AnimationUtils currentAnimationUtils) {
        Animator animator = currentAnimationUtils.getObjectAnimator();
        animatorSet.play(mObjectAnimator).after(animator);
    }
    public void playTogether(AnimationUtils... animationUtils) {
        if (animationUtils != null) {
            Animator[] animator = new Animator[animationUtils.length];
            for (int i = 0; i < animationUtils.length; i++) {
                animator[i] = animationUtils[i].getObjectAnimator();
            }

            animatorSet.playTogether(animator);
        }
    }
    public void playMultil(AnimationUtils... animationUtils) {
        if (animationUtils != null) {
            Animator[] animator = new Animator[animationUtils.length];
            for (int i = 0; i < animationUtils.length; i++) {
                animator[i] = animationUtils[i].getObjectAnimator();
            }

            animatorSet.playSequentially(animator);
        }
    }


    //@内置类：处理颜色值重写类， 可实现颜色平滑变化
    private class ColorEvaluator implements TypeEvaluator {

        private int mCurrentRed = -1;
        private int mCurrentGreen = -1;
        private int mCurrentBlue = -1;

        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            String startColor = (String) startValue;
            String endColor = (String) endValue;
            int startRed = Integer.parseInt(startColor.substring(1, 3), 16);
            int startGreen = Integer.parseInt(startColor.substring(3, 5), 16);
            int startBlue = Integer.parseInt(startColor.substring(5, 7), 16);
            int endRed = Integer.parseInt(endColor.substring(1, 3), 16);
            int endGreen = Integer.parseInt(endColor.substring(3, 5), 16);
            int endBlue = Integer.parseInt(endColor.substring(5, 7), 16);
            // 初始化颜色的值
            if (mCurrentRed == -1) {
                mCurrentRed = startRed;
            }
            if (mCurrentGreen == -1) {
                mCurrentGreen = startGreen;
            }
            if (mCurrentBlue == -1) {
                mCurrentBlue = startBlue;
            }
            // 计算初始颜色和结束颜色之间的差值
            int redDiff = Math.abs(startRed - endRed);
            int greenDiff = Math.abs(startGreen - endGreen);
            int blueDiff = Math.abs(startBlue - endBlue);
            int colorDiff = redDiff + greenDiff + blueDiff;

            if (mCurrentRed != endRed) {
                mCurrentRed = getCurrentColor(startRed, endRed, colorDiff, 0, fraction);
            } else if (mCurrentGreen != endGreen) {
                mCurrentGreen = getCurrentColor(startGreen, endGreen, colorDiff, redDiff, fraction);
            } else if (mCurrentBlue != endBlue) {
                mCurrentBlue = getCurrentColor(startBlue, endBlue, colorDiff, redDiff + greenDiff, fraction);
            }

            // 将计算出的当前颜色的值组装返回
            return "#" + getHexString(mCurrentRed) + getHexString(mCurrentGreen) + getHexString(mCurrentBlue);
        }

        /**
         * 根据fraction值来计算当前的颜色。
         */
        private int getCurrentColor(int startColor, int endColor, int colorDiff, int offset, float fraction) {
            int currentColor;
            if (startColor > endColor) {
                currentColor = (int) (startColor - (fraction * colorDiff - offset));
                if (currentColor < endColor) {
                    currentColor = endColor;
                }
            } else {
                currentColor = (int) (startColor + (fraction * colorDiff - offset));
                if (currentColor > endColor) {
                    currentColor = endColor;
                }
            }
            return currentColor;
        }

        /**
         * 将10进制颜色值转换成16进制。
         */
        private String getHexString(int value) {
            String hexString = Integer.toHexString(value);
            if (hexString.length() == 1) {
                hexString = "0" + hexString;
            }
            return hexString;
        }
    }
    //内置类：抛物线路径运动类
    private class ParabolaEvaluetor implements TypeEvaluator {
        private float speed = 200f;//默认200px/s;
        public ParabolaEvaluetor() {}
        public ParabolaEvaluetor(float speed) {
            this.speed = speed;
        }
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            PointF pointF = new PointF();
            pointF.x = (fraction * 3) * speed; //不知道为什么，fraction * 3能使得y轴轨迹等于屏幕高度
            pointF.y = 0.5f * speed * (fraction * 3) * (fraction * 3);

            return pointF;
        }
    }
    //内置类：坐标值更新类
    private class AxisUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            //得到当前的属性值
            PointF pointF = (PointF)valueAnimator.getAnimatedValue();
            mView.setX(pointF.x);
            mView.setY(pointF.y);
        }
    }
    //内置类：监听动画类，没有实际用处，仅为了防止动画不启动
    private class Listener implements ObjectAnimator.AnimatorListener {
        private final String ERRORINFO = "动画导致view发生不可逆的位移，自动修正";
        @Override
        public void onAnimationEnd(Animator animation) {
            //Log.e(TAG, "动画结束了");
        }
        @Override
        public void onAnimationCancel(Animator animation) {
            Log.e(TAG, "动画突然取消了");
            fixViewMoveError();
        }
        @Override
        public void onAnimationStart(Animator animation) {
//            Log.e(TAG, "动画开始了");
        }
        @Override
        public void onAnimationRepeat(Animator animation) {
            //Log.e(TAG, "动画开始循环");
        }

        //修补由于不合理的动画使用频率导致view发生错误的位移
        private void fixViewMoveError() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((Math.abs(mStartPosX - mView.getX()) > 20) || (Math.abs(mStartPosY - mView.getY()) > 20)) {
                        Log.e(TAG, ERRORINFO);
                        //Log.e(TAG, "原位置:" + mStartPosX + "&" + mStartPosY);
                        //Log.e(TAG, "现位置:" + mView.getX() + "&" + mView.getY());
                        mView.setX(mStartPosX);
                        mView.setY(mStartPosY);
                    }
                }
            }, 250);
        }
    }

    //子方法：进行默认的颜色变换（最终颜色固定值）
    private void autoColorful(String propetyName) {
        int autoDuration = 5000;
        String autoStartColor = "#ffffff";
        final String autoEndColor = "#2e8cf5";

        //从本身颜色开始改变
        if (mView instanceof TextView) {
            int colorInt = ((TextView)mView).getCurrentTextColor();
            int red = Color.red(colorInt);
            int green = Color.green(colorInt);
            int blue = Color.blue(colorInt);
            autoStartColor = "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
            //Log.e(TAG, "得到的初始颜色" + red + "&" + green + "&" + blue);
        }

        setOfObject(propetyName, autoDuration, autoStartColor, autoEndColor);
        start();
    }
    //子方法：检测是否允许同一对象再一次执行抖动动画
    private boolean canStartShake(long timeDelay) {
        if ((viewCheck == mView) && (System.currentTimeMillis() - time < timeDelay)) {
            Log.e(TAG, "同一对象连续动画时间间隔太短：" + (System.currentTimeMillis() - time) + "ms（大于" + timeDelay + "ms）");
            time = System.currentTimeMillis();
            return false;
        } else {
            viewCheck = mView;
            time = System.currentTimeMillis();
            return true;
        }
    }
    //子方法：转换颜色代码
    private void startRandom(int durationTime) {
        Random random = new Random();
        String startColor = "#";
        String endColor = "#";
        for (int i = 0; i < 6; i++) {
            startColor = startColor + parseColorNum(Math.abs(random.nextInt() % 16));
            endColor = endColor + parseColorNum(Math.abs(random.nextInt() % 16));
        }

        //如果相同颜色则重新获取其中一个状态的颜色
        while (startColor.equals(endColor)) {
            for (int i = 0; i < 6; i++) {
                endColor = endColor + parseColorNum(Math.abs(random.nextInt() % 16));
            }
        }

        //Log.e(TAG, "两端颜色：" + color[0] + "&&" + color[1]);
        setOfObject("color", durationTime, startColor, endColor);
        startWithRepeat(3, ValueAnimator.REVERSE);
    }
    private void startRandom(String propetyName, int durationTime) {
        if ((!propetyName.equals("color")) && (!propetyName.equals("backgroundColor"))) {
            Log.e(TAG, "暂时没有这个动画功能");
            return ;
        }

        Random random = new Random();
        String startColor = "#";
        String endColor = "#";
        for (int i = 0; i < 6; i++) {
            startColor = startColor + parseColorNum(Math.abs(random.nextInt() % 16));
            endColor = endColor + parseColorNum(Math.abs(random.nextInt() % 16));
        }

        //如果相同颜色则重新获取其中一个状态的颜色
        while (startColor.equals(endColor)) {
            for (int i = 0; i < 6; i++) {
                endColor = endColor + parseColorNum(Math.abs(random.nextInt() % 16));
            }
        }

        //Log.e(TAG, "两端颜色：" + color[0] + "&&" + color[1]);
        setOfObject(propetyName, durationTime, startColor, endColor);
        startWithRepeat(3, ValueAnimator.REVERSE);
    }
    private String parseColorNum(int num) {
        if (num > 9) {
            return (char)(num + 87) + "";
        } else {
            return num + "";
        }
    }
    //子方法：抖动
    private void postNow(final int repeatCount, final String propetyName) {
        int[] pos = new int[2];
        mView.getLocationOnScreen(pos);
        if (propetyName.equals("x")) {
            set(propetyName, 100, pos[0], pos[0] + 20, pos[0]);
        } else if (propetyName.equals("y")) {
            set(propetyName, 100, pos[1], pos[1] + 20, pos[1]);
        }
        startWithRepeat(repeatCount, ValueAnimator.REVERSE);
    }
    private void postDelay(final int repeatCount, final String propetyName,
                           final int durationTime, int delayTime, final float... val) {
        if (val.length == 0) {
            Log.e(TAG, ERROR_NULL_OBJECT);
            return ;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                set(propetyName, durationTime, val);
                startWithRepeat(repeatCount, ValueAnimator.REVERSE);
            }
        }, delayTime);
    }

    /** 属性工厂
     * 目前提供的方法
     * 1、getView 获得对象
     * 2、setVisibility 显示/隐藏状态
     * 3、setX/setY/setZ 坐标值
     * 4、setWidth/setHeight 宽高
     * 5、setRotation/setRotationX/setRotationY 旋转
     * 6、setScaleX/setScaleY 翻转
     * 7、setTranslationX/setTranslationY/setTranslationZ 平移
     * 8、setPaddingLeft/setPaddingTop/setPaddingRight/setPaddingBottom 控件内部偏移
     * 9、setColor/setBackgroundColor  颜色值（String类型）
     * 10、setAlpha 透明度
     *
     * 描述1：为不提供set方法的对象属性封装set方法实现属性动画
     * 描述2：int/float/String数值可选
     *
     * 备注：可自行添加特定属性的set方法以便使用
     */
    private class ObjectFactory {

        //获得当前属性工厂中的view对象
        public View getView() {
            return mView;
        }

        //显示/隐藏
        public void setVisible(int isVisibility) {
            if (isVisibility == View.INVISIBLE) {
                mView.setVisibility(View.INVISIBLE);
            } else if (isVisibility == View.GONE) {
                mView.setVisibility(View.GONE);
            } else if (isVisibility == View.VISIBLE) {
                mView.setVisibility(View.VISIBLE);
            }
        }

        //坐标值
        public void setX(float x) {
            mView.setX(x);
            mView.requestLayout();
        }
        public void setY(float y) {
            mView.setY(y);
            mView.requestLayout();
        }
        public void setZ(float zoomZ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mView.setZ(zoomZ);
            }
            mView.requestLayout();
        }
        public void setX(int x) {
            mView.setX(x);
            mView.requestLayout();
        }
        public void setY(int y) {
            mView.setY(y);
            mView.requestLayout();
        }
        public void setZ(int zoomZ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mView.setZ(zoomZ);
            }
            mView.requestLayout();
        }

        //宽高值
        public void setWidth(float width) {
            mView.getLayoutParams().width = (int)width;
            mView.requestLayout();//如果没有这个就不会有效果
        }
        public void setHeight(float height) {
            mView.getLayoutParams().height = (int)height;
            mView.requestLayout();
        }
        public void setWidth(int width) {
            mView.getLayoutParams().width = (int)width;
            mView.requestLayout();//如果没有这个就不会有效果
        }
        public void setHeight(int height) {
            mView.getLayoutParams().height = (int)height;
            mView.requestLayout();
        }

        //旋转
        public void setRotation(float rotation) {
            mView.setRotation(rotation);
        }
        public void setRotationX(float rotationX) {
            mView.setRotationX(rotationX);
        }
        public void setRotationY(float rotationY) {
            mView.setRotationY(rotationY);
        }
        public void setRotation(int rotation) {
            mView.setRotation(rotation);
        }
        public void setRotationX(int rotationX) {
            mView.setRotationX(rotationX);
        }
        public void setRotationY(int rotationY) {
            mView.setRotationY(rotationY);
        }

        //缩放
        public void setScaleX(float scaleX) {
            mView.setScaleX(scaleX);
        }
        public void setScaleY(float scaleY) {
            mView.setScaleY(scaleY);
        }
        public void setScaleX(int scaleX) {
            mView.setScaleX(scaleX);
        }
        public void setScaleY(int scaleY) {
            mView.setScaleY(scaleY);
        }

        //位移
        public void setTranslationX(float translationX) {
            mView.setTranslationX(translationX);
        }
        public void setTranslationY(float translationY) {
            mView.setTranslationY(translationY);
        }
        public void setTranslationZ(float translationZ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mView.setTranslationZ(translationZ);
            }
        }
        public void setTranslationX(int translationX) {
            mView.setTranslationX(translationX);
        }
        public void setTranslationY(int translationY) {
            mView.setTranslationY(translationY);
        }
        public void setTranslationZ(int translationZ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mView.setTranslationZ(translationZ);
            }
        }

        //padding偏移
        public void setPaddingLeft(float left) {
            mView.setPadding((int)left, 0, 0, 0);
        }
        public void setPaddingTop(float top) {
            mView.setPadding(0, (int)top, 0, 0);
        }
        public void setPaddingRight(float right) {
            mView.setPadding(0, 0, (int)right, 0);
        }
        public void setPaddingBottom(float bottom) {
            mView.setPadding(0, 0, 0, (int)bottom);
        }
        public void setPaddingLeft(int left) {
            mView.setPadding((int)left, 0, 0, 0);
        }
        public void setPaddingTop(int top) {
            mView.setPadding(0, (int)top, 0, 0);
        }
        public void setPaddingRight(int right) {
            mView.setPadding(0, 0, (int)right, 0);
        }
        public void setPaddingBottom(int bottom) {
            mView.setPadding(0, 0, 0, (int)bottom);
        }

        //颜色值
        public void setColor(String color) {
            ((TextView)mView).setTextColor(parseColor(color));
        }
        public void setBackgroundColor(String color) {
            mView.setBackgroundColor(parseColor(color));
        }

        //透明度
        public void setAlpha(float alpha) {
            mView.setAlpha(alpha);
        }
        public void setAlpha(int alpha) {
            mView.setAlpha(alpha);
        }
    }
}

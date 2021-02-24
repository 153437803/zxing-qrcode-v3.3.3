package lib.kalu.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;

import com.google.zxing.Result;
import lib.kalu.qrcode.camera.CameraManager;
import lib.kalu.qrcode.decode.DecodeTool;
import lib.kalu.qrcode.handler.ParseHandler;
import lib.kalu.qrcode.manager.BeepManager;
import lib.kalu.qrcode.manager.InactivityTimer;
import lib.kalu.qrcode.config.QrcodeConfig;
import lib.kalu.qrcode.view.ScansView;
import lib.kalu.qrcode.view.SeeksBar;
import lib.kalu.qrcode.view.ZxingView;

/**
 * description: 二维码扫描
 * created by kalu on 2021-02-24
 */
@Keep
public final class QrcodeActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = QrcodeActivity.class.getSimpleName();

    private CameraManager cameraManager;
    private ParseHandler handler;
    private ScansView scannersView;
    private Result lastResult;
    private boolean hasSurface;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private LinearLayout btn_scan_light;
    private ImageView iv_scan_light;
    private TextView tv_scan_light;
    private LinearLayout btn_close;
    private LinearLayout btn_photo;
    private RelativeLayout btn_dialog_bg;
    private ImageView ivScreenshot;

    private ZxingView surfaceView;
    private ImageView mIvScanZoomIn;
    private ImageView mIvScanZoomOut;
    private SeekBar mSeekBarZoom;
    private LinearLayout mLlRoomController;
    private SeeksBar mSeekBarZoomVertical;
    private ImageView mIvScanZoomOutVertical;
    private LinearLayout mLlRoomControllerVertical;
    private ImageView mIvScanZoomInVertical;

    //传递数据
    //闪光灯是否打开
    private boolean is_light_on = false;
    private boolean beepFlag = true;
    private boolean vibrateFlag = true;
    private boolean zoomControllerFlag = true;
    private int exitAnime = 0;
    private QrcodeConfig.ZoomControllerLocation zoomControllerLocation;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.lib_qrcode_activity_qrcode);
        initView();
        initIntent();
    }

    private void initIntent() {
        Intent intent = getIntent();

        QrcodeConfig qrcodeConfig = (QrcodeConfig) intent.getSerializableExtra(QrcodeManager.INTENT_KEY_CONFIG_MODEL);


        String hintText = qrcodeConfig.getScanHintText();
        String scanColor = qrcodeConfig.getScanColor();
        boolean photoFlag = qrcodeConfig.isShowPhotoAlbum();
        beepFlag = qrcodeConfig.isShowBeep();
        vibrateFlag = qrcodeConfig.isShowVibrate();
        exitAnime = qrcodeConfig.getActivityExitAnime();
        zoomControllerFlag = qrcodeConfig.isShowZoomController();
        zoomControllerLocation = qrcodeConfig.getZoomControllerLocation();

        if (!TextUtils.isEmpty(hintText)) {
            scannersView.setHintText(hintText);
        }
        if (!TextUtils.isEmpty(scanColor)) {
            scannersView.setScanLineColor(Color.parseColor(scanColor));
        }
        if (!photoFlag) {
            btn_photo.setVisibility(View.GONE);
        }
        if (exitAnime == 0) {
            exitAnime = R.anim.lib_qrcode_anim_bottom_out;
        }
    }

    /**
     * 获取相册中的图片
     */
    public void getImageFromAlbum() {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 使用Intent.ACTION_GET_CONTENT这个Action */
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setAction(Intent.ACTION_PICK);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, 1000);
        //开始转Dialog
        btn_dialog_bg.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //去相册选择图片
        if (requestCode == 1000) {
            if (data == null) {
                //隐藏Dialog
                btn_dialog_bg.setVisibility(View.GONE);
                return;
            }
            final Uri uri = data.getData();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //需要压缩图片
                    Bitmap bitmapChoose = DecodeTool.decodeUriAsBitmap(QrcodeActivity.this, uri);
                    if (bitmapChoose != null) {
                        final String decodeQRCodeFromBitmap = DecodeTool.decode(bitmapChoose);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_dialog_bg.setVisibility(View.GONE);
                                Log.i(TAG, "decodeQRCode:" + decodeQRCodeFromBitmap);
                                if (TextUtils.isEmpty(decodeQRCodeFromBitmap)) {
                                    Toast.makeText(QrcodeActivity.this, "未发现二维码", Toast.LENGTH_SHORT).show();
                                } else {
                                    finishSuccess(decodeQRCodeFromBitmap);
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_dialog_bg.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        if (handler != null && cameraManager != null && cameraManager.isOpen()) {
            return;
        }
        cameraManager = new CameraManager(getApplication());
        scannersView.setCameraManager(cameraManager);

        handler = null;
        lastResult = null;

        resetStatusView();

        beepManager.updatePrefs(beepFlag, vibrateFlag);

        inactivityTimer.onResume();
        characterSet = null;

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // 防止sdk8的设备初始化预览异常
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //取消扫码
        finishCancle();
    }

    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        lastResult = rawResult;
        //播放声音和震动
        beepManager.playBeepSoundAndVibrate();
        //关闭页面
        finishSuccess(lastResult.getText());
        //图片显示：测试才显示
        ivScreenshot.setImageBitmap(barcode);

    }

    private void finishFailed(String errorMsg) {
        Intent intent = new Intent();
        intent.putExtra(QrcodeManager.INTENT_KEY_RESULT_ERROR, errorMsg);
        this.setResult(QrcodeManager.RESULT_FAIL, intent);
        this.finish();
        finishFinal();
    }

    private void finishCancle() {
        this.setResult(QrcodeManager.RESULT_CANCLE, null);
        finishFinal();
    }

    private void finishSuccess(String result) {
        Intent intent = new Intent();
        intent.putExtra(QrcodeManager.INTENT_KEY_RESULT_SUCCESS, result);
        this.setResult(QrcodeManager.RESULT_SUCCESS, intent);
        finishFinal();
    }

    private void finishFinal() {
        this.finish();
        //关闭窗体动画显示
        this.overridePendingTransition(0, exitAnime);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // do nothing
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            displayFrameworkBugMessageAndExit("SurfaceHolder 不存在");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new ParseHandler(this, characterSet, cameraManager);
            }
        } catch (Exception e) {
            displayFrameworkBugMessageAndExit("open camera fail：" + e.toString());
        }
        //刷新控制器
        updateZoomController();
    }

    private void displayFrameworkBugMessageAndExit(String errorMessage) {
        finishFailed(errorMessage);
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        scannersView.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        scannersView.drawViewfinder();
    }

    private void initView() {
        surfaceView = findViewById(R.id.preview_view);
        scannersView = (ScansView) findViewById(R.id.viewfinder_view);
        btn_scan_light = (LinearLayout) findViewById(R.id.btn_scan_light);
        iv_scan_light = (ImageView) findViewById(R.id.iv_scan_light);
        tv_scan_light = (TextView) findViewById(R.id.tv_scan_light);
        btn_close = (LinearLayout) findViewById(R.id.btn_close);
        btn_photo = (LinearLayout) findViewById(R.id.btn_photo);
        btn_dialog_bg = (RelativeLayout) findViewById(R.id.btn_dialog_bg);
        ivScreenshot = (ImageView) findViewById(R.id.ivScreenshot);
        btn_dialog_bg.setVisibility(View.GONE);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        //点击事件
        btn_scan_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_light_on) {
                    is_light_on = false;
                    cameraManager.offLight();
                    iv_scan_light.setImageResource(R.drawable.mn_icon_scan_flash_light_off);
                    tv_scan_light.setText("打开手电筒");
                } else {
                    is_light_on = true;
                    cameraManager.openLight();
                    iv_scan_light.setImageResource(R.drawable.mn_icon_scan_flash_light_on);
                    tv_scan_light.setText("关闭手电筒");
                }
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishCancle();
            }
        });

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }
        });

        btn_dialog_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mIvScanZoomIn = (ImageView) findViewById(R.id.iv_scan_zoom_in);
        mIvScanZoomOut = (ImageView) findViewById(R.id.iv_scan_zoom_out);
        mSeekBarZoom = (SeekBar) findViewById(R.id.seek_bar_zoom);
        mLlRoomController = (LinearLayout) findViewById(R.id.ll_room_controller);

        mSeekBarZoomVertical = (SeeksBar) findViewById(R.id.seek_bar_zoom_vertical);
        mIvScanZoomOutVertical = (ImageView) findViewById(R.id.iv_scan_zoom_out_vertical);
        mIvScanZoomInVertical = (ImageView) findViewById(R.id.iv_scan_zoom_in_vertical);
        mLlRoomControllerVertical = (LinearLayout) findViewById(R.id.ll_room_controller_vertical);

        mSeekBarZoomVertical.setMaxProgress(100);
        mSeekBarZoomVertical.setProgress(0);
        mSeekBarZoomVertical.setThumbSize(8, 8);
        mSeekBarZoomVertical.setUnSelectColor(Color.parseColor("#b4b4b4"));
        mSeekBarZoomVertical.setSelectColor(Color.parseColor("#FFFFFF"));

        mIvScanZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomIn(10);
            }
        });
        mIvScanZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOut(10);
            }
        });
        mIvScanZoomInVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomIn(10);
            }
        });
        mIvScanZoomOutVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOut(10);
            }
        });

        mSeekBarZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cameraManager.setZoom(progress);
                mSeekBarZoomVertical.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarZoomVertical.setOnSlideChangeListener(new SeeksBar.SlideChangeListener() {
            @Override
            public void onStart(SeeksBar slideView, int progress) {

            }

            @Override
            public void onProgress(SeeksBar slideView, int progress) {
                cameraManager.setZoom(progress);
                mSeekBarZoom.setProgress(progress);
            }

            @Override
            public void onStop(SeeksBar slideView, int progress) {

            }
        });
    }

    private void zoomOut(int value) {
        int progress = mSeekBarZoom.getProgress() - value;
        if (progress <= 0) {
            progress = 0;
        }
        mSeekBarZoom.setProgress(progress);
        mSeekBarZoomVertical.setProgress(progress);
        cameraManager.setZoom(progress);
    }

    private void zoomIn(int value) {
        int progress = mSeekBarZoom.getProgress() + value;
        if (progress >= 100) {
            progress = 100;
        }
        mSeekBarZoom.setProgress(progress);
        mSeekBarZoomVertical.setProgress(progress);
        cameraManager.setZoom(progress);
    }

    private void updateZoomController() {
        if (cameraManager == null) {
            return;
        }
        Rect framingRect = cameraManager.getFramingRect();
        if (framingRect == null) {
            return;
        }
        //显示
        if (zoomControllerFlag) {

            float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            int size10 = (int) (10 * scale + 0.5f);

            if (zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Left) {
                //垂直方向
                RelativeLayout.LayoutParams layoutParamsVertical = (RelativeLayout.LayoutParams) mLlRoomControllerVertical.getLayoutParams();
                layoutParamsVertical.height = framingRect.bottom - framingRect.top - size10 * 2;
                layoutParamsVertical.setMargins(framingRect.left - size10 - layoutParamsVertical.width, framingRect.top + size10, 0, 0);
                mLlRoomControllerVertical.setLayoutParams(layoutParamsVertical);

                mLlRoomControllerVertical.setVisibility(View.VISIBLE);
            } else if (zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Right) {
                //垂直方向
                RelativeLayout.LayoutParams layoutParamsVertical = (RelativeLayout.LayoutParams) mLlRoomControllerVertical.getLayoutParams();
                layoutParamsVertical.height = framingRect.bottom - framingRect.top - size10 * 2;
                layoutParamsVertical.setMargins(framingRect.right + size10, framingRect.top + size10, 0, 0);
                mLlRoomControllerVertical.setLayoutParams(layoutParamsVertical);

                mLlRoomControllerVertical.setVisibility(View.VISIBLE);
            } else if (zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Bottom) {
                //横向
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLlRoomController.getLayoutParams();
                layoutParams.width = framingRect.right - framingRect.left - size10 * 2;
                layoutParams.setMargins(0, framingRect.bottom + size10, 0, 0);
                mLlRoomController.setLayoutParams(layoutParams);

                mLlRoomController.setVisibility(View.VISIBLE);
            }
        }


    }


    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float startX = 0;
    float startY = 0;
    float moveX = 0;
    float moveY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            startX = event.getX();
            startY = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //当手指离开的时候
            moveX = event.getX();
            moveY = event.getY();
            if (!zoomControllerFlag) {
                return super.onTouchEvent(event);
            }
            if (startY - moveY > 50) {
                if (zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Left
                        || zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Right) {
                    //垂直方向
                    //向上滑
                    zoomIn(1);
                }
            } else if (moveY - startY > 50) {
                if (zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Left
                        || zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Right) {
                    //垂直方向
                    //向下滑
                    zoomOut(1);
                }
            } else if (startX - moveX > 50) {
                if (zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Bottom) {
                    //垂直方向
                    //向左滑
                    zoomOut(1);
                }
            } else if (moveX - startX > 50) {
                if (zoomControllerLocation == QrcodeConfig.ZoomControllerLocation.Bottom) {
                    //垂直方向
                    //向右滑
                    zoomIn(1);
                }
            }
        }
        return super.onTouchEvent(event);
    }


}

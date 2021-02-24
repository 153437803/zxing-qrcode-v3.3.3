package com.kalu.zxing;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.zxing.android.QrcodeScanManager;
import com.google.zxing.android.config.QrcodeScanConfig;
import com.google.zxing.android.listener.OnQrcodeScanChangeListener;
import com.google.zxing.util.ZxingUtil;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "http://umspos.caitc.cn/cajinfu-wx-web/#/user/registerQrcode?posterId=AD20210218155306&staffCode=17242a4d541ee348658c12ecde90103b346f05144805be07";
        String logo = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fs9.sinaimg.cn%2Fbmiddle%2F5ceba31bg5d6503750788&refer=http%3A%2F%2Fs9.sinaimg.cn&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1616396178&t=68870ef9ba794e07c9db62b6f3365627";

        // 无LOGO
        findViewById(R.id.create_qr1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView imageView = findViewById(R.id.logo);
                imageView.setImageDrawable(null);

                String qrcode = ZxingUtil.createQrcode(getApplicationContext(), url, 400);
                if (TextUtils.isEmpty(qrcode)) {
                    Toast.makeText(MainActivity.this, "生成二维码错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                Uri parse = Uri.parse(qrcode);
                imageView.setImageURI(parse);
            }
        });

        // 有LOGO
        findViewById(R.id.create_qr2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView imageView = findViewById(R.id.logo);
                imageView.setImageDrawable(null);

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        String qrcode = ZxingUtil.createQrcodeFromUrl(getApplicationContext(), url, 400, logo);
                        if (TextUtils.isEmpty(qrcode)) {
                            Toast.makeText(MainActivity.this, "生成二维码错误", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                imageView.setImageURI(Uri.parse(qrcode));
                            }
                        });
                    }
                }).start();
            }
        });

        // 有LOGO
        findViewById(R.id.create_qr3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView imageView = findViewById(R.id.logo);
                imageView.setImageDrawable(null);

                String qrcode = ZxingUtil.createQrcodeFromRaw(getApplicationContext(), url, 400, R.raw.logo);
                if (TextUtils.isEmpty(qrcode)) {
                    Toast.makeText(MainActivity.this, "生成二维码错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                imageView.setImageURI(Uri.parse(qrcode));
            }
        });

        // 有LOGO
        findViewById(R.id.create_qr4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView imageView = findViewById(R.id.logo);
                imageView.setImageDrawable(null);

                String qrcode = ZxingUtil.createQrcodeFromAssets(getApplicationContext(), url, 400, "logo.jpg");
                if (TextUtils.isEmpty(qrcode)) {
                    Toast.makeText(MainActivity.this, "生成二维码错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                imageView.setImageURI(Uri.parse(qrcode));
            }
        });

        // 默认扫描
        findViewById(R.id.scan_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestCameraPerm();
                QrcodeScanManager.start(MainActivity.this, new OnQrcodeScanChangeListener() {
                    @Override
                    public void onSucc(@NonNull String s) {
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(@NonNull String s) {
                        Toast.makeText(getApplicationContext(), "onFail", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancle() {
                        Toast.makeText(getApplicationContext(), "onCancle", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 自定义扫描
        findViewById(R.id.scan_custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestCameraPerm();
                QrcodeScanConfig builder = new QrcodeScanConfig.Builder()
                        //设置完成震动
                        .isShowVibrate(false)
                        //扫描完成声音
                        .isShowBeep(true)
                        //显示相册功能
                        .isShowPhotoAlbum(true)
                        //打开扫描页面的动画
                        .setActivityOpenAnime(R.anim.activity_anmie_in)
                        //退出扫描页面动画
                        .setActivityExitAnime(R.anim.activity_anmie_out)
                        //自定义文案
                        .setScanHintText("请将二维码放入框中...")
                        //扫描线的颜色
                        .setScanColor("#FFFF00")
                        //是否显示缩放控制器
                        .isShowZoomController(true)
                        //显示缩放控制器位置
                        .setZoomControllerLocation(QrcodeScanConfig.ZoomControllerLocation.Bottom)
                        .builder();
                QrcodeScanManager.start(MainActivity.this, builder, new OnQrcodeScanChangeListener() {
                    @Override
                    public void onSucc(@NonNull String s) {
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(@NonNull String s) {
                        Toast.makeText(getApplicationContext(), "onFail", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancle() {
                        Toast.makeText(getApplicationContext(), "onCancle", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void requestCameraPerm() {
        //判断权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 10010);
            }
        }
    }
}

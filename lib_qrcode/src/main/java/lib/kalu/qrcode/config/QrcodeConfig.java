package lib.kalu.qrcode.config;

import androidx.annotation.Keep;

import java.io.Serializable;

import lib.kalu.qrcode.R;

/**
 * description: 二维码扫描配置信息
 * created by kalu on 2021-02-24
 */
@Keep
public class QrcodeConfig implements Serializable {

    private static final long serialVersionUID = -5260676142223049891L;

    //枚举类型：缩放控制器位置
    @Keep
    public enum ZoomControllerLocation {
        Bottom,
        Left,
        Right,
    }

    //是否显示相册
    private boolean showPhotoAlbum;
    //扫描声音
    private boolean showBeep;
    //扫描震动
    private boolean showVibrate;
    //扫描框和扫描线的颜色
    private String scanColor;
    //扫描提示文案
    private String scanHintText;
    //开启Activity动画
    private int activityOpenAnime;
    //关闭Activity动画
    private int activityExitAnime;
    //是否显示缩放控制器
    private boolean showZoomController = true;
    //控制器位置
    private ZoomControllerLocation zoomControllerLocation = ZoomControllerLocation.Right;

    private QrcodeConfig() {

    }


    private QrcodeConfig(Builder builder) {
        showPhotoAlbum = builder.showPhotoAlbum;
        showBeep = builder.showBeep;
        showVibrate = builder.showVibrate;
        scanColor = builder.scanColor;
        scanHintText = builder.scanHintText;
        activityOpenAnime = builder.activityOpenAnime;
        activityExitAnime = builder.activityExitAnime;
        showZoomController = builder.showZoomController;
        zoomControllerLocation = builder.zoomControllerLocation;
    }

    public boolean isShowZoomController() {
        return showZoomController;
    }

    public void setShowZoomController(boolean showZoomController) {
        this.showZoomController = showZoomController;
    }

    public boolean isShowPhotoAlbum() {
        return showPhotoAlbum;
    }

    public boolean isShowBeep() {
        return showBeep;
    }

    public boolean isShowVibrate() {
        return showVibrate;
    }

    public String getScanColor() {
        return scanColor;
    }

    public String getScanHintText() {
        return scanHintText;
    }

    public int getActivityOpenAnime() {
        return activityOpenAnime;
    }

    public int getActivityExitAnime() {
        return activityExitAnime;
    }

    public void setActivityExitAnime(int activityExitAnime) {
        this.activityExitAnime = activityExitAnime;
    }

    public void setShowPhotoAlbum(boolean showPhotoAlbum) {
        this.showPhotoAlbum = showPhotoAlbum;
    }

    public void setShowBeep(boolean showBeep) {
        this.showBeep = showBeep;
    }

    public void setShowVibrate(boolean showVibrate) {
        this.showVibrate = showVibrate;
    }

    public void setScanColor(String scanColor) {
        this.scanColor = scanColor;
    }

    public void setScanHintText(String scanHintText) {
        this.scanHintText = scanHintText;
    }

    public void setActivityOpenAnime(int activityOpenAnime) {
        this.activityOpenAnime = activityOpenAnime;
    }

    public ZoomControllerLocation getZoomControllerLocation() {
        return zoomControllerLocation;
    }

    public void setZoomControllerLocation(ZoomControllerLocation zoomControllerLocation) {
        this.zoomControllerLocation = zoomControllerLocation;
    }

    @Keep
    public final static class Builder {

        private boolean showPhotoAlbum = true;
        private boolean showBeep = true;
        private boolean showVibrate = true;
        private String scanColor;
        private String scanHintText;
        private int activityOpenAnime = R.anim.lib_barcode_anim_bottom_in;
        private int activityExitAnime = R.anim.lib_qrcode_anim_bottom_out;
        private boolean showZoomController = true;
        private ZoomControllerLocation zoomControllerLocation = ZoomControllerLocation.Right;

        public QrcodeConfig builder() {
            return new QrcodeConfig(this);
        }

        public Builder isShowPhotoAlbum(boolean showPhotoAlbum) {
            this.showPhotoAlbum = showPhotoAlbum;
            return this;
        }

        public Builder isShowBeep(boolean showBeep) {
            this.showBeep = showBeep;
            return this;
        }

        public Builder isShowVibrate(boolean showVibrate) {
            this.showVibrate = showVibrate;
            return this;
        }

        public Builder setScanColor(String scanColor) {
            this.scanColor = scanColor;
            return this;
        }

        public Builder setScanHintText(String scanHintText) {
            this.scanHintText = scanHintText;
            return this;
        }

        public Builder setActivityOpenAnime(int activityOpenAnime) {
            this.activityOpenAnime = activityOpenAnime;
            return this;
        }

        public Builder setActivityExitAnime(int activityExitAnime) {
            this.activityExitAnime = activityExitAnime;
            return this;
        }

        public Builder isShowZoomController(boolean showZoomController) {
            this.showZoomController = showZoomController;
            return this;
        }

        public Builder setZoomControllerLocation(ZoomControllerLocation zoomControllerLocation) {
            this.zoomControllerLocation = zoomControllerLocation;
            return this;
        }
    }
}

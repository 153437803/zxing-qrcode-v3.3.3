package lib.kalu.zxing.analyze;

import com.google.zxing.Result;

import lib.kalu.zxing.util.LogUtil;

import androidx.annotation.Nullable;

/**
 * @description:
 * @date: 2021-05-07 14:56
 */
interface AnalyzerDataImpl extends AnalyzerImageImpl {

    @Nullable
    @Override
    default Result analyzeData(byte[] data, int dataWidth, int dataHeight) {

        // ratio() > 1F ? 全屏扫描 : 区域扫描
        int outWidth = ratio() >= 1F ? dataWidth : (int) (Math.min(dataWidth, dataHeight) * ratio());
        int outHeight = ratio() >= 1F ? dataHeight : outWidth;
        int left = ratio() >= 1F ? 0 : dataWidth / 2 - outWidth / 2;
        int top = ratio() >= 1F ? 0 : dataHeight / 2 - outHeight / 2;

        LogUtil.log("analyzeData => dataWidth = " + dataWidth + ", dataHeight = " + dataHeight + ", left = " + left + ", top = " + top + ", outWidth = " + outWidth + ", outHeight = " + outHeight);
        return analyzeRect(data, dataWidth, dataHeight, left, top, outWidth, outHeight);
    }
}
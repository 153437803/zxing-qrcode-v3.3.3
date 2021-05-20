package lib.kalu.zxing.camerax;

import androidx.annotation.NonNull;

import com.google.zxing.DecodeHintType;

import java.util.EnumMap;
import java.util.Map;

/**
 * @description:
 * @date: 2021-05-07 14:55
 */
public final class DecodeFormatManager {

    /**
     * QR_CODE (最常用的二维码)
     */
    public static final Map<DecodeHintType, Object> DECODE_HINT_TYPE = createDecodeHint();

    /**
     * 支持解码的格式
     *
     * @return
     */
    public static Map<DecodeHintType, Object> createDecodeHint() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        addDecodeHintTypes(hints);
        return hints;
    }

    private static void addDecodeHintTypes(@NonNull Map<DecodeHintType, Object> hints) {
        // Spend more time to try to find a barcode; optimize for accuracy, not speed.
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        // Specifies what character encoding to use when decoding, where applicable (type String)
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
    }
}
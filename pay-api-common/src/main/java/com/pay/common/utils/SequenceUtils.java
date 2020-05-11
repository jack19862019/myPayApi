package com.pay.common.utils;

public class SequenceUtils {

    private static final int DEFAULT_LENGTH = 3;

    public static String getSequence(long seq) {
        String str = String.valueOf(seq);
        int len = str.length();
        if (len >= DEFAULT_LENGTH) {// 取决于业务规模,应该不会到达3
            return str;
        }
        int rest = DEFAULT_LENGTH - len;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rest; i++) {
            sb.append("00");
        }
        sb.append(str);
        return sb.toString();
    }
}

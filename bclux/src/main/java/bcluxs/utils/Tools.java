package bcluxs.utils;

import bcluxs.DBDao.SoldCommodity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

public class Tools {

    public static Date transTimestamp(long timestamp) {
        return Date.from(Instant.ofEpochSecond(timestamp));
    }

    public static boolean verifySerial(SoldCommodity soldCommodity, String serialNumber) {
        byte[] retailer = Tools.long2Byte((long) soldCommodity.getRetailer().getId());
        long realtime=soldCommodity.getTransactionTime().getTime()/1000;
        byte[] transactionTime = Tools.long2Byte(realtime);
        byte[] commNum = soldCommodity.getCommodity().getSerialNum().getBytes();
        byte[] source = new byte[retailer.length + transactionTime.length + commNum.length];
        System.arraycopy(retailer, 0, source, 0, retailer.length);
        System.arraycopy(transactionTime, 0, source, retailer.length, transactionTime.length);
        System.arraycopy(commNum, 0, source, retailer.length + transactionTime.length, commNum.length);
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert messageDigest != null;
        byte[] digest = messageDigest.digest(source);
        String encode = Tools.base58(digest);
        return encode.equals(serialNumber);
    }

    // base58编码
    private static String base58(byte[] input) {
        final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
        final char ENCODED_ZERO = ALPHABET[0];
        int[] INDEXES = new int[128];

        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }

        if (input.length == 0) {
            return "";
        }
        // 统计前导0
        int zeros = 0;
        while (zeros < input.length && input[zeros] == 0) {
            ++zeros;
        }
        // 复制一份进行修改
        input = Arrays.copyOf(input, input.length);
        // 最大编码数据长度
        char[] encoded = new char[input.length * 2];
        int outputStart = encoded.length;
        // Base58编码正式开始
        for (int inputStart = zeros; inputStart < input.length; ) {
            encoded[--outputStart] = ALPHABET[divmod(input, inputStart, 256, 58)];
            if (input[inputStart] == 0) {
                ++inputStart;
            }
        }
        // 输出结果中有0,去掉输出结果的前端0
        while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart;
        }
        // 处理前导0
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO;
        }
        // 返回Base58
        String bf = new String(encoded, outputStart, encoded.length - outputStart);
        StringBuffer sb = new StringBuffer(bf);
        return sb.reverse().toString();
    }

    // 进制转换代码
    private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = remainder * base + digit;
            number[i] = (byte) (temp / divisor);
            remainder = temp % divisor;
        }
        return (byte) remainder;
    }

    // 整数转换字节数组
    private static byte[] long2Byte(long src) {
        byte[] ret = new byte[8];
        ret[0] = (byte) (src >> 56);
        ret[1] = (byte) (src >> 48);
        ret[2] = (byte) (src >> 40);
        ret[3] = (byte) (src >> 32);
        ret[4] = (byte) (src >> 24);
        ret[5] = (byte) (src >> 16);
        ret[6] = (byte) (src >> 8);
        ret[7] = (byte) (src);
        return ret;
    }
}

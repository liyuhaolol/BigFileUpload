package cn.lyh.spa.bigfileupload.utils;

public class TimeUtils {

    /**
     * 生成时间戳为基准的随机数
     * @return
     */
    public static String upvideotimeStamp(){
        long time = System.currentTimeMillis();
        long i = (long) (1000 + Math.random() * (9999 - 1000+1 ));
        return String.valueOf(time)+ String.valueOf(i)+"0";
    }
}

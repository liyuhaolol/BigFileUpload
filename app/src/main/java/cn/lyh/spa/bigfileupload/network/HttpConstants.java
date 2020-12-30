package cn.lyh.spa.bigfileupload.network;


/**
 * Created by zhaolb on 2017/12/26.
 */

public class HttpConstants {
    public static final String UPLOAD_URL = "http://10.8.10.87:8080/TranserServer/upload";



    //分片上传图片
    ///public static final String UPLOAD_PIC = "http://testcms.newszu.com/uploadBySplit";
    public static final String UPLOAD_PIC = "http://10.8.10.100:8080/uploadBySplit";

    //合并图片接口
    //public static final String MERGE_PIC = "http://testcms.newszu.com/mergeFile";
    public static final String MERGE_PIC = "http://10.8.10.100:8080/mergeFile";

}

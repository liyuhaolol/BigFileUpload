package cn.lyh.spa.bigfileupload.utils.fenpian;

public interface UploadTaskListener {
    void onSuccess(String url);
    void onFailure(String msg);
    void onProgress();
}

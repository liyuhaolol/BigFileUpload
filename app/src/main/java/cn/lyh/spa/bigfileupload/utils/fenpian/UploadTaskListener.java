package cn.lyh.spa.bigfileupload.utils.fenpian;

import spa.lyh.cn.lib_https.model.Progress;

public interface UploadTaskListener {
    void onSuccess(String url);
    void onFailure(String msg);
    void onProgress(Progress progress);
}

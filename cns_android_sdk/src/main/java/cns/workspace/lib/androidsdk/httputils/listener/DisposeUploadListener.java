package cns.workspace.lib.androidsdk.httputils.listener;


/**
 * @author liyuhao
 * @function 监听上传进度
 */
public interface DisposeUploadListener {

    void onPiceSuccess(int progress, String currentSize, String sumSize);

    void onSuccess(String response);

    void onFailure(String reason);

    void onCancel(long startOffset);


}

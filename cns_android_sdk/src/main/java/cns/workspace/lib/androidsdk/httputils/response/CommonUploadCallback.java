package cns.workspace.lib.androidsdk.httputils.response;

import java.io.IOException;

import cns.workspace.lib.androidsdk.httputils.listener.DisposeDataHandle;
import cns.workspace.lib.androidsdk.httputils.response.base.CommonBase;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CommonUploadCallback extends CommonBase implements Callback {


    public CommonUploadCallback(DisposeDataHandle handle) {}


    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

    }
}

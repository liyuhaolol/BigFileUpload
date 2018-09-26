package cns.workspace.lib.androidsdk.httputils.request;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cns.workspace.lib.androidsdk.httputils.model.FilePart;
import cns.workspace.lib.androidsdk.httputils.model.PiceRequestBody;
import cns.workspace.lib.androidsdk.utils.md5.MD5resultListener;
import cns.workspace.lib.androidsdk.utils.md5.MD5utils;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.Request;

/**
 * @author 李宇昊
 * @function 接受请求参数，为我们生成request对象
 */
public class CommonRequest {
    /**
     * create the key-value Request
     *
     * @param url 链接
     * @param params 参数
     * @return request
     */
    public static Request createPostRequest(String url, RequestParams params) {
        return createPostRequest(url, params, null,false);
    }

    /**可以带请求头的Post请求
     * @param url 链接
     * @param params 参数
     * @param headers 头文件
     * @return request
     */

    public static Request createPostRequest(String url, RequestParams params, RequestParams headers, boolean isDev) {
        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                //将请求参数遍历添加到我们的请求构建类中
                mFormBodyBuild.add(entry.getKey(), entry.getValue());
            }
        }
        //添加请求头
        Headers.Builder mHeaderBuild = new Headers.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuild.add(entry.getKey(), entry.getValue());
            }
        }
        //通过请求构建类的build方法获取真正的请求对象
        FormBody mFormBody = mFormBodyBuild.build();
        //生成header
        Headers mHeader = mHeaderBuild.build();
        if (isDev){
            Log.e("WebUrl",url);
        }
        Request request = new Request.Builder().url(url).
                post(mFormBody).
                headers(mHeader)
                .build();
        return request;
    }

    /**
     * ressemble the params to the url
     *
     * @param url 链接
     * @param params 参数
     * @return request
     */
    public static Request createGetRequest(String url, RequestParams params) {

        return createGetRequest(url, params, null,false);
    }

    /**
     * 可以带请求头的Get请求
     * @param url 链接
     * @param params 参数
     * @param headers 头文件
     * @return request
     */
    public static Request createGetRequest(String url, RequestParams params, RequestParams headers, boolean isDev) {
        StringBuilder urlBuilder = new StringBuilder(url).append("?");
        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        //添加请求头
        Headers.Builder mHeaderBuild = new Headers.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuild.add(entry.getKey(), entry.getValue());
            }
        }
        Headers mHeader = mHeaderBuild.build();
        String webUrl = urlBuilder.substring(0, urlBuilder.length() - 1);//去掉末尾的&
        if (isDev){
            Log.e("WebUrl",webUrl);
        }
        return new Request.Builder().
                url(webUrl)
                .get()
                .headers(mHeader)
                .build();
    }

    /**
     * 上传文件生成Request
     * @param url 链接
     * @param params 额外的键值对参数
     * @param file 上传的文件
     * @param headers 额外的头部键值对从参数
     * @param isDev 是否为开发者模式
     * @return Request
     */
    public static Request createUploadSmallFileRequest(String url, RequestParams params, FilePart file, RequestParams headers, boolean isDev){

        List<FilePart> lst = new ArrayList<>();
        lst.add(file);
        return createUploadSmallFileRequest(url,params,lst,headers,isDev);
    }

    /**
     * 上传文件生成Request
     * @param url 链接
     * @param params 额外的键值对参数
     * @param fileList 上传的文件列表
     * @param headers 额外的头部键值对从参数
     * @param isDev 是否为开发者模式
     * @return Request
     */
    public static Request createUploadSmallFileRequest(String url, RequestParams params, List<FilePart> fileList, RequestParams headers, boolean isDev){

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                //将请求参数遍历添加到我们的请求构建类中
                multipartBodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        if (fileList != null && fileList.size() > 0){
            for (FilePart filePart:fileList){
                multipartBodyBuilder.addFormDataPart(filePart.name,filePart.fileName,filePart.body);
            }
        }

        //添加请求头
        Headers.Builder mHeaderBuild = new Headers.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuild.add(entry.getKey(), entry.getValue());
            }
        }

        //生成header
        Headers mHeader = mHeaderBuild.build();
        //构建请求体
        MultipartBody multipartBody = multipartBodyBuilder.build();

        if (isDev){
            Log.e("WebUrl",url);
        }

        Request request = new Request.Builder()
                .url(url).
                post(multipartBody).
                headers(mHeader)
                .build();
        return request;
    }

}
package cn.lyh.spa.bigfileupload.utils.md5;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import spa.lyh.cn.utils_io.IOUtils;

public class MD5utils {


    public static void getFileMD5sync1(String path, MD5resultListener listener) {
        File file = new File(path);
        getFileMD5sync1(file,listener);

    }


    public static void getFileMD5sync1(final File file, final MD5resultListener listener) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!file.isFile()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onResult("");
                }
            });
            return;
        }
        final MessageDigest[] digest = {null};
        final FileInputStream[] in = {null};
        final byte buffer[] = new byte[4 * 1024];
        final int[] len = new int[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    digest[0] = MessageDigest.getInstance("MD5");
                    in[0] = new FileInputStream(file);
                    while ((len[0] = in[0].read(buffer)) != -1) {
                        digest[0].update(buffer, 0, len[0]);
                    }
                    in[0].close();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult("");
                        }
                    });
                }

                final BigInteger bigInt = new BigInteger(1, digest[0].digest());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResult(bigInt.toString(16));
                    }
                });
            }
        });

        thread.start();
    }



    public static void getFileMD5sync(Context context,String filePath, MD5resultListener listener) {
        if (listener != null){
            File file = new File(filePath);
            if (file.exists() && file.isFile()){
                //是个文件
                FileInputStream fis = IOUtils.getFileInputStream(context,filePath);
                if (fis != null){
                    getFileMD5sync(fis,listener);
                }else {
                    listener.onResult("");
                }
            }else {
                listener.onResult("");
            }
        }
    }


    public static void getFileMD5sync(final FileInputStream fis, final MD5resultListener listener) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (fis == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onResult("");
                }
            });
            return;
        }
        final MessageDigest[] digest = {null};
        final byte buffer[] = new byte[4 * 1024];
        final int[] len = new int[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    digest[0] = MessageDigest.getInstance("MD5");
                    while ((len[0] = fis.read(buffer)) != -1) {
                        digest[0].update(buffer, 0, len[0]);
                    }
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult("");
                        }
                    });
                }

                final BigInteger bigInt = new BigInteger(1, digest[0].digest());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResult(bigInt.toString(16));
                    }
                });
            }
        });

        thread.start();
    }


    public static String getFileMD5(Context context,String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()){
            //是个文件
            FileInputStream fis = IOUtils.getFileInputStream(context,filePath);
            if (fis != null){
                return getFileMD5(fis);
            }else {
                return "";
            }
        }else {
            return "";
        }
    }

    public static String getFileMD5(FileInputStream fis) {
        if (fis == null) {
            return "";
        }
        final MessageDigest[] digest = {null};
        final byte buffer[] = new byte[4 * 1024];
        final int[] len = new int[1];
        try {
            digest[0] = MessageDigest.getInstance("MD5");
            while ((len[0] = fis.read(buffer)) != -1) {
                digest[0].update(buffer, 0, len[0]);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        final BigInteger bigInt = new BigInteger(1, digest[0].digest());
        return bigInt.toString(16);
    }
}

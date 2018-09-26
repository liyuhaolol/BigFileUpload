package cns.workspace.lib.androidsdk.utils.md5;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5utils {



    public static void getFileMD5sync(String path, MD5resultListener listener) {
        File file = new File(path);
        getFileMD5sync(file,listener);

    }


    public static void getFileMD5sync(final File file, final MD5resultListener listener) {
        final Handler handler = new Handler(Looper.getMainLooper());
        if (!file.isFile()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onResult("");
                }
            });
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

    public static String getFileMD5(String path) {
        File file = new File(path);
        return getFileMD5(file);

    }


    public static String getFileMD5(final File file) {
        if (!file.isFile()) {
            return "";
        }
        final MessageDigest[] digest = {null};
        final FileInputStream[] in = {null};
        final byte buffer[] = new byte[4 * 1024];
        final int[] len = new int[1];
        try {
            digest[0] = MessageDigest.getInstance("MD5");
            in[0] = new FileInputStream(file);
            while ((len[0] = in[0].read(buffer)) != -1) {
                digest[0].update(buffer, 0, len[0]);
            }
            in[0].close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        final BigInteger bigInt = new BigInteger(1, digest[0].digest());
        return bigInt.toString(16);
    }
}

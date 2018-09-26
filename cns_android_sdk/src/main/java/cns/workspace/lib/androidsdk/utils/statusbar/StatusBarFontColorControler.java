package cns.workspace.lib.androidsdk.utils.statusbar;

import android.app.Activity;

import cns.workspace.lib.androidsdk.utils.statusbar.lightmode.AndroidMHelper;
import cns.workspace.lib.androidsdk.utils.statusbar.lightmode.FlymeHelper;
import cns.workspace.lib.androidsdk.utils.statusbar.lightmode.MIUIHelper;

/**
 * Created by liyuhao on 2017/12/18.
 */

public class StatusBarFontColorControler {

    public static boolean setStatusBarMode(Activity activity, boolean isLightMode){
        boolean flag = false;

        if (new MIUIHelper().setLightMode(activity, isLightMode)) {
            flag = true;
        }else if (new FlymeHelper().setLightMode(activity, isLightMode)) {
            flag = true;
        }else if (new AndroidMHelper().setLightMode(activity, isLightMode)) {
            flag = true;
        }

        return flag;
    }
}

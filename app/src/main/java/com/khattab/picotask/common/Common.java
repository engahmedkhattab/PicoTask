package com.khattab.picotask.common;

import android.app.Activity;


public class Common {

    public boolean checkNetWork(Activity aActivity) {
        switch (NetworkUtil.getConnectivityStatus(aActivity)) {
            case OFFLINE:
                return false;
            case WIFI_CONNECTED_WITHOUT_INTERNET:
                return false;
            case MOBILE_DATA_CONNECTED:
            case WIFI_CONNECTED_WITH_INTERNET:
                return true;
            case UNKNOWN:
                return false;
            default:
                return false;
        }
    }

}

package com.mewhpm.mewphotoprism;

public class Const {
    public final static String APP_DB_NAME              = "mew-pp-database";
    public final static String SHARED_SETTINGS_NAME     = "com.mewhpm.mewphotoprism";
    public final static String SHARED_SETTINGS_VAL_UID  = "login-uid";
    public final static String SHARED_SETTINGS_DEFAULT_VIEW  = "default-view";

    public final static Integer ACTION_LONG_CLICK     = 1;
    public final static Integer ACTION_SHORT_CLICK    = 0;

    public final static String RECEIVER_USB_MTP_CAMERA_FRAGMENT     = "com.mewhpm.mewphotoprism.fragments.UsbCameraSyncFragment";

    public final static String BROADCAST_CAMERA_CONNECTED           = "com.mewhpm.mewphotoprism.camera_connected";
    public final static String BROADCAST_CAMERA_NAME                = "com.mewhpm.mewphotoprism.camera_name";
    public final static String BROADCAST_CAMERA_DEV_ID              = "com.mewhpm.mewphotoprism.camera_id";
    public final static String BROADCAST_CAMERA_MODEL               = "com.mewhpm.mewphotoprism.camera_model";
    public final static String BROADCAST_CAMERA_SERIAL              = "com.mewhpm.mewphotoprism.camera_serial";

    public final static String BROADCAST_SYNC_STAT_TOTAL            = "com.mewhpm.mewphotoprism.sync_stat_total";
    public final static String BROADCAST_SYNC_STAT_CURRENT          = "com.mewhpm.mewphotoprism.sync_stat_current";
    public final static String BROADCAST_SYNC_STAT_STRING           = "com.mewhpm.mewphotoprism.sync_stat_string";
}

/*
 * COPYRIGHT (C) 2023 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */

package com.fca.uconnect.managers;

import android.widget.RemoteViews;

import com.fca.uconnect.InvalidID;
import com.fca.uconnect.PropertyNotSupported;

public interface IStatusBarManager extends IFcaBaseInterface {

    public static final String version = "3.2";

    public static final int VALUE_PRIORITY_ONE   = 1;
    public static final int VALUE_PRIORITY_TWO   = 2;
    public static final int VALUE_PRIORITY_THREE = 3;
    public static final int VALUE_PRIORITY_FOUR  = 4;
    public static final int VALUE_PRIORITY_FIVE  = 5;

    int setDynamicContentToNotification(RemoteViews dynamicContentRv, int priority,
            FCAIStatusBarDynamicContent callback) throws PropertyNotSupported;

    boolean removeNotification(int id) throws PropertyNotSupported, InvalidID;

    int addIconToStatusBarList(RemoteViews iconCfgRv, FCAIStatusBarIconCallback callback)
            throws PropertyNotSupported;

    boolean removeIconFromStatusBarlist(int id) throws InvalidID;

    boolean registerStatusBarDynamicContentChange(FCAIStatusBarDynamicContent callBack);

    boolean unregisterStatusBarDynamicContentChange(FCAIStatusBarDynamicContent callBack);

    boolean registerStatusBarIconChange(FCAIStatusBarIconCallback callBack);

    boolean unregisterStatusBarIconChange(FCAIStatusBarIconCallback callBack);

    public interface FCAIStatusBarDynamicContent {
        default void onContentPressed(int id) { }
        default void onContentShownChange(int id, boolean isShown) { }
    }

    public interface FCAIStatusBarIconCallback {
        default void onIconPressed(int id) { }
        default void onIconShownChange(int id, boolean isShown) { }
    }
}

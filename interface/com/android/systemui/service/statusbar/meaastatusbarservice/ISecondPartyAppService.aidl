/*
 * COPYRIGHT (C) 2019 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */

package com.android.systemui.service.statusbar.meaastatusbarservice;
import android.widget.RemoteViews;
import com.android.systemui.service.statusbar.meaastatusbarservice.IStatusBarShortcutListener;
import com.android.systemui.service.statusbar.meaastatusbarservice.IStatusBarDynamicContentListener;
import com.android.systemui.service.statusbar.meaastatusbarservice.ILinkToDeathObject;

interface ISecondPartyAppService {

    int registerNewClient(in ILinkToDeathObject o);

    int setDynamicContentToNotification(in RemoteViews dynamicContentRv, int priority, int connectionId);
    boolean updateDynamicContent(int id, in RemoteViews dynamicContentRv, int priority, int connectionId);
    boolean removeNotification(int id, int connectionId);

    int addIconToStatusBarList(in RemoteViews iconCfgRv, int connectionId);
    boolean updateShortcutIcon(int id, in RemoteViews iconCfgRv, int connectionId);
    boolean removeIconFromStatusBarList(int id, int connectionId);

    boolean registerStatusBarDynamicContentChange(in IStatusBarDynamicContentListener l, int connectionId);
    boolean unregisterStatusBarDynamicContentChange(in IStatusBarDynamicContentListener l, int connectionId);

    boolean registerStatusBarIconChange(in IStatusBarShortcutListener l, int connectionId);
    boolean unregisterStatusBarIconChange(in IStatusBarShortcutListener l, int connectionId);
}

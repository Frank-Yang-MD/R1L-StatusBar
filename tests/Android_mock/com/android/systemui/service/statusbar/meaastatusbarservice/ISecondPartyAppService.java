/*
 * COPYRIGHT (C) 2023 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */

package com.android.systemui.service.statusbar.meaastatusbarservice;

import android.os.Binder;
import android.os.RemoteException;
import android.widget.RemoteViews;

import com.android.systemui.service.statusbar.meaastatusbarservice.IStatusBarShortcutListener;
import com.android.systemui.service.statusbar.meaastatusbarservice.IStatusBarDynamicContentListener;
import com.android.systemui.service.statusbar.meaastatusbarservice.ILinkToDeathObject;

public interface ISecondPartyAppService {

    int registerNewClient(ILinkToDeathObject o);

    int setDynamicContentToNotification(
            RemoteViews dynamicContentRv, int priority, int connectionId) throws RemoteException;
    boolean updateDynamicContent(int id, RemoteViews dynamicContentRv,
            int priority, int connectionId) throws RemoteException;
    boolean removeNotification(int id, int connectionId) throws RemoteException;

    int addIconToStatusBarList(RemoteViews iconCfgRv, int connectionId) throws RemoteException;
    boolean updateShortcutIcon(int id, RemoteViews iconCfgRv, int connectionId)
            throws RemoteException;
    boolean removeIconFromStatusBarList(int id, int connectionId) throws RemoteException;

    boolean registerStatusBarDynamicContentChange(
            IStatusBarDynamicContentListener l, int connectionId) throws RemoteException;
    boolean unregisterStatusBarDynamicContentChange(
            IStatusBarDynamicContentListener l, int connectionId) throws RemoteException;

    boolean registerStatusBarIconChange(IStatusBarShortcutListener l, int connectionId)
            throws RemoteException;
    boolean unregisterStatusBarIconChange(IStatusBarShortcutListener l, int connectionId)
            throws RemoteException;

    public static abstract class Stub extends Binder implements ISecondPartyAppService { }
}

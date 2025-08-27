/*
 * COPYRIGHT (C) 2023 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */

package com.android.systemui.service.statusbar.meaastatusbarservice;

import android.os.Binder;
import android.os.RemoteException;

public interface IStatusBarDynamicContentListener {
    void onContentPressed(int id) throws RemoteException;
    void onContentShownChange(int id, boolean isShown) throws RemoteException;

    public static abstract class Stub extends Binder implements IStatusBarDynamicContentListener {

    }
}
/*
 * COPYRIGHT (C) 2019 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */
package com.fca.ahu.lib.statusbarmanagerlib;

import android.os.Build;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.systemui.service.statusbar.meaastatusbarservice.ISecondPartyAppService;
import com.android.systemui.service.statusbar.meaastatusbarservice.IStatusBarDynamicContentListener;
import com.android.systemui.service.statusbar.meaastatusbarservice.IStatusBarShortcutListener;
import com.fca.uconnect.InvalidID;
import com.fca.uconnect.PropertyNotSupported;
import com.fca.uconnect.managers.IStatusBarManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by artur.menchenko@globallogic.com on 9/16/19.
 */
public class StatusBarManager implements IStatusBarManager {
    private static final int REMOTE_VIEW_MAX_SIZE_BYTES = 1024 * 100;//100kb
    private static final int ERROR_CODE = -1;

    private static final String TAG = StatusBarManager.class.getSimpleName();
    private static final String USER_DEBUG = "userdebug";

    private ISecondPartyAppService mService;
    private final Set<FCAIStatusBarIconCallback> mIStatusBarIconCallbackList =
            Collections.synchronizedSet(new HashSet<>());
    private final Set<FCAIStatusBarDynamicContent> mIStatusBarDynamicContentList =
            Collections.synchronizedSet(new HashSet<>());
    private int mConnectionId;

    public StatusBarManager(ISecondPartyAppService service, int connectionId) {
        super();
        if (Build.TYPE.equals(USER_DEBUG)) {
            Log.d(TAG, "StatusBarManager: constructor");
        }

        if (service == null) {
            logTheServiceIsNotInit("StatusBarManager");
            return;
        }

        mService = service;
        mConnectionId = connectionId;
        try {
            mService.registerStatusBarDynamicContentChange(mStatusBarDynamicContentListener,
                    mConnectionId);
            mService.registerStatusBarIconChange(mStatusBarShortcutListener, mConnectionId);
        } catch (RemoteException ignored) {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "StatusBarManager constructor: RemoteException");
            }
        }
    }

    @Override
    public int setDynamicContentToNotification(RemoteViews dynamicContentRv, int priority,
                                               FCAIStatusBarDynamicContent callback)
            throws PropertyNotSupported {
        if (mService == null) {
            logTheServiceIsNotInit("setDynamicContentToNotification");
            return ERROR_CODE;
        }

        if (isViewOverflowed(dynamicContentRv)) {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "setDynamicContentToNotification: RemoteView overflowed");
            }
            return ERROR_CODE;
        }
        if (Build.TYPE.equals(USER_DEBUG)) {
            Log.d(TAG, "setDynamicContentToNotification: RemoteView=" + dynamicContentRv +
                    " priority=" + priority + " callback=" + callback);
        }
        if (callback != null) {
            synchronized (mIStatusBarDynamicContentList) {
                mIStatusBarDynamicContentList.add(callback);
            }
        }
        try {
            return mService.setDynamicContentToNotification(dynamicContentRv, priority, mConnectionId);
        } catch (RemoteException e) {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "setDynamicContentToNotification: RemoteException, throwing PropertyNotSupported");
            }
            throw new PropertyNotSupported();
        }
    }

    @Override
    public boolean removeNotification(int id) throws InvalidID {
        if (mService == null) {
            logTheServiceIsNotInit("removeNotification");
            return false;
        }

        try {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "removeNotification: id" + id);
            }
            return mService.removeNotification(id, mConnectionId);
        } catch (RemoteException e) {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "removeNotification: RemoteException, throwing InvalidID");
            }
            throw new InvalidID();
        }
    }

    @Override
    public int addIconToStatusBarList(RemoteViews iconCfgRv, FCAIStatusBarIconCallback callback) {
        if (mService == null) {
            logTheServiceIsNotInit("addIconToStatusBarList");
            return ERROR_CODE;
        }

        if (isViewOverflowed(iconCfgRv)) {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "addIconToStatusBarList: RemoteView overflowed");
            }
            return ERROR_CODE;
        }
        if (Build.TYPE.equals(USER_DEBUG)) {
            Log.d(TAG, "addIconToStatusBarList: RemoteView=" + iconCfgRv + " callback=" + callback);
        }
        if (callback != null) {
            synchronized (mIStatusBarIconCallbackList) {
                mIStatusBarIconCallbackList.add(callback);
            }
        }
        try {
            return mService.addIconToStatusBarList(iconCfgRv, mConnectionId);
        } catch (RemoteException e) {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "addIconToStatusBarList: RemoteException");
            }
            return ERROR_CODE;
        }
    }

    @Override
    public boolean removeIconFromStatusBarlist(int id) throws InvalidID {
        if (mService == null) {
            logTheServiceIsNotInit("removeIconFromStatusBarlist");
            return false;
        }

        try {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "removeIconFromStatusBarlist: id=" + id);
            }
            return mService.removeIconFromStatusBarList(id, mConnectionId);
        } catch (RemoteException e) {
            if (Build.TYPE.equals(USER_DEBUG)) {
                Log.d(TAG, "removeIconFromStatusBarlist: RemoteException, throwing InvalidID");
            }
            throw new InvalidID();
        }
    }

    @Override
    public boolean registerStatusBarDynamicContentChange(FCAIStatusBarDynamicContent callBack) {
        if (Build.TYPE.equals(USER_DEBUG)) {
            Log.d(TAG, "registerStatusBarDynamicContentChange: callback=" + callBack);
        }
        synchronized (mIStatusBarDynamicContentList) {
            return mIStatusBarDynamicContentList.add(callBack);
        }
    }

    @Override
    public boolean unregisterStatusBarDynamicContentChange(FCAIStatusBarDynamicContent callBack) {
        if (Build.TYPE.equals(USER_DEBUG)) {
            Log.d(TAG, "unregisterStatusBarDynamicContentChange: callback=" + callBack);
        }
        synchronized (mIStatusBarDynamicContentList) {
            return mIStatusBarDynamicContentList.remove(callBack);
        }
    }

    @Override
    public boolean registerStatusBarIconChange(FCAIStatusBarIconCallback callBack) {
        if (Build.TYPE.equals(USER_DEBUG)) {
            Log.d(TAG, "registerStatusBarIconChange: callback=" + callBack);
        }
        synchronized (mIStatusBarIconCallbackList) {
            return mIStatusBarIconCallbackList.add(callBack);
        }
    }

    @Override
    public boolean unregisterStatusBarIconChange(FCAIStatusBarIconCallback callBack) {
        if (Build.TYPE.equals(USER_DEBUG)) {
            Log.d(TAG, "unregisterStatusBarIconChange: callback=" + callBack);
        }
        synchronized (mIStatusBarIconCallbackList) {
            return mIStatusBarIconCallbackList.remove(callBack);
        }
    }

    @Override
    public String getManagerVersion() {
        return version;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final IStatusBarShortcutListener mStatusBarShortcutListener =
            new IStatusBarShortcutListener.Stub() {

                public void onIconPressed(int id) {
                    if (Build.TYPE.equals(USER_DEBUG)) {
                        Log.d(TAG, "onIconPressed: id=" + id);
                    }
                    synchronized (mIStatusBarIconCallbackList) {
                        for (FCAIStatusBarIconCallback cb : mIStatusBarIconCallbackList) {
                            cb.onIconPressed(id);
                        }
                    }
                }

                public void onIconShownChange(int id, boolean isShown) {
                    if (Build.TYPE.equals(USER_DEBUG)) {
                        Log.d(TAG, "onIconShownChange: id=" + id + " isShown=" + isShown);
                    }
                    synchronized (mIStatusBarIconCallbackList) {
                        for (FCAIStatusBarIconCallback cb : mIStatusBarIconCallbackList) {
                            cb.onIconShownChange(id, isShown);
                        }
                    }
                }
            };

    @SuppressWarnings("FieldCanBeLocal")
    private final IStatusBarDynamicContentListener mStatusBarDynamicContentListener =
            new IStatusBarDynamicContentListener.Stub() {

                public void onContentPressed(int id) {
                    if (Build.TYPE.equals(USER_DEBUG)) {
                        Log.d(TAG, "onContentPressed: id=" + id);
                    }
                    synchronized (mIStatusBarDynamicContentList) {
                        for (FCAIStatusBarDynamicContent cb : mIStatusBarDynamicContentList) {
                            cb.onContentPressed(id);
                        }
                    }
                }

                public void onContentShownChange(int id, boolean isShown) {
                    if (Build.TYPE.equals(USER_DEBUG)) {
                        Log.d(TAG, "onContentShownChange: id=" + id + " isShown=" + isShown);
                    }
                    synchronized (mIStatusBarDynamicContentList) {
                        for (FCAIStatusBarDynamicContent cb : mIStatusBarDynamicContentList) {
                            cb.onContentShownChange(id, isShown);
                        }

                    }
                }
            };

    private static boolean isViewOverflowed(RemoteViews remoteViews) {
        //Check the size of a RemoteViews object
        final Parcel p = Parcel.obtain();
        //Disallow FDs only for checking the real size of the RemoteViews.
        p.pushAllowFds(false);
        remoteViews.writeToParcel(p, 0);
        final int dataSize = p.dataSize();
        p.recycle();

        return dataSize > REMOTE_VIEW_MAX_SIZE_BYTES;
    }

    private void logTheServiceIsNotInit(String methodName) {
        if (Build.TYPE.equals(USER_DEBUG)) {
            Log.d(TAG, methodName + ": the ISecondPartyAppService is not initialized");
        }
    }
}

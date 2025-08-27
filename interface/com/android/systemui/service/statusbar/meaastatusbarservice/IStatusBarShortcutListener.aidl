/*
 * COPYRIGHT (C) 2019 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */

package com.android.systemui.service.statusbar.meaastatusbarservice;

// IStatusBarShortcutListener.aidl
interface IStatusBarShortcutListener {
    void onIconPressed(int id);
    void onIconShownChange(int id, boolean isShown);
}
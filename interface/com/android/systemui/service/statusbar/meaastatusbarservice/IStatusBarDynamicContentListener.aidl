/*
 * COPYRIGHT (C) 2019 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */

package com.android.systemui.service.statusbar.meaastatusbarservice;

// IStatusBarDynamicContentListener.aidl
interface IStatusBarDynamicContentListener {
    void onContentPressed(int id);
    void onContentShownChange(int id, boolean isShown);
}
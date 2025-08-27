/*
 * COPYRIGHT (C) 2023 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */

package com.android.systemui.service.statusbar.meaastatusbarservice;

import android.os.Binder;

interface ILinkToDeathObject {

    public static abstract class Stub extends Binder implements ILinkToDeathObject {}

}
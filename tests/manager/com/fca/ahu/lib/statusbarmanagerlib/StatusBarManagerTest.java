/*
 * COPYRIGHT (C) 2019 MITSUBISHI ELECTRIC CORPORATION
 * ALL RIGHTS RESERVED
 */

package com.fca.ahu.lib.statusbarmanagerlib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import static org.powermock.api.support.membermodification.MemberMatcher.everythingDeclaredIn;

import android.os.Binder;
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
import com.fca.uconnect.managers.IStatusBarManager.FCAIStatusBarDynamicContent;
import com.fca.uconnect.managers.IStatusBarManager.FCAIStatusBarIconCallback;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Build.class, Binder.class, Log.class, Parcel.class})
@SuppressStaticInitializationFor({"android.os.Build"})
public class StatusBarManagerTest {

    private static final String BUILD_TYPE_FIELD_NAME = "TYPE";
    private static final String STATUS_BAR_ICON_CALLBACK_LIST_FIELD_NAME =
            "mIStatusBarIconCallbackList";
    private static final String STATUS_BAR_DYNAMIC_CALLBACK_LIST_FIELD_NAME =
            "mIStatusBarDynamicContentList";

    private static final String USER_DEBUG_BUILD = "userdebug";
    private static final String USER_BUILD = "user";

    private static final int CONNECTION_ID = 0;
    private static final int PRIORITY = 0;
    private static final int ERROR_CODE = -1;
    private static final int SUCCESS = 1;
    private static final int REMOTE_VIEW_MAX_SIZE_BYTES = 1024 * 100;

    @Mock private ISecondPartyAppService mISecondPartyAppServiceMock;
    @Mock private RemoteViews mRemoteViewsMock;
    @Mock private Parcel mParcelMock;
    @Mock private FCAIStatusBarDynamicContent mFcaIStatusBarDynamicContentMock;
    @Mock private FCAIStatusBarIconCallback mFcaIStatusBarIconCallbackMock;

    @Captor private ArgumentCaptor<IStatusBarShortcutListener> mIStatusBarShortcutListenerCaptor;
    @Captor private ArgumentCaptor<IStatusBarDynamicContentListener>
            mIStatusBarDynamicContentListenerCaptor;

    private StatusBarManager mStatusBarManager;

    @Before
    public void setup() {
        suppress(everythingDeclaredIn(Binder.class));
        mockStatic(Log.class);
    }

    @Test
    public void REF_30204_UT_001_StatusBarManager_constructor_userdebug_success() throws Exception {
        setupBuildType(USER_DEBUG_BUILD);

        assertNotNull(new StatusBarManager(mISecondPartyAppServiceMock, CONNECTION_ID));
        verifyListenersWasRegistered();
    }

    @Test
    public void REF_30204_UT_002_StatusBarManager_constructor_user_iconRemoteException()
            throws Exception {
        setupBuildType(USER_BUILD);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                registerStatusBarIconChange(any(), anyInt());

        assertNotNull(new StatusBarManager(mISecondPartyAppServiceMock, CONNECTION_ID));
    }

    @Test
    public void REF_30204_UT_003_StatusBarManager_constructor_user_dynamicRemoteException()
            throws Exception {
        setupBuildType(USER_BUILD);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                registerStatusBarDynamicContentChange(any(), anyInt());

        assertNotNull(new StatusBarManager(mISecondPartyAppServiceMock, CONNECTION_ID));
    }

    @Test
    public void REF_30204_UT_004_StatusBarManager_constructor_userdebug_dynamicRemoteException()
            throws Exception {
        setupBuildType(USER_DEBUG_BUILD);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                registerStatusBarDynamicContentChange(any(), anyInt());

        assertNotNull(new StatusBarManager(mISecondPartyAppServiceMock, CONNECTION_ID));
    }

    @Test
    public void REF_30204_UT_005_StatusBarManager_constructor_userdebug_iconRemoteException()
            throws Exception {
        setupBuildType(USER_DEBUG_BUILD);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                registerStatusBarIconChange(any(), anyInt());

        assertNotNull(new StatusBarManager(mISecondPartyAppServiceMock, CONNECTION_ID));
    }

    @Test
    public void REF_30204_UT_006_setDynamicContentToNotification_viewIsOverflowed_userdebug()
            throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES + 1);

        assertEquals(ERROR_CODE, mStatusBarManager.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_007_setDynamicContentToNotification_viewIsOverflowed_user()
            throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES + 1);

        assertEquals(ERROR_CODE, mStatusBarManager.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_008_setDynamicContentToNotification_user_success() throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        when(mISecondPartyAppServiceMock.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, CONNECTION_ID)).thenReturn(SUCCESS);

        assertEquals(SUCCESS, mStatusBarManager.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_009_setDynamicContentToNotification_userdebug_inputCallbackIsNull()
            throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        when(mISecondPartyAppServiceMock.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, CONNECTION_ID)).thenReturn(SUCCESS);

        assertEquals(SUCCESS, mStatusBarManager.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, null));
        verifyStatusBarDynamicCallbackWasNotRegistered();
    }

    @Test(expected = PropertyNotSupported.class)
    public void REF_30204_UT_010_setDynamicContentToNotification_userdebug_remoteException()
            throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                setDynamicContentToNotification(any(), anyInt(), anyInt());

        mStatusBarManager.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, mFcaIStatusBarDynamicContentMock);
    }

    @Test(expected = PropertyNotSupported.class)
    public void REF_30204_UT_011_setDynamicContentToNotification_user_remoteException()
            throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                setDynamicContentToNotification(any(), anyInt(), anyInt());

        mStatusBarManager.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, mFcaIStatusBarDynamicContentMock);
    }

    @Test
    public void REF_30204_UT_012_removeNotification_user_success() throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);

        when(mISecondPartyAppServiceMock.removeNotification(anyInt(), anyInt())).thenReturn(true);

        assertTrue(mStatusBarManager.removeNotification(CONNECTION_ID));
    }

    @Test
    public void REF_30204_UT_013_removeNotification_userdebug_failed() throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        when(mISecondPartyAppServiceMock.removeNotification(anyInt(), anyInt())).thenReturn(false);

        assertFalse(mStatusBarManager.removeNotification(CONNECTION_ID));
    }

    @Test(expected = InvalidID.class)
    public void REF_30204_UT_014_removeNotification_userdebug_remoteException() throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                removeNotification(anyInt(), anyInt());

        mStatusBarManager.removeNotification(CONNECTION_ID);
    }

    @Test(expected = InvalidID.class)
    public void REF_30204_UT_015_removeNotification_user_remoteException() throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                removeNotification(anyInt(), anyInt());

        mStatusBarManager.removeNotification(CONNECTION_ID);
    }

    @Test
    public void REF_30204_UT_016_addIconToStatusBarList_viewIsOverflowed_user() throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES + 1);

        assertEquals(ERROR_CODE, mStatusBarManager.addIconToStatusBarList(
                mRemoteViewsMock, mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_017_addIconToStatusBarList_viewIsOverflowed_userdebug()
            throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES + 1);

        assertEquals(ERROR_CODE, mStatusBarManager.addIconToStatusBarList(
                mRemoteViewsMock, mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_018_addIconToStatusBarList_userdebug_success() throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        when(mISecondPartyAppServiceMock.addIconToStatusBarList(any(), anyInt())).
                thenReturn(SUCCESS);

        assertEquals(SUCCESS, mStatusBarManager.addIconToStatusBarList(
                mRemoteViewsMock, mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_019_addIconToStatusBarList_user_failed() throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        when(mISecondPartyAppServiceMock.addIconToStatusBarList(any(), anyInt())).
                thenReturn(ERROR_CODE);

        assertEquals(ERROR_CODE, mStatusBarManager.addIconToStatusBarList(
                mRemoteViewsMock, mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_020_addIconToStatusBarList_user_remoteException() throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                addIconToStatusBarList(any(), anyInt());

        assertEquals(ERROR_CODE, mStatusBarManager.addIconToStatusBarList(
                mRemoteViewsMock, mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_021_addIconToStatusBarList_userdebug_remoteException()
            throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                addIconToStatusBarList(any(), anyInt());

        assertEquals(ERROR_CODE, mStatusBarManager.addIconToStatusBarList(
                mRemoteViewsMock, mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_022_addIconToStatusBarList_userdebug_callbackIsNull()
            throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        when(mISecondPartyAppServiceMock.addIconToStatusBarList(any(), anyInt())).
                thenReturn(SUCCESS);

        assertEquals(SUCCESS, mStatusBarManager.addIconToStatusBarList(mRemoteViewsMock, null));
        verifyStatusBarIconCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_023_removeIconFromStatusBarlist_user_success() throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);

        when(mISecondPartyAppServiceMock.removeIconFromStatusBarList(anyInt(), anyInt())).
                thenReturn(true);

        assertTrue(mStatusBarManager.removeIconFromStatusBarlist(CONNECTION_ID));
    }

    @Test
    public void REF_30204_UT_024_removeIconFromStatusBarlist_userdebug_failed() throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);

        when(mISecondPartyAppServiceMock.removeIconFromStatusBarList(anyInt(), anyInt())).
                thenReturn(false);

        assertFalse(mStatusBarManager.removeIconFromStatusBarlist(CONNECTION_ID));
    }

    @Test(expected = InvalidID.class)
    public void REF_30204_UT_025_removeIconFromStatusBarlist_userdebug_remoteException()
            throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                removeIconFromStatusBarList(anyInt(), anyInt());

        mStatusBarManager.removeIconFromStatusBarlist(CONNECTION_ID);
    }

    @Test(expected = InvalidID.class)
    public void REF_30204_UT_026_removeIconFromStatusBarlist_user_remoteException()
            throws Exception {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);

        doThrow(RemoteException.class).when(mISecondPartyAppServiceMock).
                removeIconFromStatusBarList(anyInt(), anyInt());

        mStatusBarManager.removeIconFromStatusBarlist(CONNECTION_ID);
    }

    @Test
    public void REF_30204_UT_027_registerStatusBarDynamicContentChange_user() {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);

        assertTrue(mStatusBarManager.
                registerStatusBarDynamicContentChange(mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_028_registerStatusBarDynamicContentChange_userdebug_alreadyReg() {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);

        assertTrue(mStatusBarManager.
                registerStatusBarDynamicContentChange(mFcaIStatusBarDynamicContentMock));
        assertFalse(mStatusBarManager.
                registerStatusBarDynamicContentChange(mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_029_unregisterStatusBarDynamicContentChange_user_success() {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);

        assertTrue(mStatusBarManager.
                registerStatusBarDynamicContentChange(mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasRegistered();

        assertTrue(mStatusBarManager.
                unregisterStatusBarDynamicContentChange(mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_030_unregisterStatusBarDynamicContentChange_userdebug_failed() {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);

        assertFalse(mStatusBarManager.
                unregisterStatusBarDynamicContentChange(mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_031_registerStatusBarIconChange_userdebug_success() {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);

        assertTrue(mStatusBarManager.registerStatusBarIconChange(mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_032_registerStatusBarIconChange_user_alreadyRegistered() {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);

        assertTrue(mStatusBarManager.registerStatusBarIconChange(mFcaIStatusBarIconCallbackMock));
        assertFalse(mStatusBarManager.registerStatusBarIconChange(mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasRegistered();
    }

    @Test
    public void REF_30204_UT_033_unregisterStatusBarIconChange_user_success() {
        initializeStatusBarManager(USER_BUILD, mISecondPartyAppServiceMock);

        assertTrue(mStatusBarManager.registerStatusBarIconChange(mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasRegistered();

        assertTrue(mStatusBarManager.unregisterStatusBarIconChange(mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_034_unregisterStatusBarIconChange_userdebug_notRegistered() {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);

        assertFalse(mStatusBarManager.
                unregisterStatusBarIconChange(mFcaIStatusBarIconCallbackMock));
        verifyStatusBarIconCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_035_getManagerVersion() {
        initializeStatusBarManager(USER_DEBUG_BUILD, mISecondPartyAppServiceMock);

        assertEquals(IStatusBarManager.version, mStatusBarManager.getManagerVersion());
    }

    @Test
    public void REF_30204_UT_036_onIconPressed_user() throws Exception {
        IStatusBarShortcutListener listener = captureIStatusBarShortcutListener(USER_BUILD);

        mStatusBarManager.registerStatusBarIconChange(mFcaIStatusBarIconCallbackMock);

        listener.onIconPressed(CONNECTION_ID);
        verify(mFcaIStatusBarIconCallbackMock).onIconPressed(CONNECTION_ID);
    }

    @Test
    public void REF_30204_UT_037_onIconPressed_userdebug_thereAreNoListeners() throws Exception {
        IStatusBarShortcutListener listener = captureIStatusBarShortcutListener(USER_DEBUG_BUILD);

        listener.onIconPressed(CONNECTION_ID);
        verify(mFcaIStatusBarIconCallbackMock, never()).onIconPressed(CONNECTION_ID);
    }

    @Test
    public void REF_30204_UT_038_onIconShownChange_user() throws Exception {
        IStatusBarShortcutListener listener = captureIStatusBarShortcutListener(USER_BUILD);

        mStatusBarManager.registerStatusBarIconChange(mFcaIStatusBarIconCallbackMock);

        listener.onIconShownChange(CONNECTION_ID, true);
        verify(mFcaIStatusBarIconCallbackMock).onIconShownChange(CONNECTION_ID, true);
    }

    @Test
    public void REF_30204_UT_039_onIconShownChange_userdebug_thereAreNoListeners()
            throws Exception {
        IStatusBarShortcutListener listener = captureIStatusBarShortcutListener(USER_DEBUG_BUILD);

        listener.onIconShownChange(CONNECTION_ID, false);
        verify(mFcaIStatusBarIconCallbackMock, never()).onIconShownChange(CONNECTION_ID, false);
    }

    @Test
    public void REF_30204_UT_040_onContentPressed_user() throws Exception {
        IStatusBarDynamicContentListener listener =
                captureIStatusBarDynamicContentListener(USER_BUILD);

        mStatusBarManager.registerStatusBarDynamicContentChange(mFcaIStatusBarDynamicContentMock);

        listener.onContentPressed(CONNECTION_ID);
        verify(mFcaIStatusBarDynamicContentMock).onContentPressed(CONNECTION_ID);
    }

    @Test
    public void REF_30204_UT_041_onContentPressed_userdebug_thereAreNoListeners() throws Exception {
        IStatusBarDynamicContentListener listener =
                captureIStatusBarDynamicContentListener(USER_DEBUG_BUILD);

        listener.onContentPressed(CONNECTION_ID);
        verify(mFcaIStatusBarDynamicContentMock, never()).onContentPressed(CONNECTION_ID);
    }

    @Test
    public void REF_30204_UT_042_onContentShownChange_user() throws Exception {
        IStatusBarDynamicContentListener listener =
                captureIStatusBarDynamicContentListener(USER_BUILD);

        mStatusBarManager.registerStatusBarDynamicContentChange(mFcaIStatusBarDynamicContentMock);

        listener.onContentShownChange(CONNECTION_ID, true);
        verify(mFcaIStatusBarDynamicContentMock).onContentShownChange(CONNECTION_ID, true);
    }

    @Test
    public void REF_30204_UT_043_onContentShownChange_userdebug_thereAreNoListeners()
            throws Exception {
        IStatusBarDynamicContentListener listener =
                captureIStatusBarDynamicContentListener(USER_DEBUG_BUILD);

        listener.onContentShownChange(CONNECTION_ID, false);
        verify(mFcaIStatusBarDynamicContentMock, never()).
                onContentShownChange(CONNECTION_ID, false);
    }

    @Test
    public void REF_30204_UT_044_StatusBarManager_constructor_inputServiceIsNull()
            throws Exception {
        setupBuildType(USER_DEBUG_BUILD);

        assertNotNull(new StatusBarManager(null, CONNECTION_ID));
        verifyListenersWasNeverRegistered();
    }

    @Test
    public void REF_30204_UT_045_setDynamicContentToNotification_user_serviceIsNull()
            throws Exception {
        initializeStatusBarManager(USER_BUILD, null);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        assertEquals(ERROR_CODE, mStatusBarManager.setDynamicContentToNotification(
                mRemoteViewsMock, PRIORITY, mFcaIStatusBarDynamicContentMock));
        verifyStatusBarDynamicCallbackWasNotRegistered();
    }

    @Test
    public void REF_30204_UT_046_removeNotification_user_serviceIsNull() throws Exception {
        initializeStatusBarManager(USER_BUILD, null);

        assertFalse(mStatusBarManager.removeNotification(CONNECTION_ID));
    }

    @Test
    public void REF_30204_UT_047_addIconToStatusBarList_userdebug_serviceIsNull() throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, null);
        setupParcelDataSize(REMOTE_VIEW_MAX_SIZE_BYTES);

        verifyStatusBarIconCallbackWasNotRegistered();
        assertEquals(ERROR_CODE, mStatusBarManager.addIconToStatusBarList(
                mRemoteViewsMock, mFcaIStatusBarIconCallbackMock));
    }

    @Test
    public void REF_30204_UT_048_removeIconFromStatusBarlist_userdebug_serviceIsNull()
            throws Exception {
        initializeStatusBarManager(USER_DEBUG_BUILD, null);

        assertFalse(mStatusBarManager.removeIconFromStatusBarlist(CONNECTION_ID));
    }

    /************************************ configurators ******************************************/

    private void initializeStatusBarManager(String buildType, ISecondPartyAppService service) {
        setupBuildType(buildType);

        mStatusBarManager = new StatusBarManager(service, CONNECTION_ID);
    }

    private IStatusBarShortcutListener captureIStatusBarShortcutListener(String buildType)
            throws Exception {
        initializeStatusBarManager(buildType, mISecondPartyAppServiceMock);

        verify(mISecondPartyAppServiceMock).registerStatusBarIconChange(
                mIStatusBarShortcutListenerCaptor.capture(), anyInt());

        return mIStatusBarShortcutListenerCaptor.getValue();
    }

    private IStatusBarDynamicContentListener captureIStatusBarDynamicContentListener(
            String buildType) throws Exception {
        initializeStatusBarManager(buildType, mISecondPartyAppServiceMock);

        verify(mISecondPartyAppServiceMock).registerStatusBarDynamicContentChange(
                mIStatusBarDynamicContentListenerCaptor.capture(), anyInt());

        return mIStatusBarDynamicContentListenerCaptor.getValue();
    }

    private void setupBuildType(String type) {
        mockStatic(Build.class);

        Whitebox.setInternalState(Build.class, BUILD_TYPE_FIELD_NAME, type);
    }

    private void setupParcelDataSize(int dataSize) {
        mockStatic(Parcel.class);

        when(Parcel.obtain()).thenReturn(mParcelMock);
        when(mParcelMock.dataSize()).thenReturn(dataSize);
    }

    /************************************* verificators ******************************************/

    private void verifyListenersWasRegistered() throws Exception {
        verify(mISecondPartyAppServiceMock).registerStatusBarDynamicContentChange(
                any(IStatusBarDynamicContentListener.class), eq(CONNECTION_ID));
        verify(mISecondPartyAppServiceMock).registerStatusBarIconChange(
                any(IStatusBarShortcutListener.class), eq(CONNECTION_ID));
    }

    private void verifyListenersWasNeverRegistered() throws Exception {
        verify(mISecondPartyAppServiceMock, never()).registerStatusBarDynamicContentChange(
                any(IStatusBarDynamicContentListener.class), eq(CONNECTION_ID));
        verify(mISecondPartyAppServiceMock, never()).registerStatusBarIconChange(
                any(IStatusBarShortcutListener.class), eq(CONNECTION_ID));
    }

    private void verifyStatusBarIconCallbackWasRegistered() {
        final Set<FCAIStatusBarIconCallback> callbackList = Whitebox.getInternalState(
                mStatusBarManager, STATUS_BAR_ICON_CALLBACK_LIST_FIELD_NAME);

        assertFalse(callbackList.isEmpty());
    }

    private void verifyStatusBarIconCallbackWasNotRegistered() {
        final Set<FCAIStatusBarIconCallback> callbackList = Whitebox.getInternalState(
                mStatusBarManager, STATUS_BAR_ICON_CALLBACK_LIST_FIELD_NAME);

        assertTrue(callbackList.isEmpty());
    }

    private void verifyStatusBarDynamicCallbackWasRegistered() {
        final Set<FCAIStatusBarIconCallback> callbackList = Whitebox.getInternalState(
                mStatusBarManager, STATUS_BAR_DYNAMIC_CALLBACK_LIST_FIELD_NAME);

        assertFalse(callbackList.isEmpty());
    }

    private void verifyStatusBarDynamicCallbackWasNotRegistered() {
        final Set<FCAIStatusBarIconCallback> callbackList = Whitebox.getInternalState(
                mStatusBarManager, STATUS_BAR_DYNAMIC_CALLBACK_LIST_FIELD_NAME);

        assertTrue(callbackList.isEmpty());
    }
}

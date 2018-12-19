/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui;

import static com.android.systemui.Dependency.LEAK_REPORT_EMAIL_NAME;

import android.annotation.Nullable;
import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.android.internal.colorextraction.ColorExtractor.GradientColors;
import com.android.internal.util.function.TriConsumer;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.fragments.FragmentService;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.power.EnhancedEstimatesImpl;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.notification.NotificationData;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationRowBinder;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.android.systemui.statusbar.phone.KeyguardEnvironmentImpl;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimState;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.volume.VolumeDialogComponent;

import java.util.function.Consumer;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

/**
 * Class factory to provide customizable SystemUI components.
 */
@Module
public class SystemUIFactory {
    private static final String TAG = "SystemUIFactory";

    static SystemUIFactory mFactory;
    private SystemUIRootComponent mRootComponent;

    public static <T extends SystemUIFactory> T getInstance() {
        return (T) mFactory;
    }

    public static void createFromConfig(Context context) {
        final String clsName = context.getString(R.string.config_systemUIFactoryComponent);
        if (clsName == null || clsName.length() == 0) {
            throw new RuntimeException("No SystemUIFactory component configured");
        }

        try {
            Class<?> cls = null;
            cls = context.getClassLoader().loadClass(clsName);
            mFactory = (SystemUIFactory) cls.newInstance();
            mFactory.init(context);
        } catch (Throwable t) {
            Log.w(TAG, "Error creating SystemUIFactory component: " + clsName, t);
            throw new RuntimeException(t);
        }
    }

    public SystemUIFactory() {}

    protected void init(Context context) {
        mRootComponent = DaggerSystemUIFactory_SystemUIRootComponent.builder()
                .systemUIFactory(this)
                .dependencyProvider(new com.android.systemui.DependencyProvider())
                .contextHolder(new ContextHolder(context))
                .build();
    }

    public SystemUIRootComponent getRootComponent() {
        return mRootComponent;
    }

    public StatusBarKeyguardViewManager createStatusBarKeyguardViewManager(Context context,
            ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        return new StatusBarKeyguardViewManager(context, viewMediatorCallback, lockPatternUtils);
    }

    public KeyguardBouncer createKeyguardBouncer(Context context, ViewMediatorCallback callback,
            LockPatternUtils lockPatternUtils,  ViewGroup container,
            DismissCallbackRegistry dismissCallbackRegistry,
            KeyguardBouncer.BouncerExpansionCallback expansionCallback) {
        return new KeyguardBouncer(context, callback, lockPatternUtils, container,
                dismissCallbackRegistry, FalsingManager.getInstance(context), expansionCallback);
    }

    public ScrimController createScrimController(ScrimView scrimBehind, ScrimView scrimInFront,
            LockscreenWallpaper lockscreenWallpaper,
            TriConsumer<ScrimState, Float, GradientColors> scrimStateListener,
            Consumer<Integer> scrimVisibleListener, DozeParameters dozeParameters,
            AlarmManager alarmManager) {
        return new ScrimController(scrimBehind, scrimInFront, scrimStateListener,
                scrimVisibleListener, dozeParameters, alarmManager);
    }

    public NotificationIconAreaController createNotificationIconAreaController(Context context,
            StatusBar statusBar) {
        return new NotificationIconAreaController(context, statusBar);
    }

    public KeyguardIndicationController createKeyguardIndicationController(Context context,
            ViewGroup indicationArea, LockIcon lockIcon) {
        return new KeyguardIndicationController(context, indicationArea, lockIcon);
    }

    public QSTileHost createQSTileHost(Context context, StatusBar statusBar,
            StatusBarIconController iconController) {
        return new QSTileHost(context, statusBar, iconController);
    }

    public VolumeDialogComponent createVolumeDialogComponent(SystemUI systemUi, Context context) {
        return new VolumeDialogComponent(systemUi, context);
    }

    @Singleton
    @Provides
    public NotificationData.KeyguardEnvironment provideKeyguardEnvironment(Context context) {
        return new KeyguardEnvironmentImpl();
    }

    @Singleton
    @Provides
    public NotificationLockscreenUserManager provideNotificationLockscreenUserManager(
            Context context) {
        return new NotificationLockscreenUserManagerImpl(context);
    }

    @Singleton
    @Provides
    public AssistManager provideAssistManager(DeviceProvisionedController controller,
            Context context) {
        return new AssistManager(controller, context);
    }

    @Singleton
    @Provides
    public NotificationEntryManager provideNotificationEntryManager(Context context) {
        return new NotificationEntryManager(context);
    }

    @Singleton
    @Provides
    public EnhancedEstimates provideEnhancedEstimates(Context context) {
        return new EnhancedEstimatesImpl();
    }

    @Singleton
    @Provides
    @Named(LEAK_REPORT_EMAIL_NAME)
    @Nullable
    public String provideLeakReportEmail() {
        return null;
    }

    @Singleton
    @Provides
    public NotificationListener provideNotificationListener(Context context) {
        return new NotificationListener(context);
    }

    @Module
    protected static class ContextHolder {
        private Context mContext;

        public ContextHolder(Context context) {
            mContext = context;
        }

        @Provides
        public Context provideContext() {
            return mContext;
        }
    }

    @Singleton
    @Component(modules = {SystemUIFactory.class, DependencyProvider.class, ContextHolder.class})
    public interface SystemUIRootComponent {
        @Singleton
        Dependency.DependencyInjector createDependency();

        /**
         * FragmentCreator generates all Fragments that need injection.
         */
        @Singleton
        FragmentService.FragmentCreator createFragmentCreator();
    }
}

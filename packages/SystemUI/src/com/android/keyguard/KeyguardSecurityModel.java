/*
 * Copyright (C) 2012 The Android Open Source Project
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
 * limitations under the License.
 */
package com.android.keyguard;

import static com.android.systemui.DejankUtils.whitelistIpcs;

import android.app.admin.DevicePolicyManager;
import android.content.res.Resources;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.qualifiers.Main;

import javax.inject.Inject;

@SysUISingleton
public class KeyguardSecurityModel {

    /**
     * The different types of security available.
     * @see KeyguardSecurityContainerController#showSecurityScreen
     */
    public enum SecurityMode {
        Invalid, // NULL state
        None, // No security enabled
        Pattern, // Unlock by drawing a pattern.
        Password, // Unlock by entering an alphanumeric password
        PIN, // Strictly numeric password
        SimPin, // Unlock by entering a sim pin.
        SimPuk // Unlock by entering a sim puk
    }

    private final boolean mIsPukScreenAvailable;

    private final LockPatternUtils mLockPatternUtils;

    @Inject
    KeyguardSecurityModel(@Main Resources resources, LockPatternUtils lockPatternUtils) {
        mIsPukScreenAvailable = resources.getBoolean(
                com.android.internal.R.bool.config_enable_puk_unlock_screen);
        mLockPatternUtils = lockPatternUtils;
    }

    public SecurityMode getSecurityMode(int userId) {
        KeyguardUpdateMonitor monitor = Dependency.get(KeyguardUpdateMonitor.class);

        if (mIsPukScreenAvailable && SubscriptionManager.isValidSubscriptionId(
                monitor.getNextSubIdForState(TelephonyManager.SIM_STATE_PUK_REQUIRED))) {
            return SecurityMode.SimPuk;
        }

        int subId = monitor.getUnlockedSubIdForState(TelephonyManager.SIM_STATE_PIN_REQUIRED);
        if (SubscriptionManager.isValidSubscriptionId((subId))){
            return SecurityMode.SimPin;
        }

        final int security = whitelistIpcs(() ->
                mLockPatternUtils.getActivePasswordQuality(userId));
        switch (security) {
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
                return SecurityMode.PIN;

            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
            case DevicePolicyManager.PASSWORD_QUALITY_MANAGED:
                return SecurityMode.Password;

            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                return SecurityMode.Pattern;
            case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
                return SecurityMode.None;

            default:
                throw new IllegalStateException("Unknown security quality:" + security);
        }
    }
}

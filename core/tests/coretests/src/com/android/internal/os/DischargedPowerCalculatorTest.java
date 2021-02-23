/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.internal.os;


import static com.google.common.truth.Truth.assertThat;

import android.os.BatteryManager;
import android.os.BatteryUsageStats;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DischargedPowerCalculatorTest {
    private static final double PRECISION = 0.00001;

    @Rule
    public final BatteryUsageStatsRule mStatsRule = new BatteryUsageStatsRule()
            .setAveragePower(PowerProfile.POWER_BATTERY_CAPACITY, 4000.0);

    @Test
    public void testDischargeTotals() {
        final BatteryStatsImpl batteryStats = mStatsRule.getBatteryStats();

        mStatsRule.setTime(1000, 1000);
        batteryStats.resetAllStatsCmdLocked();
        batteryStats.setNoAutoReset(true);
        batteryStats.setBatteryStateLocked(BatteryManager.BATTERY_STATUS_DISCHARGING, 100,
                /* plugType */ 0, 90, 72, 3700, 3_600_000, 4_000_000, 0, 1_000_000,
                1_000_000, 1_000_000);
        batteryStats.setBatteryStateLocked(BatteryManager.BATTERY_STATUS_DISCHARGING, 100,
                /* plugType */ 0, 80, 72, 3700, 2_400_000, 4_000_000, 0, 2_000_000,
                2_000_000, 2_000_000);

        DischargedPowerCalculator calculator =
                new DischargedPowerCalculator(mStatsRule.getPowerProfile());

        final BatteryUsageStats batteryUsageStats = mStatsRule.apply(calculator);

        assertThat(batteryUsageStats.getDischargePercentage()).isEqualTo(10);
        assertThat(batteryUsageStats.getDischargedPowerRange().getLower())
                .isWithin(PRECISION).of(360.0);
        assertThat(batteryUsageStats.getDischargedPowerRange().getUpper())
                .isWithin(PRECISION).of(400.0);
    }
}

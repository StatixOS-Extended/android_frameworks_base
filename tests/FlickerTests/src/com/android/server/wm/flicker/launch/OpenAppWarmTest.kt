/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.server.wm.flicker.launch

import android.platform.test.annotations.Presubmit
import androidx.test.filters.FlakyTest
import androidx.test.filters.RequiresDevice
import com.android.server.wm.flicker.FlickerParametersRunnerFactory
import com.android.server.wm.flicker.FlickerTestParameter
import com.android.server.wm.flicker.FlickerTestParameterFactory
import com.android.server.wm.flicker.annotation.Group1
import com.android.server.wm.flicker.helpers.setRotation
import com.android.server.wm.flicker.dsl.FlickerBuilder
import com.android.server.wm.flicker.helpers.isShellTransitionsEnabled
import org.junit.Assume.assumeFalse
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.junit.runners.Parameterized

/**
 * Test warm launching an app from launcher
 *
 * To run this test: `atest FlickerTests:OpenAppWarmTest`
 *
 * Actions:
 *     Launch [testApp]
 *     Press home
 *     Relaunch an app [testApp] and wait animation to complete (only this action is traced)
 *
 * Notes:
 *     1. Some default assertions (e.g., nav bar, status bar and screen covered)
 *        are inherited [OpenAppTransition]
 *     2. Part of the test setup occurs automatically via
 *        [com.android.server.wm.flicker.TransitionRunnerWithRules],
 *        including configuring navigation mode, initial orientation and ensuring no
 *        apps are running before setup
 */
@RequiresDevice
@RunWith(Parameterized::class)
@Parameterized.UseParametersRunnerFactory(FlickerParametersRunnerFactory::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Group1
class OpenAppWarmTest(testSpec: FlickerTestParameter) : OpenAppTransition(testSpec) {
    /**
     * Defines the transition used to run the test
     */
    override val transition: FlickerBuilder.() -> Unit
        get() = {
            super.transition(this)
            setup {
                test {
                    testApp.launchViaIntent(wmHelper)
                }
                eachRun {
                    device.pressHome()
                    wmHelper.waitForHomeActivityVisible()
                    this.setRotation(testSpec.startRotation)
                }
            }
            teardown {
                eachRun {
                    testApp.exit(wmHelper)
                }
            }
            transitions {
                testApp.launchViaIntent(wmHelper)
                wmHelper.waitForFullScreenApp(testApp.component)
            }
        }

    /** {@inheritDoc} */
    @Presubmit
    @Test
    override fun statusBarLayerRotatesScales() {
        // This test doesn't work in shell transitions because of b/206753786
        assumeFalse(isShellTransitionsEnabled)
        super.statusBarLayerRotatesScales()
    }

    /** {@inheritDoc} */
    @Presubmit
    @Test
    override fun appWindowReplacesLauncherAsTopWindow() {
        // This test doesn't work in shell transitions because of b/206094140
        assumeFalse(isShellTransitionsEnabled)
        super.appWindowReplacesLauncherAsTopWindow()
    }

    /** {@inheritDoc} */
    @Presubmit
    @Test
    override fun visibleLayersShownMoreThanOneConsecutiveEntry() {
        // This test doesn't work in shell transitions because of b/206094140
        assumeFalse(isShellTransitionsEnabled)
        super.visibleLayersShownMoreThanOneConsecutiveEntry()
    }

    /** {@inheritDoc} */
    @FlakyTest
    @Test
    override fun navBarLayerRotatesAndScales() = super.navBarLayerRotatesAndScales()

    /** {@inheritDoc} */
    @Presubmit
    @Test
    override fun appLayerReplacesLauncher() = super.appLayerReplacesLauncher()

    /** {@inheritDoc} */
    @Presubmit
    @Test
    override fun launcherWindowBecomesInvisible() = super.launcherWindowBecomesInvisible()

    /** {@inheritDoc} */
    @Presubmit
    @Test
    override fun navBarLayerIsVisible() = super.navBarLayerIsVisible()

    /** {@inheritDoc} */
    @Presubmit
    @Test
    override fun navBarWindowIsVisible() = super.navBarWindowIsVisible()

    companion object {
        /**
         * Creates the test configurations.
         *
         * See [FlickerTestParameterFactory.getConfigNonRotationTests] for configuring
         * repetitions, screen orientation and navigation modes.
         */
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun getParams(): Collection<FlickerTestParameter> {
            return FlickerTestParameterFactory.getInstance()
                .getConfigNonRotationTests()
        }
    }
}

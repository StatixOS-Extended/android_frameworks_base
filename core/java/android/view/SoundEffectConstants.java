/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.view;

import android.media.AudioManager;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.annotations.VisibleForTesting.Visibility;

import java.util.Random;

/**
 * Constants to be used to play sound effects via {@link View#playSoundEffect(int)}
 */
public class SoundEffectConstants {

    private SoundEffectConstants() {}
    private static final Random NAVIGATION_REPEAT_RANDOMIZER = new Random();
    private static int sLastNavigationRepeatSoundEffectId = -1;

    public static final int CLICK = 0;

    public static final int NAVIGATION_LEFT = 1;
    public static final int NAVIGATION_UP = 2;
    public static final int NAVIGATION_RIGHT = 3;
    public static final int NAVIGATION_DOWN = 4;
    /** Sound effect for a repeatedly triggered navigation, e.g. due to long pressing a button */
    public static final int NAVIGATION_REPEAT_LEFT = 5;
    /** @see #NAVIGATION_REPEAT_LEFT */
    public static final int NAVIGATION_REPEAT_UP = 6;
    /** @see #NAVIGATION_REPEAT_LEFT */
    public static final int NAVIGATION_REPEAT_RIGHT = 7;
    /** @see #NAVIGATION_REPEAT_LEFT */
    public static final int NAVIGATION_REPEAT_DOWN = 8;

    /**
     * Get the sonification constant for the focus directions.
     * @param direction One of {@link View#FOCUS_UP}, {@link View#FOCUS_DOWN},
     *     {@link View#FOCUS_LEFT}, {@link View#FOCUS_RIGHT}, {@link View#FOCUS_FORWARD}
     *     or {@link View#FOCUS_BACKWARD}

     * @return The appropriate sonification constant.
     * @throws {@link IllegalArgumentException} when the passed direction is not one of the
     *     documented values.
     */
    public static int getContantForFocusDirection(@View.FocusDirection int direction) {
        switch (direction) {
            case View.FOCUS_RIGHT:
                return SoundEffectConstants.NAVIGATION_RIGHT;
            case View.FOCUS_FORWARD:
            case View.FOCUS_DOWN:
                return SoundEffectConstants.NAVIGATION_DOWN;
            case View.FOCUS_LEFT:
                return SoundEffectConstants.NAVIGATION_LEFT;
            case View.FOCUS_BACKWARD:
            case View.FOCUS_UP:
                return SoundEffectConstants.NAVIGATION_UP;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
    }

    /**
     * Get the sonification constant for the focus directions
     * @param direction One of {@link View#FOCUS_UP}, {@link View#FOCUS_DOWN},
     *     {@link View#FOCUS_LEFT}, {@link View#FOCUS_RIGHT}, {@link View#FOCUS_FORWARD}
     *     or {@link View#FOCUS_BACKWARD}
     * @param repeating True if the user long-presses a direction
     * @return The appropriate sonification constant
     * @throws IllegalArgumentException when the passed direction is not one of the
     *      documented values.
     */
    public static int getConstantForFocusDirection(@View.FocusDirection int direction,
            boolean repeating) {
        if (repeating) {
            switch (direction) {
                case View.FOCUS_RIGHT:
                    return SoundEffectConstants.NAVIGATION_REPEAT_RIGHT;
                case View.FOCUS_FORWARD:
                case View.FOCUS_DOWN:
                    return SoundEffectConstants.NAVIGATION_REPEAT_DOWN;
                case View.FOCUS_LEFT:
                    return SoundEffectConstants.NAVIGATION_REPEAT_LEFT;
                case View.FOCUS_BACKWARD:
                case View.FOCUS_UP:
                    return SoundEffectConstants.NAVIGATION_REPEAT_UP;
            }
            throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, "
                    + "FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        } else {
            return getContantForFocusDirection(direction);
        }
    }

    /**
     * @param effectId any of the effect ids defined in {@link SoundEffectConstants}
     * @return true if the given effect id is a navigation repeat one
     * @hide
     */
    @VisibleForTesting(visibility = Visibility.PACKAGE)
    public static boolean isNavigationRepeat(int effectId) {
        return effectId == SoundEffectConstants.NAVIGATION_REPEAT_DOWN
                || effectId == SoundEffectConstants.NAVIGATION_REPEAT_LEFT
                || effectId == SoundEffectConstants.NAVIGATION_REPEAT_RIGHT
                || effectId == SoundEffectConstants.NAVIGATION_REPEAT_UP;
    }

    /**
     * @return The next navigation repeat sound effect id, chosen at random in a non-repeating
     * fashion
     * @hide
     */
    @VisibleForTesting(visibility = Visibility.PACKAGE)
    public static int nextNavigationRepeatSoundEffectId() {
        int next = NAVIGATION_REPEAT_RANDOMIZER.nextInt(
                AudioManager.NUM_NAVIGATION_REPEAT_SOUND_EFFECTS - 1);
        if (next >= sLastNavigationRepeatSoundEffectId) {
            next++;
        }
        sLastNavigationRepeatSoundEffectId = next;
        return AudioManager.getNthNavigationRepeatSoundEffect(next);
    }
}

/*
 * Copyright 2016 Juliane Lehmann <jl@lambdasoup.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.lambdasoup.quickfit.util;

import java.util.Locale;

/**
 * Allows referencing the locale set by the screengrabScript, or instead, if not running under
 * that, a fixed locale.
 */
public class LocaleUtil {

    /**
     * Not final, change in your test code if necessary
     */
    @SuppressWarnings("CanBeFinal")
    public static Locale FIXED_LOCALE = Locale.US;

    public static Locale getTestLocale() {
        Locale fromScreengrabScript = tools.fastlane.screengrab.locale.LocaleUtil.getTestLocale();
        return fromScreengrabScript != null ? fromScreengrabScript : FIXED_LOCALE;
    }
}

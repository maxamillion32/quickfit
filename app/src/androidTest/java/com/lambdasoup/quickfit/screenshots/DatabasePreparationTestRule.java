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

package com.lambdasoup.quickfit.screenshots;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import com.google.android.gms.fitness.FitnessActivities;
import com.lambdasoup.quickfit.model.DayOfWeek;
import com.lambdasoup.quickfit.persist.QuickFitContract.ScheduleEntry;
import com.lambdasoup.quickfit.persist.QuickFitContract.WorkoutEntry;
import com.lambdasoup.quickfit.persist.QuickFitDbHelper;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import tools.fastlane.screengrab.locale.LocaleUtil;

/**
 * TestRule that prepares the database with fixed values. Values depend on the test locale stored
 * in the instrumentation arguments (accessed via LocaleUtil.getTestLocale()), so it should
 * be used together with com.lambdasoup.quickfit.screenshots.FixedLocaleTestRule, to allow running out of AndroidStudio and not
 * just in the screengrab environment.
 */
public class DatabasePreparationTestRule implements TestRule {
    private static final String TAG = DatabasePreparationTestRule.class.getSimpleName();
    private QuickFitDbHelper dbHelper;

    static long NEXT_ALARM_MILLIS_PAST = 946681200000l; // year 2000
    static long NEXT_ALARM_MILLIS_FUTURE = 2524604400000l; // year 2050

    static ContentValues w1 = new ContentValues();
    static ContentValues w2 = new ContentValues();
    static ContentValues w3 = new ContentValues();

    static Map<Locale, Map<ContentValues, String>> labels = new HashMap<>();

    static ContentValues s11 = new ContentValues();
    static ContentValues s12 = new ContentValues();
    static ContentValues s21 = new ContentValues();
    static ContentValues s22 = new ContentValues();
    static ContentValues s23 = new ContentValues();

    static {
        w1.put(WorkoutEntry.COL_ID, 1l);
        w1.put(WorkoutEntry.COL_ACTIVITY_TYPE, FitnessActivities.DANCING);
        w1.put(WorkoutEntry.COL_DURATION_MINUTES, 90);

        w2.put(WorkoutEntry.COL_ID, 2l);
        w2.put(WorkoutEntry.COL_ACTIVITY_TYPE, FitnessActivities.HIGH_INTENSITY_INTERVAL_TRAINING);
        w2.put(WorkoutEntry.COL_DURATION_MINUTES, 15);
        w2.put(WorkoutEntry.COL_CALORIES, 120);

        w3.put(WorkoutEntry.COL_ID, 3l);
        w3.put(WorkoutEntry.COL_ACTIVITY_TYPE, FitnessActivities.BADMINTON);
        w3.put(WorkoutEntry.COL_DURATION_MINUTES, 30);

        Map<ContentValues, String> labelsEn = new HashMap<>();
        labelsEn.put(w1, "Latin dance group");
        labelsEn.put(w2, "Jenny's vid");

        Map<ContentValues, String> labelsDe = new HashMap<>();
        labelsDe.put(w1, "Lateinamerika-Tanzgruppe");
        labelsDe.put(w2, "Jennys Video");

        labels.put(Locale.US, labelsEn);
        labels.put(Locale.GERMANY, labelsDe);

        s11.put(ScheduleEntry.COL_ID, 1l);
        s11.put(ScheduleEntry.COL_WORKOUT_ID, 1l);
        s11.put(ScheduleEntry.COL_DAY_OF_WEEK, DayOfWeek.TUESDAY.name());
        s11.put(ScheduleEntry.COL_HOUR, 18);
        s11.put(ScheduleEntry.COL_MINUTE, 30);
        s11.put(ScheduleEntry.COL_NEXT_ALARM_MILLIS, NEXT_ALARM_MILLIS_FUTURE);
        s11.put(ScheduleEntry.COL_SHOW_NOTIFICATION, ScheduleEntry.SHOW_NOTIFICATION_NO);

        s12.put(ScheduleEntry.COL_ID, 5l);
        s12.put(ScheduleEntry.COL_WORKOUT_ID, 1l);
        s12.put(ScheduleEntry.COL_DAY_OF_WEEK, DayOfWeek.SATURDAY.name());
        s12.put(ScheduleEntry.COL_HOUR, 15);
        s12.put(ScheduleEntry.COL_MINUTE, 0);
        s12.put(ScheduleEntry.COL_NEXT_ALARM_MILLIS, NEXT_ALARM_MILLIS_FUTURE);
        s12.put(ScheduleEntry.COL_SHOW_NOTIFICATION, ScheduleEntry.SHOW_NOTIFICATION_NO);

        s21.put(ScheduleEntry.COL_ID, 2l);
        s21.put(ScheduleEntry.COL_WORKOUT_ID, 2l);
        s21.put(ScheduleEntry.COL_DAY_OF_WEEK, DayOfWeek.MONDAY.name());
        s21.put(ScheduleEntry.COL_HOUR, 12);
        s21.put(ScheduleEntry.COL_MINUTE, 0);
        s21.put(ScheduleEntry.COL_NEXT_ALARM_MILLIS, NEXT_ALARM_MILLIS_FUTURE);
        s21.put(ScheduleEntry.COL_SHOW_NOTIFICATION, ScheduleEntry.SHOW_NOTIFICATION_NO);

        s22.put(ScheduleEntry.COL_ID, 3l);
        s22.put(ScheduleEntry.COL_WORKOUT_ID, 2l);
        s22.put(ScheduleEntry.COL_DAY_OF_WEEK, DayOfWeek.WEDNESDAY.name());
        s22.put(ScheduleEntry.COL_HOUR, 13);
        s22.put(ScheduleEntry.COL_MINUTE, 0);
        s22.put(ScheduleEntry.COL_NEXT_ALARM_MILLIS, NEXT_ALARM_MILLIS_FUTURE);
        s22.put(ScheduleEntry.COL_SHOW_NOTIFICATION, ScheduleEntry.SHOW_NOTIFICATION_NO);

        s23.put(ScheduleEntry.COL_ID, 4l);
        s23.put(ScheduleEntry.COL_WORKOUT_ID, 2l);
        s23.put(ScheduleEntry.COL_DAY_OF_WEEK, DayOfWeek.FRIDAY.name());
        s23.put(ScheduleEntry.COL_HOUR, 17);
        s23.put(ScheduleEntry.COL_MINUTE, 0);
        s23.put(ScheduleEntry.COL_NEXT_ALARM_MILLIS, NEXT_ALARM_MILLIS_FUTURE);
        s23.put(ScheduleEntry.COL_SHOW_NOTIFICATION, ScheduleEntry.SHOW_NOTIFICATION_NO);
    }

    public DatabasePreparationTestRule() {
        this.dbHelper = new QuickFitDbHelper(InstrumentationRegistry.getTargetContext());
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try (SQLiteDatabase conn = dbHelper.getWritableDatabase()) {
                    Locale testLocale = LocaleUtil.getTestLocale();

                    conn.delete(WorkoutEntry.TABLE_NAME, null, null);

                    conn.insert(WorkoutEntry.TABLE_NAME, null, w1);
                    conn.insert(WorkoutEntry.TABLE_NAME, null, w2);
                    conn.insert(WorkoutEntry.TABLE_NAME, null, w3);

                    for (Map.Entry<ContentValues, String> label : labels.get(testLocale).entrySet()) {
                        ContentValues v = new ContentValues();
                        v.put(WorkoutEntry.COL_LABEL, label.getValue());
                        conn.update(WorkoutEntry.TABLE_NAME, v, WorkoutEntry.COL_ID + "=" + label.getKey().get(WorkoutEntry.COL_ID), null);
                    }

                    conn.insert(ScheduleEntry.TABLE_NAME, null, s11);
                    conn.insert(ScheduleEntry.TABLE_NAME, null, s12);
                    conn.insert(ScheduleEntry.TABLE_NAME, null, s21);
                    conn.insert(ScheduleEntry.TABLE_NAME, null, s22);
                    conn.insert(ScheduleEntry.TABLE_NAME, null, s23);
                }

                base.evaluate();
            }
        };
    }
}
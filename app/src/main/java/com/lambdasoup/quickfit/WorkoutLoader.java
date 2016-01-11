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

package com.lambdasoup.quickfit;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import static com.lambdasoup.quickfit.QuickFitContentProvider.AUTHORITY;
import static com.lambdasoup.quickfit.QuickFitContentProvider.PATH_WORKOUTS;

/**
 * Created by jl on 11.01.16.
 */
public class WorkoutLoader extends CursorLoader {
    public WorkoutLoader(Context context, long id) {
        super(
                context,
                ContentUris.appendId(new Uri.Builder().scheme("content").authority(AUTHORITY).path(PATH_WORKOUTS), id).build(),
                QuickFitContract.WorkoutEntry.COLUMNS,
                null,
                null,
                QuickFitContract.WorkoutEntry._ID + " ASC"
        );
    }
}

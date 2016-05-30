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

package com.lambdasoup.quickfit.ui;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.lambdasoup.quickfit.R;
import com.lambdasoup.quickfit.model.DayOfWeek;
import com.lambdasoup.quickfit.util.Arrays;

/**
 * Created by jl on 30.05.16.
 */
public class DayOfWeekDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String KEY_SCHEDULE_ID = "scheduleId";

    private OnFragmentInteractionListener listener;
    private DayOfWeek[] week;

    public DayOfWeekDialogFragment() {
        // It's a fragment, it needs a default constructor
    }

    public static DayOfWeekDialogFragment newInstance(long objectId, DayOfWeek oldValue) {
        DayOfWeekDialogFragment fragment = new DayOfWeekDialogFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_SCHEDULE_ID, objectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListenerProvider) {
            listener = ((OnFragmentInteractionListenerProvider) activity).getOnDayOfWeekDialogFragmentInteractionListener();
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnFragmentInteractionListenerProvider");
        }
        week = DayOfWeek.getWeek();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_schedule_dayOfWeek)
                .setItems(Arrays.map(week, String[].class, dayOfWeek -> getResources().getString(dayOfWeek.fullNameResId)), this)
                .setOnDismissListener(this)
                .create();

        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (listener != null) {
            listener.onListItemChanged(getArguments().getLong(KEY_SCHEDULE_ID), week[which]);
        }
    }

    interface OnFragmentInteractionListenerProvider {
        OnFragmentInteractionListener getOnDayOfWeekDialogFragmentInteractionListener();
    }

    interface OnFragmentInteractionListener {
        void onListItemChanged(long objectId, DayOfWeek newValue);
    }
}
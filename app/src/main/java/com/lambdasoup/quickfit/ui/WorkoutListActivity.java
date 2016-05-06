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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.util.SortedList;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.fitness.FitnessActivities;
import com.lambdasoup.quickfit.FitActivityService;
import com.lambdasoup.quickfit.R;
import com.lambdasoup.quickfit.persist.QuickFitContentProvider;
import com.lambdasoup.quickfit.persist.QuickFitContract.WorkoutEntry;
import com.lambdasoup.quickfit.util.ui.DividerItemDecoration;
import com.lambdasoup.quickfit.util.ui.EmptyRecyclerView;

import timber.log.Timber;

import static android.support.v7.widget.RecyclerView.NO_ID;


public class WorkoutListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        WorkoutItemRecyclerViewAdapter.OnWorkoutInteractionListener, DurationMinutesDialogFragment.OnFragmentInteractionListener,
        LabelDialogFragment.OnFragmentInteractionListener, CaloriesDialogFragment.OnFragmentInteractionListener, TimeDialogFragment.OnFragmentInteractionListenerProvider {


    public static final String EXTRA_SHOW_WORKOUT_ID = "com.lambdasoup.quickfit.show_workout_id";

    private static final String KEY_SHOW_WORKOUT_ID = "com.lambdasoup.quickfit.show_workout_id";
    private static final String KEY_SELECTED_ITEM_ID = "com.lambdasoup.quickfit.WorkoutListActivity_selected_item_id";
    private static final long FIRST_ITEM_IF_EXISTS = -2;


    private boolean isTwoPane;
    private WorkoutItemRecyclerViewAdapter workoutsAdapter;
    private EmptyRecyclerView workoutsRecyclerView;
    private long idToSelect = NO_ID;
    private FloatingActionButton fab;
    private View fabAddSchedule;
    private View fabAddWorkout;
    private float offsetFabAddWorkout;
    private float offsetFabAddSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        toolbar.setTitle(getTitle());

        fab = (FloatingActionButton) findViewById(R.id.fab);
        //noinspection ConstantConditions
        fab.setOnClickListener(view -> addNewWorkout());

        if (findViewById(R.id.schedules_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            isTwoPane = true;
            fabAddSchedule = findViewById(R.id.fab_add_schedule);
            //noinspection ConstantConditions
            fabAddSchedule.setOnClickListener(view -> addNewSchedule());
            fabAddWorkout = findViewById(R.id.fab_add_workout);
            //noinspection ConstantConditions
            fabAddWorkout.setOnClickListener(view -> addNewWorkout());

            setMiniFabOffsets();
        }

        workoutsRecyclerView = (EmptyRecyclerView) findViewById(R.id.workout_list);
        workoutsAdapter = new WorkoutItemRecyclerViewAdapter(this, isTwoPane);
        workoutsAdapter.setOnWorkoutInteractionListener(this);

        workoutsRecyclerView.setAdapter(workoutsAdapter);
        workoutsRecyclerView.addItemDecoration(new DividerItemDecoration(this, false));

        readIntentExtras();

        if (savedInstanceState != null) {
            idToSelect = savedInstanceState.getLong(KEY_SHOW_WORKOUT_ID, NO_ID);
            if (idToSelect == NO_ID) {
                idToSelect = savedInstanceState.getLong(KEY_SELECTED_ITEM_ID, NO_ID);
            }
        } else {
            idToSelect = FIRST_ITEM_IF_EXISTS;
        }

        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readIntentExtras();
    }

    private void readIntentExtras() {
        if (getIntent().hasExtra(EXTRA_SHOW_WORKOUT_ID)) {
            idToSelect = getIntent().getLongExtra(EXTRA_SHOW_WORKOUT_ID, NO_ID);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_SHOW_WORKOUT_ID, idToSelect);
        outState.putLong(KEY_SELECTED_ITEM_ID, workoutsAdapter.getSelectedItemId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_workout_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getBaseContext(), SettingsActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(getBaseContext(), AboutActivity.class));
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("creating loader");
        return new WorkoutListLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Timber.d("onLoadFinished, data size=%d", data != null ? data.getCount() : 0);
        workoutsAdapter.swapCursor(data);

        if (idToSelect == FIRST_ITEM_IF_EXISTS) {
            idToSelect = workoutsAdapter.getItemId(0);
        }

        if (idToSelect != NO_ID) {
            int pos = workoutsAdapter.getPosition(idToSelect);
            if (pos != SortedList.INVALID_POSITION) {
                workoutsAdapter.setSelectedItemId(idToSelect);
                workoutsRecyclerView.smoothScrollToPosition(pos);
                Timber.d("scrolled");
            }
            idToSelect = NO_ID;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Timber.d("onLoaderReset");
        workoutsAdapter.swapCursor(null);
    }

    private void setMiniFabOffsets() {
        int fabSize = getResources().getDimensionPixelSize(android.support.design.R.dimen.design_fab_size_normal);
        int miniFabSize = getResources().getDimensionPixelSize(android.support.design.R.dimen.design_fab_size_mini);
        int separation = getResources().getDimensionPixelSize(R.dimen.list_item_height) - miniFabSize;

        offsetFabAddWorkout = ((float) fabSize) / 2.0f + separation + ((float) miniFabSize) / 2.0f;
        offsetFabAddSchedule = offsetFabAddWorkout + miniFabSize + separation;
    }

    private void showMiniFabs() {
        // TODO: correct mini fab icons
        Timber.d("showing mini fabs");

        Timber.d("before show: fabAddWorkout.top %d fab.top %d", fabAddWorkout.getTop(), fab.getTop());
        Timber.d("before show: fabAddWorkout.bottom %d fab.bottom %d", fabAddWorkout.getBottom(), fab.getBottom());
        Timber.d("before show: fabAddWorkout.height %d fab.height %d", fabAddWorkout.getHeight(), fab.getHeight());

        if (! isTwoPane) {
            return;
        }

        fabAddSchedule.setVisibility(View.VISIBLE);
        fabAddSchedule.animate().translationY(-offsetFabAddSchedule).alpha(1.0f);
        fabAddWorkout.setVisibility(View.VISIBLE);
        fabAddWorkout.animate().translationY(-offsetFabAddWorkout).alpha(1.0f);

        fab.setOnClickListener(view -> hideMiniFabs());
        // TODO: animate fab color?
    }


    private void hideMiniFabs() {
        Timber.d("hiding mini fabs");

        if (!isTwoPane) {
            return;
        }

        fabAddSchedule.animate().translationYBy(0).alpha(0.0f).withEndAction(() -> fabAddSchedule.setVisibility(View.INVISIBLE));
        fabAddWorkout.animate().translationYBy(0).alpha(0.0f).withEndAction(() -> fabAddWorkout.setVisibility(View.INVISIBLE));

        fab.setOnClickListener(view -> showMiniFabs());
    }

    private void addNewWorkout() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WorkoutEntry.COL_ACTIVITY_TYPE, FitnessActivities.AEROBICS);
        contentValues.put(WorkoutEntry.COL_DURATION_MINUTES, 30);
        Uri newWorkoutUri = getContentResolver().insert(QuickFitContentProvider.getUriWorkoutsList(), contentValues);
        idToSelect = ContentUris.parseId(newWorkoutUri);
        hideMiniFabs();
    }

    private void addNewSchedule() {
        SchedulesFragment schedulesFragment = (SchedulesFragment) getSupportFragmentManager().findFragmentById(R.id.schedules_container);
        if (schedulesFragment != null) {
            schedulesFragment.onAddNewSchedule();
        } else {
            Timber.d("cannot add new schedule: no schedulesFragment attached");
        }
        hideMiniFabs();
    }

    private void ensureSchedulesPaneShown() {
        if (!isTwoPane) {
            return;
        }

        FrameLayout listPane = (FrameLayout) findViewById(R.id.list_pane);
        ViewGroup.LayoutParams layoutParams = listPane.getLayoutParams();
        Timber.d("ensureSchedulesPaneShown: list_pane layout params %s", layoutParams);
        if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            showSchedulesPane();
        }

    }

    private void showSchedulesPane() {
        if (!isTwoPane) {
            Timber.wtf("showSchedulesPane called despite not in two-pane layout mode");
            return;
        }
        Timber.d("showing schedules pane");

        CoordinatorLayout.LayoutParams fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        fabLayoutParams.setAnchorId(R.id.list_pane);
        fabLayoutParams.anchorGravity = Gravity.BOTTOM | Gravity.END;
        fabLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        fab.setLayoutParams(fabLayoutParams);

        fab.setOnClickListener(view -> showMiniFabs());

        // TODO: animate
        FrameLayout listPane = (FrameLayout) findViewById(R.id.list_pane);
        ViewGroup.LayoutParams layoutParams = listPane.getLayoutParams();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics());
        listPane.setLayoutParams(layoutParams);
    }

    private void hideSchedulesPane() {
        if (!isTwoPane) {
            Timber.wtf("hideSchedulesPane called despite not in two-pane layout mode");
            return;
        }
        CoordinatorLayout.LayoutParams fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        fabLayoutParams.setAnchorId(View.NO_ID);
        fabLayoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        fab.setLayoutParams(fabLayoutParams);

        hideMiniFabs();
        fab.setOnClickListener(view -> addNewWorkout());

        // TODO: animate
        FrameLayout listPane = (FrameLayout) findViewById(R.id.list_pane);
        ViewGroup.LayoutParams layoutParams = listPane.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        listPane.setLayoutParams(layoutParams);

        Fragment schedulesFragment = getSupportFragmentManager().findFragmentById(R.id.schedules_container);
        Timber.d("schedulesFragment before remove: %s", schedulesFragment);
        getSupportFragmentManager().beginTransaction()
                .remove(schedulesFragment)
                .commit();

    }

    @Override
    public void onItemSelected(long workoutId) {
        Timber.d("item selected. Id is %d", workoutId);
        if (workoutId == NO_ID) {
            hideSchedulesPane();
        } else {
            ensureSchedulesPaneShown();
            updateSchedulesPane(workoutId);
        }
    }

    private void updateSchedulesPane(long workoutId) {
        if (isTwoPane) {
            SchedulesFragment fragment = (SchedulesFragment) getSupportFragmentManager().findFragmentById(R.id.schedules_container);
            Timber.d("schedules fragment is present: %b", fragment != null);
            if (workoutId != NO_ID && (fragment == null || workoutId != fragment.getWorkoutId())) {
                // TODO: update workoutId in existing fragment?
                SchedulesFragment newFragment = SchedulesFragment.create(workoutId);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.schedules_container, newFragment)
                        .commit();
            }
        }
    }


    @Override
    public void onDoneItClick(long workoutId) {
        startService(FitActivityService.getIntentInsertSession(getApplicationContext(), workoutId));
    }

    @Override
    public void onDeleteClick(long workoutId) {
        workoutsAdapter.setSelectedItemIdAfterDeletionOf(workoutId);
        getContentResolver().delete(QuickFitContentProvider.getUriWorkoutsId(workoutId), null, null);
    }

    @Override
    public void onSchedulesEditRequested(long workoutId) {
        if (isTwoPane) {
            Timber.wtf("onSchedulesEditRequested despite in two-pane layout mode");
            return;
        }
        Intent intent = new Intent(getApplicationContext(), SchedulesActivity.class);
        intent.putExtra(SchedulesActivity.EXTRA_WORKOUT_ID, workoutId);
        startActivity(intent);
    }


    @Override
    public void onActivityTypeChanged(long workoutId, String newActivityTypeKey) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WorkoutEntry.COL_ACTIVITY_TYPE, newActivityTypeKey);
        getContentResolver().update(QuickFitContentProvider.getUriWorkoutsId(workoutId), contentValues, null, null);
    }

    @Override
    public void onDurationMinsEditRequested(long workoutId, int oldValue) {
        showDialog(DurationMinutesDialogFragment.newInstance(workoutId, oldValue));
    }

    @Override
    public void onDurationChanged(long workoutId, int newValue) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WorkoutEntry.COL_DURATION_MINUTES, newValue);
        getContentResolver().update(QuickFitContentProvider.getUriWorkoutsId(workoutId), contentValues, null, null);
    }

    @Override
    public void onLabelEditRequested(long workoutId, String oldValue) {
        showDialog(LabelDialogFragment.newInstance(workoutId, oldValue));
    }

    @Override
    public void onLabelChanged(long workoutId, String newValue) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WorkoutEntry.COL_LABEL, newValue);
        getContentResolver().update(QuickFitContentProvider.getUriWorkoutsId(workoutId), contentValues, null, null);
    }

    @Override
    public void onCaloriesEditRequested(long workoutId, int oldValue) {
        showDialog(CaloriesDialogFragment.newInstance(workoutId, oldValue));
    }

    @Override
    public void onCaloriesChanged(long workoutId, int newValue) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WorkoutEntry.COL_CALORIES, newValue);
        getContentResolver().update(QuickFitContentProvider.getUriWorkoutsId(workoutId), contentValues, null, null);
    }


    @Override
    public TimeDialogFragment.OnFragmentInteractionListener getOnFragmentInteractionListener() {
        return (SchedulesFragment) getSupportFragmentManager().findFragmentById(R.id.schedules_container);
    }
}

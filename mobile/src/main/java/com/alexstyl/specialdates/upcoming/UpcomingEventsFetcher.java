package com.alexstyl.specialdates.upcoming;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.alexstyl.specialdates.date.CelebrationDate;
import com.alexstyl.specialdates.date.Date;
import com.alexstyl.specialdates.events.bankholidays.BankHolidayProvider;
import com.alexstyl.specialdates.events.bankholidays.GreekBankHolidaysCalculator;
import com.alexstyl.specialdates.events.namedays.calendar.OrthodoxEasterCalculator;
import com.alexstyl.specialdates.events.namedays.calendar.resource.NamedayCalendarProvider;
import com.alexstyl.specialdates.service.PeopleEventsProvider;
import com.novoda.notils.exception.DeveloperError;

import java.util.List;

class UpcomingEventsFetcher {

    private static final int LOADER_ID_DATES = 2;

    private final LoaderManager loaderManager;
    private final Date startingDate;
    private final Context context;
    private Callback callback;

    UpcomingEventsFetcher(LoaderManager loaderManager, Context context, Date startingDate) {
        this.loaderManager = loaderManager;
        this.context = context;
        this.startingDate = startingDate;
    }

    void loadDatesStartingFromDate(Callback callback) {
        this.callback = callback;
        this.loaderManager.restartLoader(LOADER_ID_DATES, null, loaderCallbacks);
    }

    private final LoaderManager.LoaderCallbacks<List<CelebrationDate>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<CelebrationDate>>() {

        @Override
        public Loader<List<CelebrationDate>> onCreateLoader(int loaderID, Bundle arg1) {
            if (loaderID == LOADER_ID_DATES) {
                PeopleEventsProvider peopleEventsProvider = PeopleEventsProvider.newInstance(context);
                NamedayCalendarProvider namedayCalendarProvider = NamedayCalendarProvider.newInstance(context.getResources());
                BankHolidayProvider bankHolidayProvider = new BankHolidayProvider(new GreekBankHolidaysCalculator(OrthodoxEasterCalculator.INSTANCE));
                return new UpcomingEventsLoader(
                        context,
                        startingDate,
                        peopleEventsProvider,
                        bankHolidayProvider,
                        namedayCalendarProvider
                );
            }
            throw new DeveloperError("Unhandled loaderID: " + loaderID);
        }

        @Override
        public void onLoadFinished(Loader<List<CelebrationDate>> loader, List<CelebrationDate> celebrationDates) {
            if (loader.getId() == LOADER_ID_DATES) {
                callback.onUpcomingDatesLoaded(celebrationDates);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<CelebrationDate>> loader) {
            // no-op
        }
    };

    interface Callback {
        void onUpcomingDatesLoaded(List<CelebrationDate> dates);

    }
}

package com.alexstyl.specialdates.upcoming;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;

import com.alexstyl.specialdates.date.CelebrationDate;
import com.alexstyl.specialdates.date.ContactEvent;
import com.alexstyl.specialdates.date.Date;
import com.alexstyl.specialdates.date.DateComparator;
import com.alexstyl.specialdates.events.bankholidays.BankHoliday;
import com.alexstyl.specialdates.events.bankholidays.BankHolidayProvider;
import com.alexstyl.specialdates.events.bankholidays.BankHolidaysPreferences;
import com.alexstyl.specialdates.events.namedays.NamedayLocale;
import com.alexstyl.specialdates.events.namedays.NamedayPreferences;
import com.alexstyl.specialdates.events.namedays.NamesInADate;
import com.alexstyl.specialdates.events.namedays.calendar.NamedayCalendar;
import com.alexstyl.specialdates.events.namedays.calendar.resource.NamedayCalendarProvider;
import com.alexstyl.specialdates.service.PeopleEventsProvider;
import com.alexstyl.specialdates.ui.loader.SimpleAsyncTaskLoader;
import com.alexstyl.specialdates.util.ContactsObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class UpcomingEventsLoader extends SimpleAsyncTaskLoader<List<CelebrationDate>> {

    private static final DateComparator COMPARATOR = DateComparator.INSTANCE;

    private final PeopleEventsProvider peopleEventsProvider;
    private final NamedayPreferences namedayPreferences;
    private final Date startingPeriod;
    private final ContactsObserver contactsObserver;
    private final BankHolidaysPreferences bankHolidaysPreferences;
    private final BankHolidayProvider bankHolidayProvider;
    private final NamedayCalendarProvider namedayCalendarProvider;

    UpcomingEventsLoader(Context context,
                         Date startingPeriod,
                         PeopleEventsProvider peopleEventsProvider,
                         BankHolidayProvider bankHolidayProvider,
                         NamedayCalendarProvider namedayCalendarProvider
    ) {
        super(context);
        this.peopleEventsProvider = peopleEventsProvider;
        this.namedayPreferences = NamedayPreferences.newInstance(context);
        this.startingPeriod = startingPeriod;
        this.bankHolidayProvider = bankHolidayProvider;
        this.namedayCalendarProvider = namedayCalendarProvider;
        this.contactsObserver = new ContactsObserver(getContentResolver(), new Handler());
        this.bankHolidaysPreferences = BankHolidaysPreferences.newInstance(getContext());
        setupContactsObserver();
    }

    @Override
    protected void onUnregisterObserver() {
        super.onUnregisterObserver();
        contactsObserver.unregister();
    }

    @Override
    public List<CelebrationDate> loadInBackground() {
        TimePeriod timePeriod = TimePeriod.between(
                startingPeriod,
                startingPeriod.addDay(364)
        );
        List<CelebrationDate> celebrationDates = calculateEventsBetween(timePeriod);
        Collections.sort(celebrationDates);
        return celebrationDates;
    }

    private List<CelebrationDate> calculateEventsBetween(TimePeriod period) {
        List<ContactEvent> contactEvents = peopleEventsProvider.getCelebrationDateFor(period);
        UpcomingDatesBuilder upcomingDatesBuilder = new UpcomingDatesBuilder(period)
                .withContactEvents(contactEvents);

        if (shouldLoadBankHolidays()) {
            List<BankHoliday> bankHolidays = bankHolidayProvider.calculateBankHolidaysBetween(period);
            upcomingDatesBuilder.withBankHolidays(bankHolidays);
        }

        if (shouldLoadNamedays()) {
            List<NamesInADate> namedays = calculateNamedaysBetween(period);
            upcomingDatesBuilder.withNamedays(namedays);
        }
        return upcomingDatesBuilder.build();
    }

    private boolean shouldLoadBankHolidays() {
        return bankHolidaysPreferences.isEnabled();
    }

    private List<NamesInADate> calculateNamedaysBetween(TimePeriod timeDuration) {
        NamedayLocale selectedLanguage = namedayPreferences.getSelectedLanguage();
        NamedayCalendar namedayCalendar = namedayCalendarProvider.loadNamedayCalendarForLocale(selectedLanguage, timeDuration.getStartingDate().getYear());

        Date indexDate = timeDuration.getStartingDate();
        Date toDate = timeDuration.getEndingDate();
        List<NamesInADate> namedays = new ArrayList<>();

        while (COMPARATOR.compare(indexDate, toDate) < 0) {
            NamesInADate allNamedayOn = namedayCalendar.getAllNamedayOn(indexDate);
            if (allNamedayOn.nameCount() > 0) {
                namedays.add(allNamedayOn);
            }
            indexDate = indexDate.addDay(1);
        }
        return namedays;
    }

    private boolean shouldLoadNamedays() {
        return namedayPreferences.isEnabled() && !namedayPreferences.isEnabledForContactsOnly();
    }

    private void setupContactsObserver() {
        contactsObserver.registerWith(
                new ContactsObserver.Callback() {
                    @Override
                    public void onContactsUpdated() {
                        onContentChanged();
                    }
                }
        );
    }

    public ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }

}

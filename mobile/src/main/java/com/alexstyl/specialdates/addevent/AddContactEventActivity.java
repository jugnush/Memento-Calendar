package com.alexstyl.specialdates.addevent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alexstyl.specialdates.R;
import com.alexstyl.specialdates.addevent.ui.ContactHeroView;
import com.alexstyl.specialdates.addevent.ui.ContactsAutoCompleteView;
import com.alexstyl.specialdates.analytics.Action;
import com.alexstyl.specialdates.analytics.ActionWithParameters;
import com.alexstyl.specialdates.analytics.Analytics;
import com.alexstyl.specialdates.analytics.AnalyticsProvider;
import com.alexstyl.specialdates.contact.Contact;
import com.alexstyl.specialdates.date.Date;
import com.alexstyl.specialdates.events.database.EventTypeId;
import com.alexstyl.specialdates.events.peopleevents.EventType;
import com.alexstyl.specialdates.events.peopleevents.StandardEventType;
import com.alexstyl.specialdates.theming.MementoTheme;
import com.alexstyl.specialdates.theming.Themer;
import com.alexstyl.specialdates.ui.base.ThemedActivity;
import com.novoda.notils.caster.Views;
import com.novoda.notils.meta.AndroidUtils;

import java.util.ArrayList;

import static com.alexstyl.specialdates.events.peopleevents.StandardEventType.BIRTHDAY;

public class AddContactEventActivity extends ThemedActivity {

    private static final String EXTRA_EVENT_TYPE = "event_type";

    private ContactHeroView contactHeroView;
    private BirthdayLabelView birthdayLabel;
    private BirthdayFormPresenter presenter;
    private Analytics analytics;
    private boolean eventCreated;
    private EventType eventType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        analytics = AnalyticsProvider.getAnalytics(this);
        MementoTheme theme = Themer.get(this).getCurrentTheme();
        setContentView(R.layout.activity_add_birthday, theme);

        contactHeroView = Views.findById(this, R.id.addbirthday_hero);
        birthdayLabel = Views.findById(this, R.id.addbirthday_datepicker);
        Button addButton = Views.findById(this, R.id.addbirthday_add_button);
        Button dismissButton = Views.findById(this, R.id.addbirthday_dismiss_button);

        contactHeroView.setListener(listener);
        birthdayLabel.setOnEditListener(onBirthdayLabelClicked);
        addButton.setOnClickListener(onAddButtonClickListener);
        dismissButton.setOnClickListener(onDismissButtonClickListener);

        eventType = getEventTypeFrom(getIntent());
        presenter = new BirthdayFormPresenter(contactHeroView, birthdayLabel, addButton);

        BirthdayPickerDialog.resetListenerToDialog(getSupportFragmentManager(), birthdaySelectedListener);
        addButton.setEnabled(false);
    }

    private EventType getEventTypeFrom(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return BIRTHDAY;
        }
        @EventTypeId int rawEventType = extras.getInt(EXTRA_EVENT_TYPE, BIRTHDAY.getId());
        return StandardEventType.fromId(rawEventType);
    }

    private final View.OnClickListener onAddButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ContactPersister contactPersister = new ContactPersister(context(), getContentResolver());
            if (presenter.isPresentingNoStoredContact()) {
                // store contact
                AccountData account = getAccountToStoreContact();
                String contactName = contactHeroView.getText().toString();
                Date birthdaySelected = birthdayLabel.getDisplayingBirthday();
                contactPersister
                        .createContactWithNameAndBirthday(
                                contactName,
                                birthdaySelected,
                                account
                        );
            } else {
                Date selectedBirthday = presenter.getDisplayingBirthday();
                Contact selectedContact = presenter.getSelectedContact();
                contactPersister
                        .addBirthdayToExistingContact(
                                selectedContact,
                                selectedBirthday
                        );
            }
            finishSuccessfully();
        }

        private AccountData getAccountToStoreContact() {
            WriteableAccountsProvider accountsProvider = WriteableAccountsProvider.from(context());
            ArrayList<AccountData> availableAccounts = accountsProvider.getAvailableAccounts();
            if (availableAccounts.size() == 0) {
                return AccountData.NO_ACCOUNT;
            } else {
                return availableAccounts.get(0);
            }
        }

    };

    private void finishSuccessfully() {
        eventCreated = true;
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        trackEventCreation();
    }

    private void trackEventCreation() {
        ActionWithParameters actionWithParameters;
        if (eventCreated) {
            actionWithParameters = birthdayCreationSuccess();
        } else {
            actionWithParameters = birthdayCreationFailed();
        }
        analytics.trackAction(actionWithParameters);
    }

    private ActionWithParameters birthdayCreationSuccess() {
        return new ActionWithParameters(Action.ADD_BIRTHDAY, "success", "true");
    }

    private ActionWithParameters birthdayCreationFailed() {
        return new ActionWithParameters(Action.ADD_BIRTHDAY, "success", "false");
    }

    private final BirthdayLabelView.OnEditListener onBirthdayLabelClicked = new BirthdayLabelView.OnEditListener() {
        @Override
        public void onEdit() {
            displayBirthdayPickerDialog();
        }

        private void displayBirthdayPickerDialog() {
            Date birthday = birthdayLabel.getDisplayingBirthday();
            BirthdayPickerDialog dialog;
            if (birthday == null) {
                dialog = BirthdayPickerDialog.createDialog(birthdaySelectedListener);
            } else {
                dialog = BirthdayPickerDialog.createDialogFor(birthday, birthdaySelectedListener);
            }
            dialog.show(getSupportFragmentManager(), BirthdayPickerDialog.TAG);
        }
    };

    private final ContactHeroView.Listener listener = new ContactHeroView.Listener() {

        @Override
        public void onContactSelected(ContactsAutoCompleteView nameSuggestion, Contact contact) {
            presenter.displayContactAsSelected(contact);
            AndroidUtils.requestHideKeyboard(context(), nameSuggestion);
            nameSuggestion.clearFocus();
        }

        @Override
        public boolean onEditorAction(TextView view) {
            AndroidUtils.requestHideKeyboard(context(), view);
            view.clearFocus();
            return true;
        }

        @Override
        public void onUserChangedContactName(String newText) {
            presenter.presentNoContact();
        }

    };

    private BirthdayPickerDialog.OnBirthdaySelectedListener birthdaySelectedListener = new BirthdayPickerDialog.OnBirthdaySelectedListener() {
        @Override
        public void onBirthdaySet(Date birthday) {
            presenter.presentBirthday(birthday);
        }
    };

    private final View.OnClickListener onDismissButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    public static Intent createAddBirthdayIntent(Activity activity) {
        Intent intent = new Intent(activity, AddContactEventActivity.class);
        intent.putExtra(EXTRA_EVENT_TYPE, BIRTHDAY);
        return intent;
    }
}

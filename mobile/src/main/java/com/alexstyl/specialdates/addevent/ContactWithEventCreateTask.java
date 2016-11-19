package com.alexstyl.specialdates.addevent;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.alexstyl.specialdates.ErrorTracker;
import com.alexstyl.specialdates.R;
import com.alexstyl.specialdates.date.Date;

import java.util.ArrayList;

class ContactWithEventCreateTask extends AsyncTask<Void, Void, Boolean> {

    private static final int NEW_CONTACT_ID = 0;

    private final String contactName;
    private final Date birthday;
    private final ContentResolver contentResolver;
    private final Context context;
    private final AccountData account;
    private final int eventType;

    static ContactWithEventCreateTask createBirthday(String contactName, Date birthday, ContentResolver contentResolver, Context context, AccountData account) {
        int eventType = ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
        return new ContactWithEventCreateTask(contactName, birthday, contentResolver, context, account, eventType);
    }

    private ContactWithEventCreateTask(String contactName, Date birthday,
                                       ContentResolver contentResolver,
                                       Context context,
                                       AccountData account,
                                       int eventType
    ) {
        this.contactName = contactName;
        this.birthday = birthday;
        this.contentResolver = contentResolver;
        this.context = context;
        this.account = account;
        this.eventType = eventType;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return createNewContactWithBirthday();
    }

    private boolean createNewContactWithBirthday() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.getAccountName())
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.getAccountType())
                        .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
                        .build());

        ops.add(insertBirthdayOperation());
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        } catch (RemoteException | OperationApplicationException e) {
            ErrorTracker.track(e);
            return false;
        }
    }

    private ContentProviderOperation insertBirthdayOperation() {
        return ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, NEW_CONTACT_ID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Event.TYPE, eventType)
                .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, birthday.toString())
                .build();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, R.string.add_birthday_contact_added, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.add_birthday_failed_to_add_contact, Toast.LENGTH_SHORT).show();
        }
    }
}

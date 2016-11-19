package com.alexstyl.specialdates.addevent;

import android.content.ContentResolver;
import android.content.Context;

import com.alexstyl.specialdates.contact.Contact;
import com.alexstyl.specialdates.date.Date;

class ContactPersister {

    private final ContentResolver contentResolver;
    private final Context context;

    ContactPersister(Context context, ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.context = context;
    }

    void createContactWithNameAndBirthday(String contactName, Date birthday, AccountData account) {
        ContactWithEventCreateTask.createBirthday(contactName, birthday, contentResolver, context, account).execute();
    }

    void addBirthdayToExistingContact(Contact contact, Date birthday) {
        new AddBirthdayToContactTask(context, contentResolver, birthday, contact).execute();
    }
}

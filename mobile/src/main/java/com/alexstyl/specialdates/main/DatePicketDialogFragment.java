package com.alexstyl.specialdates.main;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.alexstyl.specialdates.date.Date;
import com.alexstyl.specialdates.ui.base.MementoDialog;
import com.novoda.notils.logger.simple.Log;

public class DatePicketDialogFragment extends MementoDialog {

    private final DatePickerDialog.OnDateSetListener NO_OP = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Log.w("onDateSet() but no listener was set");
        }
    };

    private DatePickerDialog.OnDateSetListener listener = NO_OP;

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Date today = Date.today();
        return new DatePickerDialog(getActivity(), listener, today.getYear(), today.getMonth() - 1, today.getDayOfMonth());
    }
}

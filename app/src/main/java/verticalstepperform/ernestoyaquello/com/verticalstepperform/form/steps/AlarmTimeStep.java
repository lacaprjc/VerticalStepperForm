package verticalstepperform.ernestoyaquello.com.verticalstepperform.form.steps;

import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.Serializable;

import ernestoyaquello.com.verticalstepperform.Step;
import verticalstepperform.ernestoyaquello.com.verticalstepperform.R;

public class AlarmTimeStep extends Step<AlarmTimeStep.TimeHolder> {

    private static final int DEFAULT_HOURS = 8;
    private static final int DEFAULT_MINUTES = 30;

    private TextView alarmTimeTextView;
    private TimePickerDialog alarmTimePicker;

    private int alarmTimeHour;
    private int alarmTimeMinutes;

    public AlarmTimeStep(String title) {
        this(title, "");
    }

    public AlarmTimeStep(String title, String subtitle) {
        super(title, subtitle);

        alarmTimeHour = DEFAULT_HOURS;
        alarmTimeMinutes = DEFAULT_MINUTES;
    }

    @NonNull
    @Override
    protected View createStepContentLayout() {

        // We create this step view by inflating an XML layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View timeStepContent = inflater.inflate(R.layout.step_time_layout, null, false);
        alarmTimeTextView = timeStepContent.findViewById(R.id.time);
        setupAlarmTime();

        return timeStepContent;
    }

    private void setupAlarmTime() {
        if (alarmTimePicker == null) {
            alarmTimePicker = new TimePickerDialog(getContext(),
                    (view, hourOfDay, minute) -> {
                        alarmTimeHour = hourOfDay;
                        alarmTimeMinutes = minute;

                        updatedAlarmTimeText();
                    }, alarmTimeHour, alarmTimeMinutes, true);
        } else {
            alarmTimePicker.updateTime(alarmTimeHour, alarmTimeMinutes);
        }

        if (alarmTimeTextView != null) {
            alarmTimeTextView.setOnClickListener(v -> alarmTimePicker.show());
        }
    }

    @Override
    protected void onStepOpened(boolean animated) {
        // No need to do anything here
    }

    @Override
    protected void onStepClosed(boolean animated) {
        // No need to do anything here
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {
        // No need to do anything here
    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {
        // No need to do anything here
    }

    @Override
    public TimeHolder getStepData() {
        return new TimeHolder(alarmTimeHour, alarmTimeMinutes);
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        String hourString = ((alarmTimeHour > 9) ?
                String.valueOf(alarmTimeHour) : ("0" + alarmTimeHour));
        String minutesString = ((alarmTimeMinutes > 9) ?
                String.valueOf(alarmTimeMinutes) : ("0" + alarmTimeMinutes));
        return hourString + ":" + minutesString;
    }

    @Override
    protected void restoreStepData(TimeHolder data) {
        alarmTimeHour = data.hour;
        alarmTimeMinutes = data.minutes;

        alarmTimePicker.updateTime(alarmTimeHour, alarmTimeMinutes);
        updatedAlarmTimeText();
    }

    @Override
    protected IsDataValid isStepDataValid(TimeHolder stepData) {
        return new IsDataValid(true);
    }

    private void updatedAlarmTimeText() {
        alarmTimeTextView.setText(getStepDataAsHumanReadableString());
    }

    public static class TimeHolder implements Serializable {

        public int hour;
        public int minutes;

        public TimeHolder(int hour, int minutes) {
            this.hour = hour;
            this.minutes = minutes;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getMinutes() {
            return minutes;
        }

        public void setMinutes(int minutes) {
            this.minutes = minutes;
        }
    }
}

package verticalstepperform.ernestoyaquello.com.verticalstepperform;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import ernestoyaquello.com.verticalstepperform.Step;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;
import verticalstepperform.ernestoyaquello.com.verticalstepperform.databinding.FragmentNewAlarmBinding;
import verticalstepperform.ernestoyaquello.com.verticalstepperform.form.steps.AlarmDaysStep;
import verticalstepperform.ernestoyaquello.com.verticalstepperform.form.steps.AlarmDescriptionStep;
import verticalstepperform.ernestoyaquello.com.verticalstepperform.form.steps.AlarmNameStep;
import verticalstepperform.ernestoyaquello.com.verticalstepperform.form.steps.AlarmTimeStep;
import verticalstepperform.ernestoyaquello.com.verticalstepperform.models.Alarm;

public class NewAlarmFormFragment extends Fragment implements StepperFormListener, DialogInterface.OnClickListener {

    public static final String ALARM_DATA_SERIALIZED_KEY = "newAlarmData";

    private ProgressDialog progressDialog;
    private FragmentNewAlarmBinding binding;

    private AlarmNameStep nameStep;
    private AlarmDescriptionStep descriptionStep;
    private AlarmTimeStep timeStep;
    private AlarmDaysStep daysStep;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewAlarmBinding.inflate(inflater, container, false);

        String[] stepTitles = getResources().getStringArray(R.array.steps_titles);
        //String[] stepSubtitles = getResources().getStringArray(R.array.steps_subtitles);

        nameStep = new AlarmNameStep(stepTitles[0]);//, stepSubtitles[0]);
        descriptionStep = new AlarmDescriptionStep(stepTitles[1]);//, stepSubtitles[1]);
        timeStep = new AlarmTimeStep(stepTitles[2]);//, stepSubtitles[2]);
        daysStep = new AlarmDaysStep(stepTitles[3]);//, stepSubtitles[3]);

        binding.stepperForm
                .setup(this, nameStep, descriptionStep, timeStep, daysStep)
                .init();

        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Intercept back button
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBackIfPossible();
            }
        });
    }

    @Override
    public void onCompletedForm() {
        final Thread dataSavingThread = saveData();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.form_sending_data_message));
        progressDialog.setOnCancelListener(dialogInterface -> {
            try {
                dataSavingThread.interrupt();
            } catch (RuntimeException e) {
                // No need to do anything here
            } finally {
                binding.stepperForm.cancelFormCompletionOrCancellationAttempt();
            }
        });
    }

    @Override
    public void onCancelledForm() {
        showCloseConfirmationDialog();
    }

    @Override
    public void onStepAdded(int index, Step<?> addedStep) {
        // Nothing to do here
    }

    @Override
    public void onStepRemoved(int index) {
        // Nothing to do here
    }

    private Thread saveData() {
        // Fake data saving effect
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                sendAlarmDataBack();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }

    private void sendAlarmDataBack() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                Alarm alarm = new Alarm(
                        nameStep.getStepData(),
                        descriptionStep.getStepData(),
                        timeStep.getStepData().hour,
                        timeStep.getStepData().minutes,
                        daysStep.getStepData());
                goBack(alarm);
            });
        }
    }

    private void goBackIfPossible() {
        if(binding.stepperForm.isAnyStepCompleted()) {
            showCloseConfirmationDialog();
        } else {
            goBack(null);
        }
    }

    private void showCloseConfirmationDialog() {
        new DiscardAlarmConfirmationFragment(this).show(getParentFragmentManager(), null);
    }

    private void dismissDialogIfNecessary() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            goBackIfPossible();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {

            // "Discard" button of the Discard Alarm dialog
            case DialogInterface.BUTTON_POSITIVE:
                goBack(null);
                break;

            // "Cancel" button of the Discard Alarm dialog
            case DialogInterface.BUTTON_NEGATIVE:
                binding.stepperForm.cancelFormCompletionOrCancellationAttempt();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        dismissDialogIfNecessary();
    }

    @Override
    public void onStop() {
        super.onStop();

        dismissDialogIfNecessary();
    }

    private void goBack(Alarm alarm) {
        NavController navController = NavHostFragment.findNavController(this);
        String alarmSerialized = alarm != null ? alarm.serialize() : "";
        navController.getPreviousBackStackEntry().getSavedStateHandle().set(ALARM_DATA_SERIALIZED_KEY, alarmSerialized);
        navController.navigateUp();
    }

    public static class DiscardAlarmConfirmationFragment extends DialogFragment {
        private final DialogInterface.OnClickListener onDialogButtonClicked;

        public DiscardAlarmConfirmationFragment(DialogInterface.OnClickListener onDialogButtonClicked) {
            this.onDialogButtonClicked = onDialogButtonClicked;
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Activity activity = getActivity();
            if (activity == null) {
                throw new IllegalStateException("Fragment " + this + " not attached to an activity.");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.form_discard_question)
                    .setMessage(R.string.form_info_will_be_lost)
                    .setPositiveButton(R.string.form_discard, onDialogButtonClicked)
                    .setNegativeButton(R.string.form_discard_cancel, onDialogButtonClicked)
                    .setCancelable(false);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);

            return dialog;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

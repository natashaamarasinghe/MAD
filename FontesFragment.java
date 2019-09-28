package com.mygym.fonte;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mygym.BtnClickListener;

import com.mygym.DAO.Cardio;
import com.mygym.DAO.DAOCardio;
import com.mygym.DAO.DAOFonte;
import com.mygym.DAO.DAOMachine;
import com.mygym.DAO.DAORecord;
import com.mygym.DAO.DAOStatic;
import com.mygym.DAO.Fonte;
import com.mygym.DAO.IRecord;
import com.mygym.DAO.Machine;
import com.mygym.DAO.Profile;
import com.mygym.DAO.StaticExercise;
import com.mygym.DAO.Weight;
import com.mygym.DatePickerDialogFragment;
import com.mygym.MainActivity;
import com.mygym.R;
import com.mygym.TimePickerDialogFragment;
import com.mygym.machines.ExerciseDetailsPager;
import com.mygym.machines.MachineArrayFullAdapter;
import com.mygym.machines.MachineCursorAdapter;
import com.mygym.utils.DateConverter;
import com.mygym.utils.ExpandedListView;
import com.mygym.utils.ImageUtil;
import com.mygym.utils.UnitConverter;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FontesFragment extends Fragment {
    MainActivity mActivity = null;
    Profile mProfile = null;
    EditText dateEdit = null;
    AutoCompleteTextView machineEdit = null;
    MachineArrayFullAdapter machineEditAdapter = null;
    EditText serieEdit = null;
    EditText repetitionEdit = null;
    EditText poidsEdit = null;
    LinearLayout detailsLayout = null;
    Button addButton = null;
    ExpandedListView recordList = null;
    String[] machineListArray = null;
    ImageButton machineListButton = null;
    Spinner unitSpinner = null;
    ImageButton detailsExpandArrow = null;
    EditText restTimeEdit = null;
    CheckBox restTimeCheck = null;
    DatePickerDialogFragment mDateFrag = null;
    TimePickerDialogFragment mDurationFrag = null;
    CircularImageView machineImage = null;
    TextView minText = null;
    TextView maxText = null;
    int lTableColor = 1;
    AlertDialog machineListDialog;
    LinearLayout minMaxLayout = null;
    // Selection part
    LinearLayout exerciseTypeSelectorLayout = null;
    TextView bodybuildingSelector = null;
    TextView cardioSelector = null;
    TextView staticExerciseSelector = null;
    int selectedType = DAOMachine.TYPE_FONTE;
    // Cardio Part
    LinearLayout bodyBuildingLayout = null;
    LinearLayout cardioLayout = null;
    LinearLayout restTimeLayout = null;
    EditText distanceEdit = null;
    EditText durationEdit = null;
    EditText secondsEdit = null;

    CardView serieCardView = null;
    CardView repetitionCardView = null;
    CardView secondsCardView = null;
    CardView weightCardView = null;
    CardView distanceCardView = null;
    CardView durationCardView = null;


    public MyTimePickerDialog.OnTimeSetListener timeSet = (view, hourOfDay, minute, second) -> {
        // Do something with the time chosen by the user
        String strMinute = "00";
        String strHour = "00";
        String strSecond = "00";

        if (minute < 10) strMinute = "0" + Integer.toString(minute);
        else strMinute = Integer.toString(minute);
        if (hourOfDay < 10) strHour = "0" + Integer.toString(hourOfDay);
        else strHour = Integer.toString(hourOfDay);
        if (second < 10) strSecond = "0" + Integer.toString(second);
        else strSecond = Integer.toString(second);

        String date = strHour + ":" + strMinute + ":" + strSecond;
        durationEdit.setText(date);
        hideKeyboard(durationEdit);
    };
    private DAOFonte mDbBodyBuilding = null;
    private DAOCardio mDbCardio = null;
    private DAOStatic mDbStatic = null;
    private DAORecord mDb = null;
    private DAOMachine mDbMachine = null;
    private OnClickListener collapseDetailsClick = v -> {
        detailsLayout.setVisibility(detailsLayout.isShown() ? View.GONE : View.VISIBLE);
        detailsExpandArrow.setImageResource(detailsLayout.isShown() ? R.drawable.baseline_keyboard_arrow_up_black_36 : R.drawable.baseline_keyboard_arrow_down_black_36);
        saveSharedParams();
    };
    private OnClickListener clickExerciseTypeSelector = v -> {
        switch (v.getId()) {
            case R.id.staticSelection:
                changeExerciseTypeUI(DAOMachine.TYPE_STATIC, true);
                break;
            case R.id.cardioSelection:
                changeExerciseTypeUI(DAOMachine.TYPE_CARDIO, true);
                break;
            case R.id.bodyBuildingSelection:
            default:
                changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
                break;
        }
    };
    private View.OnKeyListener checkExerciseExists = (v, keyCode, event) -> {
        Machine lMach = mDbMachine.getMachine(machineEdit.getText().toString());
        if (lMach == null) {
            showExerciseTypeSelector(true);
        } else {
            changeExerciseTypeUI(lMach.getType(), false);
        }
        return false;
    };
    private OnFocusChangeListener restTimeEditChange = (v, hasFocus) -> {
        if (!hasFocus) {
            saveSharedParams();
        }
    };
    private CompoundButton.OnCheckedChangeListener restTimeCheckChange = (buttonView, isChecked) -> saveSharedParams();
    private BtnClickListener itemClickDeleteRecord = this::showDeleteDialog;
    private BtnClickListener itemClickCopyRecord = id -> {
        IRecord r = mDb.getRecord(id);
        if (r != null) {
            // Copy values above
            setCurrentMachine(r.getExercise());
            if (r.getType() == DAOMachine.TYPE_FONTE) {
                Fonte f = (Fonte) r;
                repetitionEdit.setText(String.format("%d", f.getRepetition()));
                serieEdit.setText(String.format("%d", f.getSerie()));
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                poidsEdit.setText(numberFormat.format(f.getPoids()));
            } else if (r.getType() == DAOMachine.TYPE_STATIC) {
                StaticExercise f = (StaticExercise) r;
                secondsEdit.setText(String.format("%d", f.getSecond()));
                serieEdit.setText(String.format("%d", f.getSerie()));
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                poidsEdit.setText(numberFormat.format(f.getPoids()));
            }else if (r.getType() == DAOMachine.TYPE_CARDIO) {
                Cardio c = (Cardio) r;
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                distanceEdit.setText(numberFormat.format(c.getDistance()));
                durationEdit.setText(DateConverter.durationToHoursMinutesSecondsStr(c.getDuration()));
            }
            KToast.infoToast(getMainActivity(), getString(R.string.recordcopied), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    };
    private OnClickListener clickAddButton = v -> {
        //Check that the information is complete
        if (machineEdit.getText().toString().isEmpty()) {
            KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            return;
        }

        Date date;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = dateFormat.parse(dateEdit.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }

        int exerciseType = DAOMachine.TYPE_FONTE;
        Machine lMachine = mDbMachine.getMachine(machineEdit.getText().toString());
        if (lMachine == null) {
            exerciseType = selectedType;
        } else {
            exerciseType = lMachine.getType();
        }

        if (exerciseType == DAOMachine.TYPE_FONTE) {
            // Check that the information is complete
            if (serieEdit.getText().toString().isEmpty() ||
                repetitionEdit.getText().toString().isEmpty() ||
                poidsEdit.getText().toString().isEmpty()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            /* Weight conversion*/
            float tmpPoids = Float.parseFloat(poidsEdit.getText().toString().replaceAll(",", "."));
            int unitPoids = UnitConverter.UNIT_KG; // Kg
            if (unitSpinner.getSelectedItem().toString().equals(getView().getContext().getString(R.string.LbsUnitLabel))) {
                tmpPoids = UnitConverter.LbstoKg(tmpPoids); // Always convert to KG
                unitPoids = UnitConverter.UNIT_LBS; // LBS
            }

            mDbBodyBuilding.addBodyBuildingRecord(date,
                machineEdit.getText().toString(),
                Integer.parseInt(serieEdit.getText().toString()),
                Integer.parseInt(repetitionEdit.getText().toString()),
                tmpPoids, // Always save in KG
                getProfil(),
                unitPoids, // Store Unit for future display
                "", //Notes
                DateConverter.currentTime()
            );

            float iTotalWeightSession = mDbBodyBuilding.getTotalWeightSession(date);
            float iTotalWeight = mDbBodyBuilding.getTotalWeightMachine(date, machineEdit.getText().toString());
            int iNbSeries = mDbBodyBuilding.getNbSeries(date, machineEdit.getText().toString());

            //--Launch Rest Dialog
            boolean bLaunchRest = restTimeCheck.isChecked();
            int restTime = 60;
            try {
                restTime = Integer.valueOf(restTimeEdit.getText().toString());
            } catch (NumberFormatException e) {
                restTime = 60;
                restTimeEdit.setText("60");
            }

            // Launch Countdown

        } else if (exerciseType == DAOMachine.TYPE_STATIC) {
            // Check that the information is complete
            if (serieEdit.getText().toString().isEmpty() ||
                secondsEdit.getText().toString().isEmpty() ||
                poidsEdit.getText().toString().isEmpty()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            /* Weight conversion */
            float tmpPoids = Float.parseFloat(poidsEdit.getText().toString().replaceAll(",", "."));
            int unitPoids = UnitConverter.UNIT_KG; // Kg
            if (unitSpinner.getSelectedItem().toString().equals(getView().getContext().getString(R.string.LbsUnitLabel))) {
                tmpPoids = UnitConverter.LbstoKg(tmpPoids); // Always convert to KG
                unitPoids = UnitConverter.UNIT_LBS; // LBS
            }

            mDbStatic.addStaticRecord(date,
                machineEdit.getText().toString(),
                Integer.parseInt(serieEdit.getText().toString()),
                Integer.parseInt(secondsEdit.getText().toString()),
                tmpPoids, // Always save in KG
                getProfil(),
                unitPoids, // Store Unit for future display
                "", //Notes
                DateConverter.currentTime()
            );

            float iTotalWeightSession = mDbStatic.getTotalWeightSession(date);
            float iTotalWeight = mDbStatic.getTotalWeightMachine(date, machineEdit.getText().toString());
            int iNbSeries = mDbStatic.getNbSeries(date, machineEdit.getText().toString());

            //--Launch Rest Dialog
            boolean bLaunchRest = restTimeCheck.isChecked();
            int restTime = 60;
            try {
                restTime = Integer.valueOf(restTimeEdit.getText().toString());
            } catch (NumberFormatException e) {
                restTime = 60;
                restTimeEdit.setText("60");
            }


        } else if (exerciseType == DAOMachine.TYPE_CARDIO) {
            // Check that the information is complete
            if (durationEdit.getText().toString().isEmpty() && // Only one is mandatory
                distanceEdit.getText().toString().isEmpty()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            long duration;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date tmpDate = dateFormat.parse(durationEdit.getText().toString());
                duration = tmpDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                duration = 0;
            }

            float distance;
            if (distanceEdit.getText().toString().isEmpty()) {
                distance = 0;
            } else {
                distance = Float.parseFloat(distanceEdit.getText().toString());
            }

            mDbCardio.addCardioRecord(date,
                DateConverter.currentTime(),
                machineEdit.getText().toString(),
                distance,
                duration,
                getProfil());

            // No Countdown for Cardio
        }

        getActivity().findViewById(R.id.drawer_layout).requestFocus();
        hideKeyboard(v);

        lTableColor = (lTableColor + 1) % 2; // Change the color with each addition of data

        refreshData();

        /* Reinitialisation des machines */
        // TODO Avoid recreating each time adapting it. We can always use the same.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getView().getContext(),
            android.R.layout.simple_dropdown_item_1line, mDb.getAllMachines(getProfil()));
        machineEdit.setAdapter(adapter);

        //Add the moment of the last addition in the Add button
        addButton.setText(getView().getContext().getString(R.string.AddLabel) + "\n(" + DateConverter.currentTime() + ")");

        mDbCardio.closeCursor();
        mDbBodyBuilding.closeCursor();
        mDbStatic.closeCursor();
        mDb.closeCursor();
    };
    private OnClickListener onClickMachineListWithIcons = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Cursor c;
            Cursor oldCursor;

            // In case the dialog is already open
            if (machineListDialog != null && machineListDialog.isShowing()) {
                return;
            }

            ListView machineList = new ListView(v.getContext());

            // Version avec table Machine
            c = mDbMachine.getAllMachines();

            if (c == null || c.getCount() == 0) {
                //Toast.makeText(getActivity(), R.string.createExerciseFirst, Toast.LENGTH_SHORT).show();
                KToast.warningToast(getActivity(), getResources().getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                machineList.setAdapter(null);
            } else {
                if (machineList.getAdapter() == null) {
                    MachineCursorAdapter mTableAdapter = new MachineCursorAdapter(v.getContext(), c, 0, mDbMachine);
                    //MachineArrayFullAdapter lAdapter = new MachineArrayFullAdapter(v.getContext(),records);
                    machineList.setAdapter(mTableAdapter);
                } else {
                    MachineCursorAdapter mTableAdapter = ((MachineCursorAdapter) machineList.getAdapter());
                    oldCursor = mTableAdapter.swapCursor(c);
                    if (oldCursor != null) oldCursor.close();
                }

                machineList.setOnItemClickListener((parent, view, position, id) -> {
                    TextView textView = view.findViewById(R.id.LIST_MACHINE_ID);
                    long machineID = Long.parseLong(textView.getText().toString());
                    DAOMachine lMachineDb = new DAOMachine(getContext());
                    Machine lMachine = lMachineDb.getMachine(machineID);

                    setCurrentMachine(lMachine.getName());

                    getMainActivity().findViewById(R.id.drawer_layout).requestFocus();

                    hideKeyboard(getMainActivity().findViewById(R.id.drawer_layout));

                    if (machineListDialog.isShowing()) {
                        machineListDialog.dismiss();
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.selectMachineDialogLabel);
                builder.setView(machineList);
                machineListDialog = builder.create();
                machineListDialog.show();
            }
        }
    };

    private OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {
        showRecordListMenu(id);
        return true;
    };

    private OnItemClickListener onItemClickFilterList = (parent, view, position, id) -> setCurrentMachine(machineEdit.getText().toString());
    private DatePickerDialog.OnDateSetListener dateSet = (view, year, month, day) -> {
        dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
        hideKeyboard(dateEdit);
    };
    private OnClickListener clickDateEdit = v -> {
        switch (v.getId()) {
            case R.id.editDate:
                showDatePickerFragment();
                break;
            case R.id.editDuration:
                showTimePicker();
                break;
            case R.id.editMachine:
                //InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.showSoftInput(machineEdit, InputMethodManager.SHOW_IMPLICIT);
                //machineEdit.setText("");
                //machineEdit.set.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                break;
        }
    };
    private OnFocusChangeListener touchRazEdit = (v, hasFocus) -> {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.editDate:
                    showDatePickerFragment();
                    break;
                case R.id.editSerie:
                    serieEdit.setText("");
                    break;
                case R.id.editRepetition:
                    repetitionEdit.setText("");
                    break;
                case R.id.editSeconds:
                    secondsEdit.setText("");
                    break;
                case R.id.editPoids:
                    poidsEdit.setText("");
                    break;
                case R.id.editDuration:
                    showTimePicker();
                    break;
                case R.id.editDistance:
                    distanceEdit.setText("");
                    break;
                case R.id.editMachine:
                    machineEdit.setText("");
                    machineImage.setImageResource(R.drawable.ic_machine);
                    minMaxLayout.setVisibility(View.GONE);
                    showExerciseTypeSelector(true);
                    break;
            }
            v.post(() -> {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            });
        } else if (!hasFocus) {
            switch (v.getId()) {
                case R.id.editMachine:
                    // If a creation of a new machine is not ongoing.
                    if (exerciseTypeSelectorLayout.getVisibility() == View.GONE)
                        setCurrentMachine(machineEdit.getText().toString());
                    break;
            }
        }
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static FontesFragment newInstance(String name, int id) {
        FontesFragment f = new FontesFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    private void showRecordListMenu(final long id) {
        // Get the cursor, positioned to the corresponding row in the result set
        //Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        String[] profilListArray = new String[3];
        profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);
        profilListArray[1] = getActivity().getResources().getString(R.string.EditLabel);
        profilListArray[2] = getActivity().getResources().getString(R.string.ShareLabel);

        AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(getView().getContext());
        itemActionbuilder.setTitle("").setItems(profilListArray, (dialog, which) -> {
            ListView lv = ((AlertDialog) dialog).getListView();

            switch (which) {
                // Delete
                case 0:
                    showDeleteDialog(id);
                    break;
                // Edit
                case 1:
                    Toast.makeText(getActivity(), R.string.edit_soon_available, Toast.LENGTH_SHORT).show();
                    break;
                // Share
                case 2:
                    //Toast.makeText(getActivity(), "Share soon available", Toast.LENGTH_SHORT).show();
                    IRecord r = mDb.getRecord(id);
                    String text = "";
                    if (r.getType() == DAOMachine.TYPE_FONTE ||r.getType() == DAOMachine.TYPE_STATIC  ) {
                        Fonte fonte = (Fonte) r;
                        // Build text
                        text = getView().getContext().getResources().getText(R.string.ShareTextDefault).toString();
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamWeight), String.valueOf(fonte.getPoids()));
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamMachine), fonte.getExercise());
                    } else {
                        Cardio cardio = (Cardio) r;
                        // Build text
                        text = "I have done __METER__ in __TIME__ on __MACHINE__.";
                        text = text.replace("__METER__", String.valueOf(cardio.getDistance()));
                        text = text.replace("__TIME__", String.valueOf(cardio.getDuration()));
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamMachine), cardio.getExercise());
                    }
                    shareRecord(text);
                    break;
                default:
            }
        });
        itemActionbuilder.show();
    }

    private void showDeleteDialog(final long idToDelete) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getResources().getText(R.string.areyousure).toString())
            .setCancelText(getResources().getText(R.string.global_no).toString())
            .setConfirmText(getResources().getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener(sDialog -> {
                mDb.deleteRecord(idToDelete);

                updateRecordTable(machineEdit.getText().toString());

                //Toast.makeText(getContext(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT).show();
                // Info
                KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                sDialog.dismissWithAnimation();
            })
            .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_fontes, container, false);

        dateEdit = view.findViewById(R.id.editDate);
        machineEdit = view.findViewById(R.id.editMachine);
        serieEdit = view.findViewById(R.id.editSerie);
        repetitionEdit = view.findViewById(R.id.editRepetition);
        poidsEdit = view.findViewById(R.id.editPoids);
        recordList = view.findViewById(R.id.listRecord);
        machineListButton = view.findViewById(R.id.buttonListMachine);
        addButton = view.findViewById(R.id.addperff);
        unitSpinner = view.findViewById(R.id.spinnerUnit);
        detailsLayout = view.findViewById(R.id.notesLayout);
        detailsExpandArrow = view.findViewById(R.id.buttonExpandArrow);
        restTimeEdit = view.findViewById(R.id.editRestTime);
        restTimeCheck = view.findViewById(R.id.restTimecheckBox);
        machineImage = view.findViewById(R.id.imageMachine);
        minText = view.findViewById(R.id.minText);
        maxText = view.findViewById(R.id.maxText);
        bodyBuildingLayout = view.findViewById(R.id.bodybuildingLayout);
        cardioLayout = view.findViewById(R.id.cardioLayout);
        bodybuildingSelector = view.findViewById(R.id.bodyBuildingSelection);
        cardioSelector = view.findViewById(R.id.cardioSelection);
        staticExerciseSelector = view.findViewById(R.id.staticSelection);
        exerciseTypeSelectorLayout = view.findViewById(R.id.exerciseTypeSelectionLayout);
        minMaxLayout = view.findViewById(R.id.minmaxLayout);
        restTimeLayout = view.findViewById(R.id.restTimeLayout);
        durationEdit = view.findViewById(R.id.editDuration);
        distanceEdit = view.findViewById(R.id.editDistance);
        secondsEdit = view.findViewById(R.id.editSeconds);

        serieCardView = view.findViewById(R.id.cardviewSerie);
        repetitionCardView = view.findViewById(R.id.cardviewRepetition);
        secondsCardView = view.findViewById(R.id.cardviewSeconds);
        weightCardView = view.findViewById(R.id.cardviewWeight);
        distanceCardView = view.findViewById(R.id.cardviewDistance);
        durationCardView = view.findViewById(R.id.cardviewDuration);

        /* Button initialization */
        addButton.setOnClickListener(clickAddButton);
        machineListButton.setOnClickListener(onClickMachineListWithIcons); //onClickMachineList

        dateEdit.setOnClickListener(clickDateEdit);
        dateEdit.setOnFocusChangeListener(touchRazEdit);
        serieEdit.setOnFocusChangeListener(touchRazEdit);
        repetitionEdit.setOnFocusChangeListener(touchRazEdit);
        poidsEdit.setOnFocusChangeListener(touchRazEdit);
        distanceEdit.setOnFocusChangeListener(touchRazEdit);
        durationEdit.setOnClickListener(clickDateEdit);
        durationEdit.setOnFocusChangeListener(touchRazEdit);
        secondsEdit.setOnFocusChangeListener(touchRazEdit);
        machineEdit.setOnKeyListener(checkExerciseExists);
        machineEdit.setOnFocusChangeListener(touchRazEdit);
        machineEdit.setOnItemClickListener(onItemClickFilterList);
        recordList.setOnItemLongClickListener(itemlongclickDeleteRecord);
        detailsExpandArrow.setOnClickListener(collapseDetailsClick);
        restTimeEdit.setOnFocusChangeListener(restTimeEditChange);
        restTimeCheck.setOnCheckedChangeListener(restTimeCheckChange); //.setOnFocusChangeListener(restTimeEditChange);

        bodybuildingSelector.setOnClickListener(clickExerciseTypeSelector);
        cardioSelector.setOnClickListener(clickExerciseTypeSelector);
        staticExerciseSelector.setOnClickListener(clickExerciseTypeSelector);

        restoreSharedParams();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int defaultUnit = 0;
        try {
            defaultUnit = Integer.valueOf(SP.getString("defaultUnit", "0"));
        } catch (NumberFormatException e) {
            defaultUnit = 0;
        }
        unitSpinner.setSelection(defaultUnit);

        // Initialization of the database
        mDbBodyBuilding = new DAOFonte(getContext());
        mDbCardio = new DAOCardio(getContext());
        mDbStatic = new DAOStatic(getContext());
        mDb = new DAORecord(getContext());

        mDbMachine = new DAOMachine(getContext());
        dateEdit.setText(DateConverter.currentDate());
        selectedType = DAOMachine.TYPE_FONTE;

        machineImage.setOnClickListener(v -> {
            Machine m = mDbMachine.getMachine(machineEdit.getText().toString());
            if (m != null) {
                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(m.getId(), ((MainActivity) getActivity()).getCurrentProfil().getId());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mActivity = (MainActivity) this.getActivity();
        refreshData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    public String getName() {
        return getArguments().getString("name");
    }

    public int getFragmentId() {
        return getArguments().getInt("id", 0);
    }

    public MainActivity getMainActivity() {
        return this.mActivity;
    }

    private void showDatePickerFragment() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
            mDateFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog");
        } else {
            if (!mDateFrag.isVisible())
                mDateFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog");
        }
    }

    private void showTimePicker() {
        String tx =  durationEdit.getText().toString();
        int hour;
        try {
            hour = Integer.valueOf(tx.substring(0, 2));
        } catch (NumberFormatException e) {
            hour=0;
        }
        int min;
        try {
            min = Integer.valueOf(tx.substring(3, 5));
         } catch (NumberFormatException e) {
        min=0;
        }
        int sec;
        try {
        sec = Integer.valueOf(tx.substring(6));
        } catch (NumberFormatException e) {
            sec=0;
        }

        if (mDurationFrag == null) {
            mDurationFrag = TimePickerDialogFragment.newInstance(timeSet, hour, min, sec);
            //mDurationFrag.setTime(hour, min, sec);
            mDurationFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_time");
        } else {
            if (!mDurationFrag.isVisible()) {
                Bundle bundle = new Bundle();
                bundle.putInt("HOUR", hour);
                bundle.putInt("MINUTE", min);
                bundle.putInt("SECOND", sec);
                mDurationFrag.setArguments(bundle);
                mDurationFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_time");
            }
        }
    }

    // Share your performances with friends
    public boolean shareRecord(String text) {
        AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(getView().getContext());

        newProfilBuilder.setTitle(getView().getContext().getResources().getText(R.string.ShareTitle));
        newProfilBuilder.setMessage(getView().getContext().getResources().getText(R.string.ShareInstruction));

        // Set an EditText view to get user input
        final EditText input = new EditText(getView().getContext());
        input.setText(text);
        newProfilBuilder.setView(input);

        newProfilBuilder.setPositiveButton(getView().getContext().getResources().getText(R.string.ShareText), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, value);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        newProfilBuilder.setNegativeButton(getView().getContext().getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        newProfilBuilder.show();

        return true;
    }

    public FontesFragment getFragment() {
        return this;
    }

    private Profile getProfil() {
        return mActivity.getCurrentProfil();
    }

    public String getMachine() {
        /*if (machineEdit == null)
            machineEdit = this.getView().findViewById(R.id.editMachine);*/
        return machineEdit.getText().toString();
    }

    public void setCurrentMachine(String machineStr) {
        if (machineStr.isEmpty()) {
            machineImage.setImageResource(R.drawable.ic_machine); // Default image
            showExerciseTypeSelector(true);
            minMaxLayout.setVisibility(View.GONE);
            return;
        }

        Machine lMachine = mDbMachine.getMachine(machineStr);
        if (lMachine == null) {
            machineEdit.setText("");
            machineImage.setImageResource(R.drawable.ic_machine); // Default image
            changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
            updateMinMax(null);
            return;
        }

        // Update EditView
        machineEdit.setText(lMachine.getName());
        // Update exercise Image
        machineImage.setImageResource(R.drawable.ic_machine); // Default image
        ImageUtil imgUtil = new ImageUtil();
        ImageUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.getPicture())); // Overwrite image is there is one

        // Update Table
        updateRecordTable(lMachine.getName());
        // Update display type
        changeExerciseTypeUI(lMachine.getType(), false);

        // Depending on the machine type :
        // Update Min Max
        updateMinMax(lMachine);
        // Update last values
        updateLastRecord(lMachine);
    }

    private void updateMinMax(Machine m) {
        String unitStr = "";
        float weight = 0;
        if (getProfil() != null && m != null) {
            if (m.getType() == DAOMachine.TYPE_FONTE || m.getType() == DAOMachine.TYPE_STATIC) {
                Weight minValue = mDbBodyBuilding.getMin(getProfil(), m);
                if (minValue != null) {
                    minMaxLayout.setVisibility(View.VISIBLE);
                    if (minValue.getStoredUnit() == UnitConverter.UNIT_LBS) {
                        weight = UnitConverter.KgtoLbs(minValue.getStoredWeight());
                        unitStr = getContext().getString(R.string.LbsUnitLabel);
                    } else {
                        weight = minValue.getStoredWeight();
                        unitStr = getContext().getString(R.string.KgUnitLabel);
                    }
                    DecimalFormat numberFormat = new DecimalFormat("#.##");
                    minText.setText(numberFormat.format(weight) + " " + unitStr);

                    Weight maxValue = mDbBodyBuilding.getMax(getProfil(), m);
                    if (maxValue.getStoredUnit() == UnitConverter.UNIT_LBS) {
                        weight = UnitConverter.KgtoLbs(maxValue.getStoredWeight());
                        unitStr = getContext().getString(R.string.LbsUnitLabel);
                    } else {
                        weight = maxValue.getStoredWeight();
                        unitStr = getContext().getString(R.string.KgUnitLabel);
                    }
                    maxText.setText(numberFormat.format(weight) + " " + unitStr);
                } else {
                    minText.setText("-");
                    maxText.setText("-");
                    minMaxLayout.setVisibility(View.GONE);
                }
            } else if (m.getType() == DAOMachine.TYPE_CARDIO) {
                minMaxLayout.setVisibility(View.GONE);
            }
        } else {
            minText.setText("-");
            maxText.setText("-");
            minMaxLayout.setVisibility(View.GONE);
        }
    }

    private void updateLastRecord(Machine m) {
        IRecord lLastRecord = mDb.getLastExerciseRecord(m.getId(), getProfil());
        // Default Values
        serieEdit.setText("1");
        repetitionEdit.setText("10");
        secondsEdit.setText("60");
        poidsEdit.setText("50");
        distanceEdit.setText("1");
        durationEdit.setText("00:10:00");
        if (lLastRecord == null) {
            // Set default values or nothing.
        } else if (lLastRecord.getType() == DAOMachine.TYPE_FONTE) {
            Fonte lLastBodyBuildingRecord = (Fonte) lLastRecord;
            serieEdit.setText(String.valueOf(lLastBodyBuildingRecord.getSerie()));
            repetitionEdit.setText(String.valueOf(lLastBodyBuildingRecord.getRepetition()));
            unitSpinner.setSelection(lLastBodyBuildingRecord.getUnit());
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            if (lLastBodyBuildingRecord.getUnit() == UnitConverter.UNIT_LBS)
                poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastBodyBuildingRecord.getPoids())));
            else
                poidsEdit.setText(numberFormat.format(lLastBodyBuildingRecord.getPoids()));
        } else if (lLastRecord.getType() == DAOMachine.TYPE_CARDIO) {
            Cardio lLastCardioRecord = (Cardio) lLastRecord;
            distanceEdit.setText(String.valueOf(lLastCardioRecord.getDistance()));
            durationEdit.setText(DateConverter.durationToHoursMinutesSecondsStr(lLastCardioRecord.getDuration()));
        } else if (lLastRecord.getType() == DAOMachine.TYPE_STATIC) {
            StaticExercise lLastStaticRecord = (StaticExercise) lLastRecord;
            serieEdit.setText(String.valueOf(lLastStaticRecord.getSerie()));
            secondsEdit.setText(String.valueOf(lLastStaticRecord.getSecond()));
            unitSpinner.setSelection(lLastStaticRecord.getUnit());
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            if (lLastStaticRecord.getUnit() == UnitConverter.UNIT_LBS)
                poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastStaticRecord.getPoids())));
            else
                poidsEdit.setText(numberFormat.format(lLastStaticRecord.getPoids()));
        }
    }

    /*  */
    private void updateRecordTable(String pMachine) {
        // Informs the activity of the current machine
        this.getMainActivity().setCurrentMachine(pMachine);
        if (getView()==null) return;
        getView().post(() -> {

            Cursor c = null;
            Cursor oldCursor = null;

            IRecord r = mDb.getLastRecord(getProfil());

            // Recover the values
            //if (pMachine == null || pMachine.isEmpty()) {
            if (r != null)
                c = mDb.getTop3DatesRecords(getProfil());
            else
                return;

            if (c == null || c.getCount() == 0) {
                recordList.setAdapter(null);
            } else {
                if (recordList.getAdapter() == null) {
                    RecordCursorAdapter mTableAdapter = new RecordCursorAdapter(getContext(), c, 0, itemClickDeleteRecord, itemClickCopyRecord);
                    mTableAdapter.setFirstColorOdd(lTableColor);
                    recordList.setAdapter(mTableAdapter);
                } else {
                    RecordCursorAdapter mTableAdapter = ((RecordCursorAdapter) recordList.getAdapter());
                    mTableAdapter.setFirstColorOdd(lTableColor);
                    oldCursor = mTableAdapter.swapCursor(c);
                    if (oldCursor != null) oldCursor.close();
                }
            }
        });
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {
                mDb.setProfile(getProfil());

                ArrayList<Machine> machineListArray;
                // Version with machine table
                machineListArray = mDbMachine.getAllMachinesArray();

                /* Init machines list*/
                machineEditAdapter = new MachineArrayFullAdapter(getContext(), machineListArray);
                machineEdit.setAdapter(machineEditAdapter);

                // If profile has changed
                mProfile = getProfil();

                if (machineEdit.getText().toString().isEmpty()) {
                    IRecord lLastRecord = mDb.getLastRecord(getProfil());
                    if (lLastRecord != null) {
                        // Last recorded exercise
                        setCurrentMachine(lLastRecord.getExercise());
                    } else {
                        // Default Values
                        machineEdit.setText("");
                        serieEdit.setText("1");
                        repetitionEdit.setText("10");
                        secondsEdit.setText("60");
                        poidsEdit.setText("50");
                        distanceEdit.setText("1");
                        durationEdit.setText("00:10:00");
                        setCurrentMachine("");
                        changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
                    }
                } else { // Restore on fragment restore.
                    setCurrentMachine(machineEdit.getText().toString());
                }

                // Set Initial text
                dateEdit.setText(DateConverter.currentDate());

                // Set Table
                updateRecordTable(machineEdit.getText().toString());
            }
        }
    }

    private void showExerciseTypeSelector(boolean displaySelector) {
        if (displaySelector) exerciseTypeSelectorLayout.setVisibility(View.VISIBLE);
        else exerciseTypeSelectorLayout.setVisibility(View.GONE);
    }

    private void changeExerciseTypeUI(int pType, boolean displaySelector) {
        showExerciseTypeSelector(displaySelector);
        switch (pType) {
            case DAOMachine.TYPE_CARDIO:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.background_odd));
                bodybuildingSelector.setBackgroundColor(getResources().getColor(R.color.background));
                staticExerciseSelector.setBackgroundColor(getResources().getColor(R.color.background));
                serieCardView.setVisibility(View.GONE);
                repetitionCardView.setVisibility(View.GONE);
                weightCardView.setVisibility(View.GONE);
                secondsCardView.setVisibility(View.GONE);
                restTimeLayout.setVisibility(View.GONE);
                distanceCardView.setVisibility(View.VISIBLE);
                durationCardView.setVisibility(View.VISIBLE);
                selectedType = DAOMachine.TYPE_CARDIO;
                break;
            case DAOMachine.TYPE_STATIC:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.background));
                bodybuildingSelector.setBackgroundColor(getResources().getColor(R.color.background));
                staticExerciseSelector.setBackgroundColor(getResources().getColor(R.color.background_odd));
                serieCardView.setVisibility(View.VISIBLE);
                repetitionCardView.setVisibility(View.GONE);
                secondsCardView.setVisibility(View.VISIBLE);
                weightCardView.setVisibility(View.VISIBLE);
                restTimeLayout.setVisibility(View.VISIBLE);
                distanceCardView.setVisibility(View.GONE);
                durationCardView.setVisibility(View.GONE);
                selectedType = DAOMachine.TYPE_STATIC;
                break;
            case DAOMachine.TYPE_FONTE:
            default:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.background));
                bodybuildingSelector.setBackgroundColor(getResources().getColor(R.color.background_odd));
                staticExerciseSelector.setBackgroundColor(getResources().getColor(R.color.background));
                serieCardView.setVisibility(View.VISIBLE);
                repetitionCardView.setVisibility(View.VISIBLE);
                secondsCardView.setVisibility(View.GONE);
                weightCardView.setVisibility(View.VISIBLE);
                restTimeLayout.setVisibility(View.VISIBLE);
                distanceCardView.setVisibility(View.GONE);
                durationCardView.setVisibility(View.GONE);
                selectedType = DAOMachine.TYPE_FONTE;
        }
    }

    public void saveSharedParams() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("restTime", restTimeEdit.getText().toString());
        editor.putBoolean("restCheck", restTimeCheck.isChecked());
        editor.putBoolean("showDetails", this.detailsLayout.isShown());
        editor.apply();
    }

    public void restoreSharedParams() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        restTimeEdit.setText(sharedPref.getString("restTime", ""));
        restTimeCheck.setChecked(sharedPref.getBoolean("restCheck", true));

        if (sharedPref.getBoolean("showDetails", false)) {
            detailsLayout.setVisibility(View.VISIBLE);
        } else {
            detailsLayout.setVisibility(View.GONE);
        }
        detailsExpandArrow.setImageResource(sharedPref.getBoolean("showDetails", false) ? R.drawable.baseline_keyboard_arrow_up_black_36 : R.drawable.baseline_keyboard_arrow_down_black_36);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden)
            refreshData();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}

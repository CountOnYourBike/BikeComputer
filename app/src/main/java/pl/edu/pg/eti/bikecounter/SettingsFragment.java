package pl.edu.pg.eti.bikecounter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    Spinner mSpinner;
    MainActivity mainActivity;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        final List<String> systemList =
                Arrays.asList(getResources().getStringArray(R.array.scale_wheel_sizes));

        mSpinner = view.findViewById(R.id.spinner);
        Spinner systemSpinner = view.findViewById(R.id.systemSpinner);
        ArrayAdapter<CharSequence> scaleAdapter = ArrayAdapter.createFromResource(
                        mainActivity, R.array.scale_wheel_sizes, android.R.layout.simple_spinner_item);

        scaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        systemSpinner.setAdapter(scaleAdapter);
        String selectedSystem = mainActivity.mSharedPreferences
                .getString("WheelSizeSystem", scaleAdapter.getItem(2).toString());
        if(systemList.indexOf(selectedSystem) == -1)
            throw new IllegalArgumentException("Such wheel size system not found: " + selectedSystem);
        systemSpinner.setSelection(systemList.indexOf(selectedSystem));
        systemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.mEditor.putString("WheelSizeSystem", systemList.get(position));
                mainActivity.mEditor.apply();

                setSpinnerValues(systemList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void setSpinnerValues(final String wheelSizeSystem){
        final List<String> ValueList =
                Wheel.getValuesList(mainActivity.getApplicationContext(), wheelSizeSystem);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainActivity,
                android.R.layout.simple_spinner_item, ValueList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        String wheelSizeString =
                mainActivity.mSharedPreferences.getString("WheelSize", ValueList.get(0));
        if(ValueList.indexOf(wheelSizeString) == -1)
            wheelSizeString = ValueList.get(0);
        mSpinner.setSelection(ValueList.indexOf(wheelSizeString));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = ((TextView) view).getText().toString();

                MainActivity mainActivity = (MainActivity) getActivity();
                double circumference = Wheel.getCircValue(getContext(), wheelSizeSystem, selected);
                mainActivity.setWheelCirc(circumference);
                mainActivity.mEditor.putString("WheelSize", selected);
                mainActivity.mEditor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}

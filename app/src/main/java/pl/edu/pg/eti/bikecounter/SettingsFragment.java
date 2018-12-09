package pl.edu.pg.eti.bikecounter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    Spinner mSpinner;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }
    private final static String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        final List<String> SystemsList = Arrays.asList(getResources().getStringArray(R.array.scale_wheel_sizes));

        mSpinner = view.findViewById(R.id.spinner);
        Spinner scaleSpinner = view.findViewById(R.id.scaleSpinner);
        ArrayAdapter<CharSequence> scaleAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.scale_wheel_sizes, android.R.layout.simple_spinner_item );
        scaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        scaleSpinner.setAdapter(scaleAdapter);
        scaleSpinner.setSelection(SystemsList.indexOf(((MainActivity)getActivity()).mSharedPreferences.getString("WheelSizeScale",SystemsList.get(2))));
        scaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (SystemsList.get(position).equals(getResources().getString(R.string.ETRTO_system))){
                    ((MainActivity)getActivity()).mEditor.putString("WheelSizeScaleInt", getString(R.string.ETRTO_system));
                } else if (SystemsList.get(position).equals(getResources().getString(R.string.inch_system))){
                    ((MainActivity)getActivity()).mEditor.putString("WheelSizeScaleInt", getString(R.string.inch_system));
                } else if (SystemsList.get(position).equals(getResources().getString(R.string.circ_system))){
                    ((MainActivity)getActivity()).mEditor.putString("WheelSizeScaleInt", getString(R.string.circ_system));
                } else {
                    Toast.makeText(getContext(),"Not correct String value of wheel scale", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Not correct String value of wheel scale");
                }
                ((MainActivity)getActivity()).mEditor.commit();

                ((MainActivity)getActivity()).mEditor.putString("WheelSizeScale", SystemsList.get(position));
                ((MainActivity)getActivity()).mEditor.commit();

                setSpinnerValues(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    private void setSpinnerValues(View view){
        MainActivity mainActivity = (MainActivity)getActivity();
        final List<String> ValueList = Wheel.getValuesList(getContext(),(mainActivity.mSharedPreferences.getString("WheelSizeScaleInt", Integer.toString(R.string.circ_system))));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_item,ValueList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(ValueList.indexOf(((MainActivity)getActivity()).mSharedPreferences.getString("wheelCircInSelectedSystem",ValueList.get(0))));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();

                MainActivity mainActivity = (MainActivity)getActivity();
                double circuit = Wheel.getCircValue(getContext(),(mainActivity.mSharedPreferences.getString("WheelSizeScaleInt", Integer.toString(R.string.circ_system))),selected);
                mainActivity.setWheelCirc(circuit);
                mainActivity.mEditor.putString("wheelCircInSelectedSystem",selected);
                mainActivity.mEditor.commit();
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

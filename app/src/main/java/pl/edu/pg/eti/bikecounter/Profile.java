package pl.edu.pg.eti.bikecounter;

import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Profile extends Fragment implements AdapterView.OnItemSelectedListener {

    Spinner mSpinner;

    public static Profile newInstance() {
        return new Profile();
    }
    private final static String TAG = Profile.class.getSimpleName();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        initPreferences();

        //Spinner spinner = (Spinner) view.findViewById(R.id.spinner);

        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.wheelsizes, android.R.layout.simple_spinner_item );
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        final List<String> SystemsList = Arrays.asList(getResources().getStringArray(R.array.scalewheelsizes));

        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        Spinner scaleSpinner = (Spinner) view.findViewById(R.id.scaleSpinner);
        ArrayAdapter<CharSequence> scaleAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.scalewheelsizes, android.R.layout.simple_spinner_item );
        scaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        //TODO!!!! wstawić tablica.indexOf(sharedPreferences("WheelSizeScale",R.string.Circuitsystems)
        //int pom1=SystemsList.indexOf(((MainActivity)getActivity()).mSharedPreferences.getString("WheelSizeScale",SystemsList.get(0)));
        //String pom2 = ((MainActivity)getActivity()).mSharedPreferences.getString("WheelSizeScale",SystemsList.get(0));

        scaleSpinner.setAdapter(scaleAdapter);
        scaleSpinner.setSelection(SystemsList.indexOf(((MainActivity)getActivity()).mSharedPreferences.getString("WheelSizeScale",SystemsList.get(2))));
        scaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (SystemsList.get(position).equals(getResources().getString(R.string.ETRTOsystems))){
                    ((MainActivity)getActivity()).mEditor.putString("WheelSizeScaleInt",getString(R.string.ETRTOsystems));
                }else if (SystemsList.get(position).equals(getResources().getString(R.string.Inchsystems))){
                    ((MainActivity)getActivity()).mEditor.putString("WheelSizeScaleInt",getString(R.string.Inchsystems));
                }else if (SystemsList.get(position).equals(getResources().getString(R.string.Circuitsystems))){
                    ((MainActivity)getActivity()).mEditor.putString("WheelSizeScaleInt",getString(R.string.Circuitsystems));
                }else {
                    Log.d(TAG, "Not correct String value of wheel scale");
                }
                ((MainActivity)getActivity()).mEditor.commit();


                //todo zaktualizować w bazie prederencjach//bazie danych??
                ((MainActivity)getActivity()).mEditor.putString("WheelSizeScale",SystemsList.get(position));
                ((MainActivity)getActivity()).mEditor.commit();

                setValuesSpinner(view);
                Context context = parent.getContext();
                Toast toast = Toast.makeText(context, SystemsList.get(position), Toast.LENGTH_SHORT);
                toast.show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //setValuesSpinner(view);



//        spinner.setAdapter(adapter);
////        int pos = ((MainActivity)getActivity()).getWeelCircPosition();
////        spinner.setSelection(pos);
////        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
////            @Override
////            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
////                String selected = parent.getItemAtPosition(position).toString();
////                int pos=position;
////                Context context = parent.getContext();
////
////                int chose =position;
////                String ETRTO_system, InchOrMetricSystem , circ;
////                ETRTO_system = selected.split(" | ")[0];
////                InchOrMetricSystem = selected.split(" | ")[2];
////                circ = selected.split(" | ")[4];
////
////                ((MainActivity)getActivity()).setWheelCirc(Double.parseDouble(circ),position);
////                ((MainActivity)getActivity()).setWeelCircPosition(position);
////
////                Toast toast = Toast.makeText(context, circ+"mm", Toast.LENGTH_SHORT);
////                toast.show();
////            }
////
////            @Override
////            public void onNothingSelected(AdapterView<?> parent) {
////
////            }
////        });

        return view;
    }

    private void setValuesSpinner(View view){
        String pom =  (((MainActivity)getActivity()).mSharedPreferences.getString("WheelSizeScaleInt", Integer.toString( R.string.Circuitsystems)));
        String pom2= ((MainActivity)getActivity()).mSharedPreferences.getString("WheelSizeScaleInt",Integer.toString(R.string.Circuitsystems));
        final List<String> ValueList = Wheel.ValuesLisy(getContext(),(((MainActivity)getActivity()).mSharedPreferences.getString("WheelSizeScaleInt",Integer.toString(R.string.Circuitsystems))),Wheel.makeWheels());



        ArrayAdapter<String> adapter =new  ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item,ValueList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        //TODO Value in choies system
        //((MainActivity)getActivity()).mSharedPreferences.getString("wheelCircInSelectedSystem",ValueList.get(0));
        mSpinner.setSelection(ValueList.indexOf(((MainActivity)getActivity()).mSharedPreferences.getString("wheelCircInSelectedSystem",ValueList.get(0))));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                Context context = parent.getContext();

                //TODO zaktualizować w preferencjach, w liczniku i w main activity
                //todo zaktualizować wartość w bazie danych
                double circuit = Wheel.CirutValue(getContext(),(((MainActivity)getActivity()).mSharedPreferences.getString("WheelSizeScaleInt",Integer.toString( R.string.Circuitsystems))),selected,Wheel.makeWheels());
                ((MainActivity)getActivity()).setWheelCirc(circuit);
                ((MainActivity)getActivity()).mEditor.putString("wheelCircInSelectedSystem",selected);
                ((MainActivity)getActivity()).mEditor.commit();
                Toast toast = Toast.makeText(context, circuit+"mm", Toast.LENGTH_SHORT);
                toast.show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }



    private void initPreferences() {
        String wheelCirec = ((MainActivity)getActivity()).getWheelCirc().toString();
//        Toast toast = Toast.makeText(getContext(), wheelCirec+"mm", Toast.LENGTH_SHORT);
//        toast.show();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //String text = parent.getItemIdAtPosition(position);
        Toast.makeText(parent.getContext(), "wybrano ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

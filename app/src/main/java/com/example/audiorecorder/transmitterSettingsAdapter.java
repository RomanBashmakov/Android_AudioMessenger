package com.example.audiorecorder;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

public class transmitterSettingsAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<transmitterSetting> objects;
    SetTransmitterSettingCallback callback;

    transmitterSettingsAdapter(Context context,
                               ArrayList<transmitterSetting> transmitterSettingsList,
                               SetTransmitterSettingCallback setTransmitterSettingCallback) {
        callback = setTransmitterSettingCallback;
        ctx = context;
        objects = transmitterSettingsList;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = lInflater.inflate(R.layout.transmitter_layout_settings, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        transmitterSetting TS = getTransmitterSetting(position);

        // заполняем View в пункте списка данными из настройки: частота, время бита
        viewHolder.textFrequency.setText(Integer.toString(TS.frequency));
        viewHolder.textBD.setText(((Float.toString(TS.duration))));
        viewHolder.rb.setChecked(TS.checkedF);

        viewHolder.textBD.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                callback.newBDSetting(Float.parseFloat(viewHolder.textBD.getText().toString()), position);
                callback.setBitDuration(TS.duration);
            }
        });

        viewHolder.textFrequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                callback.newFrequencySetting(Integer.parseInt(viewHolder.textFrequency.getText().toString()), position);
                callback.setFrequency(TS.frequency);
            }
        });

        viewHolder.buttonDelete.setOnClickListener (new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    callback.deleteSetting(position);
                }
            }
        );

        viewHolder.rb.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    callback.checkSetting(position);
                    callback.setFrequency(TS.frequency);
                    callback.setBitDuration(TS.duration);
                }
            }
        );

        return view;
    }

    // настройки по позиции
    transmitterSetting getTransmitterSetting(int position)
    {
        return ((transmitterSetting) getItem(position));
    }
}

class ViewHolder {
    final Button buttonDelete; //buttonBD, buttonFrequency, buttonUseIt
    final TextView textBD, textFrequency;
    final RadioButton rb;
    ViewHolder(View view){
        textBD = view.findViewById(R.id.textBD);
        textFrequency = view.findViewById(R.id.textFrequency);
        buttonDelete = view.findViewById(R.id.buttonDelete);
        rb = view.findViewById(R.id.radioButton);
    }
}


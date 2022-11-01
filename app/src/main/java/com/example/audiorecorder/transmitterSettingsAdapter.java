package com.example.audiorecorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class transmitterSettingsAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<transmitterSetting> objects;

    transmitterSettingsAdapter(Context context, ArrayList<transmitterSetting> transmitterSettingsList) {
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
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null)
        {
            view = lInflater.inflate(R.layout.transmitter_layout_settings, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder(view);

        transmitterSetting TS = getTransmitterSetting(position);

        // заполняем View в пункте списка данными из настройки: частота, время бита
        viewHolder.textFrequency.setText(Integer.toString(TS.frequency));
        viewHolder.textBD.setText(((Float.toString(TS.duration))));

        // По нажатии обновляем настройки из View: частота, время бита
        viewHolder.buttonBD.setOnClickListener (new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    TS.setDuration(Float.parseFloat(viewHolder.textBD.getText().toString()));
                }
            }
        );

        viewHolder.buttonFrequency.setOnClickListener (new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    TS.setFrequency(Integer.parseInt(viewHolder.textFrequency.getText().toString()));
                }
            }
        );

        viewHolder.buttonUseIt.setOnClickListener (new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    MainActivity2.freq=TS.frequency;
                    MainActivity2.duration=TS.duration;
                }
            }
        );

        viewHolder.buttonDelete.setOnClickListener (new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                   MainActivity2.deleteSetting(position);
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
    final Button buttonBD, buttonFrequency, buttonDelete, buttonUseIt;
    final TextView textBD, textFrequency;
    ViewHolder(View view){
        buttonBD = view.findViewById(R.id.buttonBD);
        buttonFrequency = view.findViewById(R.id.buttonFrequency);
        textBD = view.findViewById(R.id.textBD);
        textFrequency = view.findViewById(R.id.textFrequency);
        buttonDelete = view.findViewById(R.id.buttonDelete);
        buttonUseIt = view.findViewById(R.id.buttonUseIt);
    }
}
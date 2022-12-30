package com.example.audiorecorder;

import android.content.Context;
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

public class receiverSettingsAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<ReceiverSetting> objects;
    SetReceiverSettingCallback callback;

    receiverSettingsAdapter(Context context,
                            ArrayList<ReceiverSetting> receiverSettingsList,
                            SetReceiverSettingCallback setReceiverSettingCallback) {
        callback = setReceiverSettingCallback;
        ctx = context;
        objects = receiverSettingsList;
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

        View view = lInflater.inflate(R.layout.receiver_layout_settings, parent, false);

        ViewHolderReceiver viewHolderReceiver = new ViewHolderReceiver(view);

        ReceiverSetting RS = getReceiverSetting(position);

        // заполняем View в пункте списка данными из настройки: частота, время бита
        viewHolderReceiver.textFrequency.setText(Integer.toString(RS.frequency));
        viewHolderReceiver.textBD.setText(((Float.toString(RS.duration))));
        viewHolderReceiver.rb.setChecked(RS.checkedF);

        viewHolderReceiver.textBD.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                callback.newBDSetting(Float.parseFloat(viewHolderReceiver.textBD.getText().toString()), position);
                callback.setBitDuration(RS.duration);
            }
        });

        viewHolderReceiver.textFrequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                callback.newFrequencySetting(Integer.parseInt(viewHolderReceiver.textFrequency.getText().toString()), position);
                callback.setFrequency(RS.frequency);
            }
        });

        viewHolderReceiver.buttonDelete.setOnClickListener (new View.OnClickListener()
                                                            {
                                                                @Override
                                                                public void onClick(View v)
                                                                {
                                                                    callback.deleteSetting(position);
                                                                }
                                                            }
        );

        viewHolderReceiver.rb.setOnClickListener(new View.OnClickListener()
                                                 {
                                                     @Override
                                                     public void onClick(View v)
                                                     {
                                                         callback.checkSetting(position);
                                                         callback.setFrequency(RS.frequency);
                                                         callback.setBitDuration(RS.duration);
                                                         callback.saveSetting(position);
                                                     }
                                                 }
        );

        return view;
    }

    // настройки по позиции
    ReceiverSetting getReceiverSetting(int position)
    {
        return ((ReceiverSetting) getItem(position));
    }
}

class ViewHolderReceiver {
    final Button buttonDelete;
    final TextView textBD, textFrequency;
    final RadioButton rb;
    ViewHolderReceiver(View view){
        textBD = view.findViewById(R.id.textBD);
        textFrequency = view.findViewById(R.id.textFrequency);
        buttonDelete = view.findViewById(R.id.buttonDelete);
        rb = view.findViewById(R.id.radioButton);
    }
}


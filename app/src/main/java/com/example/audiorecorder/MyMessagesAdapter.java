package com.example.audiorecorder;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MyMessagesAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<MyMessage> objects;
    MyMessagesAdapterCallback callback;

    MyMessagesAdapter(Context context,
                               ArrayList<MyMessage> myMessagesList,
                               MyMessagesAdapterCallback myMessagesAdapterCallback) {
        callback = myMessagesAdapterCallback;
        ctx = context;
        objects = myMessagesList;
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

        View view = lInflater.inflate(R.layout.my_message_layout, parent, false);

        MyMessagesViewHolder viewHolder = new MyMessagesViewHolder(view);

        MyMessage oneMessage = getOneMessage(position);

        // заполняем View в пункте списка данными из настройки: частота, время бита
        if (oneMessage.input0output1)
        {
            viewHolder.textMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            viewHolder.textData.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        else
        {
            viewHolder.textMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            viewHolder.textData.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
        viewHolder.textMessage.setText(oneMessage.messageData);
        viewHolder.textData.setText(oneMessage.deliveredTime);

        viewHolder.buttonDelete.setOnClickListener (new View.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(View v)
                                                        {
                                                            callback.deleteItem(position);
                                                        }
                                                    }
        );

        return view;
    }

    // настройки по позиции
    MyMessage getOneMessage(int position)
    {
        return ((MyMessage) getItem(position));
    }
}

class MyMessagesViewHolder {
    final Button buttonDelete;
    final TextView textData, textMessage;
    MyMessagesViewHolder(View view){
        textData = view.findViewById(R.id.textData);
        textMessage = view.findViewById(R.id.textMessage);
        buttonDelete = view.findViewById(R.id.buttonDeleteMessage);
    }
}


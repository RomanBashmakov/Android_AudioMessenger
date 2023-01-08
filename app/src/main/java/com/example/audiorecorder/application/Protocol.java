package com.example.audiorecorder.application;

//вызывает колбэк функцию, в которую передает текст порциями. В идеале должен выдавать массив бит из любой информации.


import com.casualcoding.reedsolomon.EncoderDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;
import com.google.zxing.common.reedsolomon.Util;

public class Protocol {
    //конструктор, Преамбулу даем в виде отдельной переменной
    //метод получения набора бит из текста
    //добавление преамбулы
    int preambula;

    static EncoderDecoder encoderDecoder;
    byte[] data;

    public static byte[] getPackedBytes (int _preambula, byte[] data)
    {
        byte[] encodedData = new byte[0];
        try
        {
            encoderDecoder = new EncoderDecoder();
            encodedData = encoderDecoder.encodeData(data, 5);
        }
        catch (EncoderDecoder.DataTooLargeException e)
        {
            e.printStackTrace();
        }
        return encodedData;
    }

    public static byte[] getUnPackedBytes (byte[] data)
    {
        byte[] decodedData = new byte[0];

        try
        {
            decodedData = encoderDecoder.decodeData(data, 5);
        }
        catch (EncoderDecoder.DataTooLargeException | ReedSolomonException e)
        {
            e.printStackTrace();
        }
        return decodedData;
    }
}

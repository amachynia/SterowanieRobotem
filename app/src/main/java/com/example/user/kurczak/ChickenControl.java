package com.example.user.kurczak;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;

public class ChickenControl extends AppCompatActivity {

    Button buttonStart, buttonStop, buttonSpeedUp, buttonSlowDown, buttonOff;
    SeekBar seekBarSpeed;
    TextView textViewActualSpeed;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chicken_control);

        //Odbiór adresu Bluetooth
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        buttonStart  = (Button)findViewById(R.id.buttonStart);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        //buttonSpeedUp = (Button)findViewById(R.id.buttonSpeedUp);
        //buttonSlowDown = (Button)findViewById(R.id.buttonSlowDown);
        buttonOff = (Button)findViewById(R.id.buttonOff);
        seekBarSpeed = (SeekBar)findViewById(R.id.seekBarSpeed);
        textViewActualSpeed = (TextView)findViewById(R.id.textViewActualSpeed);

        new ConnectBT().execute();

        //------------------------------------------
        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                chickenGo();      //Metoda na przód
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                chickenStop();   //Metoda stop
                msg("Funkcja dostępna w wersji premium");
            }
        });

        buttonOff.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true)
                {
                    textViewActualSpeed.setText(String.valueOf(progress));

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                chickenChange();
                msg("Pełne sterowanie dostępne po aktualizacji oprogramowania robota");
            }
        });
        //-------------------------------------------

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ChickenControl.this, "Łączenie", "Czekaj");  //Okno ładowania
        }

        @Override
        protected Void doInBackground(Void... devices) //Łączenie w tle, kiedy wyswietlane jest okno ładowania
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter(); //Pobiera urządzenie bluetooth
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//Łączy z adresem urządzenia i sprawdza czy dostępność
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//Tworzy połączenie RFCOMM (SPP)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//Rozpoczyna połączenie
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//Sprawdzenie na wypadek błedu połączenia
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //Sprawdza czy wszystko w porządku, po wykonaniu instrukcji w tle
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {

                msg("Błąd połączenia.");
                finish();
            }
            else
            {
                msg("Połączono.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    // Metoda do wyświetlania komunkatów
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    //Metoda do rozłączenia
    private void Disconnect()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.close(); //zamknięcie połączenia
            }
            catch (IOException e)
            { msg("Błąd");}
        }
        finish(); //Powrót do pierwszego ekranu
    }
    private void chickenGo()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("G".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Błąd");
            }
        }
    }
    private void chickenStop()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("S".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Błąd");
            }
        }
    }

    private void chickenChange()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(String.valueOf(progress).getBytes());
            }
            catch (IOException e)
            {
                msg("Błąd");
            }
        }
    }

    /*private void chickenSpeedUp()
    {
        if (btSocket!=null)
        {
            try
            {

                btSocket.getOutputStream().write("ST".toString().getBytes());

            }
            catch (IOException e)
            {
                msg("Błąd");
            }
        }
    }
    private void chickenSlowDown()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("ST".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Błąd");
            }
        }
    }*/
}

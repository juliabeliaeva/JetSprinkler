package com.intellij.jetSprinkler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Connection {
  private static Connection ourInstance = new Connection();

  public static Connection getInstance() {
    return ourInstance;
  }

  private Connection() {

  }

  //----------

  private static final UUID SERIAL_PORT_PROTOCOL_UID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  private BluetoothAdapter myBtAdapter = BluetoothAdapter.getDefaultAdapter();
  private BluetoothSocket myBtSocket;

  public boolean init(String address) {
    assert myBtSocket == null;

    BluetoothDevice device = myBtAdapter.getRemoteDevice(address);

    try {
      myBtSocket = device.createRfcommSocketToServiceRecord(SERIAL_PORT_PROTOCOL_UID);
    } catch (IOException e) {
      return false;
    }

    myBtAdapter.cancelDiscovery();

    try {
      myBtSocket.connect();
      new ConnectedThread(myBtSocket).start();
    } catch (IOException e) {
      return false;
    }

    return true;
  }

  public void dispose() {
    try {
      myBtSocket.close();
    } catch (IOException e) {

    }
  }


  private class ConnectedThread extends Thread {
    private final BluetoothSocket mySocket;
    private final InputStream myInStream;
    private final OutputStream myOutStream;
    private final ArrayList<Byte> myBuffer = new ArrayList<Byte>();

    public ConnectedThread(BluetoothSocket socket) {
      mySocket = socket;
      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      try {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException e) {

      }

      myInStream = tmpIn;
      myOutStream = tmpOut;
    }

    public void run() {
      byte[] buffer = new byte[256];
      int bytes;

      while (true) {
        try {
          if (myInStream.available() > 0) {
            bytes = myInStream.read(buffer);
            synchronized (myBuffer) {
              for (int i = 0; i < bytes; i++) {
                myBuffer.add(buffer[i]);
              }
            }
          }
        } catch (IOException e) {
          break;
        }
      }
    }

    public byte[] read(){
      synchronized (myBuffer){
        byte[] res = new byte[myBuffer.size()];
        for (int i=0;i<myBuffer.size();i++){
          res[i] = myBuffer.get(i);
        }
        return res;
      }
    }

    public void write(byte[] message) {
      try {
        myOutStream.write(message);
      } catch (IOException e) {
        Log.d("UNKNOWN", "ERR: " + e.getMessage());
      }
    }
  }
}

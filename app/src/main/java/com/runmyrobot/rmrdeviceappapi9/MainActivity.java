package com.runmyrobot.rmrdeviceappapi9;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity {


    Socket toWebServerSocketMemberVariable;

    AudioHandler audioHandler;
    TextToSpeech ttobj;

    //String robotID = "22027911"; // Zip
    //String robotID = "88359766"; // Skippy_old
    //String robotID = "3444925"; // Timmy
    //String robotID = "52225122"; // Pippy
    //String robotID = "19359999"; // Mikey
    //String robotID = "72378514"; // Skippy
    //String robotID = "48853711"; // Marvin
    //String robotID = "11467183"; // Pam
    //String robotID = "60484851"; // Jenny
    //String robotID = "65553815"; // BlueberrySurprise
    //String robotID = "59376173"; // BumbleBee
    //String robotID = "13918436"; // Nemo
    String robotID = "11543083"; // RedBird
    //String robotID = "68633150"; // Zero



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
            }
        });

        audioHandler = new AudioHandler(robotID);
        audioHandler.start();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        if (!Global.socketListeningInitialized) {
            try {
                // 10.0.2.2 is a special address, the computer you are developing on
                toWebServerSocketMemberVariable = IO.socket("http://runmyrobot.com:8022");
                chatSocket();
            } catch (java.net.URISyntaxException name) {
                // print error message here
                Log.e("RobotSocket", "socket error");
            }
            Global.socketListeningInitialized = true;
        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





    void chatSocket() {

        Log.i("RobotSocket", "test socket io method");

        //final Button mButton=(Button)findViewById(R.id.button1);

        toWebServerSocketMemberVariable.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.e("RobotSocket", "connection error: " + args.length + " " + args[0].toString());
                //mButton.setText("connection error: " + args.length + " " + args[0].toString());
            }

        }).on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i("RobotSocket", "connected to the webserver");
                //socket.disconnect();
                toWebServerSocketMemberVariable.emit("identify_robot_id", robotID);
            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i("RobotSocket", "on event");
            }

        }).on("chat_message_with_name", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject)args[0];
                try {
                    //Log.i("RobotSocket", "chat message with name " + obj + " " + obj.get("name") + " " + args[0].getClass().getName() + " " + this.toString());
                    // uses all but the first word (which is the name of the robot)
                    String[] splitString = ((String)obj.get("message")).split(" ", 2);
                    String message = "";
                    if (splitString.length > 1) {
                        message = splitString[1];
                    }
                    Log.i("RobotSocket", "chat message: " + message);
                    String name = (String)obj.get("name");
                    //Object[] voices = ttobj.getVoices().toArray();
                    //Log.i("RobotVoices", "voices: " + voices);
                    //ttobj.setVoice((android.speech.tts.Voice)voices[stringToHash(name, voices.length)]);
                    //ttobj.setLanguage(Locale.UK);


                    // set volume based on time of day
                    AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    int amStreamMusicMaxVol = am.getStreamMaxVolume(am.STREAM_MUSIC);
                    java.util.Calendar rightNow = java.util.Calendar.getInstance();
                    int hour = rightNow.get(java.util.Calendar.HOUR_OF_DAY);
                    Log.i("Now", "" + hour);
                    int volume = amStreamMusicMaxVol;
                    /*
                    if (hour >= 22 || hour <= 9) {
                        volume = (int)(amStreamMusicMaxVol * 0.65);
                    } else {
                        volume = (int)(amStreamMusicMaxVol * 1.0);
                    }
                    am.setStreamVolume(am.STREAM_MUSIC, volume, 0);
                    */


                    ttobj.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                    runOnUiThread(new ChatRunnable(message) {
                        @Override
                        public void run() {
                            //TextView textView = (TextView)findViewById(R.id.textView);
                            //textView.setText(this.message);
                        }
                    });
                } catch (JSONException e) {
                    Log.e("RobotSocket", "JSON Exception: " + e.toString());
                }
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i("RobotSocket", "on disconnect");
            }

        });

        toWebServerSocketMemberVariable.connect();
        Log.i("RobotSocket", "called socket connect4");

    }













}
























class AudioHandler {

    //public static int port = 51005; // dev
    public static int port = 50005; // prod

    //private String destinationInternetAddress = "192.168.1.3"; // windows machine
    private String destinationInternetAddress = "audio.runmyrobot.com"; // dev server

    private Button startButton,stopButton;

    public byte[] buffer;
    public static DatagramSocket socket;

    AudioRecord recorder;

    private int sampleRate = 16000; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //private int audioFormat = AudioFormat.ENCODING_PCM_8BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat); // this one seems better for a BLU Advance 5.0 - Unlocked Dual Sim Smartphone - US GSM - Black
    //int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 10; // this one seems better for a Blu Dash JR D141W Dual Sim Factory Unlocked Android Smartphone
    //int minBufSize = 2048*64;
    private boolean status = true;

    private String robotID;


    AudioHandler(String robotIDParameter) {
        robotID = robotIDParameter;
    }

    public void start() {

        Log.i("Audio", "minBufSize: " + minBufSize);

        if (!Global.started) {
            status = true;
            startStreaming();
            Global.started = true;
        }

    }


    public static short[] toShorts(byte[] bytes, ByteOrder byteOrder) {
        short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(byteOrder);
        for (int i = 0; i < out.length; i++) {
            out[i] = bb.getShort();
        }
        return out;
    }


    public static byte[] concatByteArrays(byte a[], byte b[]) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }


    public void startStreaming() {

        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    DatagramSocket socket = new DatagramSocket();
                    Log.d("Audio", "Socket Created");

                    byte[] buffer = new byte[minBufSize];
                    byte[] halfBuffer;

                    short[] buffer2 = new short[minBufSize];


                    Log.d("Audio","Buffer created of size " + minBufSize);
                    DatagramPacket packet;

                    final InetAddress destination = InetAddress.getByName(destinationInternetAddress);
                    Log.d("Audio", "Address retrieved: " + destination.toString());

                    Log.d("Audio", "Port: " + port);


                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                    //recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,10);
                    Log.d("Audio", "Recorder initialized");

                    recorder.startRecording();

                    int count = 0;

                    while(status == true) {

                        //Log.i("Audio", "------------------------------");

                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        // cut the data in half, just for testing
                        //halfBuffer = Arrays.copyOfRange(buffer, 0, buffer.length/2);

                        //minBufSize = recorder.read(buffer2, 0, buffer.length);

                        //todo: this will definitely mess up the sound but i'm using it to debug right now
                        //buffer[0] = (byte)count;
                        //buffer[1] = 0;

                        // create a packet that has the robot id and the audio data
                        int robotIDChunkSize = 64;
                        byte[] robotIDChunk = new byte[robotIDChunkSize];
                        // copy robot id into the chunk
                        byte[] robotIDInBytes = robotID.getBytes("UTF-8");
                        for (int k=0; k<robotIDInBytes.length; k++)
                            robotIDChunk[k] = robotIDInBytes[k];
                        byte[] fullBuffer = concatByteArrays(robotIDChunk, buffer);

                        //putting full buffer in the packet
                        packet = new DatagramPacket(fullBuffer, fullBuffer.length, destination, port);

                        String message = "";
                        for (int index=0; index<50; index++) {
                            byte b2 = buffer[index];
                            //http://stackoverflow.com/questions/12310017/how-to-convert-a-byte-to-its-binary-string-representation
                            String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF));
                            message += s2;
                            message += " ";
                        }
                        //Log.i("Audio", "binary: " + message + "...");


                        //Log.i("Audio", "buffer length: " + buffer.length);
                        //Log.i("Audio", "half buffer length: " + halfBuffer.length);

                        /*
                        short[] shorts = toShorts(buffer, ByteOrder.LITTLE_ENDIAN);
                        //short[] shorts = buffer2;
                        String message1 = "";
                        for (int index=0; index<50; index++) {
                            message1 += shorts[index];
                            message1 += " ";
                        }
                        */

                        /*
                        short[] shortsBig = toShorts(buffer, ByteOrder.BIG_ENDIAN);
                        String messageBig = "";
                        for (int index=0; index<50; index++) {
                            messageBig += shortsBig[index];
                            messageBig += " ";
                        }
                        */

                        //Log.i("Audio", "little endian: " + message1 + "..."); // looks more correct
                        //Log.i("Audio", "big endian: " + messageBig + "...");
                        //Log.i("Audio", "length: " + buffer.length);

                        socket.send(packet);
                        //System.out.println("MinBufferSize: " +minBufSize);

                        count++;

                    }



                } catch(UnknownHostException e) {
                    Log.e("Audio", "UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Audio", "IOException");
                }
            }

        });
        streamThread.start();
    }
}













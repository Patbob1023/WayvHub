package io.chirp.chirpwifi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.chirp.connect.ChirpConnect;
import io.chirp.connect.interfaces.ConnectEventListener;
import io.chirp.connect.interfaces.ConnectSetConfigListener;
import io.chirp.connect.models.ChirpError;
import io.chirp.connect.models.ConnectState;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    ChirpConnect chirpConnect;
    Context context;

    String ID = "error";
    String credentialsString = "foo";

    TextView status;
    TextView startStopListeningBtn;

    LottieAnimationView animationView;

    FirebaseFirestore db;


    // initialize a Random object somewhere; you should only need one
    Random random = new Random();



    final int RESULT_REQUEST_PERMISSIONS = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animationView = findViewById(R.id.animation_view);

        animationView.useHardwareAcceleration();

        status = findViewById(R.id.status);
        startStopListeningBtn = findViewById(R.id.startStopListening);
        context = this;
        startStopListeningBtn.setAlpha(.4f);
        startStopListeningBtn.setClickable(false);
        /*
        You can download config string and credentials from your admin panel at developers.chirp.io
         */
        String KEY = "YOUR KEY HERE";
        String SECRET = "YOUR SECRET HERE";
        String CONFIG = "YOUR CONFIG STRING HERE";

        chirpConnect = new ChirpConnect(this, KEY, SECRET);
        chirpConnect.setConfig(CONFIG, connectSetConfigListener);
        chirpConnect.setListener(connectEventListener);

    }

    ConnectSetConfigListener connectSetConfigListener = new ConnectSetConfigListener() {
        @Override
        public void onSuccess() {
            //The config is successfully set, we can enable Start/Stop button now
            startStopListeningBtn.setAlpha(1f);
            startStopListeningBtn.setClickable(true);
        }

        @Override
        public void onError(ChirpError setConfigError) {
            Log.e("SetConfigError", setConfigError.getMessage());
            setStatus("SetConfigError\n" + setConfigError.getMessage());
        }
    };

    ConnectEventListener connectEventListener = new ConnectEventListener() {

        @Override
        public void onSending(byte[] payload, byte channel) {
            /**
             * onSending is called when a send event begins.
             * The data argument contains the payload being sent.
             */
            String hexData = "null";
            if (payload != null) {
                hexData = chirpConnect.payloadToHexString(payload);
            }
            Log.v("connectdemoapp", "ConnectCallback: onSending: " + hexData + " on channel: " + channel);
        }

        @Override
        public void onSent(byte[] payload, byte channel) {
            /**
             * onSent is called when a send event has completed.
             * The data argument contains the payload that was sent.
             */
            String hexData = "null";
            if (payload != null) {
                hexData = chirpConnect.payloadToHexString(payload);
            }
            Log.v("connectdemoapp", "ConnectCallback: onSent: " + hexData + " on channel: " + channel);
        }

        @Override
        public void onReceiving(byte channel) {
            /**
             * onReceiving is called when a receive event begins.
             * No data has yet been received.
             */
            Log.v("connectdemoapp", "ConnectCallback: onReceiving on channel: " + channel);
            setStatus("Receiving...");
        }

        @Override
        public void onReceived(byte[] payload, byte channel) {
            /**
             * onReceived is called when a receive event has completed.
             * If the payload was decoded successfully, it is passed in data.
             * Otherwise, data is null.
             */
            String hexData = "null";
            if (payload != null) {
                hexData = chirpConnect.payloadToHexString(payload);
            }
            Log.v("connectdemoapp", "ConnectCallback: onReceived: " + hexData + " on channel: " + channel);

            try {
                credentialsString = new String(payload, "UTF-8");
//                List<String> credentialsList = Arrays.asList(credentialsString.split(":"));
//                String networkSSID = credentialsList.get(0);
//                String networkPass = credentialsList.get(1);

                db = FirebaseFirestore.getInstance();

                db.collection("users")
                        .whereEqualTo("ID",credentialsString)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("Error: ",document.getId() + " => " + document.getData());
                                        if (document.exists()) {
                                            Log.d("DocumentSnapshot data: ", document.getData().toString());





                                            // generate a random integer from 0 to 899, then add 100
                                            int upperBound = 999;
                                            int lowerBound = 100;
                                            int scramble = lowerBound + (int)(Math.random() * ((upperBound - lowerBound) + 1));
                                            String scrambled = "UW" + scramble;

                                            setStatus(credentialsString);

                                            try {
                                                db.collection("users").document((document.getId()).toString())
                                                        .update("ID", scrambled);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.d("Error: ", "Auth denied");
                                            }
                                            animationView.setSpeed(1);
                                            animationView.playAnimation();

                                            new CountDownTimer(1000, 500) {
                                                @Override
                                                public void onTick(long millisUntilFinished) {
                                                    // do something after 1s
                                                }
                                                @Override
                                                public void onFinish() {
                                                    animationView.reverseAnimationSpeed();
                                                    animationView.playAnimation();
                                                }
                                            }.start();
                                        }
                                        else {
                                            setStatus("User not authorized");
                                        }
                                    }
                                } else {
                                    Log.d("Error: ", task.getException().toString());
                                    setStatus("User not authorized");
                                }
                            }
                        });

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                setStatus("Received data but unable to decode credentials...");
            }
        }


        @Override
        public void onStateChanged(byte oldState, byte newState) {
            /**
             * onStateChanged is called when the SDK changes state.
             */
            Log.v("connectdemoapp", "ConnectCallback: onStateChanged " + oldState + " -> " + newState);
            ConnectState state = ConnectState.createConnectState(newState);
            if (state == ConnectState.AudioStateRunning) {
                setStatus("Listening...");
            }

        }

        @Override
        public void onSystemVolumeChanged(int oldVolume, int newVolume) {
            Log.v("connectdemoapp", "System volume has been changed, notify user to increase the volume when sending data");
        }

    };

    public void learnMore(View view) {
        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://chirp.io"));
        startActivity(intent);
    }

    public void startStopListening(View view) {
        if (chirpConnect.getConnectState() == ConnectState.AudioStateStopped) {
            ChirpError startError = chirpConnect.start();
            if (startError.getCode() > 0) {
                Log.d("startStopListening", startError.getMessage());
            } else {
                setStatus("Listening...");
                startStopListeningBtn.setText("Stop Listening");
            }

        } else {
            ChirpError stopError = chirpConnect.stop();
            if (stopError.getCode() > 0) {
                Log.d("startStopListening", stopError.getMessage());
            } else {
                setStatus("IDLE");
                startStopListeningBtn.setText("Start Listening");
            }
        }
    }

    private void setStatus(final String newStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(newStatus);
            }
        });
    }

    private void connectToWifi(String networkSSID, String networkPass) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        //remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        boolean connected = wifiManager.reconnect();
        if (connected) {
            setStatus("Connecting to " + networkSSID + "...");
            checkConnected(20);
        }
    }

    private void checkConnected(final int repeats) {
        final Handler handler = new Handler();
        final int[] attempts = {repeats};
        final Runnable r = new Runnable() {
            public void run() {
                Log.d("checkConnected", "attempt: " + attempts[0]);
                ConnectivityManager cm =
                        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isConnected = wifiNetwork != null &&
                        wifiNetwork.isConnected();
                if (isConnected) {
                    setStatus("Connected");
                } else if (attempts[0] > 0){
                    attempts[0]--;
                    handler.postDelayed(this, 1000);
                }

            }
        };
        handler.postDelayed(r, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] permissionsToRequest = new String[] {
                Manifest.permission.RECORD_AUDIO
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, RESULT_REQUEST_PERMISSIONS);
        }
    }
}

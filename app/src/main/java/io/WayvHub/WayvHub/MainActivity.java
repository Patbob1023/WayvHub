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
        String KEY = "39caccCd9F7a1A7F0BD2d27bA";
        String SECRET = "1dfD0C1d04eBB8DF7a27C1c4b8Cf4F0De1C2CD3365EAEEfec4";
        String CONFIG = "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";

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

package com.minhduc.blinkingled;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;


public class MainActivity extends Activity {
    private static final String TAG = "LED & Button";
    private String GPIO2 = "BCM2";
    private String GPIO3 = "BCM3";
    private String GPIO4 = "BCM4";
    private String GPIO20 = "BCM20";


    private static int INTERVAL_BETWEEN_BLINKS_MS = 1000;

    private Handler mHandler = new Handler();

    private Gpio mLedGpioGreen;
    private Gpio mLedGpioRed;
    private Gpio mLedGpioBlue;
    private Gpio mButtonGpio;

    private int ledState;
    private int buttonState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PeripheralManager manager = PeripheralManager.getInstance();
            mLedGpioRed = manager.openGpio(GPIO2);
            mLedGpioGreen = manager.openGpio(GPIO3);
            mLedGpioBlue = manager.openGpio(GPIO4);
            mButtonGpio = manager.openGpio(GPIO20);

            // Define in/out ports
            mLedGpioRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);

            // Assign init states
            mLedGpioRed.setActiveType(Gpio.ACTIVE_LOW);
            mLedGpioGreen.setActiveType(Gpio.ACTIVE_LOW);
            mLedGpioBlue.setActiveType(Gpio.ACTIVE_LOW);
            mButtonGpio.setActiveType(Gpio.ACTIVE_HIGH);

            // Init first state
            ledState = 1;
            buttonState = 1;

            // Post handle
            mHandler.post(mBlinkRunnable);
            mButtonGpio.registerGpioCallback(mGpioCallback);

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

    }

    private GpioCallback mGpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio button) {
            try {
                switch (buttonState) {
                    case 1:
                        if (!button.getValue()) {
                            INTERVAL_BETWEEN_BLINKS_MS = 2000;
                            buttonState = 2;
                            Log.i(TAG, "Button State 1"); //For debug
                        }
                        break;
                    case 2:
                        if (!button.getValue()) {
                            INTERVAL_BETWEEN_BLINKS_MS = 1000;
                            Log.i(TAG, "Button State 2"); //For debug
                            buttonState = 3;
                        }
                        break;
                    case 3:
                        if (!button.getValue()) {
                            INTERVAL_BETWEEN_BLINKS_MS = 500;
                            Log.i(TAG, "Button State 3"); //For debug
                            buttonState = 4;
                        }
                        break;
                    case 4:
                        if (!button.getValue()) {
                            INTERVAL_BETWEEN_BLINKS_MS = 100;
                            Log.i(TAG, "Button State 4"); //For debug
                            buttonState = 1;
                        }
                        break;
                    default:
                        break;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
            }
            return true;
        }
    };

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpioBlue == null || mLedGpioGreen == null || mLedGpioRed == null) {
                return;
            }
            try {
                // Toggle between states
                switch (ledState) {
                    case 1:
                        mLedGpioRed.setValue(true);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(false);
                        ledState = 2;
                        break;
                    case 2:
                        mLedGpioRed.setValue(false);
                        mLedGpioGreen.setValue(true);
                        mLedGpioBlue.setValue(false);
                        ledState = 3;
                        break;
                    case 3:
                        mLedGpioRed.setValue(false);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(true);
                        ledState = 1;
                        break;
                    default:
                        break;
                }

                // Reschedule runnable INTERVAL_BETWEEN_BLINKS_MS
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);

            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending blink Runnable
        mHandler.removeCallbacks(mBlinkRunnable);
        // Close the Gpio pin.
        Log.i(TAG, "Close LED GPIO");
        try {
            mLedGpioGreen.close();
            mLedGpioBlue.close();
            mLedGpioRed.close();
            mButtonGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpioRed = null;
            mLedGpioBlue = null;
            mLedGpioGreen = null;
            mButtonGpio = null;
        }
    }
}
package com.minhduc.blinkingled;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;


public class MainActivity extends Activity {
    private static final String TAG = "RGB LED";
    private String GPIO2 = "BCM2";
    private String GPIO3 = "BCM3";
    private String GPIO4 = "BCM4";

    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;

    private Handler mHandler = new Handler();

    private Gpio mLedGpioGreen;
    private Gpio mLedGpioRed;
    private Gpio mLedGpioBlue;

    private int state;

    @Override
    // This function is just like setup()
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting BlinkActivity");

        try {
            // Declare GPIO ports
            PeripheralManager manager = PeripheralManager.getInstance();
            mLedGpioRed = manager.openGpio(GPIO2);
            mLedGpioGreen = manager.openGpio(GPIO3);
            mLedGpioBlue = manager.openGpio(GPIO4);

            // Define outputs
            mLedGpioRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            // Assign initial states
            mLedGpioRed.setActiveType(Gpio.ACTIVE_LOW);
            mLedGpioGreen.setActiveType(Gpio.ACTIVE_LOW);
            mLedGpioBlue.setActiveType(Gpio.ACTIVE_LOW);

            // Init first state
            state = 1;

            // Post handle
            mHandler.post(mBlinkRunnable);

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpioBlue == null || mLedGpioGreen == null || mLedGpioRed == null) {
                return;
            }
            try {
                // State Machine
                switch (state) {
                    case 1:
                        mLedGpioRed.setValue(true);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(false);
                        state = 2;
                        break;
                    case 2:
                        mLedGpioRed.setValue(false);
                        mLedGpioGreen.setValue(true);
                        mLedGpioBlue.setValue(false);
                        state = 3;
                        break;
                    case 3:
                        mLedGpioRed.setValue(false);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(true);
                        state = 1;
                        break;
                    default:
                        break;
                }

                // Reschedule runnable in INTERVAL_BETWEEN_BLINKS_MS
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
        // Close Gpio
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            mLedGpioGreen.close();
            mLedGpioBlue.close();
            mLedGpioRed.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpioRed = null;
            mLedGpioBlue = null;
            mLedGpioGreen = null;
        }
    }
}
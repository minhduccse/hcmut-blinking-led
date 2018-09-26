package com.minhduc.blinkingled;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;


public class MainActivity extends Activity {
    private static final String TAG = "Blink LED";
    private String RED = "BCM2";
    private String GREEN = "BCM3";
    private String BLUE = "BCM4";

    private Handler mHandler = new Handler();

    private Gpio mLedGpioGreen;
    private Gpio mLedGpioRed;
    private Gpio mLedGpioBlue;

    private int ledCounter = 0;
    private int state;

    @Override
    // This function is just like setup()
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Declare GPIO ports
            PeripheralManager manager = PeripheralManager.getInstance();
            mLedGpioRed = manager.openGpio(RED);
            mLedGpioGreen = manager.openGpio(GREEN);
            mLedGpioBlue = manager.openGpio(BLUE);

            // Define outputs
            mLedGpioRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            // Assign init states
            mLedGpioRed.setActiveType(Gpio.ACTIVE_LOW);
            mLedGpioGreen.setActiveType(Gpio.ACTIVE_LOW);
            mLedGpioBlue.setActiveType(Gpio.ACTIVE_LOW);

            // Init first state
            state = 0;

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
                // Toggle between states
                switch(state) {
                    case 0:
                        mLedGpioRed.setValue(false);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(false);
                        ledCounter++;
                        if (ledCounter == 5 || ledCounter == 9) {
                            state = 2;
                        }
                        else if (ledCounter == 7) {
                            state = 4;
                        }
                        else if (ledCounter == 13) {
                            state = 3;
                        }
                        else {
                            state = 1;
                        }
                        break;
                    case 1:
                        // Red LED on 250ms
                        mLedGpioRed.setValue(true);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(false);
                        state = 0;
                        break;
                    case 2:
                        // Red & Green LED on 250ms
                        mLedGpioRed.setValue(true);
                        mLedGpioGreen.setValue(true);
                        mLedGpioBlue.setValue(false);
                        state = 0;
                        break;
                    case 3:
                        // All LED on 250ms
                        mLedGpioRed.setValue(true);
                        mLedGpioGreen.setValue(true);
                        mLedGpioBlue.setValue(true);
                        state = 0;
                        ledCounter = 0;
                        break;
                    case 4:
                        // Red & Blue LED on 250ms
                        mLedGpioRed.setValue(true);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(true);
                        state = 0;
                        break;
                    default:
                        break;
                }

                // Reschedule runnable INTERVAL_BETWEEN_BLINKS_MS
                mHandler.postDelayed(mBlinkRunnable, 250);

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
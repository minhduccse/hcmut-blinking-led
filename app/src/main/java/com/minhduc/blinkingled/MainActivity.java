package com.minhduc.blinkingled;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;
import com.leinardi.android.things.pio.SoftPwm;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = "Brightness Change";
    private String GPIO2 = "BCM2";
    private String GPIO3 = "BCM3";
    private String GPIO4 = "BCM4";
    private String GPIO20 = "BCM20";

    private Handler mHandler = new Handler();

    // PWM PIN
    private static final double MIN_DUTY_CYCLE = 0;
    private static final double MAX_DUTY_CYCLE = 100;
    private static final double DUTY_CYCLE_CHANGE_PER_STEP = 0.1;

    private int ledState;
    private int buttonState;

    private static final int STEP = 1;
    private double dutyCycle;
    private boolean isIncreasing = true;

    private Pwm mPwmRed;
    private Pwm mPwmGreen;
    private Pwm mPwmBlue;

    private Gpio mButtonGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting Activity");
        try {
            PeripheralManager manager = PeripheralManager.getInstance();
            mButtonGpio = manager.openGpio(GPIO20);

            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);

            mButtonGpio.setActiveType(Gpio.ACTIVE_HIGH);

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

        // Attempt to access the PWM port
        try {
            mPwmRed = SoftPwm.openSoftPwm(GPIO2);
            mPwmGreen = SoftPwm.openSoftPwm(GPIO3);
            mPwmBlue = SoftPwm.openSoftPwm(GPIO4);
            initializePwm(mPwmRed);
            initializePwm(mPwmGreen);
            initializePwm(mPwmBlue);

            ledState = 0;
            buttonState = 0;

            mHandler.post(changePWMRunnable);
            mButtonGpio.registerGpioCallback(mGpioCallback);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access PWM", e);
        }
    }

    private GpioCallback mGpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio button) {
            try {
                switch (buttonState) {
                    case 0:
                        if (!button.getValue()) {
                            Log.i(TAG, "Button State 1"); //For debug
                            ledState = 1;
                            buttonState = 1;
                        }
                        break;
                    case 1:
                        if (!button.getValue()) {
                            Log.i(TAG, "Button State 2"); //For debug
                            ledState = 2;
                            buttonState = 2;
                        }
                        break;
                    case 2:
                        if (!button.getValue()) {
                            Log.i(TAG, "Button State 3"); //For debug
                            ledState = 3;
                            buttonState = 3;
                        }
                        break;
                    case 3:
                        if (!button.getValue()) {
                            Log.i(TAG, "Button State 4"); //For debug
                            ledState = 0;
                            buttonState = 0;
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

    private Runnable changePWMRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPwmRed == null || mPwmGreen == null || mPwmBlue == null) {
                Log.w(TAG, "Stopping runnable since Pwm is null");
                return;
            }

            if (isIncreasing) {
                dutyCycle += DUTY_CYCLE_CHANGE_PER_STEP;
            } else {
                dutyCycle -= DUTY_CYCLE_CHANGE_PER_STEP;
            }

            if (dutyCycle > MAX_DUTY_CYCLE) {
                dutyCycle = MAX_DUTY_CYCLE;
                isIncreasing = !isIncreasing;
            } else if (dutyCycle < MIN_DUTY_CYCLE) {
                dutyCycle = MIN_DUTY_CYCLE;
                isIncreasing = !isIncreasing;
            }

            Log.d(TAG, "Changing PWM duty cycle to " + dutyCycle);

            try {
                // Toggle between states
                switch (ledState) {
                    case 0:
                        mPwmRed.setPwmDutyCycle(dutyCycle);
                        mPwmGreen.setPwmDutyCycle(100);
                        mPwmBlue.setPwmDutyCycle(100);
                        break;
                    case 1:
                        mPwmRed.setPwmDutyCycle(100);
                        mPwmGreen.setPwmDutyCycle(dutyCycle);
                        mPwmBlue.setPwmDutyCycle(100);
                        break;
                    case 2:
                        mPwmRed.setPwmDutyCycle(100);
                        mPwmGreen.setPwmDutyCycle(100);
                        mPwmBlue.setPwmDutyCycle(dutyCycle);
                        break;
                    case 3:
                        mPwmRed.setPwmDutyCycle(dutyCycle);
                        mPwmGreen.setPwmDutyCycle(dutyCycle);
                        mPwmBlue.setPwmDutyCycle(dutyCycle);
                    default:
                        break;
                }

                // Reschedule runnable INTERVAL_BETWEEN_BLINKS_MS
                mHandler.postDelayed(changePWMRunnable, STEP);

            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(changePWMRunnable);

        Log.i(TAG, "Closing PWM pin");
        if (mPwmRed != null || mPwmGreen != null || mPwmBlue != null) {
            try {
                mPwmRed.close();
                mPwmGreen.close();
                mPwmBlue.close();
                mButtonGpio.close();

                mPwmRed = null;
                mPwmGreen = null;
                mPwmBlue = null;
                mButtonGpio = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close PWM", e);
            }
        }
    }

    public void initializePwm(Pwm pwm) throws IOException {
        pwm.setPwmFrequencyHz(240);
        pwm.setPwmDutyCycle(25);

        // Enable the PWM signal
        pwm.setEnabled(true);
    }
}
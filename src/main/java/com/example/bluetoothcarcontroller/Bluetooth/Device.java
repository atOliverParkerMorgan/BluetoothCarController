package com.example.bluetoothcarcontroller.Bluetooth;

import android.bluetooth.BluetoothDevice;

public class Device {
    private final BluetoothDevice device;
    private boolean hasBeenPair;
    private final boolean isHCO5;

    public Device(BluetoothDevice device, boolean hasBeenPair, boolean isHC05) {
        this.device = device;
        this.hasBeenPair = hasBeenPair;
        this.isHCO5 = isHC05;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public boolean hasBeenPair() {
        return hasBeenPair;
    }

    public boolean isHCO5() {
        return isHCO5;
    }

    public void setHasNotBeenPair() {
        hasBeenPair = false;
    }
}

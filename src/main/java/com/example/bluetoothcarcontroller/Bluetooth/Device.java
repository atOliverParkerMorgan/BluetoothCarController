package com.example.bluetoothcarcontroller.Bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Device device1 = (Device) o;
        return hasBeenPair == device1.hasBeenPair &&
                isHCO5 == device1.isHCO5 &&
                Objects.equals(device, device1.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, hasBeenPair, isHCO5);
    }
}

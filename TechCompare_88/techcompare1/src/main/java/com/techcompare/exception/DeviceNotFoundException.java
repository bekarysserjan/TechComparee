package com.techcompare.exception;

/** Raised when a requested device ID does not exist in Supabase. */
public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(String deviceType, Integer id) {
        super(deviceType + " with id " + id + " was not found");
    }
}

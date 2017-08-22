package ru.iris.xiaomi4j.enums;

/**
 * @author Nikolay Viguro, 22.08.17
 */
public enum DoorState {
    OPEN("open"),
    CLOSE("close"),
    UNKNOWN("unknown");

    private String name;

    DoorState(String name) {
        this.name = name;
    }

    public static DoorState parse(String name) {
        for(DoorState devices : DoorState.values()) {
            if(devices.name.equals(name))
                return devices;
        }

        return UNKNOWN;
    }
}

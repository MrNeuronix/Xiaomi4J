package ru.iris.xiaomi4j.enums;

/**
 * @author Nikolay Viguro, 22.08.17
 */
public enum Devices {
    UNKNOWN("unknown"),
    GATEWAY("gateway"),
    BRIDGE("bridge"),
    SWITCH("switch"),
    SENSOR_HT("sensor_ht"),
    SENSOR_AQARA_WEATHER("sensor_weather_v1"),
    SWITCH_AQARA_1BUTTON("ctrl_neutral1"),
    SWITCH_AQARA_2BUTTONS("ctrl_neutral2"),
    SWITCH_AQARA_ZERO_1BUTTON("ctrl_neutral1"),
    SWITCH_AQARA_ZERO_2BUTTONS("ctrl_neutral2"),
    SENSOR_AQARA_MAGNET("sensor_magnet.aq2"),
    SENSOR_AQARA_FLOOD("sensor_wleak.aq1"),
	  SENSOR_MOTION("sensor_motion"),
		SENSOR_AQUARA_MOTION("sensor_motion.aq2");

    private String name;

    Devices(String name) {
        this.name = name;
    }

    public static Devices parse(String name) {
        for(Devices devices : Devices.values()) {
            if(devices.name.equals(name))
                return devices;
        }

        return UNKNOWN;
    }
}

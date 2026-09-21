package com.ridelink.common.demo;

import java.util.UUID;

/**
 * Stable demo identities shared conceptually across services (not a shared DB).
 * Driver & Vehicle Service seeds a profile whose accountId matches DRIVER_ID.
 */
public final class DemoAccounts {

    public static final UUID PASSENGER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID DRIVER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID ADMIN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    public static final String PASSWORD = "password";

    private DemoAccounts() {
    }
}

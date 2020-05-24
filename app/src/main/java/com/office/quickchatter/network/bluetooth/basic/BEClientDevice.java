package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

/// Describes the device of a client.
/// Contains information about the capabilities of the device.
public interface BEClientDevice {
    @NonNull String getName();

    interface AudioGateway extends BEClientDevice {

    }

    interface AudioIn extends AudioGateway {

    }

    interface AudioOut extends AudioGateway {

    }
}

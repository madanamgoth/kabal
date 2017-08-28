package com.kabal;

import android.app.Application;
import android.location.Address;

/**
 * Created by vinay on 27/8/17.
 */


public class Mykabal extends Application {

    private Address address;

    public Mykabal(){}

    public Mykabal(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;

    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

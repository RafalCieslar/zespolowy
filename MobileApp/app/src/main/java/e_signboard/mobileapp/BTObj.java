package e_signboard.mobileapp;

/**
 * Created by r2d2 on 2016-06-20.
 */
public class BTObj {
    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    BTObj(){
        this.rssi = -1000;
        this.address = "";
    }
    private int rssi;
    private String address;
}

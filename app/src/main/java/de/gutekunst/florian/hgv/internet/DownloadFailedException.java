package de.gutekunst.florian.hgv.internet;

public class DownloadFailedException extends Exception {

    public static final int ERR_WRONG_USERNAME = 1;
    public static final int ERR_ADRESS_UNREACHABLE = 2;
    public static final int ERR_UNKNOWN = 3;
    public static final int SELECTING_FAILED = 4;


    private int reason;

    public DownloadFailedException(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return reason;
    }

}

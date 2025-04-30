package dev.jasser.configDaemon;

public class statusCodes {
    // Success code
    public static final int SUCCESS = 0;

    // Error codes
    public static final int ERROR_UNKNOWN = 1;
    public static final int ERROR_INVALID_REQUEST = 2;
    public static final int ERROR_INTERNAL_SERVER = 3;
    public static final int ERROR_UNAUTHORIZED = 4;
    public static final int ERROR_NOT_FOUND = 5;

    // Subscription codes
    public static final int SUBSCRIBE_OK = 10;
    public static final int SUBSCRIBE_FAILED = 11;

    // Response types
    public static final int TYPE_FETCH = 12;
    public static final int TYPE_SUBSCRIPTION = 13;
    public static final int TYPE_CHANGE_NOTIFICATION = 14;

    // To prevent instantiation
    private statusCodes() {
        throw new UnsupportedOperationException("Cannot instantiate StatusCodes class");
    }
}

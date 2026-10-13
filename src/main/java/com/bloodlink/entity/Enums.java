package com.bloodlink.entity;

public final class Enums {
    private Enums() {}
    public enum Urgency { NORMAL, URGENT, CRITICAL }
    public enum RequestStatus { OPEN, CLOSED }
    public enum ResponseStatus { NOTIFIED, ACCEPTED, DECLINED }
}

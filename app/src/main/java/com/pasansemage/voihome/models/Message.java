package com.pasansemage.voihome.models;

public class Message {

    private String message;
    private int senderNumber;

    public Message() {
    }

    public Message(String message, int senderNumber) {
        this.message = message;
        this.senderNumber = senderNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(int senderNumber) {
        this.senderNumber = senderNumber;
    }
}

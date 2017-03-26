package com.runmyrobot.rmrdeviceappapi9;

/**
 * Created by Theodore on 2016-09-03.
 */
public class ChatRunnable implements Runnable {
    public String message;

    public ChatRunnable(String chatMessage) {
        message = chatMessage;
    }

    @Override
    public void run() {

    }
}

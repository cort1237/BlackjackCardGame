package com.example.cardgametest;

import android.app.Application;

public class GameApplication extends Application {
    private NetworkHandler networkHandler;

    public NetworkHandler getNetworkHandler() {
        if(networkHandler == null)
            networkHandler = new NetworkHandler();
        return networkHandler;
    }

    public void setNetworkHandler(NetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
    }
}

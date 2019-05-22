package net.manicmachine.controller;

import net.manicmachine.model.Computer;

import java.io.IOException;

public class NetworkWorker implements Runnable {
    private NetworkListener networkListener;
    private String computerId;
    private Computer computer;

    @Override
    public void run() {
        try {
            if (computer != null) {
                computer.setReachable(NetworkUtil.isReachable(computer));
            } else {
                computer = NetworkUtil.getComputerDetails(computerId);
            }

            networkListener.onNetworkComplete(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setComputerId(String computerId) {
        this.computerId = computerId;
    }

    public void setComputer(Computer computer) {
        this.computer = computer;
    }

    public Computer getComputer() {
        return computer;
    }

    public void setNetworkListener(NetworkListener networkListener) {
        this.networkListener = networkListener;
    }
}

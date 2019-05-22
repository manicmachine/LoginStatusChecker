package net.manicmachine.controller;

import net.manicmachine.model.Computer;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtil {

    private static int TIMEOUT = 1000; // 1 second

    public static String getHostName(String identifier) {
        String hostName;

        try {
            InetAddress inetAddress = InetAddress.getByName(identifier);
            hostName = inetAddress.getHostName();
        } catch (UnknownHostException e) {
            hostName = "Unknown";
        }

        return hostName;
    }

    public static String getCanonicalHostName(String identifier) {
        String canonicalHostName;

        try {
            InetAddress inetAddress = InetAddress.getByName(identifier);
            canonicalHostName = inetAddress.getCanonicalHostName();
        } catch (UnknownHostException e) {
            canonicalHostName = "Unknown";
        }

        return canonicalHostName;
    }

    public static String getIPAddress(String identifier) {
        String ipAddress;
        InetAddressValidator validator = new InetAddressValidator();

        if (validator.isValid(identifier)) {
            ipAddress = identifier;
        } else {
            try {
                InetAddress inetAddress = InetAddress.getByName(identifier);
                ipAddress = inetAddress.getHostAddress();

            } catch (UnknownHostException e) {
                ipAddress = "Unknown";
            }
        }

        return ipAddress;
    }

    public static boolean isReachable(Computer computer) throws IOException {
        boolean reachable;
        InetAddress inetAddress;

        try {
            inetAddress = InetAddress.getByName(computer.getCanonicalHostName());

            if (inetAddress.isReachable(TIMEOUT)) {
                reachable = true;
            } else {
                reachable = false;
            }
        } catch (UnknownHostException e) {

            if (computer.getIpAddress().equals("Unknown")) {
                reachable = false;
            } else {
                inetAddress = InetAddress.getByName(computer.getIpAddress());

                if (inetAddress.isReachable(TIMEOUT)) {
                    reachable = true;
                } else {
                    reachable = false;
                }
            }
        }

        return reachable;
    }

    public static Computer getComputerDetails(String identifier) throws IOException {
        Computer computer = new Computer(identifier);

        computer.setHostName(getHostName(identifier));
        computer.setCanonicalHostName(getCanonicalHostName(identifier));
        computer.setIpAddress(getIPAddress(identifier));
        computer.setReachable(isReachable(computer));

        return computer;
    }

    public static void getComputerDetails(Computer computer) throws IOException {
        computer.setHostName(getHostName(computer.getIdentifier()));
        computer.setCanonicalHostName(getCanonicalHostName(computer.getIdentifier()));
        computer.setIpAddress(getIPAddress(computer.getIdentifier()));
        computer.setReachable(isReachable(computer));
    }
}

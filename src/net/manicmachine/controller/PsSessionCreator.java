package net.manicmachine.controller;

import com.profesorfalken.jpowershell.PowerShell;

public class PsSessionCreator implements Runnable {
    private PowerShell psSession;
    private PsSessionListener psSessionListener;

    @Override
    public void run() {
        psSession = PowerShell.openSession();
        psSessionListener.onSessionCreated();
    }

    public PowerShell getSession() {
        return psSession;
    }

    public void setPsSessionListener(PsSessionListener listener) {
        this.psSessionListener = listener;
    }
}

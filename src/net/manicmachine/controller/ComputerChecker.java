package net.manicmachine.controller;

import com.profesorfalken.jpowershell.PowerShell;
import net.manicmachine.model.Computer;

public class ComputerChecker {

    private PowerShell session;

    public ComputerChecker(PowerShell session) {
        this.session = session;
    }

    public void closeSession() {
        this.session.close();
    }

    public boolean isAvailable(Computer computer) {

        return true;
    }

    public String currentUser(Computer computer) {
        String currentUser = "";

        return currentUser;
    }

    public String getComputerType(Computer computer) {
        String computerType = "";

        return computerType;
    }
}

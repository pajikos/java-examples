package com.pavelsklenar.portscanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ScannerCommand {

    public static final String PORT_SEPARATOR = "-";
    private final ScannerService scannerService;

    @Autowired
    public ScannerCommand(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @ShellMethod(value = "Scan open ports for a specific IP address")
    public String scan(
            @ShellOption(help = "IP address") String ip,
            @ShellOption(help = "Port or port range, e.g. 1-1024") String port,
            @ShellOption(help = "Weather only open ports should be displayed") boolean displayOnlyOpen
    ) throws ExecutionException, InterruptedException {
        //Add all required ports into port scanner
        List<Future<ScannerService.ScanResult>> futureList;
        if (port.contains(PORT_SEPARATOR)) {
            String[] rangeLimits = port.split(PORT_SEPARATOR);
            futureList = addToScan(ip, range(Integer.parseInt(rangeLimits[0]), Integer.parseInt(rangeLimits[1])));
        } else {
            futureList = addToScan(ip, Integer.parseInt(port));
        }

        //Read and write results
        for (final Future<ScannerService.ScanResult> scanResultFuture : futureList) {
            ScannerService.ScanResult scanResult = scanResultFuture.get();
            if (displayOnlyOpen) {
                if (scanResult.isOpen()) {
                    System.out.println(scanResult);
                }
            } else {
                System.out.println(scanResult);
            }
        }

        return "DONE";
    }

    private List<Future<ScannerService.ScanResult>> addToScan(String ip, int... ports) {
        List<Future<ScannerService.ScanResult>> result = new ArrayList<>();
        for (int port : ports) {
            result.add(scannerService.checkPort(ip, port));
        }
        return result;
    }

    private int[] range(int min, int max) {
        int[] result = new int[max - min + 1];
        for (int i = min; i <= max; i++) {
            result[i-min] = i;
        }
        return result;
    }
}

package it.polimi.ingsw.psp40.view.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Hourglass implements Runnable{
    private Frame upper;
    private Frame lower;
    private volatile boolean cancelled;

    public Hourglass(Frame upper, Frame lower) {
        this.upper = upper;
        this.lower = lower;
    }

    @Override
    public void run() {
        upper.clear();
        Terminal.hideCursor();
        while (!cancelled) {
            try {
                lower.center( URLReader(getClass().getResource("/ascii/waiting")), 100);
            } catch (IOException e) {
                //
            }
            for (int i = 1; i <= 39; i++) {
                if (cancelled){
                    break;
                }
                try {
                    upper.centerFixed(URLReader(getClass().getResource("/ascii/hourglass/" +i)), 26, 10);
                } catch (IOException e) {
                    //
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    public void cancel()
    {
        cancelled = true;
    }


    public static String URLReader(URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }


}
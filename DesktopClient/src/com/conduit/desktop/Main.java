package com.conduit.desktop;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        DataLink dataLink = new DataLink();

        System.out.println("Hello");
        while(true) {
            Scanner in = new Scanner(System.in);
            int num = in.nextInt();
            dataLink.debugLEDBlink((byte)num);
        }
    }
}

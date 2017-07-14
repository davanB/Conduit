package com.conduit.desktop;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        DataLink dataLink = new DataLink();

        dataLink.openReadingPipe((byte)1, (byte)'b');

        System.out.println("Hello");
        while(true) {
            Scanner in = new Scanner(System.in);
            int num = in.nextInt();
            dataLink.debugEcho((byte)num);
//            dataLink.openReadingPipe((byte)1, (byte)'b');
        }
    }
}

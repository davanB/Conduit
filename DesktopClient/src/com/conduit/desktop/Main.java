package com.conduit.desktop;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        DataLink dataLink = new DataLink();

        dataLink.openWritingPipe((byte)'a');
//        dataLink.openReadingPipe((byte)1, (byte)'b');

        System.out.println("Hello");
        while(true) {
            Scanner in = new Scanner(System.in);
            int num = in.nextInt();
//            dataLink.write("Hello world.".getBytes());
            dataLink.write("0Bacon ipsum dolor amet ball tad1Bacon ipsum dolor amet ball tad2Bacon ipsum dolor amet ball tad3Bacon ipsum dolor amet ball tad4Bacon ipsum dolor amet ball tad5Bacon ipsum dolor amet ball tad6Bacon ipsum dolor amet ball tad7Bacon ipsum dolor amet ball tad8Bacon ipsum dolor amet ball tad9Bacon ipsum dolor amet ball tadABacon ipsum dolor amet ball tadBBacon ipsum dolor amet ball tadCBacon ipsum dolor amet ball tadDBacon ipsum dolor amet ball tad".getBytes());

//            dataLink.openReadingPipe((byte)1, (byte)'b');
        }
    }
}

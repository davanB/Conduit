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
            dataLink.write("Hello world.".getBytes());
//            dataLink.write("Bacon ipsum dolor amet ball tip capicola flank pork chop, andouille chuck bacon ham drumstick jerky cupim prosciutto filet mignon sirloin ham hock. Pork loin burgdoggen jowl alcatra meatloaf spare ribs capicola sausage rump. Ham hock sirloin turducken meatloaf corned beef shank. Shoulder doner spare ribs jowl turkey meatloaf frankfurter bresaola cow capicola short loin shankle ground round.".getBytes());
//            dataLink.openReadingPipe((byte)1, (byte)'b');
        }
    }
}

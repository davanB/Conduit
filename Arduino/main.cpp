#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>
#include "main.h"

#define COMMAND_HEADER 16

#define COMMAND_DEBUG_LED_BLINK 100
#define COMMAND_DEBUG_ECHO 101
#define COMMAND_OPEN_WRITING_PIPE 125
#define COMMAND_OPEN_READING_PIPE 126
#define COMMAND_WRITE 127

#define COMMAND_TERMINATOR 0

#define ERROR_INVALID_COMMAND 1

#define BUFFER_SIZE 32

byte commandId = 0;

RF24 radio(9,10);
byte* buffer = new byte[BUFFER_SIZE];

void setup() {
    Serial.begin(9600);

    radio.begin();
    radio.setAutoAck(true);
    radio.enableAckPayload();
    radio.enableDynamicPayloads();
    // radio.setPALevel(RF24_PA_LOW);
    // radio.setDataRate(RF24_250KBPS);
    radio.setRetries(15,15);
}

void loop() {
    if (radio.available()) {
        Serial.println("New data");
        readRadio();
    } else if (Serial.available()) {
        if (COMMAND_HEADER == Serial.read()) {
            processCommand();
        }
    }
}

void processCommand() {
    commandId = waitForByte();
    switch(commandId) {
        case COMMAND_DEBUG_LED_BLINK:
            debugLEDBlink();
            break;
        case COMMAND_DEBUG_ECHO:
            debugEcho();
            break;
        case COMMAND_OPEN_WRITING_PIPE:
            openWritingPipe();
            break;
        case COMMAND_OPEN_READING_PIPE:
            openReadingPipe();
            break;
        case COMMAND_WRITE:
            write();
            break;
        default:
            sendError(ERROR_INVALID_COMMAND);
            break;
    }
}

void debugLEDBlink() {
    byte numBlinks = waitForByte();
    while (numBlinks > 0) {
        digitalWrite(LED_BUILTIN, HIGH);
        delay(1000);
        digitalWrite(LED_BUILTIN, LOW);
        delay(1000);
        numBlinks = numBlinks - 1;
    }
}

void debugEcho() {
    byte value = waitForByte();
    Serial.write(value);
    Serial.flush();
}

void openWritingPipe() {
    byte address = waitForByte();
    radio.stopListening();
    radio.openWritingPipe(address);
    Serial.write(56);
    Serial.flush();
}

void openReadingPipe() {
    byte pipeNumber = waitForByte();
    byte address = waitForByte();
    radio.openReadingPipe(pipeNumber, address);
    radio.startListening();
    Serial.write(57);
    Serial.flush();
}

void write() {
    uint32_t i = 0;
    byte recvByte = 0;
    Serial.println("Write start!");
    while(true) {
        // Fill buffer
        if (Serial.available()) {
            recvByte = Serial.read();
            buffer[i] = recvByte;
            if(buffer[i] == COMMAND_TERMINATOR){
                Serial.println("TERMINATED");
                tx(i + 1); // Ensure we transmit the null terminator
                break;
            }
            i++;
        }

        if (i >= BUFFER_SIZE) {
            Serial.println("TX Start");
            tx(BUFFER_SIZE);
            i = 0;
        }
    }
    Serial.println("Write done!");
}

void tx(byte payloadSize) {
    radio.stopListening(); //TODO: Evaluate effect on dropped packets
    int ack_buffer[1] = {5};
    // Write buffer to radio
    if (radio.write(buffer, payloadSize)) {
        Serial.println("...tx success");
        if (radio.isAckPayloadAvailable()) {
            radio.read(ack_buffer, sizeof(int));
            Serial.print("received ack payload is : ");
            Serial.println(ack_buffer[0]);
        } else {
            Serial.println("ACK MISSED");
        }
    } else {
        Serial.println("...tx fail");
    }
    radio.startListening(); //TODO: Evaluate effect on dropped packets
}

void readRadio() {
    int ack_buffer[1] = {5};
    radio.writeAckPayload(1, ack_buffer, sizeof(int));
    byte payloadSize = radio.getPayloadSize();
    radio.read(buffer, payloadSize);
    Serial.write(buffer, payloadSize);
}

byte waitForByte() {
    while (Serial.available() == 0);
    return Serial.read();
}

void sendError(byte code) {
}

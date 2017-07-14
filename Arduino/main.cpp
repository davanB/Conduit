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

#define ERROR_INVALID_COMMAND 1

byte commandId = 0;

RF24 radio(9,10);
byte* payload = new byte[BUFFER_SIZE];

void setup() {
    Serial.begin(9600);

    radio.begin();
    radio.setPALevel(RF24_PA_LOW);
    radio.setDataRate(RF24_250KBPS);
}

void loop() {
    if (Serial.available()) {
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
    radio.openWritingPipe(address);
}

void openReadingPipe() {
    byte pipeNumber = waitForByte();
    byte address = waitForByte();
    radio.openReadingPipe(pipeNumber, address);
}

byte waitForByte() {
    while (Serial.available() == 0);
    return Serial.read();
}

void sendError(byte code) {
}

#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

#define COMMAND_HEADER 16

#define COMMAND_DEBUG_LED_BLINK 100
#define COMMAND_OPEN_WRITING_PIPE 125
#define COMMAND_OPEN_READING_PIPE 126
#define COMMAND_WRITE 127

#define ERROR_INVALID_COMMAND 1

byte commandId = 0;

void sendError(byte code) {
}

void debugLEDBlink() {
    byte numBlinks = Serial.read();
    while (numBlinks-- > 0) {
        digitalWrite(LED_BUILTIN, HIGH);
        delay(1000);
        digitalWrite(LED_BUILTIN, LOW);
        delay(1000);
    }
}

void processCommand() {
    commandId = Serial.read();
    switch(commandId) {
        case COMMAND_DEBUG_LED_BLINK:
            debugLEDBlink();
            break;
        case COMMAND_OPEN_WRITING_PIPE:
            break;
        case COMMAND_OPEN_READING_PIPE:
            break;
        case COMMAND_WRITE:
            break;
        default:
            sendError(ERROR_INVALID_COMMAND);
            break;
    }
}

void setup() {
    Serial.begin(9600);
    pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {
    if (Serial.available()) {
        if (COMMAND_HEADER == Serial.read()) {
            processCommand();
        }
    }
}

#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>
#include "main.h"
#include "Constants.h"
#include "SerialPacket.h"

#define NUM_READ_PIPES 5
#define BUFFER_SIZE 32

SerialPacket *inPacket;
byte commandId = 0;

RF24 radio(9,10);
byte* buffer = new byte[BUFFER_SIZE];

// map pipe number to addresses
uint8_t currentReadPipe = 0;
uint32_t addresses[NUM_READ_PIPES];

void setup() {
    Serial.begin(9600);
    inPacket = new SerialPacket();

    radio.begin();
    radio.setAddressWidth(4); // 4 bytes
    radio.setAutoAck(true);
    radio.enableAckPayload();
    radio.enableDynamicPayloads();
    // radio.setPALevel(RF24_PA_LOW);
    // radio.setDataRate(RF24_250KBPS);
    radio.setRetries(15,15);
}

void loop() {
    if (radio.available(&currentReadPipe)) {
        readRadio();
    } else if (Serial.available()) {
        // Clear inPacket and parse incoming data into it
        inPacket->reset();
        inPacket->readIncomingPacket();
        processCommand();
    }
}

void processCommand() {
    switch(inPacket->commandId) {
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
            writeRadio();
            break;
        default:
            sendError(COMMAND_READ, ERROR_INVALID_COMMAND);
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
    SerialPacket resp = SerialPacket(COMMAND_DEBUG_ECHO, STATUS_SUCCESS);
    memcpy(resp.payload, inPacket->payload, PACKET_SIZE);
    resp.write();
}

void openWritingPipe() {
    uint8_t address[4] = {inPacket->payload[3], inPacket->payload[2], inPacket->payload[1], inPacket->payload[0]};
    radio.openWritingPipe(address);
    SerialPacket packet = SerialPacket(COMMAND_OPEN_WRITING_PIPE, STATUS_SUCCESS);
    sprintf(packet.payload, "WP 0x%lx\n", *((uint32_t *) address));
    packet.write();
}

void openReadingPipe() {
    uint8_t pipeNumber = inPacket->payload[0];
    uint8_t address[4] = {inPacket->payload[4], inPacket->payload[3], inPacket->payload[2], inPacket->payload[1]};

    if (pipeNumber > NUM_READ_PIPES) {
        sendError(COMMAND_OPEN_READING_PIPE, ERROR_INVALID_ARGUMENT);
        return;
    }

    addresses[pipeNumber] = *((uint32_t *)address);
    radio.openReadingPipe(pipeNumber, address);
    radio.startListening();

    SerialPacket packet = SerialPacket(COMMAND_OPEN_READING_PIPE, STATUS_SUCCESS);
    sprintf(packet.payload, "RP 0x%lx\n", *((uint32_t *) address));
    packet.write();
}

void writeRadio() {
    radio.stopListening(); //TODO: Evaluate effect on dropped packets
    uint32_t start = micros();
    uint32_t delta = 0;
    uint16_t ack_buffer = 0;

    // Write buffer to radio
    if (radio.write(inPacket->payload, PAYLOAD_SIZE)) {
        if (radio.isAckPayloadAvailable()) {
            // Get ACK payload and stop timing
            radio.read(&ack_buffer, sizeof(uint16_t));
            delta = micros() - start;

            // Send ACK response and timing delta in response (Big Endian Formatted)
            SerialPacket packet = SerialPacket(COMMAND_WRITE, STATUS_SUCCESS);
            packet.payload[0] = (uint8_t) ((ack_buffer >> 8) & 0xFF);
            packet.payload[1] = (uint8_t) ((ack_buffer >> 0) & 0xFF);
            packet.payload[2] = (uint8_t) ((delta >> 24) & 0xFF);
            packet.payload[3] = (uint8_t) ((delta >> 16) & 0xFF);
            packet.payload[4] = (uint8_t) ((delta >> 8)  & 0xFF);
            packet.payload[5] = (uint8_t) ((delta >> 0)  & 0xFF);
            packet.write();

        } else {
            sendError(COMMAND_WRITE, ERROR_ACK_MISS);
        }
    } else {
        sendError(COMMAND_WRITE, ERROR_TX_FAIL);
    }
    radio.startListening(); //TODO: Evaluate effect on dropped packets
}

void readRadio() {
    uint16_t ack_buffer = 5;
    radio.writeAckPayload(currentReadPipe, &ack_buffer, sizeof(uint16_t));
    byte payloadSize = radio.getPayloadSize();

    SerialPacket packet = SerialPacket(COMMAND_READ, STATUS_SUCCESS);
    packet.source = (uint8_t) addresses[currentReadPipe]; //ADD LSB
    radio.read(packet.payload, payloadSize);
    packet.write();
}

byte waitForByte() {
    while (Serial.available() == 0);
    return Serial.read();
}

void sendError(uint8_t commandId, uint8_t errorCode) {
    SerialPacket packet = SerialPacket(commandId, STATUS_FAILURE);
    packet.payload[0] = errorCode;
    packet.write();
}

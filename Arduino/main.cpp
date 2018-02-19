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
    SerialPacket resp = SerialPacket(COMMAND_DEBUG_ECHO, 0);
    memcpy(resp.payload, inPacket->payload, PACKET_SIZE);
    resp.write();
}

void openWritingPipe() {
    uint8_t address[4] = {inPacket->payload[3], inPacket->payload[2], inPacket->payload[1], inPacket->payload[0]};
    radio.openWritingPipe(address);
    SerialPacket packet = SerialPacket(COMMAND_OPEN_WRITING_PIPE, 1);
    packet.payload[0] = STATUS_SUCCESS;
    sprintf(packet.payload+1, "WP 0x%lx\n", *((uint32_t *) address));
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

    SerialPacket packet = SerialPacket(COMMAND_OPEN_READING_PIPE, 1);
    packet.payload[0] = STATUS_SUCCESS;
    sprintf(packet.payload+1, "RP 0x%lx\n", *((uint32_t *) address));
    packet.write();
}

void writeRadio() {
    radio.stopListening(); //TODO: Evaluate effect on dropped packets
    int ack_buffer[1] = {5};
    // Write buffer to radio
    if (radio.write(inPacket->payload, PAYLOAD_SIZE)) {
        if (radio.isAckPayloadAvailable()) {
            radio.read(ack_buffer, sizeof(int));
            // Send ACK payload

            SerialPacket packet = SerialPacket(COMMAND_WRITE, 1 + sizeof(int));
            packet.payload[0] = STATUS_SUCCESS;
            // Put ACK payload in return buffer in Big-Endian order
            packet.payload[1] = ack_buffer[0] & 0xFF00;
            packet.payload[2] = ack_buffer[1] & 0x00FF;
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
    int ack_buffer[1] = {5};
    radio.writeAckPayload(1, ack_buffer, sizeof(int));
    byte payloadSize = radio.getPayloadSize();

    SerialPacket packet = SerialPacket(COMMAND_READ, payloadSize + 1);
    packet.source = (uint8_t) addresses[currentReadPipe]; //ADD LSB
    packet.payload[0] = STATUS_SUCCESS;
    radio.read(packet.payload + 1, payloadSize);
    packet.write();
}

byte waitForByte() {
    while (Serial.available() == 0);
    return Serial.read();
}

void sendError(byte commandId, byte errorCode) {
    SerialPacket packet = SerialPacket(commandId, 2);
    packet.payload[0] = STATUS_FAILURE;
    packet.payload[1] = errorCode;
    packet.write();
}

#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>
#include "main.h"
#include "Constants.h"
#include "SerialPacket.h"

#define BUFFER_SIZE 32

byte commandId = 0;

RF24 radio(9,10);
byte* buffer = new byte[BUFFER_SIZE];

void setup() {
    Serial.begin(9600);

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
    if (radio.available()) {
        readRadio();
    } else if (Serial.available()) {
        if (CONTROL_START_OF_PACKET == Serial.read()) {
            processCommand();
        }
    }
}

void processCommand() {
    commandId = waitForByte();

    // uint32_t size = Serial.parseInt();
    // 'read' size
    waitForByte();
    waitForByte();
    waitForByte();
    waitForByte();

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
    byte value = waitForByte();
    SerialPacket packet = SerialPacket(COMMAND_DEBUG_ECHO, 2);
    packet.payload[0] = STATUS_SUCCESS;
    packet.payload[1] = value;
    packet.write();
}

void openWritingPipe() {
    byte* address = new byte[4];
    Serial.readBytes(address, 4);
    radio.stopListening();
    radio.openWritingPipe((uint32_t)*address);

    SerialPacket packet = SerialPacket(COMMAND_OPEN_WRITING_PIPE, 1);
    packet.payload[0] = STATUS_SUCCESS;
    packet.write();
}

void openReadingPipe() {
    byte pipeNumber = waitForByte();
    byte* address = new byte[4];
    Serial.readBytes(address, 4);
    radio.openReadingPipe(pipeNumber, (uint32_t)*address);
    radio.startListening();

    SerialPacket packet = SerialPacket(COMMAND_OPEN_READING_PIPE, 1);
    packet.payload[0] = STATUS_SUCCESS;
    packet.write();
}

void write() {
    uint32_t i = 0;
    byte recvByte = 0;
    while(true) {
        // Fill buffer
        if (Serial.available()) {
            recvByte = Serial.read();
            buffer[i] = recvByte;
            if(buffer[i] == CONTROL_END_OF_TEXT){
                tx(i + 1); // Ensure we transmit the null terminator
                break;
            }
            i++;
        }

        if (i >= BUFFER_SIZE) {
            tx(BUFFER_SIZE);
            i = 0;
        }
    }

    SerialPacket packet = SerialPacket(COMMAND_WRITE, 1);
    packet.payload[0] = STATUS_SUCCESS;
    packet.write();
}

void tx(byte payloadSize) {
    radio.stopListening(); //TODO: Evaluate effect on dropped packets
    int ack_buffer[1] = {5};
    // Write buffer to radio
    if (radio.write(buffer, payloadSize)) {
        if (radio.isAckPayloadAvailable()) {
            radio.read(ack_buffer, sizeof(int));
            // Send ACK payload

            SerialPacket packet = SerialPacket(COMMAND_WRITE, 1 + sizeof(int));
            packet.payload[0] = STATUS_SUCCESS;
            // TODO: Figure out a better way to do this
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

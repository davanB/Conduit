#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>
#include "main.h"
#include "SerialPacket.cpp"

#define htonl(x) ( ((x)<<24 & 0xFF000000UL) | \
                   ((x)<< 8 & 0x00FF0000UL) | \
                   ((x)>> 8 & 0x0000FF00UL) | \
                   ((x)>>24 & 0x000000FFUL) )

#define CONTROL_START_OF_PACKET 1
#define CONTROL_START_OF_TEXT 2
#define CONTROL_END_OF_TEXT 3
#define CONTROL_END_OF_PACKET 4

#define COMMAND_DEBUG_LED_BLINK 33
#define COMMAND_DEBUG_ECHO 34
#define COMMAND_OPEN_WRITING_PIPE 40
#define COMMAND_OPEN_READING_PIPE 41
#define COMMAND_WRITE 42
#define COMMAND_READ 43

#define STATUS_SUCCESS 100
#define STATUS_FAILURE 101

#define ERROR_INVALID_COMMAND 1
#define ERROR_TX_FAIL 110
#define ERROR_ACK_MISS 111

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
    Serial.write(CONTROL_START_OF_PACKET);
    Serial.write(COMMAND_DEBUG_ECHO);
    // Serial.write((uint32_t) 2);
    uint32_t packetSize = htonl(2UL);
    Serial.write((byte*)&packetSize, sizeof(packetSize));
    Serial.write(STATUS_SUCCESS);
    Serial.write(value);
    Serial.write(CONTROL_END_OF_PACKET);
    Serial.flush();
}

void openWritingPipe() {
    byte* address = new byte[4];
    Serial.readBytes(address, 4);
    radio.stopListening();
    radio.openWritingPipe((uint32_t)*address);
    Serial.write(CONTROL_START_OF_PACKET);
    Serial.write(COMMAND_OPEN_WRITING_PIPE);
    Serial.write((uint32_t) 1);
    Serial.write(STATUS_SUCCESS);
    Serial.write(CONTROL_END_OF_PACKET);
    Serial.flush();
}

void openReadingPipe() {
    byte pipeNumber = waitForByte();
    byte* address = new byte[4];
    Serial.readBytes(address, 4);
    radio.openReadingPipe(pipeNumber, (uint32_t)*address);
    radio.startListening();
    Serial.write(CONTROL_START_OF_PACKET);
    Serial.write(COMMAND_OPEN_READING_PIPE);
    Serial.write((uint32_t) 1);
    Serial.write(STATUS_SUCCESS);
    Serial.write(CONTROL_END_OF_PACKET);
    Serial.flush();
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

    Serial.write(CONTROL_START_OF_PACKET);
    Serial.write(COMMAND_WRITE);
    Serial.write((uint32_t) 1);
    Serial.write(STATUS_SUCCESS);
    Serial.write(CONTROL_END_OF_PACKET);
}

void tx(byte payloadSize) {
    radio.stopListening(); //TODO: Evaluate effect on dropped packets
    int ack_buffer[1] = {5};
    // Write buffer to radio
    if (radio.write(buffer, payloadSize)) {
        if (radio.isAckPayloadAvailable()) {
            radio.read(ack_buffer, sizeof(int));
            // Send ACK payload
            Serial.write(CONTROL_START_OF_PACKET);
            Serial.write(COMMAND_WRITE);
            Serial.write(STATUS_SUCCESS);
            Serial.print(ack_buffer[0]);
            Serial.write(CONTROL_END_OF_PACKET);
            Serial.flush();
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
    radio.read(buffer, payloadSize);

    Serial.write(CONTROL_START_OF_PACKET);
    Serial.write(COMMAND_READ);
    Serial.write(STATUS_SUCCESS);
    Serial.write(buffer, payloadSize);
    Serial.write(CONTROL_END_OF_PACKET);
}

byte waitForByte() {
    while (Serial.available() == 0);
    return Serial.read();
}

void sendError(byte commandId, byte errorCode) {
    Serial.write(CONTROL_START_OF_PACKET);
    Serial.write(commandId);
    Serial.write(STATUS_FAILURE);
    Serial.write(errorCode);
    Serial.write(CONTROL_END_OF_PACKET);
    Serial.flush();
}

#include <SPI.h>
#include "Constants.h"
#include "SerialPacket.h"

SerialPacket::SerialPacket() {
    this->reset();
}

SerialPacket::SerialPacket(uint8_t command, uint8_t status) {
    // this->reset();
    this->commandId = command;
    this->status = status;
}

SerialPacket::~SerialPacket() {
}

void SerialPacket::reset() {
    this->commandId = 0;
    this->status = 0;
    this->source = 0;
    memset(this->payload, 0, PAYLOAD_SIZE);
    this->payloadPos = 0;
}

void SerialPacket::readIncomingPacket() {
    this->commandId = SerialPacket::waitForByte();
    this->status = SerialPacket::waitForByte();
    this->source = SerialPacket::waitForByte();
    for (uint8_t i = 0; i < PAYLOAD_SIZE; i++) {
        this->payload[i] = SerialPacket::waitForByte();
    }
}

void SerialPacket::write() {
    Serial.write(this->commandId);
    Serial.write(this->status);
    Serial.write(this->source);
    Serial.write(this->payload, PAYLOAD_SIZE);
    Serial.flush();
}

static inline uint8_t SerialPacket::waitForByte() {
    while (Serial.available() == 0);
    return Serial.read();
}

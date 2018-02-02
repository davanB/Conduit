#include <SPI.h>
#include "Constants.h"
#include "SerialPacket.h"

SerialPacket::SerialPacket(uint8_t command, uint32_t size) {
    this->commandId = command;
    this->payload = new uint8_t[size]();
    this->payloadSize = size;
}

SerialPacket::~SerialPacket() {
    delete[] this->payload;
}

void SerialPacket::write() {
    Serial.write(CONTROL_START_OF_PACKET);
    Serial.write(this->commandId);

    // Convert packet size to big endian and write
    Serial.write(this->payloadSize & 0xFF000000UL);
    Serial.write(this->payloadSize & 0x00FF0000UL);
    Serial.write(this->payloadSize & 0x0000FF00UL);
    Serial.write(this->payloadSize & 0x000000FFUL);

    Serial.write(this->payload, this->payloadSize);
    Serial.write(CONTROL_END_OF_PACKET);
    Serial.flush();
}

#ifndef SERIALPACKET_H
#define SERIALPACKET_H

#include <stdint.h>
#include <HardwareSerial.h>

#define HEADER_SIZE (1 + 1 + 1) // COMMAND_ID, STATUS, SOURCE

#define INDEX_HEADER 0
#define INDEX_COMMAND (INDEX_HEADER)
#define INDEX_STATUS (INDEX_COMMAND + 1)
#define INDEX_SOURCE (INDEX_STATUS + 1)
#define INDEX_PAYLOAD (INDEX_SOURCE + 1)

#define PAYLOAD_SIZE 32
#define PACKET_SIZE (HEADER_SIZE + PAYLOAD_SIZE)

class SerialPacket {
    public:
        SerialPacket();
        SerialPacket(uint8_t command, uint8_t status);
        ~SerialPacket();
        void reset();
        void readIncomingPacket();
        void write();

        uint8_t commandId;
        uint8_t status;
        uint8_t source;
        uint8_t payload[PAYLOAD_SIZE];

        static void test(HardwareSerial serial, SerialPacket packet);

    private:
        static inline uint8_t waitForByte();
};

#endif /* SERIALPACKET_H */

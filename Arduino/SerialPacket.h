#ifndef SERIALPACKET_H
#define SERIALPACKET_H

#include <stdint.h>

class SerialPacket {
    public:
        SerialPacket(uint8_t command, uint32_t size);
        ~SerialPacket();
        void write();
        uint8_t *payload;

    private:
        uint8_t commandId;
        uint32_t payloadSize;
};

#endif /* SERIALPACKET_H */
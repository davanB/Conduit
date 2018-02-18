#ifndef CONSTANTS_H
#define CONSTANTS_H

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

#endif /* CONSTANTS_H */
byte waitForByte();
void sendError(byte commandId, byte errorCode);
void debugLEDBlink();
void debugEcho();
void openWritingPipe();
void openReadingPipe();
void writeRadio();
void readRadio();
void processCommand();

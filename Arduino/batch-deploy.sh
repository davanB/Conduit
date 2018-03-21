for f in /dev/cu.usbmodem*; do
  # echo "File -> $f"
    export ARDUINO_PORT=$f
    export MONITOR_PORT=$f
    export BOARD_TAG=uno
    make upload
done

for f in /dev/cu.usbserial*; do
  # echo "File -> $f"
    export ARDUINO_PORT=$f
    export MONITOR_PORT=$f
    export BOARD_TAG=diecimila
    export BOARD_SUB=atmega328
    make upload
done

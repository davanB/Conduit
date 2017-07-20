# ls /dev/cu.usbmodem*

export ARDUINO_PORT=/dev/cu.usbmodem1421
export MONITOR_PORT=/dev/cu.usbmodem1421
export BOARD_TAG=uno

# make upload

for f in /dev/cu.usbmodem*; do
  # echo "File -> $f"
    export ARDUINO_PORT=$f
    export MONITOR_PORT=$f
    export BOARD_TAG=uno
    make upload
done

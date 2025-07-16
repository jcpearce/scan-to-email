#!/bin/bash

# Get the current working directory where the script is executed
BASE_DIR="$(pwd)"
TEMP_DIR="$BASE_DIR/tempimages"

# Inputs
NAPS2="/Applications/NAPS2.app/Contents/MacOS/NAPS2"
DEVICE="fi-7260"
DRIVER="sane"
SOURCE="$1"       # e.g., "ADF Duplex"
BITDEPTH="$2"     # e.g., "color"
OUTPUT="$TEMP_DIR/$3"
DPI=200

# Create output directory
mkdir -p "$TEMP_DIR"

# Run scan
"$NAPS2" console --force --deskew --device "$DEVICE" --driver "$DRIVER" --source "$SOURCE" --dpi $DPI --verbose --bitdepth "$BITDEPTH" --output "$OUTPUT" >stdout.txt 2>stderr.txt
EXIT_CODE=$?
echo $EXIT_CODE > exit_code.txt

# Look for typical error indicators
ERROR_FOUND=0
if grep -iqE "could not be found|error|fail|unable|not available|not detected" stdout.txt stderr.txt; then
    ERROR_FOUND=1
fi

# Show warning if needed
if [ $EXIT_CODE -ne 0 ] || [ $ERROR_FOUND -ne 0 ]; then
    cat stdout.txt > combined_output.txt
    echo --- Error Messages --- >> combined_output.txt
    cat stderr.txt >> combined_output.txt
    osascript -e "display dialog \"$(cat combined_output.txt | tr -d '\r' | head -c 1000)\" buttons {\"OK\"} default button 1 with title \"NAPS2 Scan Error\""
    echo "Program failed or error message detected."
else
    echo "Program exited normally"
fi

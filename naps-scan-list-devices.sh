#!/bin/bash
alias naps2.console="/Applications/NAPS2.app/Contents/MacOS/NAPS2 console"
echo "Passed parameters to the echo command:"
echo "$@"
/Applications/NAPS2.app/Contents/MacOS/NAPS2 console  --listdevices --driver apple

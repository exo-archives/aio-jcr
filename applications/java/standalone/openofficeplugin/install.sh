#!/bin/bash
PATH="/opt/openoffice.org2.0/program":$PATH
killall soffice.bin
unopkg remove exo-oo-addon-1.7.zip
unopkg add "exo-oo-addon-1.7.zip"
swriter

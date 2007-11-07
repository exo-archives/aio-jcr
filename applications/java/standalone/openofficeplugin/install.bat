cd bin
path="C:\Program Files\OpenOffice.org 2.0.2\program";%path%
taskkill /F /IM soffice.bin
unopkg.exe remove exo-oo-addon-1.7.zip
unopkg.exe add "exo-oo-addon-1.7.zip"
"C:\Program Files\OpenOffice.org 2.0.2\program\swriter.exe"
cd ..
pause null

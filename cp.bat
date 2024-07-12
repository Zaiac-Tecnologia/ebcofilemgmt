
del /f S:\fs_01\hosts_share\ebcofilemgmt\*.jar
del /f S:\fs_01\hosts_share\ebcofilemgmt\lib\*.*
copy /y target\*.jar S:\fs_01\hosts_share\ebcofilemgmt
copy /y target\lib\*.jar S:\fs_01\hosts_share\ebcofilemgmt\lib

xcopy /s /e * C:\EBCOSERVICES\ebcofilemgmt /y

if exist "C:\EBCOSERVICES\ebcofilemgmt\logs" (
  echo Yes 
) else (
  mkdir "C:\EBCOSERVICES\ebcofilemgmt\logs"
)

if exist "C:\EBCOSERVICES\ebcofilemgmt\queue" (
  echo Yes 
) else (
  mkdir "C:\EBCOSERVICES\ebcofilemgmt\queue"
)

if exist "C:\EBCOSERVICES\ebcofilemgmt\missing" (
  echo Yes 
) else (
  mkdir "C:\EBCOSERVICES\ebcofilemgmt\missing"
)

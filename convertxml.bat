@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.14
set APP_HOME=C:\Users\milto\Projetos\Ebco\ebcofilemgmt\target
set APP=ebcofilemgmt-1.0.jar
set CLASSPATH=%APP_HOME%\lib;%APP_HOME%\%APP%
cd "%APP_HOME%"
"%JAVA_HOME%\bin\java" -jar %APP% -t convertxml -o %1


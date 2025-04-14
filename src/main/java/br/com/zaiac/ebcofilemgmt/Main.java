package br.com.zaiac.ebcofilemgmt;

import br.com.zaiac.ebcofilemgmt.cryptography.AsymmetricCryptography;
import br.com.zaiac.ebcofilemgmt.cryptography.GenerateKeys;
import br.com.zaiac.ebcofilemgmt.exception.ProcessIncompleteException;
import br.com.zaiac.ebcofilemgmt.model.GoogleCredentials;
import br.com.zaiac.ebcofilemgmt.model.SftpCredentials;
import br.com.zaiac.ebcofilemgmt.tools.Image;
import br.com.zaiac.ebcofilemgmt.tools.MergeFiles;
import br.com.zaiac.ebcofilemgmt.tools.Monitor;
import br.com.zaiac.ebcofilemgmt.tools.SendFiles;
import br.com.zaiac.ebcofilemgmt.xml.ConvertXmlFile;
import br.com.zaiac.ebcolibrary.ConfigProperties;
import br.com.zaiac.ebcolibrary.ConvertXML;
import br.com.zaiac.ebcolibrary.Util;
import br.com.zaiac.ebcolibrary.exceptions.WriteLogFileException;
import br.com.zaiac.ebcolibrary.xml.smiths.DataForm;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ?+------------------------------------------------------------------------+
 * ?|all.bat*****************************************************************|
 * *|******Faz o processo completo criar .ebco gerar o pie e enviar**********|
 * ?|convertjpegtotif.bat****************************************************|
 * *|******Converte o arquivo .jpg para .tif*********************************|
 * ?|convertjson.bat*********************************************************|
 * *|******Converte o arquivo .xml para .json********************************|
 * ?|convertxml.bat**********************************************************|
 * *|******Converte o arquivo .xml para CARGO********************************|
 * ?|encript.bat*************************************************************|
 * *|******Gera o arquivo .pie***********************************************|
 * ?|enqueue.bat*************************************************************|
 * *|******Enfileira para enviar*********************************************|
 * ?|enqueueWithPriority.bat*************************************************|
 * ?|ia.bat******************************************************************|
 * *|******Envia para o IA Local*********************************************|
 * ?|merge.bat***************************************************************|
 * *|******Gerar o arquivo .ebco ********************************************|
 * ?|monitor.bat*************************************************************|
 * *|******Envia informações para o backend**********************************|
 * ?|movefiles.bat***********************************************************|
 * ?|queue.bat***************************************************************|
 * *|******Envia os arquivos enfileirados************************************|
 * ?|sendpie.bat*************************************************************|
 * *|******Envia o arquivo .pie para a Cloud da Google***********************|
 * ?|version.bat*************************************************************|
 * *|Mostra a versão do Programa*********************************************|
 * ?+------------------------------------------------------------------------+
 */

public class Main {
    private static final String version = "1.4";

    public static void main(String[] args) throws IOException {
        int i;
        String type = "";
        String operation = "";
        Integer priority = 1;
        String username = null;
        String password = null;

        Boolean isType = false;
        Boolean isOperation = false;
        Boolean isPriority = false;
        Boolean isUsername = false;
        Boolean isPassword = false;

        String baseDir;
        String urlIaLocal;
        String keyDir;
        String gcProject;
        String gcsPie;
        String moveDir;
        String missingDirectory;
        String GoogleApplicationCredentials;
        String scanner = "NENHUM";
        String siteDestination;
        String siteSFTPDestination;
        String siteSFTPUsername;
        String siteSFTPPassword;
        String algoritimo;
        Integer siteSFTPPort;
        Boolean debugMode;
        String xmlConverter;
        String ebcorquestrator_directory;

        Boolean iaLocalAvailable = false;

        for (i = 0; i < args.length; i++) {
            if (isType)
                type = args[i];
            if (isOperation)
                operation = args[i];
            if (isPriority)
                priority = Integer.valueOf(args[i]);
            if (isUsername)
                username = args[i];
            if (isPassword)
                password = args[i];

            isType = false;
            isOperation = false;
            isPriority = false;
            isUsername = false;
            isPassword = false;

            if (args[i].equalsIgnoreCase("-t"))
                isType = true;
            if (args[i].equalsIgnoreCase("-o"))
                isOperation = true;
            if (args[i].equalsIgnoreCase("-p"))
                isPriority = true;
            if (args[i].equalsIgnoreCase("-u"))
                isUsername = true;
            if (args[i].equalsIgnoreCase("-pw"))
                isPassword = true;
        }

        File f = new File("config.properties");
        try {
            ConfigProperties.loadFile(f);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        if (type.equals("config")) {
            ebcorquestrator_directory = ConfigProperties.getPropertyValue("EBCORQUESTRATOR_DIRECTORY");
            if (username != null || password != null) {
                if (username == null) {
                    System.out.println("Invalid username");
                    System.exit(1);
                }
                if (password == null) {
                    System.out.println("Invalid password");
                    System.exit(1);
                }
                if (ebcorquestrator_directory == null || ebcorquestrator_directory.isEmpty()) {
                    System.out.println("Invalid ebcorquestrator_directory");
                    System.exit(1);
                }
                File fEbcorquestrator_directory = new File(ebcorquestrator_directory, "config.properties");
                try {
                    ConfigProperties.loadFile(fEbcorquestrator_directory);
                } catch (FileNotFoundException e) {
                    System.out.println(
                            String.format("Invalid %s file or directory",
                                    fEbcorquestrator_directory.getAbsolutePath()));
                    System.exit(1);
                } catch (IOException e) {
                    System.out.println(
                            String.format("Invalid %s file or directory",
                                    fEbcorquestrator_directory.getAbsolutePath()));
                    System.exit(1);
                }
                ConfigProperties.setPropertyValue("API_MANIFEST_USERNAME", Util.convertStringToBase64(username));
                ConfigProperties.setPropertyValue("API_MANIFEST_PASSWORD", Util.convertStringToBase64(password));
                try {
                    ConfigProperties.writeFile();
                } catch (FileNotFoundException e) {
                    System.out.println("Error writing file");
                    System.exit(1);
                } catch (IOException e) {
                    System.out.println("Error writing file");
                    System.exit(1);
                }
                System.out.println(
                        String.format("File %s updated sucessfully", fEbcorquestrator_directory.getAbsolutePath()));
                System.exit(0);
            }

        }

        Monitor.diskMonitor = ConfigProperties.getPropertyValue("DISK_MONITOR");
        Monitor.pingSite = Integer.valueOf(ConfigProperties.getPropertyValue("PING"));
        Monitor.siteId = ConfigProperties.getPropertyValue("SITE");
        Monitor.siteId = ConfigProperties.getPropertyValue("SITE");
        Monitor.urlBackEnd = ConfigProperties.getPropertyValue("URL_BACKEND");
        Monitor.sourceSite = ConfigProperties.getPropertyValue("SOURCE_SITE");

        scanner = ConfigProperties.getPropertyValue("SCANNER");
        if (scanner == null || scanner.isEmpty()) {
            System.out.println("Invalid scanner");
            System.exit(1);
        } else if ((scanner.equalsIgnoreCase("SMITHS")) || (scanner.equalsIgnoreCase("NUCHTECH"))) {
        } else {
            System.out.println("Invalid scanner");
            System.exit(1);
        }

        algoritimo = ConfigProperties.getPropertyValue("ALGORITIMO");
        if (scanner == null || scanner.isEmpty()) {
            System.out.println("Invalid algoritimo");
            System.exit(1);
        } else if (!("SANTOSBRASIL.JBS".contains(algoritimo))) {
            System.out.println("Invalid algorithm");
            System.exit(1);
        }

        try {
            debugMode = Boolean.valueOf(ConfigProperties.getPropertyValue("DEBUG_MODE"));
            if (debugMode) {
                System.out.println("DEBUG_MODE Ligado");
            } else {
                System.out.println("DEBUG_MODE Desligado");
            }
        } catch (Exception e) {
            debugMode = false;
            System.out.println("DEBUG_MODE Desligado");
        }

        try {
            xmlConverter = ConfigProperties.getPropertyValue("XMLCONVERTER");
            if (xmlConverter.equalsIgnoreCase("NONE") || xmlConverter.equalsIgnoreCase("CARGO")) {
            } else {
                System.out.println("XML Converter Incorrect");
                System.exit(1);
            }
        } catch (Exception e) {
            xmlConverter = "NONE";
        }

        MergeFiles.debugMode = debugMode;
        MergeFiles.xmlConverter = xmlConverter;

        SendFiles.debugMode = debugMode;
        Monitor.debugMode = debugMode;

        switch (type.toLowerCase()) {
            case "create":
                switch (operation.toLowerCase()) {
                    case "certificates":
                        GenerateKeys.createCertificates();
                        break;
                    default:
                        System.out.println("Nenhuma opção para create especificada");
                }
                break;
            case "monitor":
                Monitor.sendInformationToBackEnd(true);
                break;
            case "version":
                System.out.println("Version: " + version);
                break;
            case "convertjson":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                if (algoritimo.equals("SANTOSBRASIL")) {
                    DataForm dataForm = ConvertXML.convertSmitsXmlToObject(
                            baseDir + "\\" + operation + "\\",
                            operation + ".xml");
                    br.com.zaiac.ebcolibrary.json.smiths.santosbrasil.DataForm jsonObject = ConvertXML
                            .createFaseObjectAlgoritimoSANTOSBRASIL(dataForm);
                    String json = ConvertXML.convertObjectToJson(jsonObject);
                    ConvertXML.saveJsonToFile(baseDir + "\\" + operation + "\\", operation + "F1.json", json);
                } else {
                    System.out.println("Invalid algorithm");
                    System.exit(1);
                }
                break;
            case "convertxml":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                if (xmlConverter.equalsIgnoreCase("CARGO")) {
                    ConvertXmlFile.convertXmlFileCargo(baseDir, operation);
                }
                break;
            case "all":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                gcProject = ConfigProperties.getPropertyValue("GC_PROJECT");
                gcsPie = ConfigProperties.getPropertyValue("GCS_PIE");
                keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");
                moveDir = ConfigProperties.getPropertyValue("MOVE_DIRECTORY");
                urlIaLocal = ConfigProperties.getPropertyValue("URL_IA_LOCAL");
                iaLocalAvailable = Boolean.getBoolean(ConfigProperties.getPropertyValue("IA_LOCAL_AVAILABLE"));
                GoogleApplicationCredentials = ConfigProperties.getPropertyValue("GOOGLE_APPLICATION_CREDENTIALS");
                // scanner = ConfigProperties.getPropertyValue("SCANNER");

                Monitor.sendInformationToBackEnd(false);

                try {
                    Image.getImageCheioVazio(baseDir, urlIaLocal, iaLocalAvailable, operation);
                    MergeFiles.merge(baseDir, operation, scanner);
                    AsymmetricCryptography.encryptFile(baseDir, keyDir, operation);
                    SendFiles.uploadObject(
                            gcProject,
                            gcsPie,
                            baseDir,
                            operation,
                            keyDir,
                            GoogleApplicationCredentials);
                    SendFiles.moveFiles(baseDir, moveDir, operation, scanner);
                } catch (ProcessIncompleteException | WriteLogFileException e) {
                }
                break;
            case "queue":
                siteDestination = ConfigProperties.getPropertyValue("SITE_DESTINATION");
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                moveDir = ConfigProperties.getPropertyValue("MOVE_DIRECTORY");
                missingDirectory = ConfigProperties.getPropertyValue("MISSING_DIRECTORY");
                scanner = ConfigProperties.getPropertyValue("SCANNER");

                Monitor.sendInformationToBackEnd(false);

                if (siteDestination.equalsIgnoreCase("EBCO")) {
                    ArrayList<SftpCredentials> sftpCredentials = new ArrayList<>();
                    sftpCredentials.add(
                            new SftpCredentials(
                                    0,
                                    ConfigProperties.getPropertyValue("SITE_SFTP_DESTINATION"),
                                    Integer.valueOf(ConfigProperties.getPropertyValue("SITE_SFTP_PORT")),
                                    ConfigProperties.getPropertyValue("SITE_SFTP_USERNAME"),
                                    ConfigProperties.getPropertyValue("SITE_SFTP_PASSWORD")));
                    for (int pri = 1; pri < 10; pri++) {
                        if (ConfigProperties.getPropertyValue("SITE_SFTP_DESTINATION_PRIORITY_" + pri) != null) {
                            sftpCredentials.add(
                                    new SftpCredentials(
                                            pri,
                                            ConfigProperties.getPropertyValue("SITE_SFTP_DESTINATION_PRIORITY_" + pri),
                                            Integer.valueOf(
                                                    ConfigProperties
                                                            .getPropertyValue("SITE_SFTP_PORT_PRIORITY_" + pri)),
                                            ConfigProperties.getPropertyValue("SITE_SFTP_USERNAME_PRIORITY_" + pri),
                                            ConfigProperties.getPropertyValue("SITE_SFTP_PASSWORD_PRIORITY_" + pri)));
                        }
                    }
                    iaLocalAvailable = false;
                    urlIaLocal = null;
                    MergeFiles.queue(
                            baseDir,
                            moveDir,
                            missingDirectory,
                            false,
                            null,
                            sftpCredentials,
                            null,
                            scanner);
                } else {
                    GoogleCredentials googleCredentials = new GoogleCredentials(
                            ConfigProperties.getPropertyValue("GC_PROJECT"),
                            ConfigProperties.getPropertyValue("GCS_PIE"),
                            ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY"),
                            ConfigProperties.getPropertyValue("GOOGLE_APPLICATION_CREDENTIALS"));
                    iaLocalAvailable = Boolean.getBoolean(ConfigProperties.getPropertyValue("IA_LOCAL_AVAILABLE"));
                    urlIaLocal = ConfigProperties.getPropertyValue("URL_IA_LOCAL");
                    MergeFiles.queue(
                            baseDir,
                            moveDir,
                            missingDirectory,
                            iaLocalAvailable,
                            urlIaLocal,
                            null,
                            googleCredentials,
                            scanner);
                }
                break;
            case "merge":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                scanner = ConfigProperties.getPropertyValue("SCANNER");
                try {
                    MergeFiles.merge(baseDir, operation, scanner);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "movefiles":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                moveDir = ConfigProperties.getPropertyValue("MOVE_DIRECTORY");
                scanner = ConfigProperties.getPropertyValue("SCANNER");
                try {
                    SendFiles.moveFiles(baseDir, moveDir, operation, scanner);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "enqueue":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                Monitor.sendInformationToBackEnd(false);

                try {
                    MergeFiles.enqueue(baseDir, operation);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "enqueue-priority":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                Monitor.sendInformationToBackEnd(false);

                try {
                    MergeFiles.enqueue(baseDir, priority, operation);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "split":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                try {
                    MergeFiles.split(baseDir, operation);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "encript":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");
                try {
                    AsymmetricCryptography.encryptFile(baseDir, keyDir, operation);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "decript":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");
                try {
                    AsymmetricCryptography.decryptFile(baseDir, keyDir, operation);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "sendpie":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                gcProject = ConfigProperties.getPropertyValue("GC_PROJECT");
                gcsPie = ConfigProperties.getPropertyValue("GCS_PIE");
                keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");

                GoogleApplicationCredentials = ConfigProperties.getPropertyValue("GOOGLE_APPLICATION_CREDENTIALS");
                try {
                    SendFiles.uploadObject(
                            gcProject,
                            gcsPie,
                            baseDir,
                            operation,
                            keyDir,
                            GoogleApplicationCredentials);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "convertjpegtotiff":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                Image.convertJpegRgbToTiff(baseDir, operation, operation + "S");
                break;
            case "converttifftojpeg":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                Image.convertTiffToJpeg(baseDir, operation);
                break;
            case "ia_local":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                urlIaLocal = ConfigProperties.getPropertyValue("URL_IA_LOCAL");
                try {
                    Image.getImageCheioVazio(baseDir, urlIaLocal, iaLocalAvailable, operation);
                } catch (WriteLogFileException e) {
                }
                break;
            case "missing":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                urlIaLocal = ConfigProperties.getPropertyValue("URL_IA_LOCAL");
                try {
                    MergeFiles.checkStepMissingFile(baseDir, operation);
                } catch (ProcessIncompleteException e) {
                }
                break;
            case "sftp":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                siteDestination = ConfigProperties.getPropertyValue("SITE_DESTINATION");
                siteSFTPDestination = ConfigProperties.getPropertyValue("SITE_SFTP_DESTINATION");
                siteSFTPUsername = ConfigProperties.getPropertyValue("SITE_SFTP_USERNAME");
                siteSFTPPassword = ConfigProperties.getPropertyValue("SITE_SFTP_PASSWORD");
                siteSFTPPort = Integer.parseInt(ConfigProperties.getPropertyValue("SITE_SFTP_PORT"));
                try {
                    SendFiles.sftp(
                            baseDir,
                            siteDestination,
                            siteSFTPDestination,
                            siteSFTPPort,
                            siteSFTPUsername,
                            siteSFTPPassword,
                            operation);
                } catch (ProcessIncompleteException e) {
                }
                break;
            default:
                System.out.println("Nothing to do");
        }
        System.exit(0);
    }
}

package br.com.zaiac.ebcofilemgmt;

import br.com.zaiac.ebcofilemgmt.cryptography.AsymmetricCryptography;
import br.com.zaiac.ebcofilemgmt.cryptography.GenerateKeys;
import br.com.zaiac.ebcofilemgmt.exception.ProcessIncompleteException;
import br.com.zaiac.ebcofilemgmt.tools.Image;
import br.com.zaiac.ebcofilemgmt.tools.MergeFiles;
import br.com.zaiac.ebcofilemgmt.tools.Monitor;
import br.com.zaiac.ebcofilemgmt.tools.SendFiles;
import br.com.zaiac.ebcofilemgmt.xml.ConvertXmlFile;
import br.com.zaiac.ebcolibrary.ConfigProperties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws IOException {
        int i;
        String type = "";
        String operation = "";
        
        Boolean isType = false;
        Boolean isOperation = false;
        String baseDir;
        String urlIaLocal;
        String keyDir;
        String gcProject;
        String gcsPie;
        String moveDir;
        String missingDirectory;
        String GoogleApplicationCredentials;
        String siteDestination;
        String siteSFTPDestination;
        String siteSFTPUsername;
        String siteSFTPPassword;
        Integer siteSFTPPort;
        Boolean debugMode;
        
        Boolean iaLocalAvailable;
        
        for (i = 0; i < args.length; i++) {
            if (isType) type = args[i];
            if (isOperation) operation = args[i];
            
            isType = false;
            isOperation = false;
            
            if (args[i].equalsIgnoreCase("-t")) isType = true;
            if (args[i].equalsIgnoreCase("-o")) isOperation = true;
        }
        
        File f = new File("config.properties");
        try {
            ConfigProperties.loadFile(f);
        } catch (FileNotFoundException e) {
            
        } catch (IOException e) {
            
        }
        
        Monitor.diskMonitor = ConfigProperties.getPropertyValue("DISK_MONITOR");
        Monitor.pingSite = Integer.parseInt(ConfigProperties.getPropertyValue("PING"));                
        Monitor.siteId = ConfigProperties.getPropertyValue("SITE");
        Monitor.siteId = ConfigProperties.getPropertyValue("SITE");
        Monitor.urlBackEnd = ConfigProperties.getPropertyValue("URL_BACKEND");
        Monitor.sourceSite = ConfigProperties.getPropertyValue("SOURCE_SITE");
        
        
        try {
            debugMode = Boolean.parseBoolean(ConfigProperties.getPropertyValue("DEBUG_MODE"));
            if (debugMode) {
                System.out.println("DEBUG_MODE Ligado");
            } else {
                System.out.println("DEBUG_MODE Desligado");
            }
        } catch(Exception e) {
            debugMode = false;
            System.out.println("DEBUG_MODE Desligado");
        }
        
        MergeFiles.debugMode = debugMode;
        SendFiles.debugMode = debugMode;
        Monitor.debugMode = debugMode;
        
        
        
        switch(type.toLowerCase()) {
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
                
            case "convertxml":
                ConvertXmlFile.convertXmlFile();
                break;
                
            case "all":
                
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                gcProject = ConfigProperties.getPropertyValue("GC_PROJECT");
                gcsPie = ConfigProperties.getPropertyValue("GCS_PIE");
                keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");
                moveDir = ConfigProperties.getPropertyValue("MOVE_DIRECTORY");
                urlIaLocal = ConfigProperties.getPropertyValue("URL_IA_LOCAL");
                GoogleApplicationCredentials = ConfigProperties.getPropertyValue("GOOGLE_APPLICATION_CREDENTIALS");
                
                Monitor.sendInformationToBackEnd(false);
                    

                try {
                    Image.getImageCheioVazio(baseDir, urlIaLocal, operation);
                    MergeFiles.merge(baseDir, operation);                
                    AsymmetricCryptography.encryptFile(baseDir, keyDir, operation);
                    SendFiles.uploadObject(gcProject, gcsPie, baseDir, operation, keyDir, GoogleApplicationCredentials);
                    SendFiles.moveFiles(baseDir, moveDir, operation);
                } catch (ProcessIncompleteException e) {}
                break;
                
            case "queue":
                
                siteDestination = ConfigProperties.getPropertyValue("SITE_DESTINATION");
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                moveDir = ConfigProperties.getPropertyValue("MOVE_DIRECTORY");
                missingDirectory = ConfigProperties.getPropertyValue("MISSING_DIRECTORY");
                
                Monitor.sendInformationToBackEnd(false);
                
                if (siteDestination.equalsIgnoreCase("EBCO")) {
                    siteSFTPDestination = ConfigProperties.getPropertyValue("SITE_SFTP_DESTINATION");
                    siteSFTPUsername = ConfigProperties.getPropertyValue("SITE_SFTP_USERNAME");
                    siteSFTPPassword = ConfigProperties.getPropertyValue("SITE_SFTP_PASSWORD");
                    siteSFTPPort = Integer.parseInt(ConfigProperties.getPropertyValue("SITE_SFTP_PORT"));
                    try {
                        SendFiles.queue(baseDir, moveDir, missingDirectory, siteDestination, siteSFTPDestination, siteSFTPPort, siteSFTPUsername, siteSFTPPassword);
                    } catch (ProcessIncompleteException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {                    
                    gcProject = ConfigProperties.getPropertyValue("GC_PROJECT");
                    gcsPie = ConfigProperties.getPropertyValue("GCS_PIE");
                    keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");                     
                    urlIaLocal = ConfigProperties.getPropertyValue("URL_IA_LOCAL");                    
                    GoogleApplicationCredentials = ConfigProperties.getPropertyValue("GOOGLE_APPLICATION_CREDENTIALS");  
                    iaLocalAvailable = Boolean.getBoolean(ConfigProperties.getPropertyValue("IA_LOCAL_AVAILABLE"));
                    MergeFiles.queue(baseDir, moveDir, missingDirectory, iaLocalAvailable, gcProject, gcsPie, keyDir, GoogleApplicationCredentials, urlIaLocal);                                    
                }
                break;
            case "merge":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                try {
                    MergeFiles.merge(baseDir, operation);
                } catch (ProcessIncompleteException e) {}                    
                break;
            case "movefiles":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                moveDir = ConfigProperties.getPropertyValue("MOVE_DIRECTORY");
                try {
                    SendFiles.moveFiles(baseDir, moveDir, operation);
                } catch (ProcessIncompleteException e) {}                    
                break;
                
            case "enqueue":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                Monitor.sendInformationToBackEnd(false);
                
                try {
                    MergeFiles.enqueue(baseDir, operation);
                } catch (ProcessIncompleteException e) {}
                break;
            case "split":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                try {
                    MergeFiles.split(baseDir, operation);
                } catch (ProcessIncompleteException e) {}    
                break;

            case "encript":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");
                try {
                    AsymmetricCryptography.encryptFile(baseDir, keyDir, operation);
                } catch (ProcessIncompleteException e) {}        
                break;
                
            case "decript":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");
                try {
                    AsymmetricCryptography.decryptFile(baseDir, keyDir, operation);
                } catch (ProcessIncompleteException e) {}    
                break;
            
            case "sendpie":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                gcProject = ConfigProperties.getPropertyValue("GC_PROJECT");
                gcsPie = ConfigProperties.getPropertyValue("GCS_PIE");
                keyDir = ConfigProperties.getPropertyValue("KEYPAIR_DIRECTORY");
                
                GoogleApplicationCredentials = ConfigProperties.getPropertyValue("GOOGLE_APPLICATION_CREDENTIALS");
                try {
                    SendFiles.uploadObject(gcProject, gcsPie, baseDir, operation, keyDir, GoogleApplicationCredentials);
                } catch (ProcessIncompleteException e) {}    
                break;
                
            case "converttifftojpeg":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                Image.convertTiffToJpeg(baseDir, operation);
                break;
            case "ia_local":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                urlIaLocal = ConfigProperties.getPropertyValue("URL_IA_LOCAL");
                Image.getImageCheioVazio(baseDir, urlIaLocal, operation);
                break;
            case "missing":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                urlIaLocal = ConfigProperties.getPropertyValue("URL_IA_LOCAL");
                try {
                    MergeFiles.checkStepMissingFile(baseDir, operation);
                } catch (ProcessIncompleteException e) {}    
                break;
            case "sftp":
                baseDir = ConfigProperties.getPropertyValue("BASE_DIRECTORY");
                siteDestination = ConfigProperties.getPropertyValue("SITE_DESTINATION");
                siteSFTPDestination = ConfigProperties.getPropertyValue("SITE_SFTP_DESTINATION");
                siteSFTPUsername = ConfigProperties.getPropertyValue("SITE_SFTP_USERNAME");
                siteSFTPPassword = ConfigProperties.getPropertyValue("SITE_SFTP_PASSWORD");
                siteSFTPPort = Integer.parseInt(ConfigProperties.getPropertyValue("SITE_SFTP_PORT"));
                try {
                    SendFiles.sftp(baseDir, siteDestination, siteSFTPDestination, siteSFTPPort, siteSFTPUsername, siteSFTPPassword, operation);
                } catch (ProcessIncompleteException e) {}
                break;
            default:
                System.out.println("Nothing to do");
        }
        System.exit(0);
        
    }
    
}

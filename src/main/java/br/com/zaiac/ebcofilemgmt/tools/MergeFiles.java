package br.com.zaiac.ebcofilemgmt.tools;

import br.com.zaiac.ebcofilemgmt.cryptography.AsymmetricCryptography;
import br.com.zaiac.ebcofilemgmt.exception.ProcessIncompleteException;
import br.com.zaiac.ebcofilemgmt.xml.ConvertXmlFile;
import br.com.zaiac.ebcolibrary.LogApp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.Arrays;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

public class MergeFiles {
    private static String logDirectory;
    public static Boolean debugMode;
    public static String xmlConverter;
    
    static public synchronized void writeFileToQueue (String queueDirectory, String queueFile) throws IOException {
        BufferedWriter wfbw;
        wfbw = new BufferedWriter(new FileWriter(new File(queueDirectory + "/" + queueFile), false));
        wfbw.write("");
        wfbw.flush();
        wfbw.close();
    }
    
    static public synchronized void writeFileToQueue (String queueDirectory, String queueFile, String processStep) throws IOException {
        BufferedWriter wfbw;
        wfbw = new BufferedWriter(new FileWriter(new File(queueDirectory + "/" + queueFile), false));
        wfbw.write(processStep);
        wfbw.flush();
        wfbw.close();
    }
    
    static public synchronized String readStringFromMissingFile (String missingDirectory, String missingFile) throws IOException {
        BufferedReader rfbw;
        rfbw = new BufferedReader(new InputStreamReader(new FileInputStream(new File(missingDirectory + "/" + missingFile))));
        String line = rfbw.readLine();
        rfbw.close();
        return line;
    }
    
    
/*
  +---------------------------------------------------------------------------+
  |   Este processo le os dados que foram enfileirados e faz o processo       |
  |   de Merge, Encrypt, UploadFiles e MoveFiles                              |
  |                                                                           |      
  +---------------------------------------------------------------------------+  
*/    
    
    
    

    public static void queue(String baseDir, String moveDir, String missingDir, Boolean iaLocalAvailable, String gcProject, String gcsPie, String keyDir, String GoogleApplicationCredentials, String urlIaLocal) {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        try {
            if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Queue process started...", 0);
        } catch(IOException e) {
            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        }
        
        
        
        // Read Missing files
        String trkId = "";
        String processStep = "";
        
        Monitor.sendInformationToBackEnd(false);
        
        try {
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Reading missing queue started...", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
            File missingList = new File(new File("").getCanonicalPath() + "\\\\missing");
            File[] filesMissing = missingList.listFiles();
            Arrays.sort(filesMissing);           
            for (File file : filesMissing) {                
                trkId = file.getName();
                File baseDirFile = new File(baseDir + "\\\\" + trkId);
                
                if(!baseDirFile.exists()) {
                    try {
                        LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Source Directory " + 
                                file.getAbsolutePath() + " not found. Missing for " + file.getAbsolutePath() + " will be deleted", 2);
                        file.delete();
                        continue;
                    } catch(IOException e) {
                        System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }
                }                
                
                if (iaLocalAvailable) {

                    try {
                        Image.getImageCheioVazio(baseDir, urlIaLocal, trkId);                
                    } catch (IOException e) {
                        try {
                            System.err.println(e.toString());
                            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Check Local Analyse Cheio/Vazio for Truck Id " + trkId + " (IOException)", 2);
                        } catch(IOException e1) {
                            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                            System.exit(10);
                        }
                    } catch (Exception e) {
                        try {
                            System.err.println(e.toString());                            
                            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Check Local Analyse Cheio/Vazio for Truck Id " + trkId + " (Exception)", 2);
                        } catch(IOException e1) {
                            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                            System.exit(10);
                        }
                    }
                } else {                    
                    if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Disabled Local Analyse Cheio/Vazio for Truck Id " + trkId, 0);
                }

                
                try {
                    File fileCopy = new File(baseDirFile.getAbsolutePath());
                    File[] filesCopy = fileCopy.listFiles();
                    
                    if (!checkAllNeedFiles(filesCopy, trkId)) {
                        throw new ProcessIncompleteException();
                    }
                    
                    String currentProcessStep = readStringFromMissingFile(missingList.getAbsolutePath(), trkId);
                    Integer currentProcessStepSequence = 0;
                    if (currentProcessStep.equalsIgnoreCase("merge")) currentProcessStepSequence = 1;
                    if (currentProcessStep.equalsIgnoreCase("encryptFile")) currentProcessStepSequence = 2;
                    if (currentProcessStep.equalsIgnoreCase("uploadObject")) currentProcessStepSequence = 3;
                    if (currentProcessStep.equalsIgnoreCase("moveFiles")) currentProcessStepSequence = 4;
                    
                    processStep = "merge";
                    if (currentProcessStepSequence < 1) { 
                        MergeFiles.merge(baseDir, trkId);
                        missing(missingDir, trkId, processStep);
                    }
                    
                    processStep = "encryptFile";
                    if (currentProcessStepSequence < 2) {
                        AsymmetricCryptography.encryptFile(baseDir, keyDir, trkId);
                        missing(missingDir, trkId, processStep);
                    }
                    
                    processStep = "uploadObject";
                    if (currentProcessStepSequence < 3) {
                        SendFiles.uploadObject(gcProject, gcsPie, baseDir, trkId, keyDir, GoogleApplicationCredentials);
                        missing(missingDir, trkId, processStep);
                    }

                    processStep = "moveFiles";
                    if (currentProcessStepSequence < 4) {
                        SendFiles.moveFiles(baseDir, moveDir, trkId);
                        missing(missingDir, trkId, processStep);
                    }
                    try {
                        LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing for " + file.getAbsolutePath() + " deleted.", 0);
                        file.delete();
                    } catch(IOException e) {
                        System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }                    
                    
                    Monitor.sendInformationToBackEnd(false);
                    
                } catch (ProcessIncompleteException e) {
                    try {
                        LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Incomplete Process Issue for Truck Id " + trkId, 2);
                    } catch(IOException e2) {
                        System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }
                }
            }
            
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Reading missing queue done.", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Reading queue started...", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        
            // Read Queue Files
        
            Monitor.sendInformationToBackEnd(false);
        
            File queueList = new File(new File("").getCanonicalPath() + "\\\\queue");
            File[] filesQueue = queueList.listFiles();
            Arrays.sort(filesQueue);
            

            for (File file : filesQueue) {                
                trkId = file.getName();
                File baseDirFile = new File(baseDir + "\\\\" + trkId);
                
                if(!baseDirFile.exists()) {
                    try {
                        LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Source Directory " + 
                                file.getAbsolutePath() + " not found. Queue for " + file.getAbsolutePath() + " will be deleted", 2);
                        file.delete();
                        continue;
                    } catch(IOException e) {
                        System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }
                }
                
                try {
                    if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Queue for " + file.getAbsolutePath() + " deleted.", 0);
                    file.delete();
                } catch(IOException e) {
                    System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
                }
                
                if (iaLocalAvailable) {
                
                    try {
                        Image.getImageCheioVazio(baseDir, urlIaLocal, trkId);                
                    } catch (IOException e) {
                        try {
                            System.err.println(e.toString());
                            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Check Local Analyse Cheio/Vazio for Truck Id " + trkId + " (IOException)", 2);
                        } catch(IOException e1) {
                            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                            System.exit(10);
                        }
                    } catch (Exception e) {
                        try {
                            System.err.println(e.toString());
                            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Check Local Analyse Cheio/Vazio for Truck Id " + trkId + " (Exception)", 2);
                        } catch(IOException e1) {
                            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                            System.exit(10);
                        }
                    }
                    
                } else {
                    if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Disabled Local Analyse Cheio/Vazio for Truck Id " + trkId, 0);
                }
                try {
                    File fileCopy = new File(baseDirFile.getAbsolutePath());
                    File[] filesCopy = fileCopy.listFiles();
                    
                    processStep = "start";
                    if (!checkAllNeedFiles(filesCopy, trkId)) {
                        throw new ProcessIncompleteException();
                    }
                    MergeFiles.merge(baseDir, trkId);
                    processStep = "merge";                    
                    AsymmetricCryptography.encryptFile(baseDir, keyDir, trkId);
                    processStep = "encryptFile";
                    SendFiles.uploadObject(gcProject, gcsPie, baseDir, trkId, keyDir, GoogleApplicationCredentials);
                    processStep = "uploadObject";                    
                    SendFiles.moveFiles(baseDir, moveDir, trkId);
                    processStep = "moveFiles";                    
                } catch (ProcessIncompleteException e) {
                    try {
                        missing(missingDir, trkId, processStep);
                    } catch (ProcessIncompleteException e1) {
                        try {
                            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Incomplete Process Issue for Truck Id " + trkId, 2);
                        } catch(IOException e2) {
                            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                            System.exit(10);
                        }
                    }
                }
                Monitor.sendInformationToBackEnd(false);
            }
            
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Queue process done.", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        
        } catch (IOException e) {
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Cannot access Missing or Queue List.", 0);
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }
    }    
    
    
/*
  +---------------------------------------------------------------------------+
  |                                                                           |
  |      Enqueue to Process from the Service                                  |
  |                                                                           |      
  +---------------------------------------------------------------------------+  
*/    
    
    
    
    public static void enqueue(String baseDir, String trkId) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        Monitor.sendInformationToBackEnd(false);
        
        File sourceDirectory = new File(baseDir + "\\\\" + trkId);
        if (!sourceDirectory.exists()) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Enqueue Source Directory " + sourceDirectory.getAbsolutePath() + " not found", 2);
                System.exit(10);
                
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        File queueDirectory = null;
        
        try {
            queueDirectory = new File(new File("").getCanonicalPath() + "\\\\queue");
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Enqueue Cannot get Local Path for Queue Directory", 2);
            } catch (IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            }
            System.exit(10);
        }
        
        if (!queueDirectory.exists()) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Enqueue Source Directory " + logDirectory + " not found", 2);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }
        
        try {
            writeFileToQueue(queueDirectory.getAbsolutePath(), trkId);
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Enqueue Source Directory " + trkId + " enqueued successfully", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Enqueue Cannot write queue file " + trkId + " to queue directory " + queueDirectory.getAbsolutePath(), 2);
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            throw new ProcessIncompleteException("Cannot write queue file " + trkId + " to queue directory " + queueDirectory.getAbsolutePath());
        }
    }

    
    
/*
  +---------------------------------------------------------------------------+
  |                                                                           |
  |      Enqueue to Missing Process from the Service                          |
  |                                                                           |      
  +---------------------------------------------------------------------------+  
*/    
    
    
    
    public static void checkStepMissingFile(String baseDir, String trkId) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        File sourceDirectory = new File(baseDir + "\\\\" + trkId);
        if (!sourceDirectory.exists()) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing Source Directory " + sourceDirectory.getAbsolutePath() + " not found", 2);
                System.exit(10);
                
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        File missingDirectory = null;
        
        try {
            missingDirectory = new File(new File("").getCanonicalPath() + "\\\\missing");
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing Cannot get Local Path for Queue Directory", 2);
            } catch (IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            }
            System.exit(10);
        }
        
        if (!missingDirectory.exists()) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing Source Directory " + logDirectory + " not found", 2);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }
        
        try {
            String line = readStringFromMissingFile(missingDirectory.getAbsolutePath(), trkId);
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing Source Directory " + trkId + " Step Stopped missing is " + line, 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing Cannot write queue file " + trkId + " to missing directory " + missingDirectory.getAbsolutePath(), 2);
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            throw new ProcessIncompleteException("Cannot write missing file " + trkId + " to missing directory " + missingDirectory.getAbsolutePath());
        }
    }
    
    public static void missing(String missingDir, String trkId, String processStep) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        File sourceDirectory = new File(missingDir);
        if (!sourceDirectory.exists()) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing Source Directory " + sourceDirectory.getAbsolutePath() + " not found", 2);
                System.exit(10);                
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        File missingDirectory = null;
        
        try {
            writeFileToQueue(sourceDirectory.getAbsolutePath(), trkId, processStep);
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing Source Directory " + trkId + " enqueued successfully", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Missing Cannot write queue file " + trkId + " to missing directory " + missingDirectory.getAbsolutePath(), 2);
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            throw new ProcessIncompleteException("Cannot write missing file " + trkId + " to missing directory " + missingDirectory.getAbsolutePath());
        }
    }
    
    
/*
  +---------------------------------------------------------------------------+
  |                                                                           |
  |      Merge Files into a ebco file                                         |
  |                                                                           |      
  +---------------------------------------------------------------------------+  
*/    
    
    
    public static void merge (String baseDir, String trkId) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        
        String directory_name = baseDir + "\\\\" + trkId;
        String file_name = directory_name + "\\\\" + trkId + "S.ebco" ;
        
        File f = new File(baseDir + "\\\\" + trkId);
        
        
        File ofile = new File(file_name);   
        
        try {
            if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Merge file " + ofile.getAbsolutePath() + " started ...", 0);
        } catch(IOException e) {
            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        }

        
        if (ofile.exists()) {
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Merge Destination file " + ofile.getAbsolutePath() + " found. Will be deleted", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }            
            ofile.delete();
        }
        
        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytesOutput;
        byte[] fileBytes;
//        int bytesRead = 0;

        if (xmlConverter.equalsIgnoreCase("CARGO")) {
            ConvertXmlFile.convertXmlFileCargo(baseDir, trkId);
            
            File fi = new File(baseDir + "\\" + trkId, trkId + ".xml");
            File fo = new File(baseDir + "\\" + trkId, trkId + "_.xml");
            if (fo.exists()) {
                fo.delete();
            }
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, String.format("XML - Source -> %s, Destination -> ", fi.getAbsolutePath(), fo.getPath()), 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }            
            
            System.out.println(String.format("XML - Source -> %s, Destination -> ", fi.getAbsolutePath(), fo.getPath()));
            fi.renameTo(fo);
            
            fi = new File(baseDir + "\\" + trkId, trkId + "_converted.xml");
            fo = new File(baseDir + "\\" + trkId, trkId + ".xml");
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, String.format("XML CONVERTED - Source -> %s, Destination -> ", fi.getAbsolutePath(), fo.getPath()), 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }            

            fi.renameTo(fo);            
        }
        
        try {
            fos = new FileOutputStream(ofile, true);
            
            JsonObjectBuilder jb = Json.createObjectBuilder();
            JsonArrayBuilder ja = Json.createArrayBuilder();
            JsonObjectBuilder jab = Json.createObjectBuilder();
            
            jb.add("number_of_files", f.listFiles().length - 1);
            
            int numberOfBytes = 4096;
            int position = 4096;
            
            for (File file : f.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".xml") 
                    || name.toLowerCase().endsWith(".tif")
                    || name.toLowerCase().endsWith(".img")
                    || name.toLowerCase().endsWith("_ocr.jpg")
                    || name.toLowerCase().endsWith("s.jpg")
                    || name.toLowerCase().endsWith(".json"))) {
                jab.add("file_name", file.getName());
                jab.add("file_size", file.length());
                jab.add("position", position);
                numberOfBytes += file.length();
                position += file.length();                
                ja.add(jab.build());

            } 
            
            
            jb.add("files", ja.build());            
            int offSet = 4096;
            fileBytesOutput = new byte[numberOfBytes];
            copyBytesAtOffset(fileBytesOutput, jb.build().toString().getBytes(), 0);
            
            for (File file : f.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".xml") 
                    || name.toLowerCase().endsWith(".tif")
                    || name.toLowerCase().endsWith(".img")
                    || name.toLowerCase().endsWith("_ocr.jpg")
                    || name.toLowerCase().endsWith("s.jpg")
                    || name.toLowerCase().endsWith(".json"))) {
                
                fileBytes = new byte[(int) file.length()];
                fis = new FileInputStream(file);
                fis.read(fileBytes);
                copyBytesAtOffset(fileBytesOutput, fileBytes, offSet);
                offSet += file.length();
                fis.close();
            }
            
            fos.write(fileBytesOutput);
            fos.flush();
            fos.close();
            
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Merge file " + ofile.getAbsolutePath() + " done.", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Merge file " + ofile.getAbsolutePath() + " not complete. " + e.toString(), 2);
                throw new ProcessIncompleteException("Merge file " + ofile.getAbsolutePath() + " not complete.");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

    }
    
/*
  +---------------------------------------------------------------------------+
  |                                                                           |
  |      Extract Files into a ebco file                                       |
  |                                                                           |      
  +---------------------------------------------------------------------------+  
*/    
    
    
    public static void split(String baseDir, String trkId) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        String directory_name = baseDir + "\\\\" + trkId;
        String file_name = directory_name + "\\\\" + trkId + "S.ebco" ;
        
        File inputFile = new File(file_name);
        RandomAccessFile inputStream;
        //int readLength = 1024;
        
        int readLengthVersion1 = 1024;
        int readLengthVersion2 = 4096;
        byte[] byteChunkPart;
        JsonReader jsonReader;
        String jsonDirectory;
        JsonObject object = null;
        
        try {
            if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Split file " + inputFile.getAbsolutePath() + " started.", 0);
        } catch(IOException e) {
            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        }
        
        try {
            try {
                inputStream = new RandomAccessFile(inputFile, "rw");
                byteChunkPart = new byte[readLengthVersion1];
                inputStream.readFully(byteChunkPart, 0, readLengthVersion1);
                jsonDirectory = new String(byteChunkPart);            
                jsonReader = Json.createReader(new StringReader(jsonDirectory));
                object = jsonReader.readObject();
                jsonReader.close();
            } catch (Exception e1) {
                try {                    
                    inputStream = new RandomAccessFile(inputFile, "rw");
                    byteChunkPart = new byte[readLengthVersion2];
                    inputStream.readFully(byteChunkPart, 0, readLengthVersion2);
                    jsonDirectory = new String(byteChunkPart);            
                    jsonReader = Json.createReader(new StringReader(jsonDirectory));
                    object = jsonReader.readObject();
                    jsonReader.close();
                } catch (Exception e2) {
                    throw new ProcessIncompleteException("Erro na leitura do Diretorio");
                }
            }
            
            JsonArray array = object.getJsonArray("files");
            for (int i = 0; i < array.size(); i++) {
                JsonObject x = array.getJsonObject(i);
                byteChunkPart = new byte[x.getInt("file_size")];
                inputStream.seek(x.getInt("position"));
                inputStream.readFully(byteChunkPart, 0, x.getInt("file_size"));
                File ofile = new File(directory_name + "\\" + x.getString("file_name") + ".out");
                FileOutputStream fos = new FileOutputStream(ofile);
                fos.write(byteChunkPart);
                fos.flush();
                fos.close();
                
                try {
                    if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Split file " + ofile.getAbsolutePath() + " generated successfully.", 0);
                } catch(IOException e) {
                    System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
                }
                
            }
            
            inputStream.close();
            
            try {
                if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Split file " + inputFile.getAbsolutePath() + " done.", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IOException exception) {
            exception.printStackTrace();
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Split file " + inputFile.getAbsolutePath() + " not complete.", 2);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            throw new ProcessIncompleteException("Split file " + inputFile.getAbsolutePath() + " not complete.");
            
        }
    }
    
    private static byte[] long2byte(long l) throws IOException {
        ByteArrayOutputStream baos=new ByteArrayOutputStream(Long.SIZE/8);
        DataOutputStream dos=new DataOutputStream(baos);
        dos.writeLong(l);
        byte[] result=baos.toByteArray();
        dos.close();    
        return result;
    }


    private static long byte2long(byte[] b) throws IOException {
        ByteArrayInputStream baos=new ByteArrayInputStream(b);
        DataInputStream dos=new DataInputStream(baos);
        long result=dos.readLong();
        dos.close();
        return result;
    }
    
    private static void copyBytesAtOffset(byte[] dst, byte[] src, int offset) {
        if (dst == null) {
            throw new NullPointerException("dst == null");
        }
        if (src == null) {
            throw new NullPointerException("src == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset hast to be >= 0");
        }
        if ((src.length + offset) > dst.length) {
            throw new IllegalArgumentException("src length + offset must not be greater than size of destination");
        }
        for (int i = 0; i < src.length; i++) {
            dst[offset + i] = src[i];
        }
    }    
    
    public static boolean checkAllNeedFiles(File[] filesCopy, String dirTruckCurrent) {
        boolean img = false;
        boolean xml = false;
        boolean jpeg = false;
        boolean tif = false;
        boolean stampJpeg = false;
        
        for (File fileC : filesCopy) {
            if (fileC.isDirectory()) {
                continue;
            }
            
            String filename = fileC.getName();
            
            if (filename.equalsIgnoreCase(dirTruckCurrent + "S.tif")) {
                tif = true;            
            }
            if (filename.equalsIgnoreCase(dirTruckCurrent + "S.img")) {
                img = true;            
            }
            if (filename.equalsIgnoreCase(dirTruckCurrent + ".xml")) {
                xml = true;            
            }
            if (filename.equalsIgnoreCase(dirTruckCurrent + "S_stamp.jpg")) {
                stampJpeg = true;            
            }
            if (filename.equalsIgnoreCase(dirTruckCurrent + "S.jpg")) {
                jpeg = true;            
            }
        }
        
        if (img && xml && stampJpeg && jpeg && tif) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean checkAllNeedFilesEbco(File[] filesCopy, String dirTruckCurrent) {
        boolean img = false;
        boolean xml = false;
        boolean jpeg = false;
        boolean stampJpeg = false;
        
        for (File fileC : filesCopy) {
            if (fileC.isDirectory()) {
                continue;
            }
            
            String filename = fileC.getName();
            
            if (filename.equalsIgnoreCase(dirTruckCurrent + "S.img")) {
                img = true;            
            }
            if (filename.equalsIgnoreCase(dirTruckCurrent + ".xml")) {
                xml = true;            
            }
            if (filename.equalsIgnoreCase(dirTruckCurrent + "S_stamp.jpg")) {
                stampJpeg = true;            
            }
            if (filename.equalsIgnoreCase(dirTruckCurrent + "S.jpg")) {
                jpeg = true;            
            }
        }
        
        if (img && xml && stampJpeg && jpeg) {
            return true;
        } else {
            return false;
        }
    }
    
    
}

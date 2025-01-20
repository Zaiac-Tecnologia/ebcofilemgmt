package br.com.zaiac.ebcofilemgmt.tools;

import br.com.zaiac.ebcofilemgmt.cryptography.AsymmetricCryptography;
import br.com.zaiac.ebcofilemgmt.exception.ProcessIncompleteException;
import br.com.zaiac.ebcofilemgmt.exception.WriteMissingException;
import br.com.zaiac.ebcofilemgmt.model.GoogleCredentials;
import br.com.zaiac.ebcofilemgmt.model.SftpCredentials;
import br.com.zaiac.ebcofilemgmt.xml.ConvertXmlFile;
import br.com.zaiac.ebcolibrary.LogApp;
import br.com.zaiac.ebcolibrary.exceptions.WriteLogFileException;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

//import org.checkerframework.checker.units.qual.s;

public class MergeFiles {

    private static String logDirectory;
    public static Boolean debugMode;
    public static String xmlConverter;

    public static synchronized void writeFileToQueue(String queueDirectory, String queueFile) throws IOException {
        BufferedWriter wfbw;
        wfbw = new BufferedWriter(new FileWriter(new File(queueDirectory + "/" + queueFile), false));
        wfbw.write("");
        wfbw.flush();
        wfbw.close();
    }

    public static synchronized void writeFileToQueue(String queueDirectory, Integer priority, String queueFile)
            throws IOException {
        File queuePriorityDirectory = new File(queueDirectory + "\\" + priority);
        if (!queuePriorityDirectory.exists()) {
            queuePriorityDirectory.mkdirs();
        }
        BufferedWriter wfbw;
        wfbw = new BufferedWriter(
                new FileWriter(new File(queueDirectory + "\\" + priority + "\\" + queueFile), false));
        wfbw.write("");
        wfbw.flush();
        wfbw.close();
    }

    public static synchronized void writeFileToQueue(String queueDirectory, String queueFile, String processStep)
            throws WriteMissingException {
        BufferedWriter wfbw;
        try {
            wfbw = new BufferedWriter(new FileWriter(new File(queueDirectory + "/" + queueFile), false));
            wfbw.write(processStep);
            wfbw.flush();
            wfbw.close();
        } catch (IOException e) {
            throw new WriteMissingException(
                    "Cannot write missing file " + queueDirectory + "/" + queueFile + " with process step "
                            + processStep);
        }
        // wfbw = new BufferedWriter(new FileWriter(new File(queueDirectory + "/" +
        // queueFile), false));
        // wfbw.write(processStep);
        // wfbw.flush();
        // wfbw.close();
    }

    public static synchronized String readStringFromMissingFile(String missingDirectory, String missingFile)
            throws IOException {
        BufferedReader rfbw;
        rfbw = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(missingDirectory + "/" + missingFile))));
        String line = rfbw.readLine();
        rfbw.close();
        return line;
    }

    /*
     * +---------------------------------------------------------------------------+
     * | Este processo le os dados que foram enfileirados e faz o processo........ |
     * | de Merge, Encrypt, UploadFiles e MoveFiles............................... |
     * |.......................................................................... |
     * +---------------------------------------------------------------------------+
     */

    public static void queue(
            String baseDir,
            String moveDir,
            String missingDir,
            Boolean iaLocalAvailable,
            String urlIaLocal,
            ArrayList<SftpCredentials> sftpCredentials,
            GoogleCredentials googleCredentials,
            String scanner) {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            e.printStackTrace();
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }

        try {
            if (debugMode)
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Queue process started...", 0);
        } catch (WriteLogFileException e) {
            e.printStackTrace();
            System.err.println(
                    "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        }

        if (sftpCredentials != null) {
            for (SftpCredentials sftpCredential : sftpCredentials) {
                try {
                    if (debugMode)
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Adicional Priority (" +
                                        sftpCredential.getSitePriority() +
                                        ") for User: " +
                                        sftpCredential.getSiteSFTPUsername() +
                                        "Destination: " +
                                        sftpCredential.getSiteSFTPDestination() +
                                        " is available.",
                                0);
                } catch (WriteLogFileException e) {
                    e.printStackTrace();
                }
            }
        }

        String trkId = "";
        String processStep = "";

        Monitor.sendInformationToBackEnd(false);

        try {
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Reading missing queue started...",
                            0);
            } catch (WriteLogFileException e) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }

            /**
             * ?
             * ? Process Missing Files
             * ?
             */

            File missingList = new File(new File("").getCanonicalPath() + "\\\\missing");
            File[] filesMissing = missingList.listFiles();
            if (filesMissing == null) {
                try {
                    if (debugMode)
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Missing List is empty.",
                                0);
                } catch (WriteLogFileException e) {
                    e.printStackTrace();
                    System.err.println(
                            "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
                }
            } else {
                Arrays.sort(filesMissing);
                for (File fileMissing : filesMissing) {
                    trkId = fileMissing.getName();
                    File baseDirFile = new File(baseDir + "\\" + trkId);

                    if (!baseDirFile.exists()) {
                        try {
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Source Directory " +
                                            fileMissing.getAbsolutePath() +
                                            " not found. Missing for " +
                                            fileMissing.getAbsolutePath() +
                                            " will be deleted",
                                    2);
                            fileMissing.delete();
                            continue;
                        } catch (WriteLogFileException e) {
                            e.printStackTrace();
                            System.err.println(
                                    "Cannot write log file Directory " + logDirectory + " file name "
                                            + Constants.LOGFILE);
                            System.exit(10);
                        }
                    }

                    /**
                     * ! Verificacao da IA
                     */

                    if (iaLocalAvailable) {
                        try {
                            Image.getImageCheioVazio(baseDir, urlIaLocal, trkId);
                        } catch (IOException e) {
                            try {
                                LogApp.writeLineToFile(
                                        logDirectory,
                                        Constants.LOGFILE,
                                        "Check Local Analyse Cheio/Vazio for Truck Id " + trkId + " (IOException)",
                                        2);
                            } catch (WriteLogFileException e1) {
                                e.printStackTrace();
                                System.err.println(
                                        "Cannot write log file Directory " +
                                                logDirectory +
                                                " file name " +
                                                Constants.LOGFILE);
                                System.exit(10);
                            }
                        } catch (Exception e) {
                            try {
                                LogApp.writeLineToFile(
                                        logDirectory,
                                        Constants.LOGFILE,
                                        "Check Local Analyse Cheio/Vazio for Truck Id " + trkId + " (Exception)",
                                        2);
                            } catch (WriteLogFileException e1) {
                                e.printStackTrace();
                                System.err.println(
                                        "Cannot write log file Directory " +
                                                logDirectory +
                                                " file name " +
                                                Constants.LOGFILE);
                                System.exit(10);
                            }
                        }
                    } else {
                        try {
                            if (debugMode)
                                LogApp.writeLineToFile(
                                        logDirectory,
                                        Constants.LOGFILE,
                                        "Disabled Local Analyse Cheio/Vazio for Truck Id " + trkId,
                                        0);
                        } catch (WriteLogFileException e1) {
                            e1.printStackTrace();
                        }
                    }

                    try {
                        File fileCopy = new File(baseDirFile.getAbsolutePath());
                        File[] filesCopy = fileCopy.listFiles();

                        if (scanner.equalsIgnoreCase("SMITHS")) {
                            if (!checkAllNeedFiles(filesCopy, trkId)) {
                                throw new ProcessIncompleteException();
                            }
                        } else if (scanner.equalsIgnoreCase("NUCHTECH")) {
                            if (!checkAllNeedFilesNuchtech(filesCopy, trkId)) {
                                throw new ProcessIncompleteException();
                            }
                        }
                        processStep = readStringFromMissingFile(missingList.getAbsolutePath(), trkId);

                        if (sftpCredentials != null) {
                            processTruckIdToSftp(
                                    baseDir,
                                    missingDir,
                                    fileMissing,
                                    true,
                                    sftpCredentials,
                                    0,
                                    trkId,
                                    processStep,
                                    false);
                        } else {
                            processTruckIdToGcp(
                                    baseDir,
                                    missingDir,
                                    moveDir,
                                    logDirectory,
                                    fileMissing,
                                    true,
                                    iaLocalAvailable,
                                    urlIaLocal,
                                    googleCredentials.getGcProject(),
                                    googleCredentials.getGcsPie(),
                                    googleCredentials.getKeyDir(),
                                    googleCredentials.getGoogleApplicationCredentials(),
                                    trkId,
                                    processStep,
                                    scanner);
                        }
                        Monitor.sendInformationToBackEnd(false);
                    } catch (ProcessIncompleteException e) {
                        // System.out.println("Process Incompeto!!!!!");
                        try {
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Incomplete Process Issue for Truck Id " + trkId,
                                    2);
                        } catch (WriteLogFileException e2) {
                            e2.printStackTrace();
                            System.err.println(
                                    "Cannot write log file Directory " + logDirectory + " file name "
                                            + Constants.LOGFILE);
                            System.exit(10);
                        }
                    }
                }
            }

            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Reading missing queue done.",
                            0);
            } catch (WriteLogFileException e) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }

            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Reading queue started...",
                            0);
            } catch (WriteLogFileException e) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }

            // Read Queue Files

            Monitor.sendInformationToBackEnd(false);

            /**
             * ?
             * ? Process Queue Files
             * ?
             */

            File queueList = new File(new File("").getCanonicalPath() + "\\\\queue");
            File[] filesQueue = queueList.listFiles();
            Arrays.sort(filesQueue);

            for (File fileQueue : filesQueue) {
                if (fileQueue.isDirectory()) {
                    try {
                        Integer priority = Integer.parseInt(fileQueue.getName());
                        File queueListByPriority = new File(
                                new File("").getCanonicalPath() + "\\queue\\" + fileQueue.getName());
                        File[] filesQueueByPriority = queueListByPriority.listFiles();
                        Arrays.sort(filesQueueByPriority);
                        for (File fileQueueByPriority : filesQueueByPriority) {
                            trkId = fileQueueByPriority.getName();
                            File baseDirFile = new File(baseDir + "\\" + trkId);
                            if (!baseDirFile.exists()) {
                                try {
                                    LogApp.writeLineToFile(
                                            logDirectory,
                                            Constants.LOGFILE,
                                            "Source Directory " +
                                                    baseDirFile.getAbsolutePath() +
                                                    " not found. Queue for " +
                                                    baseDirFile.getAbsolutePath() +
                                                    " will be deleted",
                                            2);
                                    fileQueueByPriority.delete();
                                    continue;
                                } catch (WriteLogFileException e) {
                                    System.err.println(
                                            "Cannot write log file Directory " +
                                                    logDirectory +
                                                    " file name " +
                                                    Constants.LOGFILE);
                                    System.exit(10);
                                }
                            }

                            if (sftpCredentials != null) {
                                try {
                                    LogApp.writeLineToFile(
                                            logDirectory,
                                            Constants.LOGFILE,
                                            "Processing " + trkId + " as a Priority " + priority + " To SFTP Server",
                                            0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println(
                                            "Cannot write log file Directory " +
                                                    logDirectory +
                                                    " file name " +
                                                    Constants.LOGFILE);
                                    System.exit(10);
                                }
                                processTruckIdToSftp(
                                        baseDir,
                                        missingDir,
                                        fileQueueByPriority,
                                        false,
                                        sftpCredentials,
                                        priority,
                                        trkId,
                                        processStep,
                                        false);
                            } else {
                                try {
                                    LogApp.writeLineToFile(
                                            logDirectory,
                                            Constants.LOGFILE,
                                            "Processing " + trkId + " as a Priority " + priority + " To GCP Cloud",
                                            0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println(
                                            "Cannot write log file Directory " +
                                                    logDirectory +
                                                    " file name " +
                                                    Constants.LOGFILE);
                                    System.exit(10);
                                }

                                processTruckIdToGcp(
                                        baseDir,
                                        missingDir,
                                        moveDir,
                                        logDirectory,
                                        fileQueueByPriority,
                                        false,
                                        iaLocalAvailable,
                                        urlIaLocal,
                                        googleCredentials.getGcProject(),
                                        googleCredentials.getGcsPie(),
                                        googleCredentials.getKeyDir(),
                                        googleCredentials.getGoogleApplicationCredentials(),
                                        trkId,
                                        "start",
                                        scanner);
                            }
                        }
                    } catch (NumberFormatException e) {
                        try {
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Queue Directory " +
                                            fileQueue.getAbsolutePath() +
                                            "is a Directory and not a Priority Directory. Queue for " +
                                            fileQueue.getAbsolutePath() +
                                            " will be skiped.",
                                    2);
                            e.printStackTrace();
                            continue;
                        } catch (WriteLogFileException e1) {
                            System.err.println(
                                    "Cannot write log file Directory " +
                                            logDirectory +
                                            " file name " +
                                            Constants.LOGFILE);
                            System.exit(10);
                        }
                    }
                    continue;
                }

                trkId = fileQueue.getName();
                if (sftpCredentials != null) {
                    try {
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Processing " + trkId + " as a Non-Priority-File To SFTP Server",
                                0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(
                                "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }
                    processTruckIdToSftp(
                            baseDir,
                            missingDir,
                            fileQueue,
                            false,
                            sftpCredentials,
                            0,
                            trkId,
                            processStep,
                            false);
                } else {
                    try {
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Processing " + trkId + " as a Non-Priority-File To GCP Cloud",
                                0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(
                                "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }

                    processTruckIdToGcp(
                            baseDir,
                            missingDir,
                            moveDir,
                            logDirectory,
                            fileQueue,
                            false,
                            iaLocalAvailable,
                            urlIaLocal,
                            googleCredentials.getGcProject(),
                            googleCredentials.getGcsPie(),
                            googleCredentials.getKeyDir(),
                            googleCredentials.getGoogleApplicationCredentials(),
                            trkId,
                            "start",
                            scanner);
                }
            }

            try {
                if (debugMode)
                    LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Queue process done.", 0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (IOException e) {
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Cannot access Missing or Queue List.",
                            0);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }
    }

    private static String processTruckIdToSftp(
            String baseDir,
            String missingDir,
            File fileQueue,
            boolean isMissingQueue,
            ArrayList<SftpCredentials> sftpCredentials,
            Integer priority,
            String trkId,
            String processStep,
            Boolean deleteSource) {
        processStep = "start";
        try {
            File baseDirFile = new File(baseDir + "\\" + trkId);
            File fileCopy = new File(baseDirFile.getAbsolutePath());
            File[] filesCopy = fileCopy.listFiles();

            SftpCredentials sftpCredential = sftpCredentials.get(0);

            if (priority > 0)
                for (int i = 1; i < sftpCredentials.size(); i++) {
                    if (sftpCredentials.get(i).getSitePriority() == priority) {
                        sftpCredential = sftpCredentials.get(i);
                        break;
                    }
                }

            if (!MergeFiles.checkAllNeedFilesEbco(filesCopy, trkId)) {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Diretory " + fileCopy.getAbsolutePath() + " is missing files for Truck Id " + trkId,
                            0);

                throw new ProcessIncompleteException();
            }

            Integer currentProcessStepSequence = 0;
            if (processStep.equalsIgnoreCase("sftp"))
                currentProcessStepSequence = 1;
            if (processStep.equalsIgnoreCase("deleteDirectory"))
                currentProcessStepSequence = 2;

            processStep = "sftp";
            if (currentProcessStepSequence < 1) {
                sftp(
                        baseDir,
                        sftpCredential.getSiteSFTPDestination(),
                        sftpCredential.getSiteSFTPPort(),
                        sftpCredential.getSiteSFTPUsername(),
                        sftpCredential.getSiteSFTPPassword(),
                        trkId);
            }

            if (deleteSource) {
                processStep = "deleteDirectory";
                if (currentProcessStepSequence < 2) {
                    deleteDirectory(baseDir, trkId);
                }
            }
            fileQueue.delete();
        } catch (WriteLogFileException e) {
            e.printStackTrace();
            System.exit(10);

        } catch (ProcessIncompleteException e) {
            try {
                missing(missingDir, trkId, processStep);
                if (!isMissingQueue) {
                    fileQueue.delete();
                }
            } catch (WriteMissingException e1) {
                e1.printStackTrace();
                System.exit(10);
            } catch (WriteLogFileException e1) {
                e1.printStackTrace();
                System.exit(10);
            }

        }
        return processStep;
    }

    private static String processTruckIdToGcp(
            String baseDir,
            String missingDir,
            String moveDir,
            String logDirectory,
            File fileQueue, // Aqui pode ser Arquivo de File ou Missing
            boolean isMissingQueue,
            Boolean iaLocalAvailable,
            String urlIaLocal,
            String gcProject,
            String gcsPie,
            String keyDir,
            String GoogleApplicationCredentials,
            String trkId,
            String processStep,
            String scanner) {
        File baseDirFile = new File(baseDir + "\\\\" + trkId);
        /**
         * Nao posso deletar o arquivo da fila sem antes fazer todas
         * as verificações necessárias
         */

        Integer currentProcessStepSequence = 0;
        if (processStep.equalsIgnoreCase("merge"))
            currentProcessStepSequence = 1;
        if (processStep.equalsIgnoreCase("encryptFile"))
            currentProcessStepSequence = 2;
        if (processStep.equalsIgnoreCase("uploadObject"))
            currentProcessStepSequence = 3;
        if (processStep.equalsIgnoreCase("moveFiles"))
            currentProcessStepSequence = 4;
        try {
            if (!baseDirFile.exists()) {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Source Directory " +
                                fileQueue.getAbsolutePath() +
                                " not found. Queue for " +
                                fileQueue.getAbsolutePath() +
                                " will be deleted",
                        2);
                fileQueue.delete();
                return null;
            }

            File fileCopy = new File(baseDirFile.getAbsolutePath());
            File[] filesCopy = fileCopy.listFiles();

            processStep = "start";
            if (scanner.equalsIgnoreCase("SMITHS")) {
                if (!checkAllNeedFiles(filesCopy, trkId)) {
                    throw new ProcessIncompleteException();
                }
            } else if (scanner.equalsIgnoreCase("NUCHTECH")) {
                if (!checkAllNeedFilesNuchtech(filesCopy, trkId)) {
                    throw new ProcessIncompleteException();
                }
            }

            if (iaLocalAvailable) {
                try {
                    Image.getImageCheioVazio(baseDir, urlIaLocal, trkId);
                } catch (IOException e) {
                    System.err.println(e.toString());
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Check Local Analyse Cheio/Vazio for Truck Id " + trkId + " (IOException)",
                            2);
                } catch (Exception e) {
                    System.err.println(e.toString());
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Check Local Analyse Cheio/Vazio for Truck Id " + trkId + " (Exception)",
                            2);
                }
            } else {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Disabled Local Analyse Cheio/Vazio for Truck Id " + trkId,
                            0);
            }

            processStep = "merge";
            if (currentProcessStepSequence <= 1) {
                MergeFiles.merge(baseDir, trkId, scanner);
            }

            processStep = "encryptFile";
            if (currentProcessStepSequence <= 2) {
                AsymmetricCryptography.encryptFile(baseDir, keyDir, trkId);
            }
            processStep = "uploadObject";
            if (currentProcessStepSequence <= 3) {
                SendFiles.uploadObject(gcProject, gcsPie, baseDir, trkId, keyDir, GoogleApplicationCredentials);
            }
            processStep = "moveFiles";
            if (currentProcessStepSequence <= 4) {
                SendFiles.moveFiles(baseDir, moveDir, trkId, scanner);
            }
            if (debugMode)
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Queue for " + fileQueue.getAbsolutePath() + " deleted.",
                        0);
            fileQueue.delete();

        } catch (ProcessIncompleteException e) {
            try {
                missing(missingDir, trkId, processStep);
                if (!isMissingQueue) {
                    if (debugMode)
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Queue for " + fileQueue.getAbsolutePath() + " deleted.",
                                0);
                    fileQueue.delete();
                }
            } catch (WriteMissingException e1) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);

            } catch (WriteLogFileException e1) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);

            }
        } catch (

        WriteLogFileException e) {
            e.printStackTrace();
            System.exit(10);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(10);
        }

        Monitor.sendInformationToBackEnd(false);
        return processStep;
    }

    /*
     * +---------------------------------------------------------------------------+
     * | |
     * | Enqueue to Process from the Service |
     * | |
     * +---------------------------------------------------------------------------+
     */

    public static void enqueue(String baseDir, String trkId) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\logs";
        } catch (IOException e) {
            e.printStackTrace();
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }

        if (debugMode) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Source Directory " + baseDir + " Truck ID " + trkId,
                        0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Monitor.sendInformationToBackEnd(false);

        File sourceDirectory = new File(baseDir + "\\" + trkId);
        if (!sourceDirectory.exists()) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Source Directory " + sourceDirectory.getAbsolutePath() + " not found",
                        2);
                System.exit(10);
            } catch (WriteLogFileException e) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        File queueDirectory = null;

        try {
            queueDirectory = new File(new File("").getCanonicalPath() + "\\queue");
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Cannot get Local Path for Queue Directory",
                        2);
            } catch (WriteLogFileException e1) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            }
            System.exit(10);
        }

        if (!queueDirectory.exists()) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Source Directory " + logDirectory + " not found",
                        2);
            } catch (WriteLogFileException e) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        try {
            writeFileToQueue(queueDirectory.getAbsolutePath(), trkId);
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Enqueue Source Directory " + trkId + " enqueued successfully",
                            0);
            } catch (WriteLogFileException e) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Cannot write queue file " +
                                trkId +
                                " to queue directory " +
                                queueDirectory.getAbsolutePath(),
                        2);
            } catch (WriteLogFileException e1) {
                e.printStackTrace();
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            throw new ProcessIncompleteException(
                    "Cannot write queue file " + trkId + " to queue directory " + queueDirectory.getAbsolutePath());
        }
    }

    public static void enqueue(String baseDir, Integer priority, String trkId) throws ProcessIncompleteException {
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
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Source Directory " + sourceDirectory.getAbsolutePath() + " not found",
                        2);
                System.exit(10);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        File queueDirectory = null;

        try {
            queueDirectory = new File(new File("").getCanonicalPath() + "\\\\queue");
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Cannot get Local Path for Queue Directory",
                        2);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            }
            System.exit(10);
        }

        if (!queueDirectory.exists()) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Source Directory " + logDirectory + " not found",
                        2);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        try {
            writeFileToQueue(queueDirectory.getAbsolutePath(), priority, trkId);
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Enqueue Source Directory " + trkId + " with priority " + priority
                                    + " enqueued successfully",
                            0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Enqueue Cannot write queue file " +
                                trkId +
                                " to queue directory " +
                                queueDirectory.getAbsolutePath(),
                        2);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            throw new ProcessIncompleteException(
                    "Cannot write queue file " + trkId + " to queue directory " + queueDirectory.getAbsolutePath());
        }
    }

    /*
     * +---------------------------------------------------------------------------+
     * | |
     * | Enqueue to Missing Process from the Service |
     * | |
     * +---------------------------------------------------------------------------+
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
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Missing Source Directory " + sourceDirectory.getAbsolutePath() + " not found",
                        2);
                System.exit(10);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        File missingDirectory = null;

        try {
            missingDirectory = new File(new File("").getCanonicalPath() + "\\\\missing");
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Missing Cannot get Local Path for Queue Directory",
                        2);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            }
            System.exit(10);
        }

        if (!missingDirectory.exists()) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Missing Source Directory " + logDirectory + " not found",
                        2);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

        try {
            String line = readStringFromMissingFile(missingDirectory.getAbsolutePath(), trkId);
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Missing Source Directory " + trkId + " Step Stopped missing is " + line,
                            0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Missing Cannot write queue file " +
                                trkId +
                                " to missing directory " +
                                missingDirectory.getAbsolutePath(),
                        2);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            throw new ProcessIncompleteException(
                    "Cannot write missing file " +
                            trkId +
                            " to missing directory " +
                            missingDirectory.getAbsolutePath());
        }
    }

    public static void missing(String missingDir, String trkId, String processStep)
            throws WriteMissingException, WriteLogFileException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";

            File sourceDirectory = new File(missingDir);
            if (!sourceDirectory.exists()) {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Missing Source Directory " + sourceDirectory.getAbsolutePath() + " not found",
                        2);
                System.exit(10);
            }

            writeFileToQueue(sourceDirectory.getAbsolutePath(), trkId, processStep);
            if (debugMode)
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Missing Source Directory " + sourceDirectory.getAbsolutePath() + " enqueued successfully",
                        0);

        } catch (WriteLogFileException e) {
            throw new WriteLogFileException(
                    "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
        } catch (IOException e) {
            throw new WriteMissingException(
                    "Cannot write missing file " + trkId + " to missing directory " + missingDir);
        }

    }

    /*
     * +---------------------------------------------------------------------------+
     * | |
     * | Merge Files into a ebco file |
     * | |
     * +---------------------------------------------------------------------------+
     */

    public static void merge(String baseDir, String trkId, String scanner) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }

        String directory_name = baseDir + "\\" + trkId;
        String file_name = directory_name + "\\" + trkId + "S.ebco";

        File f = new File(baseDir + "\\" + trkId);

        File ofile = new File(file_name);

        try {
            if (debugMode)
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Merge file " + ofile.getAbsolutePath() + " started ...",
                        0);
        } catch (WriteLogFileException e) {
            System.err.println(
                    "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        }

        if (ofile.exists()) {
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Merge Destination file " + ofile.getAbsolutePath() + " found. Will be deleted",
                            0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            ofile.delete();
        }

        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytesOutput;
        byte[] fileBytes;
        // int bytesRead = 0;

        if (xmlConverter.equalsIgnoreCase("CARGO")) {
            ConvertXmlFile.convertXmlFileCargo(baseDir, trkId);

            File fi = new File(baseDir + "\\" + trkId, trkId + ".xml");
            File fo = new File(baseDir + "\\" + trkId, trkId + "_.xml");
            if (fo.exists()) {
                fo.delete();
            }
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            String.format("XML - Source -> %s, Destination -> ", fi.getAbsolutePath(), fo.getPath()),
                            0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }

            fi.renameTo(fo);

            fi = new File(baseDir + "\\" + trkId, trkId + "_converted.xml");
            fo = new File(baseDir + "\\" + trkId, trkId + ".xml");
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            String.format(
                                    "XML CONVERTED - Source -> %s, Destination -> ",
                                    fi.getAbsolutePath(),
                                    fo.getPath()),
                            0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }

            fi.renameTo(fo);
        }

        if (scanner.equalsIgnoreCase("NUCHTECH")) {
            Image.convertJpegRgbToTiff(baseDir + "\\" + trkId, trkId, trkId + "S");
        }

        try {
            fos = new FileOutputStream(ofile, true);

            JsonObjectBuilder jb = Json.createObjectBuilder();
            JsonArrayBuilder ja = Json.createArrayBuilder();
            JsonObjectBuilder jab = Json.createObjectBuilder();

            jb.add("number_of_files", f.listFiles().length - 1);

            int numberOfBytes = 4096;
            int position = 4096;

            for (File file : f.listFiles(
                    (File dir, String name) -> name.toLowerCase().endsWith(".xml") ||
                            name.toLowerCase().endsWith(".tif") ||
                            name.toLowerCase().endsWith(".img") ||
                            name.toLowerCase().endsWith("_ocr.jpg") ||
                            name.toLowerCase().endsWith("s.jpg") ||
                            name.toLowerCase().endsWith(".json"))) {
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

            for (File file : f.listFiles(
                    (File dir, String name) -> name.toLowerCase().endsWith(".xml") ||
                            name.toLowerCase().endsWith(".tif") ||
                            name.toLowerCase().endsWith(".img") ||
                            name.toLowerCase().endsWith("_ocr.jpg") ||
                            name.toLowerCase().endsWith("s.jpg") ||
                            name.toLowerCase().endsWith(".json"))) {
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
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Merge file " + ofile.getAbsolutePath() + " done.",
                            0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Merge file " + ofile.getAbsolutePath() + " not complete. " + e.toString(),
                        2);
                throw new ProcessIncompleteException("Merge file " + ofile.getAbsolutePath() + " not complete.");
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }
    }

    /*
     * +---------------------------------------------------------------------------+
     * | |
     * | Extract Files into a ebco file |
     * | |
     * +---------------------------------------------------------------------------+
     */

    public static void split(String baseDir, String trkId) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }

        String directory_name = baseDir + "\\\\" + trkId;
        String file_name = directory_name + "\\\\" + trkId + "S.ebco";

        File inputFile = new File(file_name);
        RandomAccessFile inputStream;
        // int readLength = 1024;

        int readLengthVersion1 = 1024;
        int readLengthVersion2 = 4096;
        byte[] byteChunkPart;
        JsonReader jsonReader;
        String jsonDirectory;
        JsonObject object = null;

        try {
            if (debugMode)
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Split file " + inputFile.getAbsolutePath() + " started.",
                        0);
        } catch (WriteLogFileException e) {
            System.err.println(
                    "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
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
                    if (debugMode)
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Split file " + ofile.getAbsolutePath() + " generated successfully.",
                                0);
                } catch (WriteLogFileException e) {
                    System.err.println(
                            "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
                }
            }

            inputStream.close();

            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Split file " + inputFile.getAbsolutePath() + " done.",
                            0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Split file " + inputFile.getAbsolutePath() + " not complete.",
                        2);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            throw new ProcessIncompleteException("Split file " + inputFile.getAbsolutePath() + " not complete.");
        }
    }

    private static byte[] long2byte(long l) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Long.SIZE / 8);
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(l);
        byte[] result = baos.toByteArray();
        dos.close();
        return result;
    }

    private static long byte2long(byte[] b) throws IOException {
        ByteArrayInputStream baos = new ByteArrayInputStream(b);
        DataInputStream dos = new DataInputStream(baos);
        long result = dos.readLong();
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

            String filename = fileC.getName().toLowerCase();

            if (filename.endsWith("s.tif")) {
                tif = true;
            }
            if (filename.endsWith("s.img")) {
                img = true;
            }
            if (filename.endsWith(".xml")) {
                xml = true;
            }
            if (filename.endsWith("s_stamp.jpg")) {
                stampJpeg = true;
            }
            if (filename.endsWith("s.jpg")) {
                jpeg = true;
            }
        }
        // System.out.println(
        // "trk" + dirTruckCurrent + "img: " + img + " xml: " + xml + " jpeg: " + jpeg +
        // " tif: " + tif
        // + " stampJpeg: " + stampJpeg);

        if (img && xml && stampJpeg && jpeg && tif) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkAllNeedFilesNuchtech(File[] filesCopy, String dirTruckCurrent) {
        boolean img = false;
        boolean xml = false;
        boolean jpeg = false;

        for (File fileC : filesCopy) {
            if (fileC.isDirectory()) {
                continue;
            }

            String filename = fileC.getName().toLowerCase();

            if (filename.endsWith(".img")) {
                img = true;
            }
            if (filename.endsWith(".xml")) {
                xml = true;
            }
            if (filename.endsWith(".jpg")) {
                jpeg = true;
            }
        }
        // System.out.println("trk" + dirTruckCurrent + "img: " + img + " xml: " + xml +
        // " jpeg: " + jpeg);

        if (img && xml && jpeg) {
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

            String filename = fileC.getName().toLowerCase();

            if (filename.endsWith("s.img")) {
                img = true;
            }
            if (filename.endsWith(".xml")) {
                xml = true;
            }
            if (filename.endsWith("s_stamp.jpg")) {
                stampJpeg = true;
            }
            if (filename.endsWith("s.jpg")) {
                jpeg = true;
            }
        }
        // System.out.println("img: " + img + " xml: " + xml + " jpeg: " + jpeg + "
        // stampJpeg: " + stampJpeg);

        if (img && xml && stampJpeg && jpeg) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean sftp(
            String baseDir,
            String siteSFTPDestination,
            Integer siteSFTPPort,
            String siteSFTPUsername,
            String siteSFTPPassword,
            String fileName) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }

        if (fileName.length() != 18) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Filename length error " + fileName + " Length " + fileName.length(),
                        2);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            return false;
        }
        File file = new File(baseDir, fileName);
        if (!file.exists()) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Filename not exist " + fileName, 2);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            return false;
        }
        if (!file.isDirectory()) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Filename is not a directory " + fileName,
                        2);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            return false;
        }

        String yearMonth = fileName.substring(0, 6);
        String yearMonthDay = fileName.substring(0, 8);
        // String siteId = fileName.substring(8, 14);
        String targetDirNameLevel1 = "./" + yearMonth;
        String targetDirNameLevel2 = targetDirNameLevel1 + "/" + yearMonthDay;
        String targetDirNameLevel3 = targetDirNameLevel2 + "/" + fileName;
        SftpATTRS attrs = null;

        ChannelSftp sftp;

        try {
            sftp = setupJsch(siteSFTPDestination, siteSFTPPort, siteSFTPUsername, siteSFTPPassword);
            sftp.connect();
            try {
                File[] files = file.listFiles();
                try {
                    attrs = sftp.stat(targetDirNameLevel1);
                } catch (Exception e) {
                    try {
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Directory Level 1 not exists. Will be created " + targetDirNameLevel1 + ".",
                                0);
                        sftp.mkdir(targetDirNameLevel1);
                    } catch (WriteLogFileException e1) {
                        System.err.println(
                                "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }
                }
                try {
                    attrs = sftp.stat(targetDirNameLevel2);
                } catch (Exception e) {
                    try {
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Directory Level 2 not exists. Will be created " + targetDirNameLevel2 + ".",
                                0);
                        sftp.mkdir(targetDirNameLevel2);
                    } catch (WriteLogFileException e1) {
                        System.err.println(
                                "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }
                }
                try {
                    attrs = sftp.stat(targetDirNameLevel3);
                } catch (Exception e) {
                    sftp.mkdir(targetDirNameLevel3);
                }

                for (File fileCopy : files) {
                    uploadAndVerifyFile(sftp, targetDirNameLevel3, fileCopy.getAbsolutePath());
                }
            } catch (ProcessIncompleteException e) {
                throw new ProcessIncompleteException(e.toString());
            } catch (SftpException e) {
                throw new ProcessIncompleteException(e.toString());
            } catch (Exception e) {
                throw new ProcessIncompleteException(e.toString());
            }
            sftp.exit();
        } catch (JSchException e) {
            throw new ProcessIncompleteException(e.toString());
        } catch (Exception e) {
            throw new ProcessIncompleteException(e.toString());
        }

        return true;
    }

    private static ChannelSftp setupJsch(String remoteHost, Integer remotePort, String username, String password)
            throws JSchException {
        JSch jsch = new JSch();
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        Session jschSession = jsch.getSession(username, remoteHost);
        jschSession.setPassword(password);
        jschSession.setConfig(config);
        jschSession.setPort(remotePort);
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    private static void uploadAndVerifyFile(ChannelSftp sftp, String dirName, String filename)
            throws ProcessIncompleteException {
        final String filePath = dirName + "/" + FilenameUtils.getName(filename);
        try {
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Uploading file " + filename + " to " + filePath + " is starting...",
                            0);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }

            sftp.put(filename, filePath);
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Uploaded file " + filename + " to " + filePath + " complete.",
                            0);
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (Exception e) {
            try {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Cannot upload file " + filename + " to " + filePath,
                        2);
                throw new ProcessIncompleteException(e.toString());
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }
    }

    public static void deleteDirectory(String baseDir, String trkId) throws ProcessIncompleteException {
        File sourceDirectory = new File(baseDir + "\\\\" + trkId);
        try {
            FileUtils.deleteDirectory(sourceDirectory);
            if (debugMode)
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Directory " + sourceDirectory.getAbsolutePath() + " Deleted.",
                        0);
        } catch (IOException | WriteLogFileException e) {
            e.printStackTrace();
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Moving files from " +
                                    sourceDirectory.getAbsolutePath() +
                                    " to " +
                                    sourceDirectory.getAbsolutePath() +
                                    " Error. " +
                                    e.toString(),
                            0);
                throw new ProcessIncompleteException(e.toString());
            } catch (WriteLogFileException e1) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }
    }
}

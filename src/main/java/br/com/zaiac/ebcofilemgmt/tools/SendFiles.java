package br.com.zaiac.ebcofilemgmt.tools;

import static br.com.zaiac.ebcofilemgmt.tools.MergeFiles.missing;
import static br.com.zaiac.ebcofilemgmt.tools.MergeFiles.readStringFromMissingFile;

import br.com.zaiac.ebcofilemgmt.exception.ProcessIncompleteException;
import br.com.zaiac.ebcofilemgmt.exception.WriteMissingException;
import br.com.zaiac.ebcolibrary.LogApp;
//import br.com.zaiac.ebcolibrary.Util;
import br.com.zaiac.ebcolibrary.exceptions.WriteLogFileException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class SendFiles {

    private static String logDirectory;
    public static Boolean debugMode;

    // private static SftpClientFactory sftpClientFactory;

    public static void queue(
            String baseDir,
            String moveDir,
            String missingDir,
            String siteDestination,
            String siteSFTPDestination,
            Integer siteSFTPPort,
            String siteSFTPUsername,
            String siteSFTPPassword,
            Boolean deleteSource) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }

        try {
            if (debugMode)
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Queue process started...", 0);
        } catch (WriteLogFileException e) {
            System.err.println(
                    "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        }

        // Read Missing files
        String trkId = "";
        String processStep = "";

        try {
            try {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Reading missing queue started...",
                            0);
            } catch (WriteLogFileException e) {
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }

            File missingList = new File(new File("").getCanonicalPath() + "\\\\missing");
            if (missingList.exists() && missingList.isDirectory()) {
                File[] filesMissing = missingList.listFiles();
                Arrays.sort(filesMissing);
                for (File file : filesMissing) {
                    trkId = file.getName();
                    File baseDirFile = new File(baseDir + "\\\\" + trkId);

                    if (!baseDirFile.exists()) {
                        try {
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Source Directory " +
                                            file.getAbsolutePath() +
                                            " not found. Missing for " +
                                            file.getAbsolutePath() +
                                            " will be deleted",
                                    2);
                            file.delete();
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

                    try {
                        File fileCopy = new File(baseDirFile.getAbsolutePath());
                        File[] filesCopy = fileCopy.listFiles();

                        if (!MergeFiles.checkAllNeedFilesEbco(filesCopy, trkId)) {
                            throw new ProcessIncompleteException();
                        }

                        String currentProcessStep = readStringFromMissingFile(
                                missingList.getAbsolutePath(),
                                trkId);
                        Integer currentProcessStepSequence = 0;
                        if (currentProcessStep.equalsIgnoreCase("sftp"))
                            currentProcessStepSequence = 1;
                        if (currentProcessStep.equalsIgnoreCase("deleteDirectory"))
                            currentProcessStepSequence = 2;

                        processStep = "sftp";
                        if (currentProcessStepSequence < 1) {
                            sftp(
                                    baseDir,
                                    siteDestination,
                                    siteSFTPDestination,
                                    siteSFTPPort,
                                    siteSFTPUsername,
                                    siteSFTPPassword,
                                    trkId);
                            missing(missingDir, trkId, processStep);
                        }

                        if (deleteSource) {
                            processStep = "deleteDirectory";
                            if (currentProcessStepSequence < 1) {
                                SendFiles.deleteDirectory(baseDir, trkId);
                                missing(missingDir, trkId, processStep);
                            }
                            try {
                                if (debugMode)
                                    LogApp.writeLineToFile(
                                            logDirectory,
                                            Constants.LOGFILE,
                                            "Missing for " + file.getAbsolutePath() + " deleted.",
                                            0);
                                file.delete();
                            } catch (WriteLogFileException e) {
                                System.err.println(
                                        "Cannot write log file Directory " +
                                                logDirectory +
                                                " file name " +
                                                Constants.LOGFILE);
                                System.exit(10);
                            }
                        }
                    } catch (WriteMissingException e) {
                        System.err.println(
                                "Cannot write Missing file Directory " + missingDir);
                        System.exit(10);
                    } catch (WriteLogFileException e) {
                        System.err.println(
                                "Cannot write Log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);

                    } catch (ProcessIncompleteException e) {
                        try {
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Incomplete Process Issue for Truck Id " + trkId,
                                    2);
                        } catch (WriteLogFileException e2) {
                            System.err.println(
                                    "Cannot write log file Directory " +
                                            logDirectory +
                                            " file name " +
                                            Constants.LOGFILE);
                            System.exit(10);
                        }
                    }
                }
            } else {
                try {
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            String.format("Missing Directory %s not exists", missingList.getAbsoluteFile()),
                            2);
                } catch (WriteLogFileException e) {
                    System.err.println(
                            "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
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
                System.err.println(
                        "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }

            // Read Queue Files

            File queueList = new File(new File("").getCanonicalPath() + "\\\\queue");
            if (queueList.exists() && queueList.isDirectory()) {
                File[] filesQueue = queueList.listFiles();
                Arrays.sort(filesQueue);

                for (File file : filesQueue) {
                    trkId = file.getName();
                    File baseDirFile = new File(baseDir + "\\\\" + trkId);

                    if (!baseDirFile.exists()) {
                        try {
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Source Directory " +
                                            file.getAbsolutePath() +
                                            " not found. Queue for " +
                                            file.getAbsolutePath() +
                                            " will be deleted",
                                    2);
                            file.delete();
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

                    try {
                        if (debugMode)
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Queue for " + file.getAbsolutePath() + " deleted.",
                                    0);
                        file.delete();
                    } catch (WriteLogFileException e) {
                        System.err.println(
                                "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }

                    try {
                        File fileCopy = new File(baseDirFile.getAbsolutePath());
                        File[] filesCopy = fileCopy.listFiles();

                        processStep = "start";
                        if (!MergeFiles.checkAllNeedFilesEbco(filesCopy, trkId)) {
                            throw new ProcessIncompleteException();
                        }

                        sftp(
                                baseDir,
                                siteDestination,
                                siteSFTPDestination,
                                siteSFTPPort,
                                siteSFTPUsername,
                                siteSFTPPassword,
                                trkId);
                        processStep = "sftp";
                        SendFiles.deleteDirectory(baseDir, trkId);
                        processStep = "deleteDirectory";
                    } catch (ProcessIncompleteException e) {
                        try {
                            MergeFiles.missing(missingDir, trkId, processStep);
                        } catch (WriteMissingException e2) {
                            System.err.println(
                                    "Cannot write Missing file Directory " + missingDir);
                            System.exit(10);
                        } catch (WriteLogFileException e2) {
                            System.err.println(
                                    "Cannot write Log file Directory " + logDirectory + " file name "
                                            + Constants.LOGFILE);
                            System.exit(10);
                        }
                    }
                }

            } else {
                try {
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            String.format("Queue Directory %s not exists", queueList.getAbsoluteFile()),
                            2);
                } catch (WriteLogFileException e) {
                    System.err.println(
                            "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
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

    public static boolean sftp(
            String baseDir,
            String siteDestination,
            String siteSFTPDestination,
            Integer siteSFTPPort,
            String siteSFTPUsername,
            String siteSFTPPassword,
            String fileName) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";

            if (fileName.length() != 18) {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Filename length error " + fileName + " Length " + fileName.length(),
                        2);
                return false;
            }
            File file = new File(baseDir, fileName);
            if (!file.exists()) {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Filename not exist " + fileName, 2);
                return false;
            }
            if (!file.isDirectory()) {
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Filename is not a directory " + fileName,
                        2);
                return false;
            }

            String yearMonth = fileName.substring(0, 6);
            String yearMonthDay = fileName.substring(0, 8);
            String siteId = fileName.substring(8, 14);
            String targetDirNameLevel1 = "./" + yearMonth;
            String targetDirNameLevel2 = targetDirNameLevel1 + "/" + yearMonthDay;
            String targetDirNameLevel3 = targetDirNameLevel2 + "/" + fileName;
            SftpATTRS attrs = null;

            ChannelSftp sftp;

            sftp = setupJsch(siteSFTPDestination, siteSFTPPort, siteSFTPUsername, siteSFTPPassword);
            sftp.connect();
            try {
                File[] files = file.listFiles();
                try {
                    attrs = sftp.stat(targetDirNameLevel1);
                } catch (Exception e) {
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Directory Level 1 not exists. Will be created " + targetDirNameLevel1 + ".",
                            0);
                    sftp.mkdir(targetDirNameLevel1);
                }
                try {
                    attrs = sftp.stat(targetDirNameLevel2);
                } catch (Exception e) {
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Directory Level 2 not exists. Will be created " + targetDirNameLevel2 + ".",
                            0);
                    sftp.mkdir(targetDirNameLevel2);
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
        } catch (WriteLogFileException e1) {
            System.err.println(
                    "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
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

    public static void uploadObject(
            String projectId,
            String bucketName,
            String baseDir,
            String trkId,
            String keyDir,
            String GoogleApplicationCredentials)
            throws ProcessIncompleteException {

        try {
            try {
                logDirectory = new File("").getCanonicalPath() + "\\\\logs";
            } catch (IOException e) {
                System.err.print("Cannot get Local Path for Log Directory");
                System.exit(10);
            }

            // System.out.println("logDirectory " + logDirectory);

            File GoogleApplicationCredentialsFile = null;
            GoogleCredentials credentials = null;

            try {
                GoogleApplicationCredentialsFile = new File(
                        new File("").getCanonicalPath() + "\\\\" + GoogleApplicationCredentials);
                if (!GoogleApplicationCredentialsFile.exists()) {
                    try {
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "UploadObject Google Credentials File " +
                                        GoogleApplicationCredentialsFile.getAbsolutePath() +
                                        " not Exists",
                                2);
                        System.exit(10);
                    } catch (WriteLogFileException e) {
                        e.printStackTrace();
                        System.err.println(
                                "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }
                }
                try {
                    credentials = GoogleCredentials.fromStream(
                            new FileInputStream(GoogleApplicationCredentialsFile))
                            .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

                    String objectNamePie = trkId + "/" + trkId + "S.pie";
                    String filePathPie = baseDir + "\\\\" + trkId + "\\\\" + trkId + "S.pie";

                    String objectNameKey = trkId + "/" + "publicKey";
                    String filePathKey;

                    if (keyDir == null || keyDir.equalsIgnoreCase("")) {
                        filePathKey = keyDir + "KeyPair\\\\" + "publicKey";
                    } else {
                        filePathKey = keyDir + "\\\\" + "publicKey";
                    }

                    Storage storage = StorageOptions.newBuilder()
                            .setCredentials(credentials)
                            .setProjectId(projectId)
                            .build()
                            .getService();
                    try {
                        BlobId blobIdKey = BlobId.of(bucketName, objectNameKey);
                        BlobInfo blobInfoKey = BlobInfo.newBuilder(blobIdKey).build();

                        if (debugMode)
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Sending key file " + new File(filePathKey).getAbsolutePath() + " started...",
                                    0);
                        storage.create(blobInfoKey, Files.readAllBytes(Paths.get(filePathKey)));
                        if (debugMode)
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Sending key file " + new File(filePathKey).getAbsolutePath() + " done.",
                                    0);

                        BlobId blobIdPie = BlobId.of(bucketName, objectNamePie);
                        BlobInfo blobInfoPie = BlobInfo.newBuilder(blobIdPie).build();

                        if (debugMode)
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Sending pie file " + new File(filePathPie).getAbsolutePath() + " started...",
                                    0);
                        storage.create(blobInfoPie, Files.readAllBytes(Paths.get(filePathPie)));
                        if (debugMode)
                            LogApp.writeLineToFile(
                                    logDirectory,
                                    Constants.LOGFILE,
                                    "Sending pie file " + new File(filePathPie).getAbsolutePath() + " done.",
                                    0);
                    } catch (IOException e) {
                        throw new ProcessIncompleteException(e.toString());
                    } catch (Exception e) {
                        throw new ProcessIncompleteException(e.toString());
                    }

                } catch (ProcessIncompleteException e) {
                    throw new ProcessIncompleteException(e.toString());
                } catch (IOException e) {
                    try {
                        LogApp.writeLineToFile(
                                logDirectory,
                                Constants.LOGFILE,
                                "Error reading Google Credentials File " +
                                        GoogleApplicationCredentialsFile.getAbsolutePath() +
                                        " not Exists",
                                2);
                        System.exit(10);
                    } catch (WriteLogFileException e1) {
                        System.err.println(
                                "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }
                }
            } catch (ProcessIncompleteException e) {
                throw new ProcessIncompleteException(e.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                try {
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "UploadObject Google Credentials File " +
                                    GoogleApplicationCredentials +
                                    " not Exists",
                            2);
                    System.exit(10);
                } catch (WriteLogFileException e1) {
                    System.err.println(
                            "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
                }
            } catch (IOException e) {
                try {
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Error reading Google Credentials File " +
                                    GoogleApplicationCredentialsFile.getAbsolutePath() +
                                    " not Exists",
                            2);
                    System.exit(10);
                } catch (WriteLogFileException e1) {
                    System.err.println(
                            "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                    System.exit(10);
                }
            }
        } catch (ProcessIncompleteException e) {
            throw new ProcessIncompleteException(e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);

        }

    }

    public static void moveFiles(String baseDir, String moveDir, String trkId, String scanner)
            throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }

        // String xmlFile = Util.getBaseFileNameFromDirectory(baseDir + "\\" + trkId);

        // System.out.println("xmlFile " + xmlFile);
        String yearMonth = null;
        String yearMonthDay = null;

        if (scanner.equalsIgnoreCase("SMITHS")) {
            yearMonth = trkId.substring(0, 6);
            yearMonthDay = trkId.substring(0, 8);
        } else {
            yearMonth = trkId.substring(9, 13);
            yearMonthDay = trkId.substring(13, 17);
        }

        File sourceDirectory = new File(baseDir + "\\\\" + trkId);
        File destinationDirectory = new File(moveDir + "\\\\" + yearMonth + "\\\\" + yearMonthDay);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }

        destinationDirectory = new File(moveDir + "\\\\" + yearMonth + "\\\\" + yearMonthDay + "\\\\" + trkId);

        try {
            if (debugMode)
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Moving files from " +
                                sourceDirectory.getAbsolutePath() +
                                " to " +
                                sourceDirectory.getAbsolutePath() +
                                " Started...",
                        0);
            if (destinationDirectory.exists()) {
                if (debugMode)
                    LogApp.writeLineToFile(
                            logDirectory,
                            Constants.LOGFILE,
                            "Moving Destination Directory Exist " +
                                    sourceDirectory.getAbsolutePath() +
                                    ". Will be Deleted.",
                            0);
                FileUtils.deleteDirectory(destinationDirectory);
            }

            FileUtils.moveDirectory(sourceDirectory, destinationDirectory);

            if (debugMode)
                LogApp.writeLineToFile(
                        logDirectory,
                        Constants.LOGFILE,
                        "Moving files from " +
                                sourceDirectory.getAbsolutePath() +
                                " to " +
                                sourceDirectory.getAbsolutePath() +
                                " Done...",
                        0);
        } catch (IOException | WriteLogFileException e) {
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

    public static String convertStringToBase64(String value) {
        byte[] byteData = value.getBytes();
        String base64String = Base64.getEncoder().encodeToString(byteData);
        return base64String;
    }
}

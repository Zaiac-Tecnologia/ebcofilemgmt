package br.com.zaiac.ebcofilemgmt.tools;

import br.com.zaiac.ebcofilemgmt.exception.SendInformationException;
import br.com.zaiac.ebcofilemgmt.rest.MethodPost;
import br.com.zaiac.ebcolibrary.LogApp;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.io.FilenameUtils;

public class Monitor {
        public static String siteId;
        public static String urlBackEnd;
        public static String diskMonitor;
        public static Integer pingSite;
        public static String sourceSite;
        
        private static String logDirectory;
        public static Boolean debugMode;
        


    public static String getDiskInformation(String diskMonitor) {
        String disks[] = diskMonitor.split(",");
        StringBuffer ret = new StringBuffer();
        ret.append("\"discos\": [");
        for (int i = 0; i < disks.length; i++) {
            File file = new File(disks[i]);
            long totalSpace = file.getTotalSpace();
            long freeSpace = file.getFreeSpace();
            ret.append(String.format("{\"drive\": \"%s\", \"total\": %.0f, \"livre\": %.0f}", disks[i], (double)(totalSpace /1024 /1024), (double)(freeSpace/1024 /1024)));
            if (i < disks.length - 1) {
                ret.append(", ");
            }
        }
        ret.append("]");
        return ret.toString();
    }
    
    
    public static String getMemoryInformation() {
        //MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        double memorySize = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getTotalPhysicalMemorySize();
        double memoryFree = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getFreePhysicalMemorySize();
        return String.format("{\"total\": %.0f, \"livre\": %.0f}", (double)(memorySize /1024 /1024), (double)(memoryFree/1024 /1024));
    }
    
    public static String getCpuInformation() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();        
        //System.out.println("Load Average: " + operatingSystemMXBean.getSystemLoadAverage());
        //System.out.println("Available Processors: " + operatingSystemMXBean.getAvailableProcessors());
        return String.format("{\"total\": %.0f}", (double)operatingSystemMXBean.getAvailableProcessors());
    }
    
    public static String getAppVersionInformation(String dir) throws IOException {
        SimpleDateFormat dt = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");
        File file = new File(dir);
        
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean value;
                // return files only that begins with test
                if(pathname.getName().endsWith(".ver")){
                    value=true;
                }
                else{
                    value=false;
                }
                return value;
            }
        });
        
        StringBuffer ret = new StringBuffer();
        ret.append("\"versoes\": [");
        for (int x = 0; x < files.length; x++) {            
            String version = LogApp.readStringFromFile(files[x].getPath());
            String fileNameWithOutExt = FilenameUtils.removeExtension(files[x].getName());
            Date lastModified = new Date(files[x].lastModified());
            ret.append(String.format("{\"arquivo\": \"%s\", \"versao\": \"%s\", \"dataultimaexecucao\": \"%s\"}", fileNameWithOutExt, version, dt.format(lastModified)));
            if (x < files.length -1) {
                ret.append(", ");
            }
        }
        ret.append("]");
        return ret.toString();
    }
    
    public static void getCpuJvmInformation() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for(Long threadID : threadMXBean.getAllThreadIds()) {
            ThreadInfo info = threadMXBean.getThreadInfo(threadID);
            System.out.println("Thread name: " + info.getThreadName());
            System.out.println("Thread State: " + info.getThreadState());
            System.out.println(String.format("CPU time: %s ns", threadMXBean.getThreadCpuTime(threadID)));
        }        
    }
    
    public static void sendInformationToBackEnd(Boolean force) {
        String canonicalPath;
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        
        String versao = Constants.APP_VERSION;
        String filemgmt = Constants.LOGFILE;          
        
        try {
            canonicalPath = new File("").getCanonicalPath();

            File f = new File(canonicalPath, filemgmt + ".ver");
            if (!f.exists()) {
                try {
                    LogApp.writeVersionToFile(canonicalPath, filemgmt + ".ver", Constants.APP_VERSION);
                } catch (Exception e) {
                    System.err.print(String.format("Cannot write version file to %f. Check directory or permissions", canonicalPath));
                    System.exit(10);
                }
                return;
            }
            
            Calendar dataSys = Calendar.getInstance();
            Calendar dataFile = Calendar.getInstance();
            dataFile.setTime(new Date(f.lastModified()));
            
            dataSys.add(Calendar.SECOND, pingSite * -1);

            if (dataSys.after(dataFile) || force) {
                try {
                    LogApp.writeVersionToFile(canonicalPath, filemgmt + ".ver", Constants.APP_VERSION);
                } catch (Exception e) {
                    System.err.print(String.format("Cannot write version file to %f. Check directory or permissions", canonicalPath));
                    System.exit(10);
                }
                try {
                    
                    try {
                        if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, String.format("Request Start to Backend %s", urlBackEnd), 0);
                    } catch(IOException e1) {
                        System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }            
                    
                    StringBuffer ret = new StringBuffer();
                    ret.append("{");
                    ret.append(String.format("\"site\": \"%s\"",  siteId));
                    ret.append(", ");                    
                    ret.append(String.format("\"origem\": \"%s\"",  sourceSite));
                    ret.append(", ");
                    ret.append(Monitor.getDiskInformation(diskMonitor));
                    ret.append(", ");
                    ret.append("\"cpu\": " + Monitor.getCpuInformation());
                    ret.append(", ");
                    ret.append("\"memoria\": " + Monitor.getMemoryInformation());
                    ret.append(", ");
                    ret.append(Monitor.getAppVersionInformation(canonicalPath));
                    ret.append("}");
                    MethodPost.httpPing(ret.toString(), urlBackEnd);
                    
                    try {
                        if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, String.format("Request Complete to Backend %s", urlBackEnd), 0);
                        if (debugMode) LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, String.format("Sent Information %s", ret.toString()), 0);
                    } catch(IOException e1) {
                        System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                        System.exit(10);
                    }            
                } catch (Exception e) {
                    System.err.println(e.toString());
                    throw new SendInformationException(e.toString());
                }
            }
        } catch (Exception e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, e.toString(), 2);
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }            
        }
    }
    
    
    public static void getMemoryJvmInformation() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        System.out.println(String.format("Initial memory: %.2f GB", (double)memoryMXBean.getHeapMemoryUsage().getInit() /1073741824));
        System.out.println(String.format("Used heap memory: %.2f GB", (double)memoryMXBean.getHeapMemoryUsage().getUsed() /1073741824));
        System.out.println(String.format("Max heap memory: %.2f GB", (double)memoryMXBean.getHeapMemoryUsage().getMax() /1073741824));
        System.out.println(String.format("Committed memory: %.2f GB", (double)memoryMXBean.getHeapMemoryUsage().getCommitted() /1073741824)); 
    }
    
    
    
    public static void getCpuLoadInformation() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();        
        double processCpuLoad = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getProcessCpuLoad();      
        double systemCpuLoad = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getSystemCpuLoad();
        double processCpuTime = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getProcessCpuTime();       
        System.out.println(String.format("Process Cpu Load Size %.2f", processCpuLoad));
        System.out.println(String.format("System Cpu Load Size %.2f", systemCpuLoad));
        System.out.println(String.format("Process Cpu Time Size %.2f", processCpuTime));
    }
    
}

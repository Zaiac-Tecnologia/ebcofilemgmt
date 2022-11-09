package br.com.zaiac.ebcofilemgmt.cryptography;

import br.com.zaiac.ebcofilemgmt.exception.ProcessIncompleteException;
import br.com.zaiac.ebcofilemgmt.tools.Constants;
import br.com.zaiac.ebcolibrary.LogApp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AsymmetricCryptography {
    private static String logDirectory;
    
    
    public AsymmetricCryptography() {
    }
    
    static private void processFile(Cipher ci, InputStream in, OutputStream out)
    {
        try {
            byte[] ibuf = new byte[1024];
            int len;
            while ((len = in.read(ibuf)) != -1) {
                byte[] obuf = ci.update(ibuf, 0, len);
                if ( obuf != null ) out.write(obuf);
            }
            byte[] obuf = ci.doFinal();
            if ( obuf != null ) out.write(obuf);
        } catch (Exception e) {
            System.out.println("Erro ao processFile " + e.toString());
        }
    }    
    
    // https://docs.oracle.com/javase/8/docs/api/java/security/spec/PKCS8EncodedKeySpec.html
    static public PrivateKey getPrivate(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    // https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
    static public PublicKey getPublic(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    
    
/*
  +---------------------------------------------------------------------------+
  |                                                                           |
  |      Encryption file                                                      |
  |                                                                           |      
  +---------------------------------------------------------------------------+  
*/    
    
    static public void encryptFile(File input, File output, String keyDir) throws ProcessIncompleteException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        try {
            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt Source file " + input.getAbsolutePath() + " started...", 0);
        } catch(IOException e) {
            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        }
        
        
        
        SecureRandom srandom = new SecureRandom();
        
        //        
        // 1. Geração da Chave AES
        //
        KeyGenerator kgen;
        SecretKey skey = null;
        try {
            
            kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            skey = kgen.generateKey();  
            
        } catch (NoSuchAlgorithmException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "NoSuchAlgorithmException", 2);
                throw new ProcessIncompleteException("NoSuchAlgorithmException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }   

        }
        
        
        //
        // 2 Inicializando o Vetor de mesmo tamanho
        //
        
        byte[] iv = new byte[128/8];
        srandom.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        PrivateKey privateKey = null;
        
        try {            
            privateKey = getPrivate(keyDir + "\\\\privateKey");
        } catch (Exception e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt error getting PrivateKey", 2);
                throw new ProcessIncompleteException("Encrypt error getting PrivateKey");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }   
        }
        
        // 3. Save the AES Key
        
        if (output.exists()) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt destination file " + output.getAbsolutePath() + " exists. Will be deleted.", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }            
            output.delete();
        }
        
        FileOutputStream out;
        
        try {
            out = new FileOutputStream(output);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] b = cipher.doFinal(skey.getEncoded());
            out.write(b);

            // 4. Write the Initialization Vector

            out.write(iv);

            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);

            FileInputStream in = new FileInputStream(input);
            processFile(ci, in, out);
            
            in.close();
            
            out.flush();
            out.close();

            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt destination file " + output.getAbsolutePath() + " done.", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
            
            
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Source file " + input.getAbsolutePath() + " deleted.", 0);
                input.delete();
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (NoSuchAlgorithmException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt NoSuchAlgorithmException " + e.toString(), 2);
                throw new ProcessIncompleteException("NoSuchAlgorithmException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (FileNotFoundException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt FileNotFoundException " + e.toString(), 2);
                throw new ProcessIncompleteException("FileNotFoundException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (InvalidKeyException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt InvalidKeyException " + e.toString(), 2);
                throw new ProcessIncompleteException("InvalidKeyException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (NoSuchPaddingException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt NoSuchPaddingException "  + e.toString(), 2);
                throw new ProcessIncompleteException("NoSuchPaddingException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt IOException "  + e.toString(), 2);
                throw new ProcessIncompleteException("IOException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IllegalBlockSizeException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt IllegalBlockSizeException "  + e.toString(), 2);
                throw new ProcessIncompleteException("IllegalBlockSizeException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (InvalidAlgorithmParameterException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt InvalidAlgorithmParameterException "  + e.toString(), 2);
                throw new ProcessIncompleteException("InvalidAlgorithmParameterException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (BadPaddingException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Encrypt BadPaddingException "  + e.toString(), 2);
                throw new ProcessIncompleteException("BadPaddingException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }
        
    }
    
/*
  +---------------------------------------------------------------------------+
  |                                                                           |
  |      Decryption file                                                      |
  |                                                                           |      
  +---------------------------------------------------------------------------+  
*/    
    
    
    static public void decryptFile(File input, File output, String keyDir) throws ProcessIncompleteException { 
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }
        
        PublicKey publicKey = null;
        try {
            
            publicKey = getPublic(keyDir + "\\\\publicKey");
            
        } catch (Exception e) {            
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt error getting PrivateKey", 2);
                throw new ProcessIncompleteException("Exception");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }   
            
            
        }
        if (output.exists()) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt destination file " + output.getAbsolutePath() + " exists. Will be deleted.", 2);
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }   
            
            output.delete();
        }
        
        try {
            FileOutputStream out = new FileOutputStream(output);                


            FileInputStream in = new FileInputStream(input);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] b = new byte[128];
            in.read(b);
            byte[] keyb = cipher.doFinal(b);
            SecretKeySpec skey = new SecretKeySpec(keyb, "AES");
            //
            // 3 Reading initializing vector
            //

            byte[] iv = new byte[128/8];
            in.read(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            //
            //
            //
            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.DECRYPT_MODE, skey, ivspec);        
            processFile(ci, in, out);
            
            in.close();
            out.flush();
            out.close();
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt destination file " + output.getAbsolutePath() + " done.", 0);
            } catch(IOException e) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
            
        } catch (NoSuchAlgorithmException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt NoSuchAlgorithmException " + e.toString(), 2);
                throw new ProcessIncompleteException("NoSuchAlgorithmException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (FileNotFoundException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt FileNotFoundException " + e.toString(), 2);
                throw new ProcessIncompleteException("FileNotFoundException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        } catch (InvalidKeyException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt InvalidKeyException " + e.toString(), 2);
                throw new ProcessIncompleteException("InvalidKeyException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (NoSuchPaddingException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt NoSuchPaddingException "  + e.toString(), 2);
                throw new ProcessIncompleteException("NoSuchPaddingException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IOException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt IOException "  + e.toString(), 2);
                throw new ProcessIncompleteException("IOException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (IllegalBlockSizeException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt IllegalBlockSizeException "  + e.toString(), 2);
                throw new ProcessIncompleteException("IllegalBlockSizeException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (InvalidAlgorithmParameterException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt InvalidAlgorithmParameterException "  + e.toString(), 2);
                throw new ProcessIncompleteException("InvalidAlgorithmParameterException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
            
        } catch (BadPaddingException e) {
            try {
                LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Dencrypt BadPaddingException "  + e.toString(), 2);
                throw new ProcessIncompleteException("BadPaddingException");
            } catch(IOException e1) {
                System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
                System.exit(10);
            }
        }

    }
    
    
    static public void encryptFile(String baseDir, String keyDir, String trkId) throws ProcessIncompleteException {
        SecureRandom srandom = new SecureRandom();
        String directory_name = baseDir + "\\\\" + trkId;
        File inputFile = new File(directory_name + "\\\\" + trkId + "S.ebco");
        File outputFile = new File(directory_name + "\\\\" + trkId + "S.pie");
        encryptFile(inputFile, outputFile, keyDir);
    }
    

    static public void decryptFile(String baseDir, String keyDir, String trkId) throws ProcessIncompleteException {
        SecureRandom srandom = new SecureRandom();
        
        String directory_name = baseDir + "\\\\" + trkId;
        File inputFile = new File(directory_name + "\\\\" + trkId + "S.pie");
        File outputFile = new File(directory_name + "\\\\" + trkId + "S.ebco");
        
        decryptFile(inputFile, outputFile, keyDir);
    }
    
    
}

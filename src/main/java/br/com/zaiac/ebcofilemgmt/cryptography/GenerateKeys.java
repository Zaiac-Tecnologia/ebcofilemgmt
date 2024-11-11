package br.com.zaiac.ebcofilemgmt.cryptography;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class GenerateKeys {
    private final KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public GenerateKeys(int keylength) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keylength);
        createKeys();
        File file;

        file = new File("KeyPair/publicKey");
        if (!file.exists()) {
            writeToFile("KeyPair/publicKey", publicKey.getEncoded());
        }
        file = new File("KeyPair/privateKey");
        if (!file.exists()) {
            writeToFile("KeyPair/privateKey", privateKey.getEncoded());
        }
    }

    public void createKeys() {
        pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void writeToFile(String path, byte[] key) {
        File f = new File(path);
        FileOutputStream fos;

        f.getParentFile().mkdirs();

        try {
            fos = new FileOutputStream(f);
            fos.write(key);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.out.println("Não consegui gravar o arquivo " + f.getAbsolutePath());
            System.exit(10);
        }
    }

    public static void createCertificates() {
        File file;
        Scanner sc = new Scanner(System.in);

        file = new File("KeyPair/privateKey");
        if (file.exists()) {
            while (true) {
                System.out.print("Chave Privada já existe, deseja recriar (Sim/Não) ? ");
                String response = sc.nextLine();
                if ("SimsimNaoNãonaonão".contains(response)) {
                } else {
                    System.out.println("Resposta invalida digite Sim/Não");
                    continue;
                }
                if ("NãonãoNaonao".contains(response)) {
                    System.out.println("Nenhuma ação foi tomada");
                    System.exit(0);
                }
                file.delete();
                break;
            }
        }
        sc.close();

        file = new File("KeyPair/publicKey");
        if (file.exists()) {
            file.delete();
        }
        // GenerateKeys gk;
        // try {
        // GenerateKeys gk = new GenerateKeys(1024);
        System.out.println("Chaves foram geradas com sucesso no diretorio: " + file.getParent());
        System.out.println("+-----------------------------------------------------------------------+");
        System.out.println("!===> OBSERVACAO <======================================================!");
        System.out.println("!     ==========                                                        !");
        System.out.println("!     1) Armazene com segurança a sua chave privada pois ela sera       !");
        System.out.println("!        necessária para  a criptografia dos  arquivos que  serão       !");
        System.out.println("!        enviados para a Nuvem.                                         !");
        System.out.println("!     2) Armazene  com  segurança a  sua  chave publica, pois ela       !");
        System.out.println("!        será necessaria para a descriptografia dos  arquivos que       !");
        System.out.println("!        virão da Nuvem.                                                !");
        System.out.println("+-----------------------------------------------------------------------+");
        // } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException e1)
        // {
        // System.out.println("Programas com a geração das chaves" + e1.toString());
        // }
    }

}

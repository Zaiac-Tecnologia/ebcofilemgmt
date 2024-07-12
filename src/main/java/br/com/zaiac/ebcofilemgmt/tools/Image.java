package br.com.zaiac.ebcofilemgmt.tools;

import br.com.zaiac.ebcolibrary.LogApp;
import br.com.zaiac.ebcolibrary.exceptions.WriteLogFileException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bytedeco.opencv.opencv_core.Mat;

public class Image {
    private static String logDirectory;

    public static BufferedImage toBufferedImage(Mat mat) {
        int type = 0;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else {
            throw new IllegalArgumentException("Unsupported number of channels: " + mat.channels());
        }
        BufferedImage bufferedImage = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        mat.data().get(targetPixels);
        return bufferedImage;
    }

    public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }

    public static BufferedImage convert(BufferedImage src, int bufImgType) {
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), bufImgType);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return img;
    }

    public static void convertTiffToJpeg(String baseDir, String fileName) {
        File file = new File(baseDir + "\\" + fileName, fileName + "S.tif");
        if (!file.exists()) {
            System.out.println("Arquivo " + file.getAbsolutePath() + " Não existe");
            System.exit(10);
        }
        try {
            System.out.println("Continuar processo");
            byte[] fileContent = Files.readAllBytes(file.toPath());
            InputStream is = new ByteArrayInputStream(fileContent);
            BufferedImage bufferedImage = ImageIO.read(is);

            bufferedImage = convert(bufferedImage, BufferedImage.TYPE_INT_RGB);
            byte[] b = toByteArray(bufferedImage, "jpg");
        } catch (IOException e) {
            System.out.println("OPS!!!!! Erro");
        }
    }

    public static void getImageCheioVazio(String baseDir, String urlIaLocal, String trkId)
            throws IOException, WriteLogFileException {
        try {
            logDirectory = new File("").getCanonicalPath() + "\\\\logs";
        } catch (IOException e) {
            System.err.print("Cannot get Local Path for Log Directory");
            System.exit(10);
        }

        try {
            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Cheio/Vazio analyse started to " + trkId + "...",
                    0);
        } catch (WriteLogFileException e) {
            System.err.println("Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
            System.exit(10);
        }
        System.out.println("BaseDir " + baseDir);
        System.out.println("File Name " + trkId);

        File file = new File(baseDir + "\\" + trkId, trkId + "S.tif");
        if (!file.exists()) {
            System.out.println("Arquivo " + file.getAbsolutePath() + " Não existe");
            System.exit(10);
        }
        System.out.println("File Name " + file.getAbsolutePath());
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        String encodedString = Base64.getEncoder().encodeToString(fileContent);

        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("imgf", trkId + "S.tif")
                .add("encoded", encodedString);

        JsonObject jo = job.build();

        try {
            HttpPost req = new HttpPost(urlIaLocal);
            req.addHeader("Content-Type", "application/json");
            req.setEntity(new StringEntity(jo.toString()));
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(req);
            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);
            File fileia = new File(baseDir + "\\" + trkId, "IAR.json");
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileia));
            writer.write(apiOutput);
            writer.close();
            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE,
                    "Cheio/Vazio Response for " + trkId + " " + apiOutput, 0);
        } catch (Exception e) {
            LogApp.writeLineToFile(logDirectory, Constants.LOGFILE, "Cheio/Vazio API not Available for " + trkId, 1);
        }
    }

}

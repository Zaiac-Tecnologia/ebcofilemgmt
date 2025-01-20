package br.com.zaiac.ebcofilemgmt.tools;

//import static org.bytedeco.opencv.opencv_core.CV_16UC1;
//import static org.bytedeco.opencv.global.opencv_core.CV_16UC1;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
//import static org.bytedeco.opencv.global.opencv_core.split;
//import org.bytedeco.opencv.opencv_core.im;
//import org.bytedeco.opencv.opencv_imgcodecs.imread;

//im//port static org.bytedeco.opencv.global.opencv_core.imre
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
//import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_UNCHANGED;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

//import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
//import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
//import static org.bytedeco.opencv.global.opencv_imgproc.equalizeHist;

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
//import java.nio.ShortBuffer;
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
//import org.bytedeco.javacpp.ShortPointer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
//import org.bytedeco.javacpp.indexer.UShortIndexer;
import org.bytedeco.opencv.opencv_core.Mat;

//import org.bytedeco.opencv.opencv_core.MatVector;

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

    public static void convertJpegRgbToTiff(String baseDir, String fileName, String targetFileName) {
        File file = new File(baseDir, fileName + ".jpg");
        if (!file.exists()) {
            System.out.println("Arquivo " + file.getAbsolutePath() + " Não existe");
            System.exit(10);
        }
        Mat imageRGB = imread(file.getAbsolutePath(), IMREAD_COLOR);

        if (imageRGB.empty()) {
            System.out.println("OPS!!!!! Erro Nao carregado");
            System.exit(10);
        }

        if (imageRGB.channels() != 3) {
            System.out.println("OPS!!!!! Erro Nao é RGB");
            System.exit(10);
        }
        Mat imageGray = new Mat(imageRGB.size(), CV_8UC1); // 8-bit, 1 channel

        // Step 3: Use MatIndexer for pixel access
        UByteIndexer rgbIndexer = imageRGB.createIndexer();
        UByteIndexer grayIndexer = imageGray.createIndexer();

        // Step 4: Loop through each pixel in the image and compute grayscale value
        for (int y = 0; y < imageRGB.rows(); y++) {
            for (int x = 0; x < imageRGB.cols(); x++) {
                // Extract the RGB values (BGR format in OpenCV)
                int b = rgbIndexer.get(y, x, 0) & 0xFF; // Blue
                int g = rgbIndexer.get(y, x, 1) & 0xFF; // Green
                int r = rgbIndexer.get(y, x, 2) & 0xFF; // Red

                // Apply the grayscale formula
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // Assign the computed grayscale value to the grayscale image
                grayIndexer.put(y, x, (byte) gray);
            }
        }

        imwrite(baseDir + "\\" + targetFileName + ".tif", imageGray);
    }

    public static void convertTiffToJpeg(String baseDir, String fileName) {
        File file = new File(baseDir + "\\" + fileName, fileName + "S.tif");
        if (!file.exists()) {
            System.out.println("Arquivo " + file.getAbsolutePath() + " Não existe");
            System.exit(10);
        }
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            InputStream is = new ByteArrayInputStream(fileContent);
            BufferedImage bufferedImage = ImageIO.read(is);

            bufferedImage = convert(bufferedImage, BufferedImage.TYPE_INT_RGB);
            // byte[] b = toByteArray(bufferedImage, "jpg");
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
            LogApp.writeLineToFile(
                    logDirectory,
                    Constants.LOGFILE,
                    "Cheio/Vazio analyse started to " + trkId + "...",
                    0);
        } catch (WriteLogFileException e) {
            System.err.println(
                    "Cannot write log file Directory " + logDirectory + " file name " + Constants.LOGFILE);
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
        job.add("imgf", trkId + "S.tif").add("encoded", encodedString);

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
            LogApp.writeLineToFile(
                    logDirectory,
                    Constants.LOGFILE,
                    "Cheio/Vazio Response for " + trkId + " " + apiOutput,
                    0);
        } catch (Exception e) {
            LogApp.writeLineToFile(
                    logDirectory,
                    Constants.LOGFILE,
                    "Cheio/Vazio API not Available for " + trkId,
                    1);
        }
    }
}

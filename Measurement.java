import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.lang.Math;
import java.util.Arrays;

public class Measurement {

    public static void main(String[] args) {

        String[] algorithms = {"Kumar"}; //"Naseem", "C4S"
        for (String a: algorithms) {
            double[] bers = getUnderAttackBER(a);
            System.out.println("For " + a + ": BER_gaussianNoise = " + bers[0]
                    + ": BER_speckleNoise = " + bers[1]
                    + ": BER_saltAndPepperNoise = " + bers[2]
                    + ": BER_contrastAdjustment = " + bers[3]);
        }

        /* calculate PSNR/SSIM/BER on watermarked images and extracted watermark
        String[] algorithms = {"Kumar", "Naseem", "C4S"};
        for (String a: algorithms) {
            double[] measurement = getMeasurement(a);
            System.out.println("For " + a + ": PSNR = " + measurement[0] + ", SSIM = "+ measurement[1] +
                    ", BER = " + measurement[2]);
        }*/
    }

    public static double[] getUnderAttackBER(String algorithm) {
        // read the whole watermark to embed
        int[] wholeWatermark = Util.loadWatermark("src/watermark.txt");

        double[] results = new double[4];
        // double[] tamperDetection = new double[4];
        String[] attacks = {"gaussianNoise", "speckleNoise", "saltAndPepperNoise", "contrastAdjustment"};
        for (int i=0; i < attacks.length; i++) {
            String attack = attacks[i];
            // double tamperDetected = 0;
            double ber = 0;
            String sourcePath = "src/results/attackedResults/" + attack +"/" + algorithm+"/";
            File dir = new File(sourcePath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    // double numtampered = 0;
                    if (child.getName().endsWith(".txt")) {
                        // load watermark
                        int[] extractedWatermark = Util.loadWatermark(sourcePath + child.getName());
                        // for (int wmBit: extractedWatermark) {
                            //if (wmBit == 2) {
                                //numtampered += 1;
                            //}
                        //}
                        ber += getBER(extractedWatermark, Arrays.copyOfRange(wholeWatermark, 0, extractedWatermark.length));
                        // tamperDetected += numtampered/((double) extractedWatermark.length);
                    }
                }
            }
            // tamperDetection[i] = tamperDetected/1000.0;
            results[i] = ber/1000;
        }
        //if (algorithm.equals("C4S")) {
            //System.out.println("TamperDetection_gaussianNoise = " + tamperDetection[0]
                    //+ ": TamperDetection_speckleNoise = " + tamperDetection[1]
                    //+ ": TamperDetection_saltAndPepperNoise = " + tamperDetection[2]
                    //+ ": TamperDetection_contrastAdjustment = " + tamperDetection[3]);
        //}
        return results;
    }

    public static double[] getMeasurement(String algorithm) { // algorithm = Kumar, Naseem, C4S
        // read the whole watermark to embed
        int[] wholeWatermark = Util.loadWatermark("src/watermark.txt");

        double psnr =0, ssim=0, ber=0;
        String[] classes = {"NORMAL/", "PNEUMONIA/"};
        for (String c : classes) {
            String sourcePath = "src/data/" + c;
            File dir = new File(sourcePath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (child.getName().endsWith(".jpeg")) {
                        // load the cover image
                        BufferedImage cover = Util.loadImage(sourcePath + child.getName());
                        // load stegImage
                        BufferedImage stegImage = null;
                        if (algorithm.equals("Naseem")) {
                            stegImage = Util.loadImage("src/results/" + algorithm + "/" + c + child.getName().split("\\.")[0] + "_recovered.png");
                        } else if ((algorithm.equals("Kumar")) ||(algorithm.equals("C4S"))){
                            stegImage = Util.loadImage("src/results/" + algorithm + "/" + c + child.getName().split("\\.")[0] + "_steg.png");
                        }
                        int[] extractedWatermark = Util.loadWatermark("src/results/" + algorithm + "/" + c + child.getName().split("\\.")[0] + "_extracted.txt");
                        psnr += getPNSR(cover, stegImage);
                        ssim += getSSIM(cover, stegImage, 8);
                        ber += getBER(extractedWatermark, Arrays.copyOfRange(wholeWatermark, 0, extractedWatermark.length));
                    }
                }
            }
        }
        double[] result = {psnr/1000, ssim/1000, ber/1000};
        return result;
    }


    public static double getPNSR(BufferedImage original, BufferedImage embedded) {
        /***
         * Peak Signal-to-Noise Ratio
         */
        assert (original.getWidth()==embedded.getWidth()) && (original.getHeight()==embedded.getHeight()):
                "Image sizes do not match";
        double B = 0;
        byte[] originalPixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
        int[] usignedOriginalPixels = new int[originalPixels.length];
        int[] usignedEmbeddedPixels = new int[originalPixels.length];
        if (embedded.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            B = Math.pow(2, 8); // largest value of signal
            // convert to unsigned byte type
            for (int i = 0; i < originalPixels.length; i ++) {
                usignedOriginalPixels[i] = originalPixels[i] & 0xFF;
            }
            // get embedded pixels
            byte[] embeddedPixels = ((DataBufferByte) embedded.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < embeddedPixels.length; i ++) {
                usignedEmbeddedPixels[i] = embeddedPixels[i] & 0xFF;
            }
        }
        if (embedded.getType() == BufferedImage.TYPE_USHORT_GRAY) {
            B = Math.pow(2, 16); // largest value of signal
            for (int i = 0; i < originalPixels.length; i ++) {
                usignedOriginalPixels[i] = (originalPixels[i] & 0xFF) * 256;
            }
            // get embedded pixels
            short[] embeddedPixels = ((DataBufferUShort) embedded.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < embeddedPixels.length; i ++) {
                usignedEmbeddedPixels[i] = embeddedPixels[i] & 0xFFFF;
            }

        }
        double mse = Util.getMSE(usignedOriginalPixels, usignedEmbeddedPixels);
        //return 20 * Math.log10(B/Math.sqrt(mse));
        return 20.0 * Math.log10(B) - 10*Math.log10(mse);
    }

    public static double getSSIM(BufferedImage x, BufferedImage y, int pixelDepth) {
        /***
         * Structural Similarity Index (SSIM): a measure of imperceptibility/ degree of reversibility
         */
        assert (x.getWidth()==y.getWidth()) && (x.getHeight()==y.getHeight()):
                "Image sizes do not match";
        byte[] xPixels = ((DataBufferByte) x.getRaster().getDataBuffer()).getData();
        byte[] yPixels = ((DataBufferByte) y.getRaster().getDataBuffer()).getData();
        double mu_x = Util.getMean(xPixels), mu_y = Util.getMean(yPixels),
                variance_x = Util.getVariance(xPixels), variance_y = Util.getVariance(yPixels),
                covariance_xy = Util.getCovariance(xPixels, yPixels);
        double k1 = 0.01, k2 = 0.03; // by default
        double L = Math.pow(2, pixelDepth);// dynamic range of the pixel-values
        double c1 = Math.pow(k1*L, 2), c2 = Math.pow(k2*L, 2);
        return (2 * mu_x * mu_y + c1) * (2 * covariance_xy + c2)
                /(mu_x * mu_x + mu_y * mu_y + c1) / (variance_x + variance_y + c2);
    }

    public static double getBER(String x, String y) {
        /***
         * Bit Error Rate
         */
        assert x.length() == y.length(): "Numbers of bits do not match";
        int error = 0;
        for (int i=0; i<x.length(); i++) {
            if (x.charAt(i) != y.charAt(i)) {
                error ++;
            }
        }
        return error/x.length();
    }

    public static double getBER(int[] original, int[] extracted) {
        /***
         * Bit Error Rate
         */
        assert original.length == extracted.length: "Numbers of bits do not match";
        double error = 0;
        for (int i=0; i<original.length; i++) {
            if (original[i] != extracted[i]) {
                error += 1;
            }
        }
        return error/original.length;
    }

}

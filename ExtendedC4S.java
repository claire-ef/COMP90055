import com.mathworks.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;


public class ExtendedC4S {
    static double[][] GOLDCODE =
                    {{-1, 1, 1, -1, 1, -1, 1, 1},
                    {1, -1, -1, 1, 1, 1, 1, -1},
                    {1, 1, 1, 1, 1, -1, -1, -1},
                    {-1, -1, -1, 1, -1, 1, -1, 1},
                    {-1, -1, 1, 1, -1, -1, 1, -1},
                    {-1, -1, 1, -1, -1, 1, -1, 1},
                    {1, -1, 1, 1, -1, -1, -1, 1},
                    {1, 1, -1, 1, -1, -1, -1,1}};
    static double[][] KASAMICODE =
            {{-1, -1, -1, 1, 1, -1, -1, -1},
            {-1, 1, -1, -1, 1,  -1, 1, -1},
            {1, -1, 1, -1, 1, 1, -1, -1},
            {-1, 1, 1, 1, -1, 1, -1, 1},
            {-1, -1, 1, 1, -1, -1, 1, 1},
            {1, 1, -1, -1, 1, 1, -1, -1},
            {-1, 1, 1, -1, 1, -1, -1, -1},
            {1, -1, -1, 1, -1, -1,  1, -1}};


    public static void main(String[] args) throws Exception {
        ExtendedC4S c4s = new ExtendedC4S();
        int cr = 8;
        double rho = 0.01, epsilon = 0.004;

        // perform extraction on attacked watermarked images
        String[] attacks = {"gaussianNoise", "speckleNoise", "saltAndPepperNoise", "contrastAdjustment"};
        //for (String c: classes) {
        for (String attack: attacks) {
            String sourcePath = "src/attackedImages/" + attack + "/C4S/";
            File dir = new File(sourcePath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (child.getName().endsWith(".png")) {
                        // load the attacked image
                        String imgPath = sourcePath + child.getName();
                        BufferedImage attacked = Util.loadImage(imgPath);
                        int watermarkLen = (attacked.getWidth() / 8) * (attacked.getHeight() / 8) * cr;
                        // extract watermark from attacked images
                        //int[] watermark = Arrays.copyOfRange(wholeWatermark, 0, watermarkLen);
                        int[] extractedWatermark = c4s.extract(attacked, watermarkLen, GOLDCODE, cr, rho, epsilon);
                        String outputPath = "src/results/attackedResults/" + attack + "/C4S/" + child.getName().split("\\.")[0];
                        FileWriter writer = new FileWriter(outputPath + "_extracted.txt");
                        for (int j = 0; j < extractedWatermark.length; j++) {
                            writer.append(String.valueOf(extractedWatermark[j]));
                            writer.append(",");
                        }
                        writer.close();
                    }
                }
            }

        }

        /* perform embedding and extracting on all images
        // read the whole watermark to embed
        int[] wholeWatermark = Util.loadWatermark("src/watermark.txt");
        String[] classes = {"NORMAL/", "PNEUMONIA/"};
        for (String c: classes) {
            String sourcePath = "src/data/" + c;
            File dir = new File(sourcePath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (child.getName().endsWith(".jpeg")) {
                        String coverImgPath = sourcePath + child.getName();
                        // load the cover image
                        BufferedImage cover = Util.loadImage(coverImgPath);
                        int watermarkLen = (cover.getWidth() / 8) * (cover.getHeight() / 8) * cr;

                        // watermark to embed
                        int[] watermark = Arrays.copyOfRange(wholeWatermark, 0, watermarkLen);
                        StegResult result = c4s.C4S(cover, watermark, GOLDCODE, cr, rho, epsilon);
                        BufferedImage stegImage = result.stegImage;
                        int[] extractedWatermark = Util.binaryStringToInt(result.watermark);

                        String outputPath = "src/results/C4S/" + c + child.getName().split("\\.")[0];
                        ImageIO.write(stegImage, "png", new File(outputPath + "_steg.png"));
                        FileWriter writer = new FileWriter(outputPath + "_extracted.txt");
                        for (int j = 0; j < extractedWatermark.length; j++) {
                            writer.append(String.valueOf(extractedWatermark[j]));
                            writer.append(",");
                        }
                        writer.close();
                    }
                }
            }
        }*/

        /* experiment with different compression rates, rhos and epsilons
        int[] compressionRates = {2,3,4,5,6, 7, 8, 9, 10, 11, 12};

        // record PSNR, SSIM, BER
        double[] psnr = new double[compressionRates.length];
        double[] ssim = new double[compressionRates.length];
        double[] ber = new double[compressionRates.length];

        for (int i = 0; i < compressionRates.length; i++) {
            double rho = 0.01, epsilon = 0.004;
            int cr = compressionRates[i];
            int watermarkLen = (cover.getWidth()/8) *  (cover.getHeight()/8) * cr;
            int[] watermark = Arrays.copyOfRange(wholeWatermark, 0, watermarkLen);
            StegResult result = c4s.C4S(cover, watermark, GOLDCODE, cr, rho, epsilon);
            BufferedImage stegImage = result.stegImage;
            String extractedWatermark = result.watermark;

            // record PSNR, SSIM, BER
            psnr[i] = Measurement.getPNSR(cover, stegImage);
            ssim[i] = Measurement.getSSIM(cover, stegImage, 8);
            ber[i] = Measurement.getBER(watermark, Util.binaryStringToInt(extractedWatermark));
            //System.out.print(ssim[i] + ", ");
            System.out.println("For cr = " + cr + ": PSNR = " + psnr[i] + ", SSIM = " + ssim[i] + ", BER = " + ber[i]);
        }*/
    }

    public StegResult C4S(BufferedImage X, int[] wm, double[][] W, int cr, double rho, double epsilon) {
        int imgWidth = X.getWidth(), imgHeight = X.getHeight();
        byte[] pixels = ((DataBufferByte) X.getRaster().getDataBuffer()).getData();
        double[][] pixels2D = new double[imgWidth][imgHeight];
        for (int i = 0; i < imgWidth; i ++) {
            for (int j = 0; j < imgHeight; j++) {
                pixels2D[i][j] = pixels[j*imgWidth+i] & 0xFF; // unsigned byte
            }
        }

        // variables
        int L = wm.length;
        double maxDistortion = 40;
        int numChannels = (int) Math.pow(2, cr);
        double n=4, channelGap = n*epsilon;
        int polarisedChannels = numChannels/2;
        int nzbitvalue = polarisedChannels - 1;
        int pzbitvalue = polarisedChannels;

        String watermark = "";

        int iSubBlock = 0;
        int polarity = 0, channel = 0;
        double ecc = 0;
        // divide X into 8*8 sub-blocks
        for (int upperleftX = 0; upperleftX+8 <= imgWidth; upperleftX+=8) {
            for (int upperleftY = 0; upperleftY + 8 <= imgHeight; upperleftY += 8) {

                /**
                 * Embed
                 */
                String msg = Util.intToBinaryString(Arrays.copyOfRange(wm, iSubBlock*cr, iSubBlock*cr+cr));

                // get the 8*8 subblock with upperleft coordinate
                double[][] X_i = getSubBlocks(pixels2D, upperleftX, upperleftY);

                // compute alpha
                int decMsg = Integer.parseInt(msg,2);
                if (decMsg <= nzbitvalue) {
                    polarity = -1;
                    channel = polarisedChannels - decMsg;
                    ecc = -rho - (channel - 1) * channelGap;
                }
                if (decMsg >= pzbitvalue) {
                    polarity = 1;
                    channel = decMsg - (polarisedChannels - 1);
                    ecc = rho + (channel - 1) * channelGap;
                }
                double alpha = (ecc - Util.getCorrelation(X_i, W))/polarity;

                // compute steg block
                double[][] Y_i;
                if (ecc >= 0) { // embed positive correlation bit group watermark
                    Y_i= Util.matrixAdd(X_i, Util.scalarMul(W, alpha));
                } else { // embed negative correlation bit group watermark
                    Y_i= Util.matrixAdd(X_i, Util.scalarMul(W, -alpha));
                }
                pixels2D = updateSubBlock(pixels2D, upperleftX, upperleftY, Y_i);

                /**
                 * Extract
                 */
                Y_i = getSubBlocks(pixels2D, upperleftX, upperleftY);

                double p = Util.getCorrelation(Util.doubleToInt(Util.scalarMul(Y_i, 256)), W)/256;
                String extracted = null;
                int bitValue = findBitGroup(p, polarisedChannels, rho, channelGap, epsilon);
                if (bitValue==-1) {
                    extracted = StringUtils.repeat("2", cr);
                } else {
                    extracted = String.format("%1$" + cr + "s", Integer.toBinaryString(bitValue)).replaceAll(" ", "0");
                }
                watermark += extracted;
                iSubBlock++;
            }
        }
        // compose stego image
        BufferedImage stegImage = toUByteImage(pixels2D);
        return new StegResult(watermark, stegImage);
    }

    public int findBitGroup(double p, int polarisedChannels, double rho, double channelGap, double epsilon) {
        double[] rho_n = new double[polarisedChannels * 2];
        for (int i = 0; i < polarisedChannels; i++) {
            rho_n[i] = -rho - (polarisedChannels - 1 - i) * channelGap;
            rho_n[polarisedChannels * 2 - 1 - i] = rho + (polarisedChannels - 1 - i) * channelGap;
        }
        // robust extraction of watermark
        int bitValue = -1;
        epsilon = channelGap/2;
        for (int j = 0; j < (polarisedChannels*2); j++) {
            double desiredCorr = rho_n[j];
            if ((p <= desiredCorr + epsilon) && (p >= desiredCorr - epsilon)) {
                bitValue = j;
            }
        }

        if (p< rho_n[0]-epsilon) {
            bitValue = 0;
        }

        if (p>rho_n[polarisedChannels * 2-1]+epsilon) {
            bitValue = polarisedChannels * 2-1;
        }
        /* fragile version
        int bitValue = -1;
        for (int j = 0; j < (polarisedChannels*2); j++) {
            double desiredCorr = rho_n[j];
            if ((p <= desiredCorr + epsilon) && (p >= desiredCorr - epsilon)) {
                bitValue = j;
            }
        }*/
        return bitValue;
    }

    public BufferedImage embed(BufferedImage cover, int[] watermark, double[][] spreadSequence, int cr, double rho, double epsilon) {
        // get 2d pixel arrays
        int imgWidth = cover.getWidth(), imgHeight = cover.getHeight();
        byte[] pixels = ((DataBufferByte) cover.getRaster().getDataBuffer()).getData();
        double[][] pixels2D = new double[imgWidth][imgHeight];
        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                pixels2D[i][j] = pixels[j * imgWidth + i] & 0xFF; // unsigned byte
            }
        }

        // variables
        int L = watermark.length;
        double maxDistortion = 40;
        int numChannels = (int) Math.pow(2, cr);
        double n = 4, channelGap = n * epsilon;
        int polarisedChannels = numChannels / 2;
        int nzbitvalue = polarisedChannels - 1;
        int pzbitvalue = polarisedChannels;

        int iSubBlock = 0;
        int polarity = 0, channel = 0;
        double ecc = 0;
        // divide X into 8*8 sub-blocks
        for (int upperleftX = 0; upperleftX + 8 <= imgWidth; upperleftX += 8) {
            for (int upperleftY = 0; upperleftY + 8 <= imgHeight; upperleftY += 8) {
                // message bit group to embed in the subblock
                String msg = "";
                for (int i = 0; i < cr; i++) {
                    if (watermark[iSubBlock * cr + i] == 1) {
                        msg += "1";
                    } else {
                        msg += "0";
                    }
                }

                // get the 8*8 subblock with upperleft coordinate
                double[][] X_i = getSubBlocks(pixels2D, upperleftX, upperleftY);

                // compute alpha
                int decMsg = Integer.parseInt(msg, 2);
                if (decMsg <= nzbitvalue) {
                    polarity = -1;
                    channel = polarisedChannels - decMsg;
                    ecc = -rho - (channel - 1) * channelGap;
                }
                if (decMsg >= pzbitvalue) {
                    polarity = 1;
                    channel = decMsg - (polarisedChannels - 1);
                    ecc = rho + (channel - 1) * channelGap;
                }
                double alpha = (ecc - Util.getCorrelation(X_i, spreadSequence)) / polarity;

                // compute steg block
                double[][] Y_i;
                if (ecc >= 0) { // embed positive correlation bit group watermark
                    Y_i = Util.matrixAdd(X_i, Util.scalarMul(spreadSequence, alpha));
                } else { // embed negative correlation bit group watermark
                    Y_i = Util.matrixAdd(X_i, Util.scalarMul(spreadSequence, -alpha));
                }
                pixels2D = updateSubBlock(pixels2D, upperleftX, upperleftY, Y_i);
                iSubBlock++;
            }
        }
        return toUShortImage(pixels2D);
    }

    public int[] extract(BufferedImage stegImage, int watermarkLen, double[][] spreadSequence, int cr, double rho, double epsilon) {
        int imgWidth = stegImage.getWidth(), imgHeight = stegImage.getHeight();
        byte[] pixels = ((DataBufferByte) stegImage.getRaster().getDataBuffer()).getData();
        double[][] pixels2D = new double[imgWidth][imgHeight];
        for (int i = 0; i < imgWidth; i ++) {
            for (int j = 0; j < imgHeight; j++) {
                pixels2D[i][j] = pixels[j*imgWidth+i] & 0xFFFF; // unsigned short
            }
        }

        // variables
        int numChannels = (int) Math.pow(2, cr);
        double n = 4, channelGap = n * epsilon;
        int polarisedChannels = numChannels / 2;

        int iSubBlock = 0;
        int[] watermark = new int[watermarkLen];
        // divide X into 8*8 sub-blocks
        for (int upperleftX = 0; upperleftX+8 <= imgWidth; upperleftX+=8) {
            for (int upperleftY = 0; upperleftY + 8 <= imgHeight; upperleftY += 8) {
                double[][] Y_i = getSubBlocks(pixels2D, upperleftX, upperleftY);
                double p = Util.getCorrelation(Y_i, spreadSequence)/256;
                String extracted = null;
                int bitValue = findBitGroup(p, polarisedChannels, rho, channelGap, epsilon);
                if (bitValue==-1) {
                    //System.out.println("tampered");
                    extracted = StringUtils.repeat("2", cr);
                } else {
                    extracted = String.format("%1$" + cr + "s", Integer.toBinaryString(bitValue)).replaceAll(" ", "0");
                }
                for (int i = 0; i<cr; i++) {
                    if (extracted.charAt(i) == '1') {
                        watermark[iSubBlock*cr + i] = 1;
                    } else if (extracted.charAt(i) == '0') {
                        watermark[iSubBlock*cr + i] = 0;
                    } else {
                        watermark[iSubBlock*cr + i] = 2;
                    }
                }
                iSubBlock++;
            }
        }
        return watermark;
    }

    public class StegResult {
        String watermark;
        BufferedImage stegImage;

        public StegResult(String watermark, BufferedImage stegImage) {
            this.watermark = watermark;
            this.stegImage = stegImage;
        }
    }

    public BufferedImage toUByteImage(double[][] pixels) {
        int imgWidth = pixels.length, imgHeight = pixels[0].length;
        byte[][] pixelsByte = new byte[imgWidth][imgHeight];
        for (int i = 0; i < imgWidth; i ++) {
            for (int j = 0; j < imgHeight; j++) {
                double value = pixels[i][j];
                // remove underflow and overflow
                if (value > Math.pow(2, 8)-1) {
                    value = Math.pow(2, 8)-1;
                }
                if (value < 0) {
                    value = 0;
                }
                pixelsByte[i][j] = (byte) value;
            }
        }
        BufferedImage stegImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
        stegImage.getRaster().setDataElements(0, 0, imgWidth, imgHeight, Util.toColumnWise(pixelsByte));
        return stegImage;
    }

    public BufferedImage toUShortImage(double[][] pixels) {
        /***
         * takes an array of double pixels value and returns an image of type TYPE_USHORT_GRAY
         */
        int imgWidth = pixels.length, imgHeight = pixels[0].length;
        short[][] pixelsShort = new short[imgWidth][imgHeight];
        for (int i = 0; i < imgWidth; i ++) {
            for (int j = 0; j < imgHeight; j++) {
                int usignedShort = (int) pixels[i][j]*256;
                // remove underflow and overflow
                if (usignedShort > Math.pow(2, 16)-1) {
                    usignedShort = (int) Math.pow(2, 16)-1;
                }
                if (usignedShort < 0) {
                    usignedShort = 0;
                }
                pixelsShort[i][j] = (short) usignedShort;
            }
        }
        BufferedImage stegImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_USHORT_GRAY);
        stegImage.getRaster().setDataElements(0, 0, imgWidth, imgHeight, Util.toColumnWise(pixelsShort));
        return stegImage;
    }

    public double[][] updateSubBlock(double[][] X, int upperleftX, int upperleftY, double[][] block) {
        /***
         * get 8*8 subblock with upperleft coordinate
         */
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                //System.out.println(block[i][j]);
                X[upperleftX+i][upperleftY+j] = block[i][j];
            }
        }
        return X;
    }
    public double[][] getSubBlocks(double[][] X, int upperleftX, int upperleftY) {
        /***
         * get 8*8 subblock with upperleft coordinate
         */
        double[][] result = new double[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                result[i][j] = X[upperleftX+i][upperleftY+j];
            }
        }
        return result;
    }

}

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class NaseemSS {
    int alpha = 6;
    double x0 = 0.25, r = 3.58;
    public static void main(String[] args) throws Exception {
        NaseemSS nss = new NaseemSS();
        int cr = 2;

        // perform extraction on attacked watermarked images
        String[] attacks = {"gaussianNoise", "speckleNoise", "saltAndPepperNoise", "contrastAdjustment"};
        //for (String c: classes) {
        for (String attack: attacks) {
            String sourcePath = "src/attackedImages/" + attack + "/Naseem/";
            File dir = new File(sourcePath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (child.getName().endsWith(".png")) {
                        // load the attacked image
                        String imgPath = sourcePath + child.getName();
                        BufferedImage attacked = Util.loadImage(imgPath);
                        int roniSize = (int) (attacked.getWidth() * attacked.getHeight() * (1.0 - 9.0 / 16.0));
                        int watermarkLen = nss.getMaxWatermarkLen(roniSize, cr);
                        // extract watermark from attacked images
                        String outputPath = "src/results/attackedResults/" + attack + "/Naseem/" + child.getName().split("\\.")[0];
                        NaseemResult result = nss.extract(attacked, watermarkLen, cr);
                        if (result != null) {
                            int[] extractedWatermark = result.watermark;
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
                        int roniSize = (int) (cover.getWidth() * cover.getHeight() * (1.0 - 9.0 / 16.0));
                        int watermarkLen = nss.getMaxWatermarkLen(roniSize, cr);
                        if (watermarkLen <= 0) {
                            System.out.println("RONI is not big enough to embed any watermark");
                            System.exit(0);
                        }

                        // watermark to embed
                        int[] watermark = Arrays.copyOfRange(wholeWatermark, 0, watermarkLen);
                        BufferedImage stegImage = nss.embed(cover, watermark, cr);
                        String outputPath = "src/results/Naseem/" + c + child.getName().split("\\.")[0];
                        ImageIO.write(stegImage, "png", new File(outputPath + "_steg.png"));
                        NaseemResult result = nss.extract(stegImage, watermarkLen, cr);
                        if (result != null) {
                            int[] extractedWatermark = result.watermark;
                            FileWriter writer = new FileWriter(outputPath + "_extracted.txt");
                            for (int j = 0; j < extractedWatermark.length; j++) {
                                writer.append(String.valueOf(extractedWatermark[j]));
                                writer.append(",");
                            }
                            writer.close();
                            ImageIO.write(result.recoveredImage, "png", new File(outputPath + "_recovered.png"));
                        }
                    }
                }
            }

        }*/

        /* experiment with different sized chipRates
        int[] chipRates = {1, 2, 4, 6, 8, 10, 12, 16, 32, 64};
        // load the cover image
        BufferedImage cover = Util.loadImage("src/person1_virus_6.jpeg");
        int roniSize = (int) (cover.getWidth() * cover.getHeight() * (1.0-9.0/16.0));

        // record PSNR, SSIM, BER
        double[] psnr = new double[chipRates.length];
        double[] ssim = new double[chipRates.length];
        double[] ber = new double[chipRates.length];
        for (int i = 0; i < chipRates.length; i++) {
            int cr = chipRates[i];
            int watermarkLen = nss.getMaxWatermarkLen(roniSize, cr);
            if (watermarkLen <= 0) {
                System.out.println("RONI is not big enough to embed any watermark");
                System.exit(0);
            }
            // generate a random binary sequence as the watermark to embed
            int[] watermark = Util.getRandomBinarySequence(watermarkLen);
            BufferedImage stegImage = nss.embed(cover, watermark, cr);
            NaseemResult result = nss.extract(stegImage, watermarkLen, cr);
            if (result != null) {
                int[] extractedWatermark = result.watermark;
                BufferedImage recoveredImage = result.recoveredImage;
                // record PSNR, SSIM, BER
                psnr[i] = Measurement.getPNSR(cover, recoveredImage);
                ssim[i] = Measurement.getSSIM(cover, recoveredImage, 8);
                ber[i] = Measurement.getBER(watermark, extractedWatermark);
                System.out.println("For cr = " + cr + ": watermarkLen = " + watermarkLen + ", PSNR = " + psnr[i] + ", SSIM = " + ssim[i] + ", BER = " + ber[i]);
            }
        }*/
    }
    public BufferedImage embed(BufferedImage cover, int[] watermark, int cr) throws NoSuchAlgorithmException {
        /**
         * roiPosition: [XOffset, YOffset, Width, Height]
         * watermark: binary watermark vector (0 or 1)
         */
        int imgWidth = cover.getWidth(), imgHeight = cover.getHeight();
        int roiX = imgWidth/8, roiY = imgHeight/8, roiWidth = imgWidth*6/8, roiHeight = imgHeight*6/8;
        // arrange all pixels of RONI in column-wise vector form
        ArrayList<Integer> roniPixels = new ArrayList<Integer>();
        ArrayList<Integer> roiResidue = new ArrayList<Integer>();
        for (int x = 0; x < imgWidth; x ++) {
            for (int y = 0; y < imgHeight; y++) {
                if (!((x>=roiX) && (x<roiX+roiWidth) && (y>=roiY) && (y<roiY+roiHeight))) {
                    roniPixels.add(Util.getUnsignedBytePixel(cover, x, y));
                } else {
                    // step 2: get residue pairs for every pixel of ROI
                    roiResidue.add(getResiduePair(Util.getUnsignedBytePixel(cover, x, y)));
                }
            }
        }

        // step 3: generate embedding positions
        int totalWatermarkLen = watermark.length * cr + 128; // length of spreaded binary + 128 bit hash
        int[] s = getSumSequence(totalWatermarkLen);

        // step 5: spread binary watermark by cr
        int[] spreadedLogo = spreadSequence(watermark, cr);

        // step 8
        // set LSB's of embedding position to zero
        for (int i=0; i < s.length; i++) {
            int value = roniPixels.get(s[i]);
            if (value % 2 != 0) {
                roniPixels.set(s[i], value-1);
            }
        }

        //embed spreaded watermark in RONI
        double[] P = getChaoticSequence(spreadedLogo.length);
        for (int i = 0; i < spreadedLogo.length; i++) {
            int currentValue = roniPixels.get(s[i]), newValue;
            int a;
            if (spreadedLogo[i] == 0) {
                a = 1;
            } else {
                a = -1;
            }
            // embed
            if (P[i] >= 0.647) {
                newValue = currentValue + alpha * a;
            } else {
                newValue = currentValue - alpha * a;
            }

            // deal with overflow and underflow
            if (newValue > 255) {
                newValue = 255;
            } else if (newValue < 0) {
                newValue = 0;
            }
            roniPixels.set(s[i], newValue);
        }

        // step 9: combine RONI and ROI
        byte[] combinedPixelsBytes = new byte[imgHeight*imgWidth];
        for (int i=0; i < roniPixels.size(); i++) {
            combinedPixelsBytes[i]= (byte) ((int) roniPixels.get(i));
        }
        for (int i = 0; i < roiResidue.size(); i ++) {
            combinedPixelsBytes[roniPixels.size()+i] = (byte) ((int) roiResidue.get(i));
        }

        // step 10: compute hash for authentication
        MessageDigest digest = MessageDigest.getInstance("MD5");
        int[] hashBits = toBinaryVector(digest.digest(combinedPixelsBytes));

        // step 11: embed authentication watermark
        for (int i=0; i<128; i++) {
            int currentValue = roniPixels.get(s[spreadedLogo.length+i]);
            int bitToEmbed = hashBits[i];
            if (bitToEmbed == 1) { // watermark bit: 1
                roniPixels.set(s[spreadedLogo.length+i], currentValue+1);
            }
        }
        // step 12: rearrange the RONI pixels and ROI in its original positions
        byte[][] stegPixels = new byte[imgWidth][imgHeight];
        int iRONI = 0;
        int iROI = 0;
        for (int x = 0; x < imgWidth; x ++) {
            for  (int y = 0; y < imgHeight; y++) {
                if (((x>=roiX) && (x<roiX+roiWidth) && (y>=roiY) && (y<roiY+roiHeight))) { // in roi
                    stegPixels[x][y] = (byte) ((int) roiResidue.get(iROI));
                    iROI++;
                } else { // in roni
                    stegPixels[x][y] = (byte) ((int) roniPixels.get(iRONI));
                    iRONI ++;
                }
            }
        }
        BufferedImage stegImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
        stegImage.getRaster().setDataElements(0, 0, imgWidth, imgHeight, Util.toColumnWise(stegPixels));
        return stegImage;
    }

    public NaseemResult extract(BufferedImage steg, int watermarkLen, int cr) throws NoSuchAlgorithmException, IOException {
        int imgWidth = steg.getWidth(), imgHeight = steg.getHeight();
        int roiX = imgWidth/8, roiY = imgHeight/8, roiWidth = imgWidth*6/8, roiHeight = imgHeight*6/8;
        // Seperate RONI and ROI
        ArrayList<Integer> roiPixels = new ArrayList<Integer>();
        ArrayList<Integer> roniPixels = new ArrayList<Integer>();
        for (int x = 0; x < imgWidth; x ++) {
            for (int y = 0; y < imgHeight; y++) {
                if (!((x>=roiX) && (x<roiX+roiWidth) && (y>=roiY) && (y<roiY+roiHeight))) {
                    roniPixels.add(Util.getUnsignedBytePixel(steg, x, y));
                } else {
                    roiPixels.add(Util.getUnsignedBytePixel(steg, x, y));
                }
            }
        }

        // step 2: generate watermark positions
        int[] s = getSumSequence(watermarkLen*cr+128);

        // step 3: extract hash bits from LSB's of S_k+1...Sn
        int[] hashBitsExtracted = new int[128];
        for (int i=0; i<128; i++) {
            int value = roniPixels.get(s[watermarkLen*cr+i]);
            hashBitsExtracted[i] = value % 2;
            // place 0 at position
            if (hashBitsExtracted[i] != 0) {
                roniPixels.set(s[watermarkLen*cr+i], value-1);
            }
        }

        // step 4: combine RONI with ROI and compute hash
        byte[] combinedPixelsBytes = new byte[steg.getWidth() * steg.getHeight()];
        for (int i=0; i < roniPixels.size(); i++) {
            combinedPixelsBytes[i]= (byte) ((int) roniPixels.get(i));
        }
        for (int i=0; i < roiPixels.size(); i++) {
            combinedPixelsBytes[roniPixels.size()+i]= (byte) ((int) roiPixels.get(i));
        }

        // compute hash
        MessageDigest digest = MessageDigest.getInstance("MD5");
        int[] hashBitsComputed = toBinaryVector(digest.digest(combinedPixelsBytes));

        // step 6: compare the two hashes
        if (!Arrays.equals(hashBitsComputed, hashBitsExtracted)) {
            //System.out.println("The image is tampered.");
        }

        // step 5: collect logo watermark from S1...S2
        double[] corrL = new double[watermarkLen];
        int[] watermark = new int[watermarkLen];
        double[] P = getChaoticSequence(watermarkLen*cr);
        for (int i = 0; i < watermarkLen; i ++) {
            double corr = 0;
            for (int j = 0; j < cr; j++) {
                int value = roniPixels.get(s[i * cr + j]);
                if (P[i * cr + j] >= 0.647) {
                    corr += value;
                } else {
                    corr -= value;
                }
            }
            corrL[i] = corr/cr/alpha;
            if (corr >= 0) {
                watermark[i] = 0;
            } else {
                watermark[i] = 1;
            }
        }

        // step 7: get original pixels of ROI
        int[] originalROI = new int[roiPixels.size()];
        for (int i=0; i<roiPixels.size(); i++) {
            originalROI[i] = getOriginalPixel(roiPixels.get(i));
        }

        // step 8: combine ROI and RONI to get the original image
        byte[][] recoveredPixels = new byte[imgWidth][imgHeight];
        int iRONI = 0;
        int iROI = 0;
        for (int x = 0; x < imgWidth; x ++) {
            for  (int y = 0; y < imgHeight; y++) {
                if (((x>=roiX) && (x<roiX+roiWidth) && (y>=roiY) && (y<roiY+roiHeight))) { // in roi
                    recoveredPixels[x][y] = (byte) originalROI[iROI];
                    iROI++;
                } else { // in roni
                    recoveredPixels[x][y] = (byte) ((int) roniPixels.get(iRONI));
                    iRONI ++;
                }
            }
        }
        BufferedImage recoveredImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
        recoveredImage.getRaster().setDataElements(0, 0, imgWidth, imgHeight, Util.toColumnWise(recoveredPixels));
        return new NaseemResult(recoveredImage, watermark);
    }

    public class NaseemResult {
        BufferedImage recoveredImage;
        int[] watermark;
        public NaseemResult(BufferedImage recoveredImage, int[] watermark) {
            this.recoveredImage = recoveredImage;
            this.watermark = watermark;
        }
    }

    public int[] toBinaryVector(byte[] bytes) {
        int[] bits = new int[bytes.length*8];
        for (int i=0; i < bytes.length; i++) {
            int unsignedByte = bytes[i] & 0xFF;
            String binaryByte = String.format("%8s", Integer.toBinaryString(unsignedByte).replaceAll(" ", "0"));
            for (int k = 0; k < 8; k++) {
                if (binaryByte.charAt(k)=='0') {
                    bits[i*8 + k] = 0;
                } else {
                    bits[i*8 + k] = 1;
                }
            }
        }
        return bits;
    }
    public int[] spreadSequence(int[] sequence, int cr) {
        int[] spreaded = new int[sequence.length*cr];
        for (int i = 0; i < sequence.length; i++) {
            for (int j = 0; j < cr; j++) {
                spreaded[i*cr+j] = sequence[i];
            }
        }
        return spreaded;
    }

    public int getMaxWatermarkLen(int roniSize, int cr) {
        int allowable = 0;
        double lastX = r * x0 * (1-x0);
        int lastPosition = (int) Math.ceil(lastX*8);
        while (lastPosition < roniSize) {
            allowable ++;
            lastX = r * lastX * (1-lastX);
            lastPosition += (int) Math.ceil(lastX*8);
        }
        if (allowable > 128) {
            return (allowable-128)/cr;
        } else {
            return -1;
        }

    }

    public int[] getSumSequence(int len) {
        double[] c = getChaoticSequence(len);
        int sum = 0;
        int[] s = new int[len];
        for (int i=0; i<len; i++) {
            sum += (int) Math.ceil(c[i]*8);
            s[i] = sum;
        }
        return s;
    }

    public double[] getChaoticSequence(int len) {
        double[] sequence = new double[len];
        double lastX = x0;
        for (int i = 0; i < len; i++) {
            sequence[i] = r * lastX * (1-lastX);
            lastX = sequence[i];
        }
        return sequence;
    }

    public int getOriginalPixel(int residue) {
        String binaryByte = String.format("%8s", Integer.toBinaryString(residue)).replaceAll(" ", "0");
        int r1 = Integer.parseInt(binaryByte.substring(0, 4), 2);
        int r2 = Integer.parseInt(binaryByte.substring(4, 8), 2);
        if ((r1 == 15) && (r2 == 15)) {
            return 255;
        }else if (r2 == 15) {
            r2 = r1;
            r1 = 16;
        }
        return (15 * ((r1*8) % 17) + 17* ((r2*8) % 15)) % 255;
    }

    public int getResiduePair(int X) {
        int[] pair = new int[2];
        if (X == 255) { // send 255 as (15,15)
            pair[0] = 15;
            pair[1] = 15;
        } else {
            pair[0] = X % 17;
            pair[1] = X % 15;
            if (pair[0] == 16) { // apply special mapping for x1=16
                pair[0] = pair[1];
                pair[1] = 15;
            }
        }
        return (pair[0] * 16 + pair[1]);
    }
}

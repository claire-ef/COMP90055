import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Util {
    public static void displayResult(BufferedImage cover, BufferedImage stegImage, BufferedImage watermark) throws Exception {
        // display the images
        JFrame f = new JFrame("Results");

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.LIGHT_GRAY);
        p.setBorder(new LineBorder(Color.LIGHT_GRAY,5));

        JLabel coverLabel = new JLabel("Cover Image:", JLabel.CENTER);
        coverLabel.setBackground(Color.LIGHT_GRAY);
        coverLabel.setOpaque(true);
        p.add(coverLabel);
        p.add(new JLabel(new ImageIcon(Util.scaleImage(cover, 2))));

        JLabel stegLabel = new JLabel("Watermarked Image:", JLabel.CENTER);
        stegLabel.setBackground(Color.LIGHT_GRAY);
        stegLabel.setOpaque(true);
        p.add(stegLabel);
        p.add(new JLabel(new ImageIcon(Util.scaleImage(stegImage, 2))));
        f.getContentPane().add(p, BorderLayout.WEST);

        f.getContentPane().add(new JLabel(new ImageIcon(Util.scaleImage(watermark, 1))), BorderLayout.EAST);

        f.setVisible(true);
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void displayResult(BufferedImage cover, BufferedImage stegImage, String watermark) throws Exception {
        // display the images
        JFrame f = new JFrame("Results");

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.LIGHT_GRAY);
        p.setBorder(new LineBorder(Color.LIGHT_GRAY,5));

        JLabel coverLabel = new JLabel("Cover Image:", JLabel.CENTER);
        coverLabel.setBackground(Color.LIGHT_GRAY);
        coverLabel.setOpaque(true);
        p.add(coverLabel);
        p.add(new JLabel(new ImageIcon(Util.scaleImage(cover, 2))));

        JLabel stegLabel = new JLabel("Watermarked Image:", JLabel.CENTER);
        stegLabel.setBackground(Color.LIGHT_GRAY);
        stegLabel.setOpaque(true);
        p.add(stegLabel);
        p.add(new JLabel(new ImageIcon(Util.scaleImage(stegImage, 2))));
        f.getContentPane().add(p, BorderLayout.WEST);

        f.getContentPane().add(p, BorderLayout.WEST);
        TextArea textArea = new TextArea();
        textArea.setText(watermark);
        f.getContentPane().add(textArea, BorderLayout.EAST);

        f.setVisible(true);
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public static int getUnsignedBytePixel(BufferedImage img, int x, int y) {
        int imgWidth = img.getWidth();
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        return pixels[y*imgWidth+x] & 0xFF; // unsigned byte
    }
    public static String textToBinary(String text) {
        String binary = "";
        char[] chars = text.toCharArray();
        for (char c : chars) {
            binary += String.format("%8s", Integer.toBinaryString(c)).replaceAll(" ", "0");
        }
        return binary;
    }

    public static String binaryToText(String binaryText) {
        String text = "";
        for (int start = 0; start+8 < binaryText.length(); start+=8) {
            String currentByte = binaryText.substring(start, start + 8);
            char c = (char) Integer.parseInt(currentByte, 2);
            text += c;
        }
        return text;
    }

    public static int[] binaryStringToInt(String binaryText) {
        int[] result = new int[binaryText.length()];
        for (int i = 0; i < binaryText.length(); i++) {
            if (binaryText.charAt(i) == '1') {
                result[i] = 1;
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    public static String intToBinaryString(int[] watermark) {
        String result = "";
        for (int i = 0; i < watermark.length; i++) {
            if (watermark[i] == 1) {
                result += "1";
            } else {
                result += "0";
            }
        }
        return result;
    }

    public static int[] getRandomBinarySequence(int len) {
        int[] watermark = new int[len];
        for (int i=0; i < len; i++) {
            watermark[i] = (int) Math.round(Math.random());
        }
        return watermark;
    }

    public static BufferedImage loadImage(String pathname) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(pathname));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public static int[] loadWatermark(String pathname) {
        ArrayList<Integer> watermark = new ArrayList<Integer>();
        String text = "";
        try {
            Scanner myReader = new Scanner(new File(pathname));
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                text += line;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String[] bits = text.split(",");
        int[] result = new int[bits.length];
        for (int i = 0; i<bits.length; i++) {
            if (bits[i].equals("0")) {
                result[i] = 0;
            } else if (bits[i].equals("1")){
                result[i] = 1;
            } else if (bits[i].equals("2")){
                result[i] = 2;
            }
        }
        return result;
    }

    public static String loadText(String pathname) {
        String text = "";
        try {
            Scanner myReader = new Scanner(new File(pathname));
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                text += line + "\n";
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static BufferedImage scaleImage(BufferedImage img, int scaleFactor) {
        int newWidth = img.getWidth()/scaleFactor, newHeight = img.getHeight()/scaleFactor;
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, img.getType());
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(img, 0, 0,newWidth, newHeight, null);
        g.dispose();
        return scaledImage;
    }

    public static double[][] matrixAdd(double[][] m1, double[][] m2) {
        int nRow = m1.length;
        int nCol = m1[0].length;
        if ((nRow==m2.length) && (nCol==m2[0].length)) {
            double[][] result = new double[nRow][nCol];
            for (int i=0; i<nRow; i++) {
                for (int j = 0; j < nCol; j++) {
                    result[i][j] = m1[i][j] + m2[i][j];
                }
            }
            return result;
        }
        System.out.println("Matrix sizes does not match");
        return null;
    }
    public static double[][] matrixAdd(int[][] m1, double[][] m2) {
        return matrixAdd(intToDouble(m1), m2);
    }
    public static double[] matrixAdd(double[] m1, double[] m2) {
        if (m1.length==m2.length) {
            double[] result = new double[m1.length];
            for (int i=0; i<m1.length; i++) {
                result[i] = m1[i] + m2[i];
            }
            return result;
        }
        System.out.println("Matrix sizes does not match");
        return null;
    }


    public static double[][] scalarMul(double[][] matrix, double scalar) {
        int nRow = matrix.length;
        int nCol = matrix[0].length;
        double[][] result = new double[nRow][nCol];
        for (int i=0; i<nRow; i++) {
            for (int j=0; j<nCol; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }
        return result;
    }
    public static double[] scalarMul(double[] matrix, double scalar) {
        double[] result = new double[matrix.length];
        for (int i=0; i<matrix.length; i++) {
            result[i] = matrix[i] * scalar;
        }
        return result;
    }

    public static byte[] toRowWise(byte[][] matrix) {
        int nRow = matrix.length;
        int nCol = matrix[0].length;
        byte[] result = new byte[nRow*nCol];
        for (int i=0; i<nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                result[i*nCol+j] = matrix[i][j];
            }
        }
        return result;
    }

    public static byte[] toColumnWise(byte[][] matrix) {
        int nRow = matrix.length; // imgHeight
        int nCol = matrix[0].length; // imgWidth
        byte[] result = new byte[nRow*nCol];
        for (int j=0; j<nCol; j++) {
            for (int i = 0; i < nRow; i++) {
                result[j*nRow+i] = matrix[i][j];
            }
        }
        return result;
    }

    public static short[] toColumnWise(short[][] matrix) {
        int nRow = matrix.length; // imgHeight
        int nCol = matrix[0].length; // imgWidth
        short[] result = new short[nRow*nCol];
        for (int j=0; j<nCol; j++) {
            for (int i = 0; i < nRow; i++) {
                result[j*nRow+i] = matrix[i][j];
            }
        }
        return result;
    }


    public static byte[][] toByte(double[][] matrix) {
        int nRow = matrix.length;
        int nCol = matrix[0].length;
        byte[][] result = new byte[nRow][nCol];
        for (int i=0; i<nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                result[i][j] = (byte) matrix[i][j];
            }
        }
        return result;
    }
    public static short[][] toShort(double[][] matrix) {
        int nRow = matrix.length;
        int nCol = matrix[0].length;
        short[][] result = new short[nRow][nCol];
        for (int i=0; i<nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                result[i][j] = (short) matrix[i][j];
            }
        }
        return result;
    }

    public static byte[][] toByte(int[][] matrix) {
        int nRow = matrix.length;
        int nCol = matrix[0].length;
        byte[][] result = new byte[nRow][nCol];
        for (int i=0; i<nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                result[i][j] = (byte) matrix[i][j];
            }
        }
        return result;
    }

    public static int[][] doubleToUnsignedByte(double[][] matrix) {
        int nRow = matrix.length;
        int nCol = matrix[0].length;
        int[][] result = new int[nRow][nCol];
        for (int i=0; i<nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                byte signedByted = (byte) matrix[i][j];
                result[i][j] = signedByted & 0xFF;
            }
        }
        return result;
    }

    public static double[][] intToDouble(int[][] matrix) {
        int nRow = matrix.length;
        int nCol = matrix[0].length;
        double[][] result = new double[nRow][nCol];
        for (int i=0; i<nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                result[i][j] = matrix[i][j];
            }
        }
        return result;
    }

    public static int[][] doubleToInt(double[][] matrix) {
        int nRow = matrix.length;
        int nCol = matrix[0].length;
        int[][] result = new int[nRow][nCol];
        for (int i=0; i<nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                result[i][j] = (int) matrix[i][j];
            }
        }
        return result;
    }

    public static double getMean(double[] array) {
        double result = 0;
        for (int i = 0; i<array.length; i++) {
            result += array[i];
        }
        return result/array.length;
    }
    public static double getMean(byte[] array) {
        double result = 0;
        for (int i = 0; i<array.length; i++) {
            result += array[i];
        }
        return result/array.length;
    }

    public static double getVariance(byte[] x) {
        double result = 0;
        double mean = getMean(x);
        for (int i = 0; i < x.length; i++) {
            result += (x[i]-mean) * (x[i]-mean);
        }
        return result/x.length;

    }

    public static double getCovariance(byte[] x, byte[] y) {
        assert x.length==y.length: "Array sizes do not match";
        double result = 0, mean_x = getMean(x), mean_y=getMean(y);
        for (int i = 0; i < x.length; i++) {
            result += (x[i]-mean_x) * (y[i]-mean_y);
        }
        return result/(x.length-1);
    }

    public static double getMSE(int[] x, int[] y) {
        assert x.length==y.length: "Array sizes do not match";
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            result += (x[i]-y[i]) * (x[i]-y[i]);
        }
        return result/x.length;
    }

    public static double getMSE(double[][] x, double[][] y) {
        assert (x.length==y.length)&&(x[0].length==y[0].length): "Array sizes do not match";
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                result += (x[i][j] - y[i][j]) * (x[i][j] - y[i][j]);
            }
        }
        return result/x.length/x[0].length;
    }
    public static double getMSE(int[][] x, int[][] y) {
        assert (x.length==y.length)&&(x[0].length==y[0].length): "Array sizes do not match";
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                result += (x[i][j] - y[i][j]) * (x[i][j] - y[i][j]);
            }
        }
        return result/x.length/x[0].length;
    }

    public static double getCorrelation(double[][] m1, double[][] m2) {
        int m = m1.length, n = m1[0].length;
        if ((m==m2.length) && (n==m2[0].length)) {
            double result = 0;
            for (int i=0; i<m; i++) {
                for (int j = 0; j < n; j++) {
                    result += m1[i][j] * m2[i][j];
                }
            }
            return result/m/n;
        }
        System.out.println("Matrix sizes does not match");
        return 0;
    }
    public static double getCorrelation(int[][] m1, double[][] m2) {
        return getCorrelation(intToDouble(m1), m2);
    }
}

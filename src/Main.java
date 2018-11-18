import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Creator creator = new Creator();
            creator.fillInNoiseFromFile();
            creator.fillInMatrix();
            // creator.process();
            // creator.semivar();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }
    }
}

class Creator {
    private int n;
    private double[] ksi;
    private double[] x;
    private double[] semivar;
    private double l;
    private double w;
    private double[][] matrixSP;

    public Creator() {
        n = 100;
        w = 1.0;
        ksi = new double[n + 1];
        l = Math.sqrt(2);
        x = new double[n + 1];
        semivar = new double[n + 1];
        matrixSP = new double[5][n + 1];
    }

    public double R(double t) {
        if (Math.abs(t) > (1.0 / w)) {
            return 0;
        } else {
            return (1.0 - w * Math.abs(t));
        }
    }

    public void semivar() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter("semivar.txt");

        for (int i = 0; i < 20; i++) {
            semivar[i] = (R(0) - R(i));
            pw.write(String.valueOf(semivar[i]) + ", ");
        }
        pw.close();
    }

    public void writeNoiseInFile() throws FileNotFoundException {
        Random random = new Random();
        PrintWriter pw = new PrintWriter("sv.txt");

        for (int i = 0; i < x.length; i++) {
            x[i] = random.nextGaussian();
            pw.write(String.valueOf(x[i]) + " ");
            System.out.println(x[i]);
        }
        pw.close();
    }

    public void fillInNoiseFromFile() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("sv.txt")).useDelimiter(" ");

        for (int i = 0; i < x.length; i++) {
            x[i] = sc.nextDouble();
        }
        sc.close();
    }

    public void fillInMatrix() throws FileNotFoundException {
        double sum;
        double wt = w;
        int N = (int) Math.round(l / wt) + 1;
        w = 1;
        double c0 = 1 / Math.sqrt(N);
        PrintWriter pw = new PrintWriter("matrix.txt");

        for (int j = 0; j < 5; j++) {
            if (j > 0) {
                w /= 2;
                l = Math.sqrt(Math.pow(1 / w, 2) + 1);
                wt = w;
                N = (int) Math.round(l / wt) + 1;
                c0 = 1 / Math.sqrt(N);
            }
            for (int i = 0; i < matrixSP[j].length; i++) {
                sum = 0;
                for (int k = 0; k < N - 1; k++) {
                    if (i - k >= 0) {
                        sum += x[i - k];
                    }
                }
                matrixSP[j][i] = c0 * sum;
                pw.write(String.valueOf(matrixSP[j][i]) + ", ");
            }
            pw.write("\r\n");
        }
        pw.close();
    }

    public void process() throws FileNotFoundException {
        double wt = w;
        double sum;
        int N = (int) Math.round(l / wt) + 1;
        double c0 = 1 / Math.sqrt(N);

        PrintWriter pw = new PrintWriter("output.txt");

        for (int i = 0; i < ksi.length; i++) {
            sum = 0;
            for (int k = 0; k < N - 1; k++) {
                if (i - k >= 0) {
                    sum += x[i - k];
                }
            }
            ksi[i] = c0 * sum;
            pw.write(String.valueOf(ksi[i]) + ", ");
        }
        pw.close();
    }
}


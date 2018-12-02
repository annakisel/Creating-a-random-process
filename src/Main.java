import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

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
            creator.createZ();
            creator.calcDispersionOfZ();
            creator.calcEstimationOfSemivar();
            creator.calcEstimationOfZ();
            // creator.checkingCorrelation();
            creator.semivarOfZ();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }
    }
}

class Creator {
    private int n;
    private double[] x;
    private double[] z;
    private int t0;
    private double[][] matrixSP;

    public Creator() {
        n = 100;
        t0 = 1;
        x = new double[n];
        z = new double[n];
        matrixSP = new double[5][n];
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
        int N = t0;
        double c0 = 1 / Math.sqrt(N);
        PrintWriter pw = new PrintWriter("matrix.txt");

        for (int j = 0; j < matrixSP.length; j++) {
            if (j > 0) {
                t0 *= 2;
                N = t0;
                c0 = 1 / Math.sqrt(N);
            }
            for (int i = 0; i < matrixSP[j].length; i++) {
                sum = 0;
                for (int k = 0; k < N; k++) {
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

    public void createZ() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter("z.txt");
        double[] b = {0.1, 0.1, 0.3, 0.3, 0.2};
        // double[] b = {1.0, 2.0, 3.0, 4.0, 5.0};
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < matrixSP.length; j++) {
                z[i] += matrixSP[j][i] * b[j];
            }
            pw.write(String.valueOf(z[i]) + ", ");
        }
        pw.close();
    }

    private double sampleOfZ() {
        double _z = 0;
        for (int i = 0; i < n; i++) {
            _z += z[i];
        }
        _z /= n;
        System.out.println("sampleOfZ " + _z);
        return _z;
    }

    public void calcDispersionOfZ() {
        double d = 0;
        double _z = sampleOfZ();
        for (int i = 0; i < n; i++) {
            d += Math.pow(z[i] - _z, 2);
        }
        d /= n;
        System.out.println("calcDispersionOfZ " + d);
    }

    public void calcEstimationOfSemivar() throws FileNotFoundException {
        //  System.out.println("calcEstimationOfSemivar");
        double[][] estim = new double[matrixSP.length][n];
        double sum;
        PrintWriter pw = new PrintWriter("estimate.txt");

        for (int j = 0; j < matrixSP.length; j++) {
            for (int h = 0; h < n; h++) {
                sum = 0;
                for (int i = 0; i < n - h; i++) {
                    sum += Math.pow(matrixSP[j][i] - matrixSP[j][i + h], 2);
                }
                estim[j][h] = sum / 2.0 / (n - h);
                // if (h <= 3 * n / 4) {
                pw.write(String.valueOf(estim[j][h]) + ", ");
                // }
            }
            pw.write("\n");
        }
        pw.close();
    }

    public void calcEstimationOfZ() throws FileNotFoundException {
        double sum;
        double buff;
        PrintWriter pw = new PrintWriter("estimateZ.txt");
        for (int h = 0; h < n; h++) {
            sum = 0;
            for (int i = 0; i < n - h; i++) {
                sum += Math.pow(z[i] - z[i + h], 2);
            }
            buff = sum / 2.0 / (n - h);
            pw.write(String.valueOf(buff) + ", ");
        }
        pw.close();
    }

    public void semivarOfZ() throws FileNotFoundException {
        // double[] sZ = new double[n];
        double sum = 0;
        double[] b = {0.1, 0.1, 0.3, 0.3, 0.2};
        double[] r = {1.0, 2.0, 4.0, 8.0, 16.0};
        PrintWriter pw = new PrintWriter("sZ.txt");

        for (int h = 0; h < n; h++) {
            sum = 0;
            for (int i = 0; i < matrixSP.length; i++) {
                sum += b[i] * b[i] * h / r[i];
            }
            pw.write(String.valueOf(sum) + ", ");
        }

        pw.close();
    }

    private double[] getColumn(int column) {
        double[] c = new double[matrixSP.length];
        for (int i = 0; i < matrixSP.length; i++) {
            c[i] = matrixSP[i][column];
        }
        return c;
    }

    public void checkingSpearmansCorrelation() {
        // double[][] m = {{1.0, 2.0}, {2.0, 4.0}};
        RealMatrix matrix = new Array2DRowRealMatrix(matrixSP);
        SpearmansCorrelation corrInstance = new SpearmansCorrelation();
        double print = corrInstance.correlation(getColumn(0), getColumn(3));
        System.out.println(print);
    }

    public void checkingCorrelation() {
        int T = matrixSP.length;
        double res;
        for (int i = 0; i < matrixSP.length; i++) {
            double a = chisl(i, getColumn(0));
            double b = zn1(i, getColumn(0));
            double c = zn2(i, getColumn(0));
            // System.out.println(a + " " + b + " " + c);
            System.out.print(" " + a / b / c);
        }
        // System.out.println("");
    }

    private double zn1(int s, double[] y) {
        int T = y.length;
        double sum = 0;
        for (int l = 0; l < T - s; l++) {
            sum += y[l];
        }
        double sum1 = sum / (T - s);
        sum = 0;
        for (int t = 0; t < T - s; t++) {
            sum += (y[t] - sum1) * (y[t] - sum1);
        }
        return Math.sqrt(sum / (T - s));
    }

    private double zn2(int s, double[] y) {
        int T = y.length;
        double sum = 0;
        for (int l = 0; l < T - s; l++) {
            sum += y[l + s];
        }
        double sum1 = sum / (T - s);
        sum = 0;
        for (int t = 0; t < T - s; t++) {
            sum += (y[t + s] - sum1) * (y[t + s] - sum1);
        }
        return Math.sqrt(sum / (T - s));
    }

    private double chisl(int s, double[] y) {
        int T = y.length;
        double sum = 0;
        for (int l = 0; l < T - s; l++) {
            sum += y[l];
        }
        double firstSum = sum / (T - s);
        sum = 0;
        for (int l = 0; l < T - s; l++) {
            sum += y[l + s];
        }
        double secondSum = sum / (T - s);

        sum = 0;
        for (int t = 0; t < T - s; t++) {
            sum += (y[t] - firstSum) * (y[t + s] - secondSum);
        }
        return sum / (T - s);
    }
}

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
            // creator.semivar();
            creator.fillInMatrix();
            creator.createZ();
            // creator.process();
            creator.calcDispersionOfZ();
            creator.calcEstimationOfSemivar();
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
    private double[] z;
    private int t0;
    private double[][] matrixSP;

    public Creator() {
        n = 100;
        t0 = 1;
        ksi = new double[n + 1];
        x = new double[n + 1];
        z = new double[n + 1];
        semivar = new double[n + 1];
        matrixSP = new double[5][n + 1];
    }

    /*public doube R(double t) {
        if (Math.abs(t) > (1.0 / this.t)) {
            return 0;
        } else {
            return (1.0 - this.t * Math.abs(t));
        }
    }*/

    /*public void semivar() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter("semivar.txt");

        for (int i = 0; i < 67; i++) {
            semivar[i] = (R(0) - R(i));
            pw.write(String.valueOf(semivar[i]) + ", ");
        }
        pw.close();
    }*/

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
        int  N = t0;
        double c0 = 1 / Math.sqrt(N);
        PrintWriter pw = new PrintWriter("matrix.txt");

        for (int j = 0; j < 5; j++) {
            if (j > 0) {
                t0 *= 2;
                // wt = t;
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
        double[] b = {2.0, 4.0, 6.0, 8.0, 10.0};
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < 5; j++) {
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
        System.out.println("calcEstimationOfSemivar");
        double[][] estim = new double[5][n];
        double sum;
        PrintWriter pw = new PrintWriter("estimate.txt");

        for (int j = 0; j < 5; j++) {
            for (int h = 0; h < n; h++) {
                sum = 0;
                for (int i = 0; i < n - h - 1; i++) {
                    sum += Math.pow(matrixSP[j][i] - matrixSP[j][i + h], 2);
                }
                estim[j][h] = sum * 1.0 / 2.0 / (n - h);
                // if (h <= 3 * n / 4) {
                pw.write(String.valueOf(estim[j][h]) + ", ");
                // }
            }
            pw.write("\n");
        }
        pw.close();
    }
}

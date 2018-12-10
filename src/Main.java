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
            creator.calcDispAndSampleZ();
            creator.calcEstimationOfSemivar();
            creator.calcEstimationOfZ();
            creator.semivarOfZ();
            creator.dispAndSampleOfEveryProcess();
            creator.Rz();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }
    }
}

class Creator {
    private int n;
    private double[] x;
    private double[] z;
    private double[][] matrixSP;
    private double[] r;
    private double[] b;

    public Creator() throws FileNotFoundException {
        n = 100;
        r = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        // b = new double[]{0.1, 0.1, 0.3, 0.3, 0.2};
        b = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};

        x = new double[n];
        z = new double[n];
        matrixSP = new double[5][n];

        PrintWriter pw = new PrintWriter("R.txt");

        for (int k = 0; k < 5; k++) {
            for (int i = 0; i <= 10; i++) {
                pw.write(String.valueOf(R(i, r[k])) + ", ");
            }
            pw.write(String.valueOf("\n"));

        }
        pw.close();

        PrintWriter pw1 = new PrintWriter("semivar.txt");
        double a;
        for (int k = 0; k < 5; k++) {
            for (int i = 0; i <= 10; i++) {
                a = R(0, r[k]) - R(i, r[k]);
                pw1.write(String.valueOf(a) + ", ");
            }
            pw1.write(String.valueOf("\n"));

        }
        pw1.close();
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

        double N = r[0];
        double c0 = 1 / Math.sqrt(N);
        PrintWriter pw = new PrintWriter("matrix.txt");

        for (int j = 0; j < matrixSP.length; j++) {
            if (j > 0) {
                N = r[j];
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
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < matrixSP.length; j++) {
                z[i] += matrixSP[j][i] * b[j];
            }
            pw.write(String.valueOf(z[i]) + ", ");
        }
        pw.close();
    }

    private double sample(double[] z) {
        double _z = 0;
        for (double item : z) {
            _z += item;
        }
        _z /= n;
        System.out.println("среднее значение " + _z);
        return _z;
    }

    private void calcDispersion(double[] z) {
        double d = 0;
        double _z = sample(z);
        for (int i = 0; i < z.length; i++) {
            d = d + (z[i] - _z) * (z[i] - _z);
        }
        d /= n;
        System.out.println("дисперсия " + d);
    }

    public void calcDispAndSampleZ() {
        System.out.println("--------process z--------");
        calcDispersion(z);
    }

    private double R(double h, double r) {
        if (Math.abs(h) > r) {
            return 0;
        } else {
            return (1.0 - Math.abs(h) / r);
        }
    }

    public void Rz() throws FileNotFoundException {
        double sum;
        PrintWriter pw = new PrintWriter("Rz.txt");

        for (int h = 0; h < n; h++) {
            sum = 0;
            for (int j = 0; j < matrixSP.length; j++) {
                sum += (b[j] * b[j] * R(h, r[j]));
            }
            if (h <= 10) {
                pw.write(String.valueOf(sum) + ", ");
            }
        }
        pw.close();
    }

    public void dispAndSampleOfEveryProcess() {
        System.out.println("--------matrix--------");
        for (int i = 0; i < matrixSP.length; i++) {
            System.out.print(i + 1 + " ");
            calcDispersion(matrixSP[i]);
        }
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
                if (h <= 10) {
                    pw.write(String.valueOf(estim[j][h]) + ", ");
                }
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
            if (h <= 10) {
                pw.write(String.valueOf(buff) + ", ");
            }
        }
        pw.close();
    }

    public void semivarOfZ() throws FileNotFoundException {
        double sum;
        PrintWriter pw = new PrintWriter("sZ.txt");

        for (int h = 0; h < n; h++) {
            sum = 0;
            for (int i = 0; i < b.length; i++) {
                sum += (b[i] * b[i] * (1 - R(h, r[i])));
            }
            if (h <= 10) {
                pw.write(String.valueOf(sum) + ", ");
            }
        }
        pw.close();
    }
}

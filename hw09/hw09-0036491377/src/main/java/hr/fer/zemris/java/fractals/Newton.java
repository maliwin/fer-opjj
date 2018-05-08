package hr.fer.zemris.java.fractals;

import hr.fer.zemris.java.fractals.viewer.FractalViewer;
import hr.fer.zemris.java.fractals.viewer.IFractalProducer;
import hr.fer.zemris.java.fractals.viewer.IFractalResultObserver;
import hr.fer.zemris.math.Complex;
import hr.fer.zemris.math.ComplexPolynomial;
import hr.fer.zemris.math.ComplexRootedPolynomial;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Newton {
    private static ComplexRootedPolynomial rootedPolynomial;
    private static ComplexPolynomial polynomial;
    private static final double CONV_THRESHOLD = 1E-3;
    private static final double ROOT_THRESHOLD = 2E-3;
    private static final int MAX_ITERATIONS = 16;

    public static void main(String[] args) {
        System.out.println("Welcome to the Newton-Raphson iteration-based fractal viewer.");
        System.out.println("Please enter at least two roots, one root per line. Enter 'done' when done.");
        Scanner sc = new Scanner(System.in);

        int rootCount = 1;
        List<Complex> roots = new ArrayList<>();
        while (true) {
            System.out.print(String.format("Root %d> ", rootCount));
            String root = sc.nextLine().toLowerCase();

            if (root.equals("done")) {
                System.out.println("Image of the fractal will appear shortly.");
                break;
            }

            try {
                roots.add(parseComplex(root));
            } catch (IllegalArgumentException ex) {
                System.out.println("Could not parse as complex number: " + root);
                continue;
            }

            System.out.println(parseComplex(root));
            rootCount++;
        }

        Complex[] rootsArr = roots.toArray(new Complex[0]);
        rootedPolynomial = new ComplexRootedPolynomial(rootsArr);
        polynomial = rootedPolynomial.toComplexPolynom();
        FractalViewer.show(new FractalProducer());
    }

    private static Complex parseComplex(String str) {
        if (str == null) {
            throw new NullPointerException("String cannot be null.");
        }

        if (str.isEmpty()) {
            throw new IllegalArgumentException("Cannot parse empty string as complex number");
        }

        // Good luck
        String regex = "^(?<imaginaryNoCoef>(-)?i)?$|" // Matches complex numbers "i" or "-i"
                       + "^(?<onlyImaginary>(-)?i([0-9]++(\\.[0-9]+)?)?)?$|" // Only imaginary, i.e. "i3" or "-i2.2"
                       + "^(?<real>([+\\-])?[0-9]+(\\.[0-9]+)?)?(\\+)?" // Matches the real part of the number
                       + "(?<imaginary>(([+\\-])?i([0-9]*(\\.[0-9]+)?)?))?$"; // Matches the imaginary part of number

        Pattern p = Pattern.compile(regex);
        str = str.replaceAll("\\s+", "");
        Matcher m = p.matcher(str);

        double real = 0;
        double imaginary = 0;

        if (!m.find()) {
            throw new IllegalArgumentException("Could not parse string as complex number. Input: " + str);
        }

        String realGroup = m.group("real");
        String imaginaryGroup = m.group("imaginary");
        String noCoefImaginaryGroup = m.group("imaginaryNoCoef");
        String onlyImaginaryGroup = m.group("onlyImaginary");

        if (realGroup != null) {
            real = Double.parseDouble(realGroup);
        }

        if (imaginaryGroup != null) {
            imaginaryGroup = imaginaryGroup.replace("i", "").trim();
            if (imaginaryGroup.equals("") || imaginaryGroup.equals("-")) {
                imaginaryGroup += "1";
            }
            imaginary = Double.parseDouble(imaginaryGroup);
        }

        if (noCoefImaginaryGroup != null) {
            noCoefImaginaryGroup = noCoefImaginaryGroup.replace("i", "");
            if (noCoefImaginaryGroup.startsWith("-")) {
                imaginary = -1.0;
            } else {
                imaginary = 1.0;
            }
        }

        if (onlyImaginaryGroup != null) {
            onlyImaginaryGroup = onlyImaginaryGroup.replace("i", "").trim();
            imaginary = Double.parseDouble(onlyImaginaryGroup);
        }

        return new Complex(real, imaginary);
    }

    public static class FractalProducer implements IFractalProducer {
        @Override
        public void produce(double reMin, double reMax, double imMin, double imMax,
                int width, int height, long requestNo, IFractalResultObserver observer) {

            short[] data = new short[width * height];

            Newton.calculate(reMin, reMax, imMin, imMax, width, height, 0, height - 1, data);
            observer.acceptResult(data, (short) (polynomial.order() + 1), requestNo);
        }
    }

    public static void calculate(double reMin, double reMax, double imMin, double imMax, int width, int height, int
            yMin, int yMax, short[] data) {
        int offset = yMin * width;

        for (int y = yMin; y <= yMax; y++) {
            for (int x = 0; x < width; x++) {
                double cre = (double) x / ((double) width - 1.0D) * (reMax - reMin) + reMin;
                double cim = (double) (height - 1 - y) / ((double) height - 1.0D) * (imMax - imMin) + imMin;
                Complex zn = new Complex(cre, cim);
                ComplexPolynomial derived = polynomial.derive();
                int iterations = 0;
                double modulus;

                do {
                    Complex numerator = polynomial.apply(zn);
                    Complex denominator = derived.apply(zn);
                    Complex fraction = numerator.divide(denominator);
                    Complex zn1 = zn.sub(fraction);
                    modulus = zn1.sub(zn).module();
                    zn = zn1;
                    iterations++;
                } while (iterations < MAX_ITERATIONS && modulus > CONV_THRESHOLD);

                int index = rootedPolynomial.indexOfClosestRootFor(zn, ROOT_THRESHOLD);
                data[offset++] = (short) (index + 1);
            }
        }
    }
}

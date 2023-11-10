package math;

public class AlgebraFunctions {
    public static double nonstandardMult(double a, double b) {
        if (a == 0 && (Double.isNaN(b) || Double.isInfinite(b)))
            return 0;

        if ((Double.isNaN(a) || Double.isInfinite(a)) && b == 0)
            return 0;

        return a * b;
    }

    public static double nonstandardPow(double a, double b) {
        if (a == 0 && b == 0)
            return 1;

        return Math.pow(a,b);
    }

    public static double algebraicMean(double a, double b) {
        return (a + b)/2;
    }
    
    public static long binomialCoefficient(int n, int k) {
        /*
        Given nCk, nC(k+1) is equal to nCk * (n-k) / (k+1)
        We iterate from nC0 = 1.

        nC0 = 1
        nC1 = nC0 * (n-0) / (0+1) = n
        nC2 = nC1 * (n-1) / (1+1) = n(n-1)/2
        nC3 = nC2 * (n-2) / (2+1) = n(n-1)(n-2)/6
        ...
         */

        long nChooseI = 1;
        for (int i = 0; i < k; i++)
            nChooseI = nChooseI * (n-i) / (i+1);

        return nChooseI;
    }

    public static double bernstein(int n, int k, double t) {
        return binomialCoefficient(n,k) * nonstandardPow(t,k) * nonstandardPow(1-t,n-k);
    }

    public static double bernsteinPrime(int n, int k, double t) {
        return binomialCoefficient(n,k) * (nonstandardMult(k, nonstandardPow(t,k-1)) * nonstandardPow(1-t,n-k) - nonstandardPow(t,k) * nonstandardMult(n-k, nonstandardPow(1-t,n-k-1)));
    }
}

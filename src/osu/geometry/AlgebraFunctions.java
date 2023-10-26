package osu.geometry;

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
    
    public static int binomialCoefficient(int n, int k) {
        int[] pascalArray = new int[n+1];
        pascalArray[0] = 1;

        for (int currentRow = 1; currentRow <= n; currentRow++) {
            pascalArray[currentRow] = 1;

            for (int sample = currentRow - 1; sample > 0; sample--) {
                pascalArray[sample] = pascalArray[sample] + pascalArray[sample-1];
            }
        }

        return pascalArray[k];
    }

    public static double bernstein(int n, int k, double t) {
        return binomialCoefficient(n,k) * nonstandardPow(t,k) * nonstandardPow(1-t,n-k);
    }

    public static double bernsteinPrime(int n, int k, double t) {
        return binomialCoefficient(n,k) * (nonstandardMult(k, nonstandardPow(t,k-1)) * nonstandardPow(1-t,n-k) - nonstandardPow(t,k) * nonstandardMult(n-k, nonstandardPow(1-t,n-k-1)));
    }
}

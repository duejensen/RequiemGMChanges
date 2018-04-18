// 
// Decompiled by Procyon v0.5.30
// 

package org.reqiuem.mods.gmchanges.utils;

import java.util.Random;

public final class StdRandom
{
    private static Random random;
    private static long seed;
    
    static {
        StdRandom.seed = System.currentTimeMillis();
        StdRandom.random = new Random(StdRandom.seed);
    }
    
    public static void setSeed(final long s) {
        StdRandom.seed = s;
        StdRandom.random = new Random(StdRandom.seed);
    }
    
    public static long getSeed() {
        return StdRandom.seed;
    }
    
    public static double uniform() {
        return StdRandom.random.nextDouble();
    }
    
    public static int uniform(final int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("argument must be positive");
        }
        return StdRandom.random.nextInt(n);
    }
    
    @Deprecated
    public static double random() {
        return uniform();
    }
    
    public static int uniform(final int a, final int b) {
        if (b <= a || b - a >= 2147483647L) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + "]");
        }
        return a + uniform(b - a);
    }
    
    public static double uniform(final double a, final double b) {
        if (a >= b) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + "]");
        }
        return a + uniform() * (b - a);
    }
    
    public static boolean bernoulli(final double p) {
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("probability p must be between 0.0 and 1.0");
        }
        return uniform() < p;
    }
    
    public static boolean bernoulli() {
        return bernoulli(0.5);
    }
    
    public static double gaussian() {
        double r;
        double x;
        do {
            x = uniform(-1.0, 1.0);
            final double y = uniform(-1.0, 1.0);
            r = x * x + y * y;
        } while (r >= 1.0 || r == 0.0);
        return x * Math.sqrt(-2.0 * Math.log(r) / r);
    }
    
    public static double gaussian(final double mu, final double sigma) {
        return mu + sigma * gaussian();
    }
    
    public static int geometric(final double p) {
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("probability p must be between 0.0 and 1.0");
        }
        return (int)Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
    }
    
    public static int poisson(final double lambda) {
        if (lambda <= 0.0) {
            throw new IllegalArgumentException("lambda must be positive");
        }
        if (Double.isInfinite(lambda)) {
            throw new IllegalArgumentException("lambda must not be infinite");
        }
        int k = 0;
        double p = 1.0;
        final double expLambda = Math.exp(-lambda);
        do {
            ++k;
            p *= uniform();
        } while (p >= expLambda);
        return k - 1;
    }
    
    public static double pareto() {
        return pareto(1.0);
    }
    
    public static double pareto(final double alpha) {
        if (alpha <= 0.0) {
            throw new IllegalArgumentException("alpha must be positive");
        }
        return Math.pow(1.0 - uniform(), -1.0 / alpha) - 1.0;
    }
    
    public static double cauchy() {
        return Math.tan(3.141592653589793 * (uniform() - 0.5));
    }
    
    public static int discrete(final double[] probabilities) {
        if (probabilities == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        final double EPSILON = 1.0E-14;
        double sum = 0.0;
        for (int i = 0; i < probabilities.length; ++i) {
            if (probabilities[i] < 0.0) {
                throw new IllegalArgumentException("array entry " + i + " must be nonnegative: " + probabilities[i]);
            }
            sum += probabilities[i];
        }
        if (sum > 1.0 + EPSILON || sum < 1.0 - EPSILON) {
            throw new IllegalArgumentException("sum of array entries does not approximately equal 1.0: " + sum);
        }
        int j = 0;
    Block_5:
        while (true) {
            final double r = uniform();
            sum = 0.0;
            for (j = 0; j < probabilities.length; ++j) {
                sum += probabilities[j];
                if (sum > r) {
                    break Block_5;
                }
            }
        }
        return j;
    }
    
    public static int discrete(final int[] frequencies) {
        if (frequencies == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        long sum = 0L;
        for (int i = 0; i < frequencies.length; ++i) {
            if (frequencies[i] < 0) {
                throw new IllegalArgumentException("array entry " + i + " must be nonnegative: " + frequencies[i]);
            }
            sum += frequencies[i];
        }
        if (sum == 0L) {
            throw new IllegalArgumentException("at least one array entry must be positive");
        }
        if (sum >= 2147483647L) {
            throw new IllegalArgumentException("sum of frequencies overflows an int");
        }
        final double r = uniform((int)sum);
        sum = 0L;
        for (int j = 0; j < frequencies.length; ++j) {
            sum += frequencies[j];
            if (sum > r) {
                return j;
            }
        }
        assert false;
        return -1;
    }
    
    public static double exp(final double lambda) {
        if (lambda <= 0.0) {
            throw new IllegalArgumentException("lambda must be positive");
        }
        return -Math.log(1.0 - uniform()) / lambda;
    }
    
    public static void shuffle(final Object[] a) {
        if (a == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        for (int n = a.length, i = 0; i < n; ++i) {
            final int r = i + uniform(n - i);
            final Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    public static void shuffle(final double[] a) {
        if (a == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        for (int n = a.length, i = 0; i < n; ++i) {
            final int r = i + uniform(n - i);
            final double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    public static void shuffle(final int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        for (int n = a.length, i = 0; i < n; ++i) {
            final int r = i + uniform(n - i);
            final int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    public static void shuffle(final char[] a) {
        if (a == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        for (int n = a.length, i = 0; i < n; ++i) {
            final int r = i + uniform(n - i);
            final char temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    public static void shuffle(final Object[] a, final int lo, final int hi) {
        if (a == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        if (lo < 0 || lo >= hi || hi > a.length) {
            throw new IndexOutOfBoundsException("invalid subarray range: [" + lo + ", " + hi + ")");
        }
        for (int i = lo; i < hi; ++i) {
            final int r = i + uniform(hi - i + 1);
            final Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    public static void shuffle(final double[] a, final int lo, final int hi) {
        if (a == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        if (lo < 0 || lo >= hi || hi > a.length) {
            throw new IndexOutOfBoundsException("invalid subarray range: [" + lo + ", " + hi + ")");
        }
        for (int i = lo; i < hi; ++i) {
            final int r = i + uniform(hi - i + 1);
            final double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    public static void shuffle(final int[] a, final int lo, final int hi) {
        if (a == null) {
            throw new IllegalArgumentException("argument array is null");
        }
        if (lo < 0 || lo >= hi || hi > a.length) {
            throw new IndexOutOfBoundsException("invalid subarray range: [" + lo + ", " + hi + ")");
        }
        for (int i = lo; i < hi; ++i) {
            final int r = i + uniform(hi - i + 1);
            final int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    public static int[] permutation(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("argument is negative");
        }
        final int[] perm = new int[n];
        for (int i = 0; i < n; ++i) {
            perm[i] = i;
        }
        shuffle(perm);
        return perm;
    }
    
    public static int[] permutation(final int n, final int k) {
        if (n < 0) {
            throw new IllegalArgumentException("argument is negative");
        }
        if (k < 0 || k > n) {
            throw new IllegalArgumentException("k must be between 0 and n");
        }
        final int[] perm = new int[k];
        for (int i = 0; i < k; ++i) {
            final int r = uniform(i + 1);
            perm[i] = perm[r];
            perm[r] = i;
        }
        for (int i = k; i < n; ++i) {
            final int r = uniform(i + 1);
            if (r < k) {
                perm[r] = i;
            }
        }
        return perm;
    }
}

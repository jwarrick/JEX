package utilities;

import java.util.List;

import jex.statics.JEXStatics;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

/*************************************************************************
 *  Compilation:  javac StdRandom.java
 *  Execution:    java StdRandom
 *
 *  A library of static methods to generate random numbers from
 *  different distributions (bernoulli, uniform, gaussian,
 *  discrete, and exponential). Also includes a method for
 *  shuffling an array.
 *
 *  % java StdRandom 5
 *  90 26.36076 false 8.79269 0
 *  13 18.02210 false 9.03992 1
 *  58 56.41176 true  8.80501 0
 *  29 16.68454 false 8.90827 0
 *  85 86.24712 true  8.95228 0
 *
 *
 *  Remark
 *  ------
 *    - Uses Math.random() which generates a pseudorandom real number
 *      in [0, 1)
 *
 *    - This library does not allow you to set the pseudorandom number
 *      seed. See java.util.Random.
 *
 *    - See http://www.honeylocust.com/RngPack/ for an industrial
 *      strength random number generator in Java.
 *
 *************************************************************************/


/**
 *  <i>Standard random</i>. This class provides methods for generating
 *  random number from various distributions.
 *  <p>
 *  For additional documentation, see <a href="http://www.cs.princeton.edu/introcs/22library">Section 2.2</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
 */
public class StatisticsUtility {


    /**
     * Return real number uniformly in [0, 1).
     */
    public static double uniform() {
        return Math.random();
    }

    /**
     * Return real number uniformly in [a, b).
     */
    public static double uniform(double a, double b) {
        return a + Math.random() * (b-a);
    }

    /**
     * Return an integer uniformly between 0 and N-1.
     */
    public static int uniform(int N) {
        return (int) (Math.random() * N);
    }

    /**
     * Return a boolean, which is true with probability p, and false otherwise.
     */
    public static boolean bernoulli(double p) {
        return Math.random() < p;
    }

    /**
     * Return a boolean, which is true with probability .5, and false otherwise.
     */
    public static boolean bernoulli() {
        return bernoulli(0.5);
    }

    /**
     * Return a real number with a standard Gaussian distribution.
     */
    public static double gaussian() {
        // use the polar form of the Box-Muller transform
        double r, x, y;
        do {
            x = uniform(-1.0, 1.0);
            y = uniform(-1.0, 1.0);
            r = x*x + y*y;
        } while (r >= 1 || r == 0);
        return x * Math.sqrt(-2 * Math.log(r) / r);

        // Remark:  y * Math.sqrt(-2 * Math.log(r) / r)
        // is an independent random gaussian
    }

    /**
     * Return a real number from a gaussian distribution with given mean and stddev
     */
    public static double gaussian(double mean, double stddev) {
        return mean + stddev * gaussian();
    }

    /**
     * Return an integer with a geometric distribution with mean 1/p.
     */
    public static int geometric(double p) {
        // using algorithm given by Knuth
        return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
    }

    /**
     * Return an integer with a Poisson distribution with mean lambda.
     */
    public static int poisson(double lambda) {
        // using algorithm given by Knuth
        // see http://en.wikipedia.org/wiki/Poisson_distribution
        int k = 0;
        double p = 1.0;
        double L = Math.exp(-lambda);
        do {
            k++;
            p *= uniform();
        } while (p >= L);
        return k-1;
    }

    /**
     * Return a real number with a Pareto distribution with parameter alpha.
     */
    public static double pareto(double alpha) {
        return Math.pow(1 - uniform(), -1.0/alpha) - 1.0;
    }

    /**
     * Return a real number with a Cauchy distribution.
     */
    public static double cauchy() {
        return Math.tan(Math.PI * (uniform() - 0.5));
    }

    /**
     * Return a number from a discrete distribution: i with probability a[i].
     */
    public static int discrete(double[] a) {
        // precondition: sum of array entries equals 1
        double r = Math.random();
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum = sum + a[i];
            if (sum >= r) return i;
        }
        assert (false);
        return -1;
    }

    /**
     * Return a real number from an exponential distribution with rate lambda.
     */
    public static double exp(double lambda) {
        return -Math.log(1 - Math.random()) / lambda;
    }
    
    /**
     * Return the mean value of the list
     * @param dList
     * @return
     */
    public static double mean(Double[] dList){
    	double result = 0;
    	for (double d: dList){
    		result = result + d/dList.length;
    	}
    	return result;
    }
    
    /**
     * Return the mean value of the list
     * @param dList
     * @return
     */
    public static double mean(List<Double> dList){
    	double result = 0;
    	for (double d: dList){
    		result = result + d/dList.size();
    	}
    	return result;
    }
    
    /**
     * Return the min of the list
     * @param dList
     * @return
     */
    public static double min(List<Double> dList){
    	double result = dList.get(0);
    	for (double d: dList){
    		if(result > d) result = d;
    	}
    	return result;
    }
    
    /**
     * Return the min of the list
     * @param dList
     * @return
     */
    public static double max(List<Double> dList){
    	double result = dList.get(0);
    	for (double d: dList){
    		if(result < d) result = d;
    	}
    	return result;
    }    
    
    /**
     * Return the index of the first value closest to the specified value in the list
     * @param dList
     * @return -1 if list is empty
     */
    public static int nearestIndex(List<Double> dList, double target){
    	int result = -1;
    	double currentDifference = Double.MAX_VALUE;
    	double thisDifference;
    	for(int i = 0; i < dList.size(); i++)
    	{
    		thisDifference = Math.abs(target - dList.get(i));
    		if(currentDifference > thisDifference)
    		{
    			currentDifference = thisDifference;
    			result = i;
    		}
    	}
    	return result;
    }
    
    /**
     * Return the index of the first value closest to the specified value in the list
     * @param dList
     * @return -1 if list is empty
     */
    public static int farthestIndex(List<Double> dList, double target){
    	int result = -1;
    	double currentDifference = -1;
    	double thisDifference;
    	for(int i = 0; i < dList.size(); i++)
    	{
    		thisDifference = Math.abs(target - dList.get(i));
    		if(currentDifference < thisDifference)
    		{
    			currentDifference = thisDifference;
    			result = i;
    		}
    	}
    	return result;
    }
    
    /**
     * Return the index of the first min value of the list
     * @param dList
     * @return -1 if list is empty
     */
    public static int minIndex(List<Double> dList){
    	int result = -1;
    	double currentMin = Double.MAX_VALUE;
    	for(int i = 0; i < dList.size(); i++)
    	{
    		if(currentMin > dList.get(i))
    		{
    			currentMin = dList.get(i);
    			result = i;
    		}
    	}
    	return result;
    }
    
    
    /**
     * Return the index of the first max value of the list
     * @param dList
     * @return -1 if list is empty
     */
    public static int maxIndex(List<Double> dList){
    	int result = -1;
    	double currentMax = Double.MIN_VALUE;
    	for(int i = 0; i < dList.size(); i++)
    	{
    		if(currentMax < dList.get(i))
    		{
    			currentMax = dList.get(i);
    			result = i;
    		}
    	}
    	return result;
    }
    
    /**
     * Rearrange the elements of an array in random order.
     */
    public static void shuffle(Object[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N-i);     // between i and N-1
            Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearrange the elements of a double array in random order.
     */
    public static void shuffle(double[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N-i);     // between i and N-1
            double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearrange the elements of an int array in random order.
     */
    public static void shuffle(int[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N-i);     // between i and N-1
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    /**
     * For this distribution, X, this method returns P(X < x). 
     * @return
     */
    public static Double tDistCDF(double x, int dof)
    {
    	TDistributionImpl tdist = new TDistributionImpl(dof);
    	try
    	{
			return tdist.cumulativeProbability(x);
		} 
    	catch (MathException e) 
		{
			e.printStackTrace();
		}
		return null;
    }
    
    /**
     * For this distribution, X, this method returns the critical point x, such that P(X < x) = p.
     * Returns Double.NEGATIVE_INFINITY for p=0 and Double.POSITIVE_INFINITY for p=1.
     */
    public static Double tDistInverseCDF(double p, int dof)
    {
    	TDistributionImpl tdist = new TDistributionImpl(dof);
    	try
    	{
			return tdist.inverseCumulativeProbability(p);
		} 
    	catch (MathException e) 
		{
			e.printStackTrace();
		}
		return null;
    }
    
    /**
     * Get the critical value for determining outliers using Grubb's method
     * http://www.itl.nist.gov/div898/handbook/eda/section3/eda35h1.htm
     * @param mean
     * @param stdev
     * @param n
     * @param alpha
     * @return g_critical, critical outlier threshold for furthest point.
     */
    public static Double grubbsOutlierLimit(double mean, double stdev, int n, double alpha)
    {
    	double t = -1*tDistInverseCDF(alpha/((2*n)), n-2);
    	double g_critical = ((n - 1)/(Math.sqrt(n)))*Math.sqrt((t*t)/(n-2+t*t));
    	return g_critical;
    }
    
    /**
     * Use Grubb's statistic for determining if there is an outlier and
     * throw the point with the largest deviation from the mean if true.
     * @param values
     * @return remaining values after culling if necessary
     */
    public static int getOutlier(List<Double> values, double alpha)
    {
    	if(values.size() < 3) return -1;
    	double[] arrayValues = toArray(values);
    	double stdev = stdDev(arrayValues);
    	double mean = mean(arrayValues);
    	double g_critical = grubbsOutlierLimit(mean, stdev, values.size(), alpha);
    	Double  maxOutlier = null;
    	int outlierIndex = -1;
    	for(Double value : values)
    	{
    		if(maxOutlier == null)
    		{
    			maxOutlier = value;
    		}
    		if(Math.abs(mean - value) > Math.abs(mean - maxOutlier))
    		{
    			maxOutlier = value;
    		}
    	}
    	if(Math.abs(mean - maxOutlier)/stdev > g_critical)
    	{
    		outlierIndex = values.indexOf(maxOutlier);
    		JEXStatics.logManager.log("Found outlier at index: " + outlierIndex, 1, "StatisticsUtility");
    	}
    	return outlierIndex;
    }
    
    public static double[] toArray(List<Double> values)
    {
    	int count = 0;
    	double[] arrayValues = new double[values.size()];
    	for(Double value : values)
    	{
    		arrayValues[count] = value;
    		count = count + 1;
    	}
    	return arrayValues;
    }
    
    public static Double stdDev(double[] values)
    {
    	StandardDeviation stdevCalculator = new StandardDeviation();
    	return stdevCalculator.evaluate(values);
    }
    
    public static Double mean(double[] values)
    {
    	Mean meanCalculator = new Mean();
    	return meanCalculator.evaluate(values);
    }

}

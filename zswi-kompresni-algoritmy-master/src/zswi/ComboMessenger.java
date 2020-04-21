package zswi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ComboMessenger
{
	/**
	 * File lists of each category
	 */
	List<File> algorithms, metrics, objects;
	/**
	 * List of combinations
	 */
	final List<FilesMessenger> testCombinations;
	/**
	 * Directories of each category
	 */
	final File algDir, metDir, objDir;
	
	/**
	 * Temporary folder for testing algorithms
	 */
	static final File tempDir = new File("temp");
	
	/**
	 * Longest name in category of each category
	 */
	private static int longestAlg, longestMet, longestObj;
	
	/**
	 * Creates list for combinations and sets names of directories for each category
	 */
	ComboMessenger()
	{
		testCombinations = new ArrayList<>();
		algDir = new File("alg");
		metDir = new File("met");
		objDir = new File("obj");
	}
	
	/**
	 * Saves longest name of algorithm
	 *
	 * @param file algorithm file
	 */
	static void longestAlgName(File file)
	{
		longestAlg = Math.max(longestAlg, file.getName().length());
	}
	
	/**
	 * Saves longest name of metric
	 *
	 * @param file metric file
	 */
	static void longestMetName(File file)
	{
		longestMet = Math.max(longestMet, file.getName().length());
	}
	
	/**
	 * Saves longest name of object
	 *
	 * @param file object file
	 */
	static void longestObjName(File file)
	{
		longestObj = Math.max(longestObj, file.getName().length());
	}
	
	/**
	 * @return length of longest algorithm name
	 */
	static int getLongestAlg()
	{
		return longestAlg;
	}
	
	/**
	 * @return length of longest metric name
	 */
	static int getLongestMet()
	{
		return longestMet;
	}
	
	/**
	 * @return length of longest object name
	 */
	static int getLongestObj()
	{
		return longestObj;
	}
}

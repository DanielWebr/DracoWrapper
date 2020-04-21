package zswi;

import java.io.File;

class FilesMessenger
{
	/**
	 * Save directories of each category
	 */
	final File algorithmDir, metricDir, objectDir;
	/**
	 * True if combination is already tested false otherwise
	 */
	final boolean done;
	
	/**
	 * @param algorithmDir algorithm directory
	 * @param metricDir metric directory
	 * @param objectDir object directory
	 * @param done if combination is already tested
	 */
	FilesMessenger(File algorithmDir, File metricDir, File objectDir, boolean done)
	{
		this.algorithmDir = algorithmDir;
		this.metricDir = metricDir;
		this.objectDir = objectDir;
		this.done = done;
	}
	
	@Override
	/**
	 * @return combination and each category is centred against longest name in that category
	 */
	public String toString()
	{
		return String.format("%s - %s - %s",
							 center(algorithmDir.getName(), ComboMessenger.getLongestAlg()),
							 center(metricDir.getName(), ComboMessenger.getLongestMet()),
							 center(objectDir.getName(), ComboMessenger.getLongestObj()));
	}
	
	/**
	 * @return combination name for directory
	 */
	String getDir()
	{
		return algorithmDir.getName() + "_" + metricDir.getName() + "_" + objectDir.getName();
	}
	
	/**
	 * Centres given string to given size
	 *
	 * @param s string that will be centred
	 * @param size size of output string
	 * @return centred string
	 */
	private static String center(String s, int size)
	{
		StringBuilder sb = new StringBuilder(size);
		for(int i = 0; i < (size - s.length()) / 2; i++)
			sb.append(' ');
		sb.append(s);
		while(sb.length() < size)
			sb.append(' ');
		return sb.toString();
	}
}

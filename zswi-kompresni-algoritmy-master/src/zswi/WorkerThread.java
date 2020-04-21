package zswi;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static zswi.Main.*;

class WorkerThread extends Thread
{
	private BigDecimal score;
	private Process process;
	private double currentBitrate, previousBitrate;
	private boolean forceStop;
	private final double bitrateStep, top, bottom, center, initialStep;
	private final File algCopyDir, metCopyDir;
	private final String 	resultObj, compressLaunch, compressedObjName,
							decompressLaunch, metricLaunch, reindexLaunch;
	private final Lock forceStopLock;
	private final Path 	objOriginal, algOriginal, metOriginal,
						algDirPath, metDirPath, objDirPath;
	
	/**
	 * List of numbers given to compression algorithm
	 */
	private List<Double> deltas;
	/**
	 * List of bit rates of result file
	 */
	private List<Double> bitRates;
	/**
	 * List of distortion results from metric
	 */
	private List<BigDecimal> results;
	/**
	 * Number of vertexes in original object
	 */
	private final long vertexInOriginal;
	/**
	 * Max number of bitrate that will be tested
	 */
	private final double maxBitrate;
	/**
	 * Tested combination
	 */
	private final FilesMessenger combination;
	
	/**
	 * Constructor setts up all necessary variables
	 *
	 * @param combination tested combination
	 */
	WorkerThread(FilesMessenger combination)
	{
		this.combination = combination;
		
		top = 1.0;
		bottom = 0.0;
		center = bottom + (top - bottom) / 2.0;
		initialStep = 0.0625;
		forceStop = false;
		forceStopLock = new ReentrantLock();
		results = new ArrayList<>();
		deltas = new ArrayList<>();
		bitRates = new ArrayList<>();
		maxBitrate = getMaxBitrate();
		bitrateStep = getBitrateStep();
		
		String objDir = combination.objectDir.getAbsolutePath();
		String metDir = combination.metricDir.getAbsolutePath();
		String algDir = combination.algorithmDir.getAbsolutePath();
		String comboDir = ComboMessenger.tempDir.getAbsolutePath() + "\\" + combination.getDir();
		String objName = combination.objectDir.getName() + ".obj";
		String reIndexedObjName = "reindexed.obj";
		compressedObjName = combination.objectDir.getName() + ".obj.bin";
		String metName = combination.metricDir.getName() + ".exe";
		String metDirName = combination.metricDir.getName();
		String algDirName = combination.algorithmDir.getName();
		
		objOriginal = Paths.get(objDir + "\\" + objName);
		algOriginal = Paths.get(algDir);
		metOriginal = Paths.get(metDir);
		
		algDirPath = Paths.get(comboDir + "\\" + algDirName);
		metDirPath = Paths.get(comboDir + "\\" + metDirName);
		objDirPath = Paths.get(comboDir + "\\" + algDirName + "\\" + objName);
		
		vertexInOriginal = numOfVertex(objOriginal.toFile());
		
		algCopyDir = algDirPath.toFile();
		metCopyDir = metDirPath.toFile();
		
		String reindexFile = "reindex.exe";
		String compressFile = "compress.exe";
		String decompressFile = "decompress.exe";
		resultObj = "mesh.obj";
		
		reindexLaunch = "\"" + comboDir + "\\" + algDirName + "\\" + reindexFile + "\" \""
				+ comboDir + "\\" + algDirName + "\\" + objName + "\" \""
				+ comboDir + "\\" + algDirName + "\\" + reIndexedObjName + "\"";
		compressLaunch = "\"" + comboDir + "\\" + algDirName + "\\" + compressFile + "\" \""
				+ comboDir + "\\" + algDirName + "\\" + objName + "\" ";
		decompressLaunch = "\"" + comboDir + "\\" + algDirName + "\\" + decompressFile + "\" \""
				+ comboDir + "\\" + algDirName + "\\" + compressedObjName + "\"";
		metricLaunch = "\"" + comboDir + "\\" + metDirName + "\\" + metName + "\" \"" + comboDir
				+ "\\" + algDirName + "\\" + reIndexedObjName + "\" \"" + comboDir + "\\"
				+ algDirName + "\\" + resultObj + "\"";
	}
	
	@Override
	/**
	 * Copies original algorithm, metric and object,
	 * runs reindexing of original object, then run tests
	 * from center to top and then from center to bottom
	 * and then saves results to database.
	 */
	public void run()
	{
		copyFolder(algOriginal, algDirPath);
		copyFolder(metOriginal, metDirPath);
		copyFile(objOriginal, objDirPath);
		
		process = launchExe(reindexLaunch, algCopyDir);
		if(waitForProcess(System.nanoTime()))
			return;
		
		if(testTopHalf())
			return;
		
		if(testBottomHalf())
			return;
		
		
		List<BigDecimal> newResults = new ArrayList<>();
		List<Double> newDeltas = new ArrayList<>();
		List<Double> newBitrates = new ArrayList<>();
		
		for(int i = 0; i < deltas.size(); i++)
		{
			double delta = deltas.get(i);
			if(!newDeltas.contains(delta))
			{
				newDeltas.add(delta);
				newResults.add(results.get(i));
				newBitrates.add(bitRates.get(i));
			}
		}
		
		results = newResults;
		deltas = newDeltas;
		bitRates = newBitrates;
		
		int comboId;
		String condition = "metric_name = '" + combination.metricDir.getName() +
				"' && object_name = '" + combination.objectDir.getName() +
				"' && algorithm_name = '" + combination.algorithmDir.getName() + "'";
		if(Database.recordExists("combo", condition))
		{
			comboId = Database.selectID("combo", condition);
			Database.tryDeleteRecords("results", "combo_id = " + comboId);
			Database.tryDeleteRecords("combo", "id = " + comboId);
		}
		
		comboId = Database.insertAndReturnID("combo",
											 "metric_name, object_name, algorithm_name, id",
											 "'" + combination.metricDir.getName() + "', '"
													 + combination.objectDir.getName() + "', '"
													 + combination.algorithmDir.getName() + "', 0");
		
		for(int i = 0; i < results.size(); i++)
		{
			if(Database.errorOccurred())
				return;
			Database.insertRecord("results", "id, delta, distortion, rate, combo_id",
								  "0, '" + deltas.get(i) + "', '" + results.get(i).toString() +
										  "', " + bitRates.get(i) + ", '" + comboId + "'");
		}
	}
	
	/**
	 * Tests algorithm from center to top. If distance between current and previous
	 * bitrate is too big then delta is set back to previous value
	 * and testing continues with smaller steps. If previous bitrate is same
	 * as current bitrate then testing ends.
	 *
	 * @return true if testing was stopped otherwise false
	 */
	private boolean testTopHalf()
	{
		List<Double> testedDeltas = new ArrayList<>();
		double step = initialStep;
		double delta = center - step;
		previousBitrate = -1.0;
		
		while(true)
		{
			if(isForceStopped(false))
				return false;
			
			delta += step;
			if(step <= 0.00000001 || delta <= 0.0)
				break;
			
			if(testing(delta))
				return true;
			
			if(previousBitrate != -1.0)
				if(previousBitrate - currentBitrate > bitrateStep)
				{
					delta -= step * 2;
					step /= 2;
				}
				else if(previousBitrate == currentBitrate)
					break;
				else
					testedDeltas.add(delta);
			else
				testedDeltas.add(delta);
			
			if(currentBitrate <= 0.0)
				break;
		}
		return false;
	}
	
	/**
	 * Tests algorithm from center to bottom. If distance between current and previous
	 * bitrate is too big then delta is set back to previous value
	 * and testing continues with smaller steps.
	 *
	 * @return true if testing was stopped otherwise false
	 */
	private boolean testBottomHalf()
	{
		List<Double> testedDeltas = new ArrayList<>();
		double step = initialStep;
		double delta = center + step;
		previousBitrate = -1.0;
		
		while(true)
		{
			if(isForceStopped(false))
				return false;
			
			delta -= step;
			if(step <= 0.00000001 || delta <= 0.0)
				break;
			
			if(testing(delta))
				return true;
			
			if(previousBitrate != -1.0)
				if(currentBitrate - previousBitrate > bitrateStep)
				{
					delta += step * 2;
					step /= 2;
				}
				else
					testedDeltas.add(delta);
			else
				testedDeltas.add(delta);
			
			if(currentBitrate >= maxBitrate)
				break;
		}
		return false;
	}
	
	/**
	 * Copies original object, gives it to compress algorithm,
	 * output of compress is then decompressed, then bitrate is calculated,
	 * copy of object is rewritten by copy of original (in case of tested algorithm corrupting it),
	 * metric compares decompressed object with original and returns value that is saved to list.
	 *
	 * @param delta number given to compress algorithm
	 * @return true if testing was stopped otherwise false
	 */
	private boolean testing(double delta)
	{
		process = launchExe(compressLaunch + delta, algCopyDir);
		if(waitForProcess(System.nanoTime()) || isForceStopped(true))
			return true;
		
		File resultObjFile = new File(algCopyDir + "\\" + compressedObjName);
		previousBitrate = currentBitrate;
		currentBitrate = resultObjFile.length() * 8.0 / vertexInOriginal;
		bitRates.add(currentBitrate);
		
		process = launchExe(decompressLaunch, algCopyDir);
		if(waitForProcess(System.nanoTime()) || isForceStopped(true))
			return true;
		
		process = launchExe(metricLaunch, metCopyDir);
		if(waitForProcess(System.nanoTime()) || isForceStopped(true))
			return true;
		
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
		String line;
		try
		{
			line = reader.readLine();
		}
		catch(IOException e)
		{
			printToLog("Error while reading output of algorithm", true);
			return true;
		}
		if(line != null)
		{
			try
			{
				score = new BigDecimal(
						line.trim().replace(",", "."));
			}
			catch(NumberFormatException e)
			{
				printToLog("Combination [" + combination
								   + "] did not return expected value", false);
				return true;
			}
			System.out.format("[%s] - Delta: %.10f - Distortion: %19.15f - Bitrate: %f\n",
							  combination, delta, score, currentBitrate);
			results.add(score);
			deltas.add(delta);
			return false;
		}
		else
		{
			printToLog("Combination [" + combination + "] did not return any value",
					   false);
			return true;
		}
	}
	
	/**
	 * Sets "forceStop" to true causing all processes being forced
	 * to stop and no more testing is done by this thread.
	 */
	void forceStopWork()
	{
		synchronized(forceStopLock)
		{
			forceStop = true;
		}
	}
	
	/**
	 * @param printToLog Prints error to log if true otherwise not
	 * @return 	Returns true when forceStop is true and
	 *			prints error to log if printToLog is true
	 * 	 		otherwise returns false.
	 */
	private boolean isForceStopped(boolean printToLog)
	{
		forceStopLock.lock();
		if(forceStop)
		{
			forceStopLock.unlock();
			if(printToLog)
				printToLog("Combination [" + combination
								   + "] was forced to stop", false);
			return true;
		}
		forceStopLock.unlock();
		return false;
	}
	
	/**
	 * Waits for process for number of seconds given by user.
	 * If process ends until timeout then false is returned.
	 * If process lives after timeout or is force stopped then it is destroyed and true is returned.
	 *
	 * @param start how long can process live until it is destroyed
	 * @return true if was force stopped or process did not end until timeout otherwise false
	 */
	private boolean waitForProcess(long start)
	{
		if(process == null)
		{
			printToLog("[" + combination + "] - Process did not started",
					   false);
			return true;
		}
		while(process.isAlive() && start + 1000000000L * getTimeout() > System.nanoTime())
		{
			if(isForceStopped(false))
			{
				process.destroy();
				return true;
			}
			try
			{
				sleep(10);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if(process.isAlive())
		{
			process.destroy();
			printToLog("[" + combination + "] - Process was terminated (timeout)",
					   false);
			return true;
		}
		else
			return false;
	}
	
	/**
	 * @param file file to search for vertexes
	 * @return number of vertex found in file
	 */
	private static long numOfVertex(File file)
	{
		long numOfVertex = 0;
		try
		{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String line = bufferedReader.readLine();
			while(line != null)
			{
				if(line.length() > 0 && line.charAt(0) == 'v' && line.charAt(1) == ' ')
					numOfVertex++;
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			System.out.println(numOfVertex);
		}
		catch(IOException e)
		{
			printToLog("Error while reading object", true);
		}
		return numOfVertex;
	}
}
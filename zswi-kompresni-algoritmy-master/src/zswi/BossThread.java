package zswi;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.ArrayList;
import java.util.List;

import static zswi.Main.getNumOfThreads;

class BossThread extends Thread
{
	private TestingState state = TestingState.RUN;
	private final Lock remainingLock = new ReentrantLock();
	private final Lock stateLock = new ReentrantLock();
	private final Lock doneLock = new ReentrantLock();
	private final BooleanProperty done = new SimpleBooleanProperty();
	private final IntegerProperty remaining;
	
	/**
	 * Maximum number of threads that can be used.
	 */
	private final int numOfThreads = getNumOfThreads();
	/**
	 * List of combinations of alg/met/obj to test.
	 */
	private final List<FilesMessenger> testCombinations;
	
	/**
	 * Constructor copies all elements from parameter testCombinations
	 * to this.testCombinations and sets remaining.
	 *
	 * @param testCombinations
	 */
	BossThread(List<FilesMessenger> testCombinations)
	{
		this.testCombinations = new ArrayList<>(testCombinations);
		remaining = new SimpleIntegerProperty(testCombinations.size());
	}
	
	@Override
	/**
	 * Assigns combinations to instances of WorkerThreads. Maximum concurrently running
	 * WorkerThreads is limited by "numOfThreads". If "state" becomes "STOP" then all
	 * list of remaining tests is emptied. If "state" becomes FORCE_STOP then
	 * each WorkerThread destroys its running processes and then stops.
	 */
	public void run()
	{
		List<WorkerThread> workerThreads = new ArrayList<>();
		
		while(!(testCombinations.isEmpty() && workerThreads.isEmpty()))
		{
			stateLock.lock();
			TestingState testingState = state;
			stateLock.unlock();
			
			if(testingState == TestingState.FORCE_STOP)
			{
				for(WorkerThread thread : workerThreads)
					thread.forceStopWork();
				break;
			}
			
			if(workerThreads.size() < numOfThreads && !testCombinations.isEmpty())
			{
				if(testingState == TestingState.RUN)
				{
					workerThreads.add(new WorkerThread(
							testCombinations.get(testCombinations.size() - 1)));
					workerThreads.get(workerThreads.size() - 1).start();
					testCombinations.remove(testCombinations.size() - 1);
				}
				else
					testCombinations.clear();
			}
			else
				try
				{
					sleep(10);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			
			for(int i = 0; i < workerThreads.size(); i++)
				if(!workerThreads.get(i).isAlive())
				{
					workerThreads.remove(i--);
					remainingLock.lock();
					remaining.setValue(remaining.getValue() - 1);
					remainingLock.unlock();
				}
		}
		doneLock.lock();
		done.setValue(true);
		doneLock.unlock();
	}
	
	/**
	 * @return true if all tests are done or stopped otherwise false
	 */
	BooleanProperty isDone()
	{
		synchronized(doneLock)
		{
			return done;
		}
	}
	
	/**
	 * @return number of remaining tests
	 */
	IntegerProperty getRemaining()
	{
		synchronized(remainingLock)
		{
			return remaining;
		}
	}
	
	/**
	 * Changes "state" to STOP
	 */
	void stopWork()
	{
		stateLock.lock();
		state = TestingState.STOP;
		stateLock.unlock();
	}
	
	/**
	 * Changes "state" to FORCE_STOP
	 */
	void forceStopWork()
	{
		stateLock.lock();
		state = TestingState.FORCE_STOP;
		stateLock.unlock();
	}
}
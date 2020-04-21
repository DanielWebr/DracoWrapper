package zswi;

public enum Category
{
	/**
	 * Categories without "d" at the beginning represents local file
	 * and with d at the beginning represents records in database
	 */
	Algorithm, Metric, Object, dAlgorithm, dMetric, dObject, dCombination;
	
	/**
	 * @return 	If category starts with "d" then "d" is removed
	 * 			and " (Database)" is added at the end of the name
	 * 			otherwise returns name unchanged
	 */
	@Override
	public String toString()
	{
		return super.name().charAt(0) == 'd'
				? super.name().replaceFirst("d", "") + " (Database)"
				: super.name();
	}
}

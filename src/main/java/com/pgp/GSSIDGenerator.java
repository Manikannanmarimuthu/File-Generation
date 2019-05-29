/**
 * 
 */
package com.pgp;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vijaya Kumar S
 *
 */
public final class GSSIDGenerator {

	/**
	 * instance
	 */
	private static GSSIDGenerator instance = new GSSIDGenerator();
	
	private static final String PATTERN = "ddMMyyyy";
	
	/**
	 * id container
	 */
	private final Map<String, Integer> idMap;
	
	/**
	 * id container
	 */
	private final Map<String, BigDecimal> idDblMap;
	
	/**
	 * current date
	 */
	private Date date;
	
	/**
	 * maximum id value
	 */
	private final static int MAX = 99999;
	
	/**
	 * maximum id value
	 */
	private final static double DBL_MAX = 9999999999999.99;
	
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(final Date date) {
		this.date = date;
	}

	/**
	 * @return the idMap
	 */
	public Map<String, Integer> getIdMap() {
		return idMap;
	}
	
	/**
	 * @return the idMap
	 */
	public Map<String, BigDecimal> getIdDblMap() {
		return idDblMap;
	}

	private GSSIDGenerator () {
		idMap = new HashMap<String, Integer>();
		idDblMap = new HashMap<String, BigDecimal>();
		date = FileUtil.getDate(PATTERN);
	}
	
	/**
	 * @return
	 */
	public static GSSIDGenerator getInstance() {
		return instance;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public Integer nextValue(final String key) {
		Integer runningNumber = null;
		synchronized (this) {
			if (this.isDateChanged()) {
				this.reset();
			}
			if (idMap.containsKey(key)) {
				runningNumber = idMap.get(key);
			} else {
				runningNumber = 0;
			}
			runningNumber++;
			if (runningNumber > MAX) {
				runningNumber = 1;
			}
			idMap.put(key, runningNumber);
		}
		return runningNumber;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public BigDecimal nextDblValue(final String key, final String fixVal, String incVal) {
		BigDecimal runningNumber = null;
		synchronized (this) {
			if (this.isDateChanged()) {
				this.reset();
			}
			if (idDblMap.containsKey(key)) {
				runningNumber = idDblMap.get(key);
			} else {
				if(fixVal == null){
					runningNumber = new BigDecimal(0.1);
				} else {
					BigDecimal valBD = new BigDecimal(fixVal);
					//Float flVal = Float.parseFloat(fixVal);
					DecimalFormat df = new DecimalFormat("0.00");
					df.setMaximumFractionDigits(2);
					//DecimalFormat form = new DecimalFormat("#.##");
					runningNumber = new BigDecimal(df.format(valBD));
				}
			}
			if(incVal == null){
				runningNumber = runningNumber.add(new BigDecimal(0.01));
			} else {
				runningNumber = runningNumber.add(new BigDecimal(incVal));
			}
			BigDecimal defValBD = new BigDecimal(runningNumber.toString());
			//Float defVal = Float.parseFloat(runningNumber.toString());
			DecimalFormat df = new DecimalFormat("0.00");
			df.setMaximumFractionDigits(2);
			runningNumber = new BigDecimal(df.format(defValBD));
			if (runningNumber.doubleValue() > DBL_MAX){
				runningNumber = new BigDecimal(0.1);
			}
			idDblMap.put(key, runningNumber);
		}
		return runningNumber;
	}
	/**
	 * @param key
	 * @param leftPad
	 * @return
	 */
	public String nextValue(final String key, final Integer leftPad) {
		return String.format("%0"+leftPad+"d", nextValue(key));
	}
	
	private boolean isDateChanged() {
		return date.compareTo(FileUtil.getDate(PATTERN)) > 0;
	}
	
	private void reset() {
		this.idMap.clear();
		this.date = FileUtil.getDate(PATTERN);
	}
}

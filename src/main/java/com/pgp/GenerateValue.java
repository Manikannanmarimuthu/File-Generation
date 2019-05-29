package com.pgp;

import java.math.BigDecimal;
import java.util.Properties;

public class GenerateValue {

	public static String generate(String column, Properties prop, String fileCnt) {
		String value = null;
		String format = prop.getProperty(column + ".format");
		if (format != null && !format.isEmpty()) {
			String pref = prop.getProperty(column + ".pref");
			String suff = prop.getProperty(column + ".suff");
			String gen = "";
			switch (format) {
				case "dt_num": {
					gen = genDt(column, prop, fileCnt);
					break;
				}
				case "str_num": {
					gen = genStrNum(column, prop, fileCnt);
					break;
				}
				case "amt": {
					gen = genAmtVal(column, prop);
					break;
				}
				default:
					break;
			}
			value = concat(pref, gen, suff);
		}
		return value;
	}
	
	private static String genDt(String column, Properties prop, String fileCnt) {
		String format = prop.getProperty(column + ".dt");
		String value = fileCnt;
		if (format != null) {
			value += FileUtil.getDateTime(format);
			value += genNum(column, prop);
		}
		return value;
	}
	
	
	private static String genStrNum(String column, Properties prop, String fileCnt) {
		String value = fileCnt;
		value += genNum(column, prop);
		return value;
	}
	
	private static String genAmtVal(String column, Properties prop) {
		String value = null;
		String num = prop.getProperty(column + ".num");
		String incVal = prop.getProperty(column + ".inc.value");
		final BigDecimal dblValue = GSSIDGenerator.getInstance().nextDblValue(column, num, incVal);
		String curr = prop.getProperty("transfer_ccy.default.value");
		if("JPY".equalsIgnoreCase(curr)){
			value =  String.valueOf(dblValue.intValue());
		} else {
			value = String.valueOf(dblValue);
		}
		return value;
	}
	
	private static String genNum(String column, Properties prop) {
		String format = prop.getProperty(column + ".num");
		int max = Integer.parseInt(format);
		return GSSIDGenerator.getInstance().nextValue(column, max);
	}
	
	private static String concat(String... values) {
		String value = "";
		for (final String data : values) {
			if (data != null) {
				value += data;
			}
		}
		return value;
	}
}
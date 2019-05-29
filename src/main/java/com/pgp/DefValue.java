package com.pgp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefValue {

	public DefValue(String[] columns, Properties prop) throws IOException {
		this.init(columns, prop);
	}

	private Map<String, String[]> map = new HashMap<String, String[]>();

	private Map<String, Integer> indexMap = new HashMap<String, Integer>();

	public String getDefValue(String column) {
		String value = null;
		if (map.containsKey(column)) {
			String[] values = map.get(column);
			int index = indexMap.get(column);
			index ++;
			if (index >= values.length) {
				index = 0;
			}
			indexMap.put(column, index);
			value = values[index];
		}
		return value;
	}

	private void init(String[] columns, Properties prop) throws IOException {
		for (int i = 0; i < columns.length; i++) {
			String colName = columns[i];
			String value = prop.getProperty(colName + ".default.value");
			if (value == null || value.isEmpty()) {
				continue;
			}
			map.put(colName, value.split(","));
			indexMap.put(colName, -1);
		}
	}
}
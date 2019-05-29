package com.pgp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class FileUtil {

	public static File[] listFiles(String path) {
		File file = new File(path);
		return file.listFiles();
	}

	public static void write2File(String fileName, String data) {
		final File file = new File(fileName);
		try (FileWriter writer = new FileWriter(file);) {
			writer.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void write2File(String fileName, List<String[]> data) {
		final File file = new File(fileName);
		try (FileWriter writer = new FileWriter(file);) {
			for (String dt[] : data) {
				writer.write(dt[0]);
				for (int i = 1; i < dt.length; i++) {
					writer.write("," + dt[i]);
				}
				writer.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static InputStream getInputStream(final String fileName) throws FileNotFoundException {
		final File file = new File(fileName);
		InputStream stream = null;
		if (file.exists()) {
			stream = new FileInputStream(file);
		} else {
			stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		}
		return stream;
	}

	public static Properties getProperty(final String path) throws IOException {
		final Properties property = new Properties();
		body: try (InputStream stream = FileUtil.getInputStream(path)) {
			if (stream == null) {
				System.out.println("file not found");
				break body;
			}
			try (final InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				property.load(isr);
			}
		}
		return property;
	}

	public static String getDateTime(String dtFormat) {
		SimpleDateFormat format = new SimpleDateFormat(dtFormat);
		return format.format(new Date());
	}

	public static File getFile(final String dir) {
		String fileName = dir + File.separator + getDateTime("yyyy_MM_dd'T'hh_mm_ss_SSS");
		return new File(fileName);
	}

	public static Date getDate(final String pattern) {
		final Date date = new Date();
		final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
		final String now = format.format(date);
		Date formatted = null;
		try {
			formatted = format.parse(now);
		} catch (ParseException e) {
			formatted = new Date();
		}
		return formatted;
	}
}

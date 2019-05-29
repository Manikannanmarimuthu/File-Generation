package com.pgp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mvi.cmm.util.pgp.CompressionAlgorithm;
import com.mvi.cmm.util.pgp.EncryptionAlgorithm;
import com.mvi.cmm.util.pgp.FileKeyProvider;
import com.mvi.cmm.util.pgp.HashingAlgorithm;
import com.mvi.cmm.util.pgp.PGPCipher;
import com.mvi.cmm.util.pgp.PGPException;

public class ReadPropertiesFile {
	public static void main(String[] args) throws IOException {
		final Properties prop = FileUtil.getProperty("conf.properties");
		NeedGenerateFile(prop, "gen.inp.file", false);
		NeedGenerateFile(prop, "gen.supp.file", true);
	}
	
	static void NeedGenerateFile(final Properties prop, final String genFileKeyName, 
			final boolean genSuppFileFlag) throws IOException{
		final String genFileFlagStr = prop.getProperty(genFileKeyName);
		if("Y".equals(genFileFlagStr)){
			generateFile(prop, genSuppFileFlag);
		}
	}
	
	static void generateFile(final Properties prop, final boolean genSuppFileFlag) throws IOException{
		
		final String maxfilecount = prop.getProperty("max.file.count");
		final String maxreccount = prop.getProperty("max.rec.count");
		final String marketId = prop.getProperty("market_id.default.value");
		final String currency = prop.getProperty("transfer_ccy.default.value");
		final String encFileLoc = prop.getProperty("to.enc.loc");
		final String keystoreFileLoc = prop.getProperty("keystore.loc");
		final String[] column = getAndSplitFromProp(prop, genSuppFileFlag ? "supp.column.def" : "column.def");
		final String[] data = getAndSplitFromProp(prop, genSuppFileFlag ? "supp.header" : "header");
		
		final DefValue defValue = new DefValue(column, prop);
		
		Integer accNumPassCnt = null;
		String[] accNumbers = null;
		if(!genSuppFileFlag){
			accNumPassCnt = Integer.parseInt(prop.getProperty("acct_num.pass.count"));
			accNumbers = getAndSplitFromProp(prop,"acct_num.default.values");
		}
		
		final String loc = prop.getProperty("to.loc");
		final List<String> fileNameList = new ArrayList<String>();
		final int result1 = Integer.parseInt(maxfilecount);
		final int result = Integer.parseInt(maxreccount);
		String value;
		for (Integer j = 1; j <= result1; j++) {
			final List<String[]> list = new ArrayList<String[]>();
			list.add(data);
			for (int r = 1; r <= result; r++) {
				final String[] values = new String[column.length];
				for (int c = 0; c < column.length; c++) {
					value = defValue.getDefValue(column[c]);
					if (value == null) {
						value = GenerateValue.generate(column[c], prop, String.format("%02d", j));
					}
					values[c] = value;
				}
				if(!genSuppFileFlag){
					final String benefType = values[5];
					if (benefType != null) {
						if ("C".equals(benefType)) {
							values[12] = "";
						} else if ("P".equals(benefType)) {
							values[13] = "";
						}
					}
					if (r <= accNumPassCnt) {
						values[2] = accNumbers[0];
					} else {
						values[2] = accNumbers[1];
					}
				}
				
				list.add(values);
			}
			fileNameList.add(write2File(list, loc, currency, marketId, j, genSuppFileFlag));
		}

		
		filecreated(fileNameList, maxfilecount, maxreccount, loc, encFileLoc, keystoreFileLoc, prop);
	}

	private static String write2File(final List<String[]> data, final String loc, final String currency,
			final String marketId, final Integer j, final boolean genSuppFileFlag) {
		final String fileName = genFileName(loc, currency, marketId, j, genSuppFileFlag);
		final String fileNameWithLoc = loc + File.separator + fileName;
		
		FileUtil.write2File(fileNameWithLoc, data);
		return fileName;
	}

	public static String genFileName(final String decLoc, final String currency, final String marketId, 
			Integer j, final boolean genSuppFileFlag) {
		String fileName;
		body: {
			final String suppSuffix = genSuppFileFlag ? "supp_" : "";
			fileName = marketId + "_" + currency + "_" + FileUtil.getDateTime("yyyyMMdd") + "_"
					+ suppSuffix + String.format("%06d", j) + ".csv.out";
			final File decFile = new File(decLoc);
			final String[] decFileArr = decFile.list();
			if (decFileArr == null || decFileArr.length == 0) {
				break body;
			}
			for (final String name : decFileArr) {
				if (fileName.equals(name)) {
					fileName = marketId + "_" + currency + "_" + FileUtil.getDateTime("yyyyMMdd") + "_"
							+ suppSuffix + String.format("%06d", j + 1) + ".csv.out";
					j++;
				}
			}
		}
		return fileName;
	}

	public static String[] getAndSplitFromProp(final Properties prop, final String keyName) throws IOException {
		final String def = prop.getProperty(keyName);
		return def.split(",");
	}
	

	public static void encrypt(final String fileName, final String loc, final String encFileLoc,
			final String keystoreFileLoc, final Properties prop) {
		PGPCipher cipher = null;
		final String currency = prop.getProperty("transfer_ccy.default.value");
		final String cltPubKey = prop.getProperty(currency + ".client.pub.key");
		final String gsPubKey = prop.getProperty(currency + ".gss.pub.key");
		final String cltSecKey = prop.getProperty(currency + ".client.sec.key");
		final String cltPassphrase = prop.getProperty(currency + ".client.passphrase");
		try {
			cipher = getPGPCipher();
			final FileKeyProvider encPubKeyProvider = new FileKeyProvider(new String[] {
					keystoreFileLoc + File.separator + cltPubKey, keystoreFileLoc + File.separator + gsPubKey }, null,
					null);
			final FileKeyProvider priKeyProvider = new FileKeyProvider(null, keystoreFileLoc + File.separator
					+ cltSecKey, cltPassphrase);
			final InputStream inputStream = new FileInputStream(loc + File.separator + fileName);
			final String destFilePath = encFileLoc + File.separator + fileName;
			cipher.encrypt(encPubKeyProvider, priKeyProvider, inputStream, destFilePath);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PGPException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws PGPException
	 */
	public static PGPCipher getPGPCipher() throws PGPException {
		final PGPCipher cipher = new PGPCipher.Builder()
				.withEncryptionAlgorithm(EncryptionAlgorithm.AES128)
				.withHashingAlgorithm(HashingAlgorithm.SHA256)
				.withCompression(CompressionAlgorithm.Uncompressed, 0)
				.build();
		return cipher;
	}

	public static void filecreated(final List<String> fileNameList, final String maxfilecount,
			final String maxreccount, final String loc, final String encFileLoc, final String keystoreFileLoc,
			final Properties prop) {
		System.out.println(maxfilecount + " File(s) Created with " + maxreccount + " instruction(s) each."
				+ "\nBelow are file name(s) created in location : \n" + loc);
		for (final String fileName : fileNameList) {
			encrypt(fileName, loc, encFileLoc, keystoreFileLoc, prop);
			System.out.println(fileName);
		}
	}
}
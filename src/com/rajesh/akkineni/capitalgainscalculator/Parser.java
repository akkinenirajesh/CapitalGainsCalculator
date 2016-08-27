package com.rajesh.akkineni.capitalgainscalculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Parser {
	public static DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyy");
	private static Date date;

	public static List<Trade> parse(String[] args) throws Exception {
		List<Trade> ret = new ArrayList<>();
		for (String file : args) {
			ret.addAll(parse(file));
		}
		return ret;
	}

	public static List<Trade> parse(String file) throws Exception {
		List<Trade> ret = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				Trade trade = parseTrade(line);
				if (trade != null) {
					ret.add(trade);
				}
			}
		}
		return ret;
	}

	private static Trade parseTrade(String line) throws ParseException {
		Trade trade = new Trade();

		String[] split = line.split(",");
		if (split.length == 1) {
			int dateIndex = split[0].indexOf("Trade Date: ");
			if (dateIndex == 0) {
				date = dateFormat1.parse(split[0].substring(12));
			}
		}
		if (split.length < 13) {
			return null;
		}
		if (!split[7].equals("CASH")) {
			return null;
		}
		trade.date = date;
		trade.name = split[6];
		trade.buy = !split[8].equals("0");
		if (trade.buy) {
			trade.qty = Integer.parseInt(split[8]);
		} else {
			trade.qty = Integer.parseInt(split[9]);
		}
		trade.rate = Double.parseDouble(split[10]);
		trade.brok = Double.parseDouble(split[11]);
		return trade;
	}

}

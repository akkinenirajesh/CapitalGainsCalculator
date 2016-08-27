package com.rajesh.akkineni.capitalgainscalculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Parser {
	public static DateTimeFormatter dateFormat1 = new DateTimeFormatterBuilder().appendPattern("dd-MMM-yyy")
			.toFormatter();
	private static LocalDate date;

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
				} else {
					// System.err.println("Ignored " + line);
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
				date = LocalDate.parse(split[0].substring(12), dateFormat1);
			}
		}
		if (split.length < 13) {
			return null;
		}
		if (!(split[3].equals("BSE") || split[3].equals("NSE"))) {
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

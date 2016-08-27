package com.rajesh.akkineni.capitalgainscalculator;

import java.util.Date;

public class Trade implements Cloneable {
	Date date;
	String name;
	boolean buy;
	int qty;
	double rate;
	double brok;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(",");
		sb.append(Parser.dateFormat1.format(date));
		sb.append(",");
		if (buy) {
			sb.append("buy");
		} else {
			sb.append("sell");
		}
		sb.append(",");
		sb.append(qty);
		sb.append(",");
		sb.append(rate);
		sb.append(",");
		sb.append(brok);
		return sb.toString();
	}

	public Trade clone(){
		Trade trade = new Trade();
		trade.date = date;
		trade.name = name;
		trade.qty = qty;
		trade.buy = buy;
		trade.rate = rate;
		trade.brok = brok;
		return trade;
	}

}
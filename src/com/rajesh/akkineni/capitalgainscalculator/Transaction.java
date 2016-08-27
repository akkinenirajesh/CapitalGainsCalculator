package com.rajesh.akkineni.capitalgainscalculator;

import java.util.Date;

public class Transaction {

	String name;
	Date buyDate;
	Date sellDate;
	int qty;
	double buyRate;
	double sellRate;
	double buyBrok;
	double sellBrok;
	double profit;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(",");
		sb.append(qty);
		sb.append(",");
		sb.append(Parser.dateFormat1.format(buyDate));
		sb.append(",");
		sb.append(buyRate);
		sb.append(",");
		sb.append(buyBrok);
		sb.append(",");
		sb.append(Parser.dateFormat1.format(sellDate));
		sb.append(",");
		sb.append(sellRate);
		sb.append(",");
		sb.append(sellBrok);
		sb.append(",");
		sb.append(String.format("%.2f", profit));
		return sb.toString();
	}

}

package com.rajesh.akkineni.capitalgainscalculator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CapitalGainsCalculator {

	private int fy;
	private Date startDate;
	private Date endDate;

	public CapitalGainsCalculator(int fy) {
		this.fy = fy;
		Calendar cal = Calendar.getInstance();
		cal.set(2000 + fy, 3, 1);
		this.startDate = cal.getTime();
		cal.set(2001 + fy, 2, 31);
		this.endDate = cal.getTime();
	}

	public void calculate(String[] args) throws Exception {
		List<Trade> trades = Parser.parse(args);
		// sort by date
		trades.sort((o1, o2) -> o1.date.compareTo(o2.date));

		trades = combineByDateAndPrice(trades);
		// make a clone
		List<Trade> clone = trades.stream().map(Trade::clone).collect(Collectors.toList());
		List<Trade> buys = clone.stream().filter(i -> i.buy).collect(Collectors.toList());
		List<Trade> sells = clone.stream().filter(i -> !i.buy).collect(Collectors.toList());
		// trades.forEach(i -> System.out.println(i));

		HashMap<String, Integer> missing = findMissingTrade(buys, sells);
		// .filterv(i -> i.date.after(startDate) && i.date.before(endDate))
		for (Entry<String, Integer> item : missing.entrySet()) {
			System.out.println("Missing : " + item.getKey() + " : " + item.getValue());
		}
		List<Transaction> tx = computeTransactions(buys, sells);
		tx.forEach(t -> System.out.println(t));
	}

	private List<Trade> combineByDateAndPrice(List<Trade> trades) {
		return trades.stream().collect(ArrayList<Trade>::new, (a, b) -> {
			Trade match = null;
			for (Trade trade : a) {
				if (trade.buy == b.buy && trade.name.equals(b.name) && trade.date.equals(b.date)
						&& trade.rate == b.rate) {
					match = trade;
				}
			}
			if (match != null) {
				match.qty += b.qty;
			} else {
				a.add(b);
			}
		} , (a, b) -> a.addAll(b));
	}

	private List<Transaction> computeTransactions(List<Trade> buys, List<Trade> sells) {
		List<Transaction> ret = new ArrayList<>();
		final List<Trade> buys1 = buys.stream().map(Trade::clone).collect(Collectors.toList());
		sells = sells.stream().map(Trade::clone).collect(Collectors.toList());
		// find missing transactions
		sells.forEach(i -> {
			for (Trade t : buys1) {
				if (!t.name.equals(i.name)) {
					continue;
				}
				if (t.date.after(i.date)) {
					continue;
				}
				if (t.qty == 0) {
					continue;
				}
				if (i.qty == 0) {
					break;
				}
				int qty = (Math.min(t.qty, i.qty));
				i.qty -= qty;
				t.qty -= qty;
				if (inFy(i)) {
					Transaction tx = new Transaction();
					tx.name = i.name;
					tx.buyDate = t.date;
					tx.sellDate = i.date;
					tx.qty = qty;
					tx.buyRate = t.rate;
					tx.buyBrok = t.brok;
					tx.sellRate = i.rate;
					tx.sellBrok = i.brok;
					tx.profit = (tx.qty) * (tx.sellRate - tx.buyRate);
					ret.add(tx);
				}
			}
		});
		return ret;
	}

	private HashMap<String, Integer> findMissingTrade(List<Trade> buys, List<Trade> sells) {
		HashMap<String, Integer> missing = new HashMap<>();
		final List<Trade> buys1 = buys.stream().map(Trade::clone).collect(Collectors.toList());
		sells = sells.stream().map(Trade::clone).collect(Collectors.toList());
		// find missing transactions
		sells.forEach(i -> {
			for (Trade t : buys1) {
				if (!t.name.equals(i.name)) {
					continue;
				}
				if (t.date.after(i.date)) {
					continue;
				}
				if (t.qty == 0) {
					continue;
				}
				int qty = (Math.min(t.qty, i.qty));
				i.qty -= qty;
				t.qty -= qty;
			}
			if (i.qty > 0 && inFy(i)) {
				Integer qty = missing.get(i.name);
				if (qty == null) {
					missing.put(i.name, i.qty);
				} else {
					missing.put(i.name, i.qty + qty);
				}
			}
		});
		return missing;
	}

	private boolean inFy(Trade i) {
		return i.date.after(startDate) && i.date.before(endDate);
	}

	public static void main(String[] args) throws Exception {
		new CapitalGainsCalculator(15)
				.calculate(new String[] { "C:\\Users\\rajesh\\dev\\trading-ruby-parser\\Rajesh2014.csv",
						"C:\\Users\\rajesh\\dev\\trading-ruby-parser\\Rajesh2016.csv" });
	}

}

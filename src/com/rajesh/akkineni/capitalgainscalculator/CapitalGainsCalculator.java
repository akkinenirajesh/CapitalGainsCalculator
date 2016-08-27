package com.rajesh.akkineni.capitalgainscalculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CapitalGainsCalculator {

	private int fy;
	private LocalDate startDate;
	private LocalDate endDate;

	public CapitalGainsCalculator(int fy) {
		this.fy = fy;
		this.startDate = LocalDate.of(2000 + fy, 4, 1);
		this.endDate = LocalDate.of(2001 + fy, 3, 31);
	}

	public void calculate(String[] args) throws Exception {
		List<Trade> trades = Parser.parse(args);

		// sort by date
		trades.sort((o1, o2) -> o1.date.compareTo(o2.date));

		trades = combineByDateAndPrice(trades);

		printTrades(trades);
		// make a clone
		List<Trade> clone = trades.stream().map(Trade::clone).collect(Collectors.toList());
		List<Trade> buys = clone.stream().filter(i -> i.buy).collect(Collectors.toList());
		List<Trade> sells = clone.stream().filter(i -> !i.buy).collect(Collectors.toList());

		HashMap<String, Integer> missing = findMissingTrade(buys, sells);
		for (Entry<String, Integer> item : missing.entrySet()) {
			System.out.println("Missing : " + item.getKey() + " : " + item.getValue());
		}
		List<Transaction> tx = computeTransactions(buys, sells);
		tx.forEach(t -> System.out.println(t));
		double shortTermGains = tx.stream().filter(t -> !t.isLongTerm()).mapToDouble(a -> a.profit).sum();
		double longTermGains = tx.stream().filter(t -> t.isLongTerm()).mapToDouble(a -> a.profit).sum();
		System.out.println("Short Term Gains: " + String.format("%.2f", shortTermGains));
		System.out.println("Long Term Gains: " + String.format("%.2f", longTermGains));
	}

	private void printTrades(List<Trade> trades) {
		Map<String, List<Trade>> collect = trades.stream().collect(Collectors.groupingBy(i -> i.name));
		for (String name : collect.keySet()) {
			System.out.println(" --- " + name + " --- ");
			collect.get(name).forEach(i -> System.out.println(i));
			System.out.println("\n\n");
		}

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
				if (t.date.isAfter(i.date)) {
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
			if (i.qty > 0 && inFy(i)) {
				Transaction tx = new Transaction();
				tx.name = i.name;
				tx.buyDate = i.date.minusYears(2);
				tx.sellDate = i.date;
				tx.qty = i.qty;
				tx.buyRate = 0;
				tx.buyBrok = 0;
				tx.sellRate = i.rate;
				tx.sellBrok = i.brok;
				tx.profit = (tx.qty) * (tx.sellRate - tx.buyRate);
				ret.add(tx);
			}
		});
		return ret;
	}

	private HashMap<String, Integer> findMissingTrade(List<Trade> buys, List<Trade> sells) {
		HashMap<String, Integer> missing = new HashMap<>();
		final List<Trade> buys1 = buys.stream().map(Trade::clone).collect(Collectors.toList());
		sells = sells.stream().map(Trade::clone).collect(Collectors.toList());
		// find missing transactions
		sells.forEach(sell -> {
			for (Trade buy : buys1) {
				if (!buy.name.equals(sell.name)) {
					continue;
				}
				if (sell.date.isBefore(buy.date)) {
					continue;
				}
				if (buy.qty == 0) {
					continue;
				}
				int qty = (Math.min(buy.qty, sell.qty));
				sell.qty -= qty;
				buy.qty -= qty;
			}
			if (sell.qty > 0 && inFy(sell)) {
				Integer qty = missing.get(sell.name);
				if (qty == null) {
					missing.put(sell.name, sell.qty);
				} else {
					missing.put(sell.name, sell.qty + qty);
				}
			}
		});
		return missing;
	}

	private boolean inFy(Trade i) {
		return i.date.isAfter(startDate) && i.date.isBefore(endDate);
	}

	public static void main(String[] args) throws Exception {
		new CapitalGainsCalculator(15)
				.calculate(new String[] { "C:\\Users\\rajes_000\\Google Drive\\dev\\Rajesh2016.csv" });
	}

}

package marketmaker.services.algorithms;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.javatuples.Pair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;

import com.ripple.core.coretypes.Issue;
import com.ripple.core.types.known.sle.entries.Offer;

import edu.princeton.cs.algorithms.BellmanFordSP;
import edu.princeton.cs.algorithms.DirectedEdge;
import edu.princeton.cs.algorithms.EdgeWeightedDigraph;
import edu.princeton.cs.introcs.StdOut;

/***
 * @author rmartins
 */
public class RippleArbitrager implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(RippleArbitrager.class);

	@Value("${ripplemm.arbitrager.currencies}")
	public String currencies;

	private HashMap<Pair<Issue, Issue>, Double> listValues = new HashMap<>();

	private String[] listCurrencies;

	public RippleArbitrager() {

	}

	@JmsListener(destination = "offerbook")
	public void onMessage(String message) throws IOException {
		JSONObject json = new JSONObject(message);
		Offer bestOffer = (Offer) Offer.fromJSONObject(json.optJSONArray("offers").getJSONObject(0));
		Double quality = json.optJSONArray("offers").getJSONObject(0).optDouble("quality");
		if (quality != null) {
			String getIssue = json.getString("getIssue");
			String payIssue = json.getString("payIssue");
			Pair<Issue, Issue> pair = new Pair(getIssue, payIssue);
			listValues.put(pair, quality);
		} else {
			log.error("Not found quality on Offers " + json.optJSONArray("offers").getJSONObject(0).toString());
		}
		processAll(listValues);
	}

	private void processAll(HashMap<Pair<Issue, Issue>, Double> listValues2) throws IOException {

		HashMap<String, double[]> results = new HashMap<>();
		for (int i = 0; i < listCurrencies.length; i++) {
			double[] values = new double[listCurrencies.length];
			values[i] = 1;
			results.put(listCurrencies[i], values);
		}

		Set keys = listValues2.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			Pair<String, String> key = (Pair<String, String>) iterator.next();

			double[] values = results.get(key.getValue0());
			int payIdx = findPayIdx(listCurrencies, key.getValue1().toString());
			values[payIdx] = listValues.get(key);
		}

		execute(results);

		for (int i = 0; i < listCurrencies.length; i++) {
			double[] values = results.get(listCurrencies[i]);
			log.info(listCurrencies[i] + " " + Arrays.toString(values));
		}
	}

	private int findPayIdx(String[] listCurrencies2, String string) {
		for (int i = 0; i < listCurrencies2.length; i++) {
			if (listCurrencies2[i].equals(string)) {
				return i;
			}
		}
		return -1;
	}

	public void execute(HashMap<String, double[]> listValues) throws IOException {
		// V currencies
		int V = listValues.size();
		String[] name = new String[V];
		String[] pairs = listValues.keySet().toArray(new String[0]);

		// create complete network
		EdgeWeightedDigraph G = new EdgeWeightedDigraph(V);
		for (int v = 0; v < V; v++) {
			name[v] = pairs[v];
			for (int w = 0; w < V; w++) {
				double rate = listValues.get(pairs[v])[w];
				DirectedEdge e = new DirectedEdge(v, w, -Math.log(rate));
				G.addEdge(e);
			}
		}

		// find negative cycle
		BellmanFordSP spt = new BellmanFordSP(G, 0);
		if (spt.hasNegativeCycle()) {
			double stake = 1;
			for (DirectedEdge e : spt.negativeCycle()) {
				StdOut.printf("%10.5f %s ", stake, name[e.from()]);
				stake *= Math.exp(-e.weight());
				StdOut.printf("= %10.5f %s\n", stake, name[e.to()]);
			}
		} else {
			StdOut.printf("No arbitrage opportunity");
		}
	}

	public HashMap<Pair<Issue, Issue>, double[]> permutation(String[] currencies) {
		HashMap<Pair<Issue, Issue>, double[]> result = new HashMap<>();
		for (int i = 0; i < currencies.length; i++) {
			for (int j = 0; j < currencies.length; j++) {
				if (!currencies[i].equals(currencies[j])) {
					Issue getIssue = Issue.fromString(currencies[i]);
					Issue payIssue = Issue.fromString(currencies[j]);
					result.put(new Pair<Issue, Issue>(getIssue, payIssue), new double[0]);
				}
			}
		}

		return result;
	}

	public static void main(String[] args) throws IOException {
		String[] currencies = new String[] { "USD", "EUR", "GBP", "CHF", "CAD", "JPY", "BRL", "BTC", "CNY", "XAU",
				"XAG", "STR", "SGD" };
		HashMap<String, double[]> listCurrency = new HashMap<>();

		RippleArbitrager arbitrage = new RippleArbitrager();

		Random rnd = new Random();
		for (int i = 0; i < currencies.length; i++) {
			for (int j = 0; j < currencies.length; j++) {
				if (!currencies[i].equals(currencies[j])) {
					String pair = new String(currencies[i] + "/" + currencies[j]);
					double[] randomValues = new double[currencies.length * currencies.length];
					for (int z = 0; z < randomValues.length; z++) {
						// rnd.doubles(currencies.length *
						// currencies.length).toArray();
						if ((z % 2) == 0) {
							randomValues[z] = rnd.nextDouble() * z;
						} else {
							randomValues[z] = rnd.nextDouble() * rnd.nextDouble() + 1;
						}
					}
					listCurrency.put(pair, randomValues);
				}
			}
		}

		arbitrage.execute(listCurrency);

		System.out.println(listCurrency.size());
		String[] keys = listCurrency.keySet().toArray(new String[0]);
		for (int i = 0; i < keys.length; i++) {
			double[] values = listCurrency.get(keys[i]);
			System.out.println(keys[i]);
			for (int j = 0; j < values.length; j++) {
				System.out.print("\t" + values[j]);
			}
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		listCurrencies = currencies.split(",");
		// listValues = permutation(currencies.split(","));
		// log.info("Permutations " + listValues);
	}

}
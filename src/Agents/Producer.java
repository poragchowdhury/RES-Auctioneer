package Agents;

import java.util.ArrayList;
import java.util.Random;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;


public class Producer extends Agent{
	
	public double meanBidPrice = 45;
	public double stddevPrice = 5;
	
	public Producer(String name, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.neededMWh = neededMWh;
		meanBidPrice = mean;
		stddevPrice = stddev;
		this.type = agentType.PRODUCER;
	}
	
	
	public Producer(){
		this.playerName = "MonopolyProducer";
		this.greenPoint = 0;
		this.type = agentType.PRODUCER;
		this.neededMWh = 1000;
	}
	
	@Override
	public void submitAsks(ArrayList<Ask> asks, Observer ob) {
		
		// Gausian distribution
    	Random r = new Random();
		// mean 50, std deviation 45
		double price =   Math.abs((r.nextGaussian()*this.stddevPrice)+this.meanBidPrice) * -1;
		Ask ask = new Ask(this.playerName, price, this.neededMWh);
//    		System.out.println(this.playerName + " submitting " + price + " limitPrice " + amount + " minMWh ");
		asks.add(ask);	
//    	Ask ask2 = new Ask(this.playerName, 1.7, 1);
//		Ask ask3 = new Ask(this.playerName, 1.7, 1);
//		Ask ask4 = new Ask(this.playerName, 1.8, 1);
//		asks.add(ask2);
//		asks.add(ask4);
//		asks.add(ask3);
//		asks.add(ask1);
	}
	@Override
	public void submitBids(ArrayList<Bid> bids, Observer ob) {
		
	}

}

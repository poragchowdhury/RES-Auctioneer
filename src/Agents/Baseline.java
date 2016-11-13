package Agents;

import java.util.ArrayList;
import java.util.Random;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;


public class Baseline extends Agent {

	public double meanBidPrice = 0;
	public double stddevPrice = 0;
	
	public Baseline(String name, double neededMWh, double mean, double stddev){
		this.playerName = name;
		this.neededMWh = neededMWh;
		meanBidPrice = mean;
		stddevPrice = stddev;
		this.type = agentType.BROKER;
	}
	
	public Baseline(double neededMWh){
		this.playerName = "Baseline";
		this.neededMWh = neededMWh;
		this.type = agentType.BROKER;
	}
	
	@Override
	public void submitAsks(ArrayList<Ask> asks, Observer ob) {
			
	}
	
	@Override
	public void submitBids(ArrayList<Bid> bids, Observer ob) {
		Random r = new Random();
		
		// Bidding configuration
		double limitPrice = Math.abs((r.nextGaussian()*this.stddevPrice)+this.meanBidPrice);
		if(this.neededMWh > 0){
			Bid bid = new Bid(this.playerName, limitPrice, this.neededMWh);
			bids.add(bid);
		}
	}

}


/*
int flag = 0;

numberofbids = Math.abs(this.neededMWh) / minMWh;
		
		while(true){
			if(numberofbids < MAX_NUM_BIDS){
				while(true){
					if(Math.round(numberofbids * Math.abs(unitPrice)) <= Math.round(priceRange)){
						flag = 1;
						break;
					}
					else{
						if(unitPrice > 0){ 
							unitPrice -= 0.05;
							if(unitPrice <= 0)
							{
								unitPrice += 0.05;
								flag = 1;
								break;
							}
						}
						else{
							unitPrice += 0.05;
							if(unitPrice >= 0)
							{
								unitPrice -= 0.05;
								flag = 1;
								break;
							}
						}
					}
				}
				if(flag == 1)
					break;
			}
			else{
				minMWh += 0.10;
				numberofbids = Math.abs(this.neededMWh) / minMWh;
			}
		}
*/
package Agents;

import java.util.ArrayList;

import Auctioneer.Ask;
import Auctioneer.Bid;
import Observer.Observer;

public abstract class Agent {
	public String playerName = "defaultPlayer"; //Overwrite this variable in your player subclass
	public double neededMWh = 0;
	public double greenPoint = 0;
	public static enum agentType {
	    PRODUCER, BROKER 
	} 
	public agentType type;
	public abstract void submitAsks(ArrayList<Ask> asks, Observer ob);
	public abstract void submitBids(ArrayList<Bid> bids, Observer ob);
	public Object getMCTS(){
		return null;
	}
	
}

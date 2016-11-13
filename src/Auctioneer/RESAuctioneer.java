package Auctioneer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import Agents.Agent;
import Agents.Baseline;
import Agents.MonopolyProducer;
import Agents.Producer;
import Observer.Observer;

public class RESAuctioneer {
	private int day;
	private int hour;
	private int currentTimeSlot;
	private int hourAhead;
	public ArrayList<Bid> bids = new ArrayList<Bid>();
	public ArrayList<Ask> asks = new ArrayList<Ask>();

	private Observer observer = new Observer();

	public int TOTAL_SIM_DAYS = 7;
	public int HOURS_IN_A_DAY = 24;
	public int TOTAL_HOUR_AHEAD_AUCTIONS = 24;

	public RESAuctioneer(){
		day = 0;
		hour = 0;
		currentTimeSlot = 0;
		hourAhead = 0;
		observer.ERROR_PERCENTAGE = 10;
		observer.totalHoursAhead = TOTAL_HOUR_AHEAD_AUCTIONS;
	}


	public double clearAuction(ArrayList<Ask> asks, ArrayList<Bid> bids){
		double clearingPrice = 0;
		Collections.sort(asks);
		Collections.sort(bids);

		Ask oldask = new Ask();
		Bid oldbid = new Bid();

		int askid = 0;
		int bidid = 0;

		for(;askid < asks.size();){
			
			if(bidid >= bids.size()){
				if(bidid == 0 && bids.size() == 0){
					System.out.println("Auction didn't clear.");
					return 0;
				}
				clearingPrice = ((Math.abs(oldask.price)+oldbid.price)/2);
				System.out.println("\nClearing price is : " + clearingPrice + "\n");
				observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
				observer.clearedAuctionCount++;
				return clearingPrice;
			}
			for(;bidid < bids.size();){
				if(askid >= asks.size()){
					clearingPrice = ((Math.abs(oldask.price)+oldbid.price)/2);
					System.out.println("\nClearing price is : " + clearingPrice + "\n");
					observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
					observer.clearedAuctionCount++;
					return clearingPrice;
				}
				Bid bid = bids.get(bidid);
				Ask ask = asks.get(askid);
				// Check prices
				if(bid.price < Math.abs(ask.price)){
					if(bidid == 0 && askid == 0){
						// Auction will not clear;
						System.out.println("\nAuction did not clear.\n");
						return 0;
					}
					else{
						clearingPrice = ((Math.abs(oldask.price)+oldbid.price)/2);
						System.out.println("\nClearing price is : " + clearingPrice + "\n");
						observer.meanClearingPrice = ((observer.meanClearingPrice*observer.clearedAuctionCount)+clearingPrice)/(observer.clearedAuctionCount+1);
						observer.clearedAuctionCount++;
						return clearingPrice;
					}
				}
				// Clear Auction
				else
				{
					// Check quantities
					if(bid.amount == ask.amount){
						// clear both bid and ask
						askid++;
						bidid++;
						System.out.println(bid.toString());
						System.out.println(ask.toString());
						observer.addClearedTrades(new Bid(bid.agentName, 0, ask.amount));
						observer.addClearedTrades(new Ask(ask.agentName, 0, ask.amount));
					}
					else if(bid.amount > ask.amount){
						// clear part of the bid and ask
						askid++;
//						System.out.println(bid.toString() + " Bid Partially cleared :" + ask.amount);
//						System.out.println(ask.toString());
						observer.addClearedTrades(new Bid(bid.agentName, 0, ask.amount));
						observer.addClearedTrades(new Ask(ask.agentName, 0, ask.amount));
						ask.amount = 0;
						bid.amount = bid.amount - ask.amount;
					}
					else{
						// clear part of the ask and whole bid
						bidid++;
//						System.out.println(bid.toString());
//						System.out.println(ask.toString() + " Ask Partially cleared :" + bid.amount);
						observer.addClearedTrades(new Bid(bid.agentName, 0, bid.amount));
						observer.addClearedTrades(new Ask(ask.agentName, 0, bid.amount));
						ask.amount = ask.amount - bid.amount;
						bid.amount = 0;
					}
				}
				oldask = ask;
				oldbid = bid;
			}
		}

//		for(Ask a:asks){
//			System.out.println(a.toString());
//		}
//		for(Bid b:bids){
//			System.out.println(b.toString());
//		}
		System.out.println("Auction didn't clear.");
		return 0;
	}

	public void startSimulation(){
		observer.NUMBER_OF_BROKERS = 5;
		observer.NUMBER_OF_PRODUCERS = 1;
		double neededMWhBroker = observer.totalEnergyDemand/observer.NUMBER_OF_BROKERS;
		double neededMWhProducer = observer.totalEnergySupply/observer.NUMBER_OF_PRODUCERS;
		double prevCleared = 0.0;
		
		// import data from the csv files
		observer.importProducerData();
		
		// Initializing agents
		for(int i = 1; i <= observer.NUMBER_OF_BROKERS; i++){
			Agent baseline = new Baseline("Baseline"+i, neededMWhBroker, 50, 10);
			observer.addAgents(baseline);
		}
		
		for(int i = 0; i < observer.NUMBER_OF_PRODUCERS; i++){
			Agent producer = new Producer("Producer"+i, neededMWhProducer, 50, 10);
			observer.addAgents(producer);
		}
		
//		MonopolyProducer producer = new MonopolyProducer();
//		observer.addAgents(producer);
		
		observer.setTime(day,hour,hourAhead, currentTimeSlot);
		// Generate clearing price distribution for the simulation
		// Days * Hours -> rows
		// HourAhead -> column
		// Simulate clearing price
		// observer.generateClearingPrices(0,0);

		currentTimeSlot = 0;
		for(day = 0; day < TOTAL_SIM_DAYS; day++){
			for(hour = 0; hour < HOURS_IN_A_DAY; hour++){

				//observer.generateAvailableEnergySupply(0, 0);
				//observer.addneededTotalVolumes("MonopolyProducer",1000);
				for(int i = 1; i <= observer.NUMBER_OF_BROKERS; i++)
					observer.addneededTotalVolumes("Baseline"+i,neededMWhBroker);
				
				for(int i = 0; i < observer.NUMBER_OF_PRODUCERS; i++)
					observer.addneededTotalVolumes("Producer"+i,observer.arrProducerBidVolume[i][observer.currentTimeSlot]);
				//observer.addneededTotalVolumes("Baseline2",neededMWh);
				
				observer.setNeededVolumes();

				for(hourAhead = TOTAL_HOUR_AHEAD_AUCTIONS; hourAhead >= 0; hourAhead--){

					observer.setTime(day,hour,hourAhead, currentTimeSlot);

					////System.out.println("*******************AUCTION*******************");
					////System.out.println("Day "+ days + " Hour " + hours + " HourAhead " + hourAhead);

					// get the bids and asks from the agents
					for(Agent agent : observer.agents){
						agent.submitAsks(asks, observer);
						agent.submitBids(bids, observer);
					}

					// clear the auction
					// Add cleared asks and bids to clearBids , clearAsks, clearedVolumesBids & clearedVolumesAsks
					double clearingPrice = clearAuction(asks, bids);


					// update clear trades for corresponding agents
					// Get the values from clearedVolumesBids and clearedVolumesAsks
					// Updates to clearedTotalBidVolumes, clearedTotalAskVolumes
					// Add to costTotal
					observer.printClearedVolume(clearingPrice);


					// Adjust agent's needed energy
					// rest clearedVolumes, clearedVolumesBids and clearedVolumesAsks
					observer.adjustNeededVolumes();

			    	//observer.updateNumberOfSuccessfullBids(SPOT2.playerName);

					// clean auctioner's bids asks
					asks.clear();
					bids.clear();
					observer.clear();

					////System.out.println("**************************************\n");
				}

				observer.doBalancing();

				currentTimeSlot++;
				observer.currentTimeSlot++;
				//System.out.println(observer.currentTimeSlot);
			}
		}

		//System.out.println("******************FINAL******************");
		try {
			// Printing the output to file
			// Reset clearedTotalBidVolumes and clearedTotalAskVolumes
			// Reset neededTotalVolumes
			// Rest costTotal
			observer.printTotalClearedVolume();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("**************************************\n");
		System.out.println("Finished");
		System.out.println("Mean Clearing Price : " + observer.meanClearingPrice);
	
	}

	public static void main(String [] args){
		RESAuctioneer auctioneer = new RESAuctioneer();
		auctioneer.startSimulation();
	}
}

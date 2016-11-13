package Observer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import Agents.Agent;
import Agents.MonopolyProducer;
import Agents.Baseline;
import Auctioneer.Ask;
import Auctioneer.Bid;

public class Observer {
	
	public int day = 0;
	public int hour = 0;
	public int hourAhead = 0;
	public int currentTimeSlot = 0;
	public double [][] arrMean;
	public double [][] arrStddev;
	
	public double [][] arrProducerBidPrice;
	public double [][] arrProducerBidVolume;
	
	public int ERROR_PERCENTAGE;
	public int HOUR_AHEAD_AUCTIONS = 24;
	
	public double totalEnergyDemand = 1000;
	public double totalEnergySupply = 2000;
	public double NUMBER_OF_BROKERS;
	public int NUMBER_OF_PRODUCERS = 1;
	
	public int totalHoursAhead = 0;
	public int MCTSSimulation = 0;
	public double nanoTime = 0;
	public double nanoTimeCount = 0;
	public int printNamesCount = 0;
	public double meanClearingPrice = 0;
	public int clearedAuctionCount = 0;
	
	public HashMap<String, Double> clearedVolumes;
	public HashMap<String, Double> clearedVolumesBids;
	public HashMap<String, Double> clearedVolumesAsks;
	
	public HashMap<String, Double> clearedTotalVolumes;
	public HashMap<String, Double> clearedTotalBidVolumes;
	public HashMap<String, Double> clearedTotalAskVolumes;
	
	public ArrayList<Ask> clearedAsks;
	public ArrayList<Bid> clearedBids;
	
	public HashMap<String, Double> neededVolumes;
	public HashMap<String, Double> neededTotalVolumes;
	
	public HashMap<String, Double> costTotal;
	
	
	public ArrayList<Agent> agents;
	
	public double [][] arrClearingPrices;

	public double [] arrBalacingPrice;
	public double [] arrAvailableEnergy;
	
	
	public Observer(){
		clearedVolumes = new HashMap<String, Double>();
		clearedVolumesBids = new HashMap<String, Double>();
		clearedVolumesAsks = new HashMap<String, Double>();
		
		clearedTotalVolumes = new HashMap<String, Double>();
		clearedTotalBidVolumes = new HashMap<String, Double>();
		clearedTotalAskVolumes = new HashMap<String, Double>();
		
		//neededVolumes = new HashMap<String, Double>();
		
		neededTotalVolumes = new HashMap<String, Double>();
		costTotal = new HashMap<String, Double>();
		
		clearedAsks = new ArrayList<Ask>();
		clearedBids = new ArrayList<Bid>();
		
		agents = new ArrayList<Agent>();
		arrClearingPrices = new double[100][HOUR_AHEAD_AUCTIONS];

		arrBalacingPrice = new double[100];
		
		arrAvailableEnergy = new double[HOUR_AHEAD_AUCTIONS];
		
		arrMean = new double[100][HOUR_AHEAD_AUCTIONS];
		arrStddev = new double[100][HOUR_AHEAD_AUCTIONS];
		
		arrProducerBidPrice = new double[NUMBER_OF_PRODUCERS][168];
		arrProducerBidVolume = new double[NUMBER_OF_PRODUCERS][168];
		
		//agents.add(new Producer());
	}
	
	public void setTime(int day, int hour, int hourAhead, int currentTimeSlot){
		this.day = day;
		this.hour = hour;
		this.hourAhead = hourAhead;
		this.currentTimeSlot = currentTimeSlot;
	}
	
	public void importProducerData(){
		try
        {
            File gFile = new File("loads.csv");
            if(!gFile.exists()){
                System.out.println("Load file doesn't exist");
            	return;
            }
            
			CSVParser parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			int hour = 0;
            for (CSVRecord csvRecord : parser) {
                Iterator<String> itr = csvRecord.iterator();
                // Time Stamp	
                String strTimeStamp = itr.next();
                // Name
                String strName = itr.next();
                // LBMP ($/MWHr)
                String strLBMP = itr.next();
                arrProducerBidVolume[0][hour] = Double.parseDouble(strLBMP);
                hour++;
                if(hour >= 7*24)
                	break;
            }
            parser.close();
            
            gFile = new File("prices.csv");
            if(!gFile.exists()){
                System.out.println("Price file doesn't exist");
            	return;
            }
            
			parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			hour = 0;
            for (CSVRecord csvRecord : parser) {
                Iterator<String> itr = csvRecord.iterator();
                // Time Stamp	
                String strTimeStamp = itr.next();
                // Name
                String strName = itr.next();
                // LBMP ($/MWHr)
                String strLBMP = itr.next();
                arrProducerBidPrice[0][hour] = Double.parseDouble(strLBMP);
                
                hour++;
                if(hour >= 7*24)
                	break;
            }
            parser.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	}
	
	public void generateClearingPrices(int mean, int stddev){
        if(mean == 0)
        	mean = 40;
        if(stddev == 0) 
        	stddev = 5;
		Random r = new Random();
		
		double stdMean = 0;
		double stdStddev = 2;
		
		//System.out.println("HourAhead, Actual Clearing Price , Error %, Predicted Clearing Price");
		
		double NUMBER_OF_AUCTIONS = 100.0;
		System.out.println("[hr,hrAd] Mean Stddev ClearingPrice PredictedPrice");
		for(int i = 0; i < NUMBER_OF_AUCTIONS; i++){
	    	for(int k = 0; k < HOUR_AHEAD_AUCTIONS; k++){
	    		arrMean[i][k] =  Math.abs((r.nextGaussian()*stddev)+mean);
	    		arrStddev[i][k] = Math.abs((r.nextGaussian()*stdStddev)+stdMean);
	    		System.out.printf("["+ i + ","+ k + "] ");
	    		System.out.printf(" %.2f ", arrMean[i][k]);
	    		System.out.printf(" %.2f ", arrStddev[i][k]);
	    		arrClearingPrices[i][k] = Math.abs((r.nextGaussian()*arrStddev[i][k])+arrMean[i][k]);
	    		//System.out.println("arrClearingPrices ["+ k + "] " + arrClearingPrices[k]);
	    		
	    		// 3% error in prediction
	    		//System.out.printf("%.2f %% ", (error-1)*100);
	    		
	    		System.out.printf(" %.2f ", arrClearingPrices[i][k]);
	    		//System.out.printf(" %.2f \n", arrPredictedClearingPrices[i][k]);
	    		
	    	}
	    	System.out.println();
	    	arrBalacingPrice[i] =  Math.abs(Math.abs((r.nextGaussian()*stddev))+(mean*2));
		}
		
		// Testing purpose
//		arrClearingPrices[0][0] = 34;
//		arrClearingPrices[0][1] = 30;
//		arrClearingPrices[0][2] = 35;
		
    	
    	System.out.println();
	}
	
	public void generateAvailableEnergySupply(int mean, int stddev){
        if(mean == 0)
        	mean = 1500;
        if(stddev == 0) 
        	stddev = 200;
		Random r = new Random();
		System.out.println("HourAhead, EnergySupply (mwh)");
    	for(int k = 0; k < HOUR_AHEAD_AUCTIONS; k++){
    		arrAvailableEnergy[k] =  Math.abs((r.nextGaussian()*stddev)+mean);
    		System.out.println(k + ", " + arrAvailableEnergy[k]);
    	}
    	System.out.println();
	}
	
	
	public double randError(int errorRate) {

		int min = 100 - errorRate;
		int max = 100 + errorRate;
	    // NOTE: This will (intentionally) not run as written so that folks
	    // copy-pasting have to think about how to initialize their
	    // Random instance.  Initialization of the Random instance is outside
	    // the main scope of the question, but some decent options are to have
	    // a field that is initialized once and then re-used as needed or to
	    // use ThreadLocalRandom (if using at least Java 1.7).
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    double randomNum = (rand.nextInt((max - min) + 1) + min);

	    double randomGauss = (rand.nextGaussian()*errorRate)+0;
	    
	    randomGauss = 100 + randomGauss;
	    
	    return randomGauss/100;
	}
	
	public void addClearedTrades(Bid bid){
		Double vol = clearedVolumesBids.get(bid.agentName); 
		if(vol == null)
			vol = bid.amount;
		else
			vol = vol.doubleValue() + bid.amount;
	
		clearedVolumesBids.put(bid.agentName, vol);		
		clearedBids.add(bid);
	}
	
	public void addClearedTrades(Ask ask){
		Double vol = clearedVolumesAsks.get(ask.agentName); 
		if(vol == null)
			vol = ask.amount;
		else
			vol = vol.doubleValue() + ask.amount;
	
		clearedVolumesAsks.put(ask.agentName, vol);
		clearedAsks.add(ask);
	}
	
//	public void addClearedTrades(String brokerName, double volume){
//		Double vol = clearedVolumes.get(brokerName); 
//		if(vol == null)
//			vol = volume;
//		else
//			vol = vol.doubleValue() + volume;
//	
//		clearedVolumes.put(brokerName, vol);		
//	}

	public void addneededTotalVolumes(String brokerName, double neededMWh){
		Double neededTotalMWh = neededTotalVolumes.get(brokerName);
		if(neededTotalMWh == null){
			neededTotalMWh = neededMWh;
		}
		else{
			neededTotalMWh += neededMWh;
		}
		neededTotalVolumes.put(brokerName, neededTotalMWh);
	}
	
	public void addTotalClearTrade(Bid bid){
		Double volTotal = clearedTotalBidVolumes.get(bid.agentName); 
		if(volTotal == null){
			volTotal = bid.amount;
		}
		else{
			volTotal += bid.amount;
		}
		clearedTotalBidVolumes.put(bid.agentName, volTotal);
	}
	
	public void addTotalTradeCost(Bid bid){
		Double volTotal = costTotal.get(bid.agentName); 
		if(volTotal == null){
			volTotal = bid.amount*bid.price;
		}
		else{
			volTotal += (bid.amount*bid.price);
		}
		costTotal.put(bid.agentName, volTotal);
	}
	
	public void addTotalTradeCost(Ask ask){
		Double volTotal = costTotal.get(ask.agentName); 
		if(volTotal == null){
			volTotal = ask.amount*ask.price;
		}
		else{
			volTotal += (ask.amount*ask.price);
		}
		costTotal.put(ask.agentName, volTotal);
	}
	
	
	public void addTotalClearTrade(Ask ask){
		Double volTotal = clearedTotalAskVolumes.get(ask.agentName); 
		if(volTotal == null){
			volTotal = ask.amount;
		}
		else{
			volTotal += ask.amount;
		}
		clearedTotalAskVolumes.put(ask.agentName, volTotal);
	}

	
	
//	public void addTotalClearTrade(String brokerName, double neededMWh, double vol){
//		Double volTotal = clearedTotalVolumes.get(brokerName); 
//		Double neededTotalMWh = neededTotalVolumes.get(brokerName);
//		
//		if(volTotal == null){
//			volTotal = vol;
//		}
//		else{
//			volTotal += vol;
//		}
//		clearedTotalVolumes.put(brokerName, volTotal);
//	
//		if(neededTotalMWh == null){
//			neededTotalMWh = neededMWh;
//		}
//		else{
//			neededTotalMWh += neededMWh;
//		}
//		neededTotalVolumes.put(brokerName, neededTotalMWh);
//	}

	public void addAgents(Agent agent){	
		agents.add(agent);
	}
	
	
	public void addAgents(String brokerName, double neededMWh){	
		if(brokerName.equalsIgnoreCase("SPOT"))
			agents.add(new Baseline(neededMWh));
	}
	
	public void setNeededVolumes(){
		// Set agents neededMWh
		for(Agent agent : agents){
			if(agent.type == Agent.agentType.BROKER)
				agent.neededMWh = totalEnergyDemand/NUMBER_OF_BROKERS;
			else
				agent.neededMWh = totalEnergySupply/NUMBER_OF_PRODUCERS;
		}
	}
	
	public double getAgentNeededMWh(String brokerName){
		for(Agent agent : agents){
			if(agent.playerName.equalsIgnoreCase(brokerName)){
				return agent.neededMWh;
			}
		}
		return 0;
	}
	
	public void adjustNeededVolumes(){
		// Set agents neededMWh
		for(Agent agent : agents){
			String brokerName = agent.playerName;
			Double volumeCleared = 0.0;
			if(Agent.agentType.BROKER == agent.type){
				volumeCleared = getClearedBidVolume(brokerName);
				if(volumeCleared == null)
					volumeCleared = 0.0;
			}
			else{
				volumeCleared = getClearedAskVolume(brokerName);
				if(volumeCleared == null)
					volumeCleared = 0.0;
			}
			agent.neededMWh = agent.neededMWh-volumeCleared.doubleValue();
			
			clearedVolumes.put(brokerName,0.00);
		    clearedVolumesBids.put(brokerName,0.00);
		    clearedVolumesAsks.put(brokerName,0.00);
		}
	}
	
	public Double getClearedBidVolume(String brokerName){
		return clearedVolumesBids.get(brokerName);
	}
	public Double getClearedAskVolume(String brokerName){
		return clearedVolumesAsks.get(brokerName);
	}
	
	public Double getClearedVolume(String brokerName){
		return clearedVolumes.get(brokerName);
	}
	
	
	public double getTotalBidClearedVolume(String brokerName){
		if(clearedTotalBidVolumes.get(brokerName) == null)
			return 0.0;
		return clearedTotalBidVolumes.get(brokerName).doubleValue();
	}
	
	public double getTotalAskClearedVolume(String brokerName){
		if(clearedTotalAskVolumes.get(brokerName) == null)
			return 0.0;
		return clearedTotalAskVolumes.get(brokerName).doubleValue();
	}
	
	public double getTotalCost(String brokerName){
		if(costTotal.get(brokerName) == null)
			return 0.0;
		return costTotal.get(brokerName).doubleValue();
	}
	
	public double getTotalClearedVolume(String brokerName){
		return clearedTotalVolumes.get(brokerName).doubleValue();
	}
	
	public void printClearedVolume(double clearingPrice){
		
		for (Agent a : agents) {
		    String brokerName = a.playerName;
		    if(a.type == Agent.agentType.BROKER){
			    Double clearedBidMWh = getClearedBidVolume(brokerName);
			    if(clearedBidMWh == null)
			    	clearedBidMWh = 0.00;
			    addTotalClearTrade(new Bid(brokerName,0,clearedBidMWh));
			    addTotalTradeCost(new Bid(brokerName,clearingPrice,clearedBidMWh));
		    } else {
			    Double clearedAskMWh = getClearedAskVolume(brokerName);
			    if(clearedAskMWh == null)
			    	clearedAskMWh = 0.00;
			    addTotalClearTrade(new Ask(brokerName,0,clearedAskMWh));
			    addTotalTradeCost(new Ask(brokerName,clearingPrice,clearedAskMWh));
		    }
		}
	}
	
	public void printTotalClearedVolume() throws IOException{
		
		if(printNamesCount == 0)
		{
			System.out.print("Auctions,\tHourAhead,\t");
			for(Agent agent : agents){
		    	String brokerName = agent.playerName + ",\t";
		    	System.out.print(brokerName);
			}
		    System.out.println();
		    printNamesCount++;
		}
		
		int printTrack = 0;
		//for (Map.Entry<String, Double> entry : costTotal.entrySet()) {
		for(Agent agent : agents){
			//String brokerName = entry.getKey();
			String brokerName = agent.playerName;
		    double neededTotalMWh = neededTotalVolumes.get(brokerName).doubleValue();//entry.getValue().doubleValue();
		    
		    double clearedTotalMWh = 0;
		    
		    double totalCost = getTotalCost(brokerName);
		    double totalPower = 0;
		    if(agent.type == Agent.agentType.BROKER){
		    	totalPower = currentTimeSlot*(totalEnergyDemand/NUMBER_OF_BROKERS);
		    	clearedTotalMWh = getTotalBidClearedVolume(brokerName);
		    }
		    else {
		    	totalPower = currentTimeSlot*(totalEnergySupply/NUMBER_OF_PRODUCERS);
		    	clearedTotalMWh = getTotalAskClearedVolume(brokerName);
		    }
		    
		    //System.out.println(brokerName + "'s bid cleared " + ((clearedTotalBidMWh/neededTotalMWh)*100) + "% ," + " ask cleared " + ((clearedTotalAskMWh/neededTotalMWh)*100) + "%");
		    //System.out.println(brokerName + "'s unitCost " + (totalCost/totalPower) + "$ ");
		    if(printTrack == 0){
			    System.out.print((currentTimeSlot)+ ",\t\t"
			    		+ totalHoursAhead + ",\t\t" // HourAhead
			    		);
			    		//+ (totalCost/totalPower) + ", ");
			    
			    System.out.printf("%.2f (%.2f),\t",(totalCost/totalPower),((clearedTotalMWh/neededTotalMWh)*100));

			    printTrack++;
		    }
		    else{
		    	//System.out.print((totalCost/totalPower) + ", ");
		    	System.out.printf("%.2f (%.2f),\t",(totalCost/totalPower),((clearedTotalMWh/neededTotalMWh)*100));
			    //pwOutputVolume.print(((clearedTotalMWh/neededTotalMWh)*100) + "%,");
		    }
		    /*
		    System.out.println( brokerName + ","
		    		+ ((day+1)*(hour+1))+ ","
		    		+ hoursAhead + "," // HourAhead
		    		+ MCTSSimulation + ","
		    		+ ((clearedTotalBidMWh/neededTotalMWh)*100) + "%,"
		    		+ (totalCost/totalPower) + ", "
		    		+ nanoTime/nanoTimeCount);
		    */
		    
		    costTotal.put(brokerName, 0.0);
		    neededTotalVolumes.put(brokerName, 0.0);
		    clearedTotalBidVolumes.put(brokerName, 0.0);
		    clearedTotalAskVolumes.put(brokerName, 0.0);
		    nanoTime = 0;
		    nanoTimeCount = 0;
		}

		costTotal.clear();
		neededTotalVolumes.clear();
		clearedTotalBidVolumes.clear();
		clearedTotalAskVolumes.clear();
		
		currentTimeSlot = 0;
		System.out.println();
	}
	
	public void doBalancing(){
		Random r = new Random();
	    double balancingPrice = Math.abs(Math.abs((r.nextGaussian()*10))+(45*2));
	    for (Agent a : agents) {
		    
			String brokerName = a.playerName;
		    double clearedBalancingMWh = a.neededMWh;
		    //addTotalClearTrade(new Bid(brokerName,0,clearedBalancingMWh));
		    //addTotalClearTrade(new Ask(brokerName,0,clearedAskMWh));
		    if(a.type == Agent.agentType.BROKER)
		    	addTotalTradeCost(new Bid(brokerName,balancingPrice,clearedBalancingMWh));
		    else 
		    	addTotalTradeCost(new Ask(brokerName,balancingPrice,clearedBalancingMWh));
		}
		
		// Best strategy calculation
		//Arrays.sort(arrClearingPrices[day*hour]);
//		double minClearingPrice = getMinClearingPrice(arrClearingPrices[currentTimeSlot]);
//		addTotalTradeCost(new Bid("BestAgent",minClearingPrice,1000));

	}
	
	public double getMinClearingPrice(double [] arrClearingPrice){
		double minClrPrice = Double.MAX_VALUE;
		for(int i = 0; i <= totalHoursAhead; i++){
			if(minClrPrice > arrClearingPrice[i])
				minClrPrice = arrClearingPrice[i];
		}
		return minClrPrice;
	}
	
	
	public void clear(){
		clearedAsks.clear();
		clearedBids.clear();
	}
	
	public int getNumberOfClearedBids(String brokerName){
		int count = 0;
		for(Bid bid : clearedBids){
			if(bid.agentName.equalsIgnoreCase(brokerName))
				count++;
		}
		return count;
	}
}

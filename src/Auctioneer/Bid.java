package Auctioneer;

public class Bid implements Comparable {
	public String agentName;
	public double price;
	public double amount;
	
	public Bid(){
		this.agentName = "Default";
		this.price = 0;
		this.amount = 0;
	}
	
	public Bid(String name, double price, double amount){
		this.agentName = name;
		this.price = price;
		this.amount = amount;
	}
	
	@Override
	public int compareTo(Object o) {
		if(this.price > ((Bid)o).price)
			return -1;
		else if(this.price < ((Bid)o).price)
			return 1;
		else 
			return 0;
	}
	
	@Override
    public String toString() {
        return "[ agentName=" + this.agentName + ", price=" + price + ", amount=" + amount + "]";
    }
}

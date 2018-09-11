import java.util.ArrayList;
import java.util.List;
//Candidate Number: 60670

// this class implements the factor node data structure. It stores information
// on the moves, player, children, payoffs and a method to compute the node.
// Once computed, it also stores the best response move index 
// (for the list of moves) as well as the corresponding best response move.

// Note that: Does not contain full implementation of a factor node since we 
// only consider commitment games in the overall program here. Thus, e.g. perfect 
// information assumed and no parent node needs to be stored.
public class FactorNode
{
	private List<List<Double>> payoffs;
	private List<String> moves;
	private int player;

	private List<FactorNode> children;
	
	private int bestResponseIndex;
	private String bestResponse;
	
	public FactorNode(){
		payoffs = new ArrayList<List<Double>>();
		moves= new ArrayList<String>();
		children= new ArrayList<FactorNode>();	
	}
	
	public void setChildren(List<FactorNode> children){
		this.children = children;
	}
	
	public void addChild(FactorNode child){
		this.children.add(child);
	}
	
	public void setPayoffs(List<List<Double>> payoffs){
		this.payoffs = payoffs;
	}
	
	public void setMoves(List<String> moves){
		this.moves = moves;
	}
	
	public void setPlayers(int player){
		this.player = player;
	}
	
	public List<FactorNode> getChildren(){
		return children;
	}
	
	public List<List<Double>> getPayoffs(){
		return payoffs;
	}
	
	public List<String> getMoves(){
		return moves;
	}
	
	public int getPlayer(){
		return player;
	}

	// method computes factor node and returns the corresponding equilibrium 
	// payoffs (assuming generic commitment game - will print warning if not)
	public List<Double> computeFactorNode(){
		List<List<Double>> payoffs = getPayoffs();
		List<String> moves = getMoves();
		int player = getPlayer();
		
		// find best response (maximum) payoff
		List<Double> bestResponsePayoffList = payoffs.get(0);
		double bestResponsePayoff = bestResponsePayoffList.get(player);
		String bestResponse = moves.get(0);
		int bestResponseIndex = 0;
		for(int i = 1; i<payoffs.size(); i++){
			double currentPayoff = payoffs.get(i).get(player);
			if(currentPayoff>bestResponsePayoff){
				bestResponsePayoff = currentPayoff;
				bestResponsePayoffList = payoffs.get(i);
				bestResponse = moves.get(i);
				bestResponseIndex = i;
			}
		}
		// check if non-generic payoffs using the best response payoff 
		// computed above, if >1 move achieves this payoff then non-generic
		int counter = 0;
		for(int i = 1; i<payoffs.size(); i++){	
			double currentPayoff = payoffs.get(i).get(player);
			if(currentPayoff == bestResponsePayoff){
				counter += 1;
				if(counter>1) // more than one best response move
				{
					// output non-genericity warning to user
					System.out.println();
					System.out.println("WARNING: Non-generic payoffs found: e.g. for the moves " +bestResponse+ " and " + moves.get(i) + " for player 2.");
					System.out.println("         The output below ONLY gives the set of Nash equilibria corresponding to one");
					System.out.println("         pair of pure on-equilibrium-path moves for each move of player I (if any exists).\n");
					break;
				}
			}
		}
		this.bestResponse = bestResponse;
		this.bestResponseIndex = bestResponseIndex;
		return bestResponsePayoffList;
	}
		
	// gets best response move: computerFactorNode must be called first
	public String getBestResponse(){
		return bestResponse;
	}
	
	// gets response move index: computerFactorNode must be called first
	public int getBestResponseIndex(){
		return bestResponseIndex;
	}
}


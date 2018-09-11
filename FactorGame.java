import java.util.ArrayList;
import java.util.List;
//Candidate Number: 60670

// this class implements the factor game data structure
// it holds the root of the game 
public class FactorGame 
{
	private FactorNode root;
	
	private List<Integer> players;
	
	public FactorGame(int numberOfPlayers, int[][] numberMoves, 
			Double[][] payoffsI, Double[][] payoffsII){
		this.players = new ArrayList<Integer>();
		// construct game
		constructFactorGame(numberOfPlayers,numberMoves,payoffsI,payoffsII);
	}
	
	public void setRoot(FactorNode root){
		this.root = root;
	}
	
	public void setPlayers(List<Integer> players){
		this.players=players;
	}
	
	public FactorNode getRoot(){
		return root;
	}
	
	public List<Integer> getPlayers(){
		return players;
	}
	
	// method constructs the factor game, constructing the nodes and 
	// formatting the input for them
	public void constructFactorGame(int numberOfPlayers, int[][] numberMoves, 
			Double[][] payoffsI, Double[][] payoffsII){
		int numberOfMovesPlayerI = numberMoves[0][0];
		int[] numberOfMovesPlayerII = numberMoves[1];
		
		FactorNode root = new FactorNode();
		
		// add player I for root subgame: player I = "0"
		root.setPlayers(0);
		
		// moves player I
		List<String> movesI = new ArrayList<String>();
		for(int i = 0; i<numberOfMovesPlayerI; i++)
			// use letters A,B,C... for moves player I
			movesI.add(String.valueOf((char)(65 + i))); 
		
		root.setMoves(movesI);
		
		// moves player II
		for(int i = 0; i<numberOfMovesPlayerI; i++){
			FactorNode child = new FactorNode();
			// add player II for root children subgames: player II = "1"
			child.setPlayers(1);
			
			List<String> childMoves = new ArrayList<String>();
			List<List<Double>> payoffs = new ArrayList<List<Double>>();
			for(int c = 0; c<numberOfMovesPlayerII[i]; c++){
				//  use letters a,b,c... for moves player II
				childMoves.add(String.valueOf((char)
						(i*(numberOfMovesPlayerII[i]) + 97 + c))); 
				List<Double> payoffPair = new ArrayList<Double>();
				payoffPair.add(payoffsI[i][c]);
				payoffPair.add(payoffsII[i][c]);	
				payoffs.add(payoffPair);
			}
			
			child.setPayoffs(payoffs);
			child.setMoves(childMoves);
			
			// add child to factor game
			root.addChild(child);
		}
		
		this.root = root;
		List<Integer> playersOfGame = new ArrayList<Integer>();
		playersOfGame.add(0);
		playersOfGame.add(1);
		setPlayers(playersOfGame);
	}
}


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
// Candidate Number: 60670

// This class controls the main flow of the program and runs the main algorithm.
// It also provides methods for reading the input and printing the output.
public class CommitmentGameNE{
	
	// maximum size payoff (in length) for alignment of printed game 
	static int maxSizePayoff;
	
	// list of 'move' labels for columns of (skewed) payoff matrix in output
	static List<String> columnLabels = new ArrayList<String>();		
	
	// main method controlling flow of program
	public static void main(String args[])	{
		// # players must stay fixed in this program for commitment games 
		int numberOfPlayers = 2;
		
		// output precision for the payoffs and probabilities
		int precisionProb = 3;
		int precisionPayoff = 1;
		
		// input text file with specified format
		File input = new File(args[0]);

		// construct game from parameters
		FactorGame g = readInput(input,precisionPayoff,numberOfPlayers);
	
		// print game
		printGame(g, maxSizePayoff);
	
		// solve game
		SolutionCG output = gcbNE(g);
		
		System.out.println("The Nash equilibria of G:\n");
		
		// output solution
		printEquilibria(output.getBetaI(), output.getBetaII(), 
				output.getEquilibPayoffs(), g,
				precisionProb,precisionPayoff); 
	}
	
	// method running gcbNE algorithm for commitment games and returns solution
	public static SolutionCG gcbNE(FactorGame g){
		// input
		List<String> movesI = g.getRoot().getMoves();
		List<FactorNode> decisionNodesII = g.getRoot().getChildren();
		
		// equilibrium strategies
		List<Double[]> betaI = new ArrayList<Double[]>();
		List<List<List<Double[]>>> betaII = new ArrayList<List<List<Double[]>>>();	
		
		// corresponding equilibrium payoffs
		List<List<Double>> equilibPayoffs = new ArrayList<List<Double>>();
		
		for(int i = 0; i<decisionNodesII.size(); i++){
			FactorNode child = decisionNodesII.get(i);
			List<Double> equilibPayoffList = child.computeFactorNode();
			
			int equilibMoveIndexII = child.getBestResponseIndex();
			
			// equilibrium payoff for player I (leader)
			double equilibPayoffI = equilibPayoffList.get(0);
			
			List<List<Double[]>> equilibriumSetII = new ArrayList<List<Double[]>>();
	loop:	for(int j = 0; j<decisionNodesII.size(); j++){
				List<String> movesII = decisionNodesII.get(j).getMoves();
				if(j==i){
					// add on path local strategy player II for current extreme equilibrium
					Double[] onpathlocalStrategyII = new Double[movesII.size()];
					onpathlocalStrategyII[equilibMoveIndexII] = 1.0; // the rest are zero by default
					List<Double[]> onpathExtremePoints = new ArrayList<Double[]>();
					onpathExtremePoints.add(onpathlocalStrategyII);
					equilibriumSetII.add(onpathExtremePoints);
				}
				if(j!=i){ // for each unreached decision node
					List<List<Double>> payoffs = decisionNodesII.get(j).getPayoffs();		
							
					List<Integer> YES_vertices = new ArrayList<Integer>();
					List<Integer> NO_vertices = new ArrayList<Integer>();
					
					// find YES and NO vertices
					for(int c = 0; c<payoffs.size(); c++)	{
						List<Double> payoffList = payoffs.get(c);
						if(payoffList.get(0) <= equilibPayoffI)
							YES_vertices.add(c);	
						else
							NO_vertices.add(c);
					}
					
					// if no YES-vertices, move i of pI is strictly dominated
					if(YES_vertices.isEmpty())
						break loop; // break to consider next move of player I
									
					// add each valid pure move strategy as vertex
					List<Double[]> validVertices = new ArrayList<Double[]>();
					for(int c :YES_vertices){
						Double[] localStrategyII = new Double[payoffs.size()];
						localStrategyII[c] = 1.0;
						validVertices.add(localStrategyII);
					}
					
					// if no NO-vertices, all possible distributions at node j
					// are valid hence we have already stored them all so skip
					if(!(NO_vertices.isEmpty())){	
						// find set of extreme points for this decision node
						for(int c1:YES_vertices){
							// check to make sure we don't get a pure move point
							if(payoffs.get(c1).get(0)!=equilibPayoffI){
								for(int c2:NO_vertices){
									Double[] localStrategyII = new 
											Double[payoffs.size()];
									Double probc1 = (equilibPayoffI - payoffs.get(c2).get(0))/(payoffs.get(c1).get(0)-payoffs.get(c2).get(0));
									localStrategyII[c1] = probc1;
									localStrategyII[c2] = 1-probc1;
									validVertices.add(localStrategyII);
								}
							}
						}
					}
				
					// add set of vertices of valid polytope to equilibrium set
					equilibriumSetII.add(validVertices);
				}
			}
			
			// add set of strategies of player II supporting move i of 
			// player I if i not dominated 
			if(equilibriumSetII.size() == movesI.size()){// if no move of player I dominates her current move then there should be a set of strategies for each of decision nodes of player II 
				// equilibrium does exist for move i of player I 
				// add strategy player I for current extreme equilibrium
				Double[] strategyI = new Double[movesI.size()];
				strategyI[i] = 1.0; // the rest are zero by default
				betaI.add(strategyI);
				betaII.add(equilibriumSetII);	
				
				// store equilib payoffs for this set of equilibria (for strategy player I)
				equilibPayoffs.add(equilibPayoffList);
			}
		}
		
		// checks genericity of payoffs for player I before finishing game.
		// we do this here as we now have the best response move stored for 
		// each of player II's decision nodes. If there was more than one for 
		// some node then we already would have terminated the program due to 
		// non-genericity found in player II's payoffs during computing the
		// given factor node: computeFactorNode() method in class FactorNode.
		for(int i = 0; i<decisionNodesII.size(); i++){
			FactorNode node1 = decisionNodesII.get(i);
			int bestResponse1 = node1.getBestResponseIndex();
			for(int j = i+1; j<decisionNodesII.size(); j++){
				FactorNode node2 = decisionNodesII.get(j);
				int bestResponse2 = node2.getBestResponseIndex();
				// check if the two nodes have same best response payoff to player I
				if(Double.compare(node1.getPayoffs().get(bestResponse1).get(0),node2.getPayoffs().get(bestResponse2).get(0))==0){
					System.out.println();
					// output non-genericity warning to user
					System.out.println("WARNING: Non-generic payoffs found: e.g. for the moves " +movesI.get(i)+ " and " + movesI.get(j) + " for player 1.");
					System.out.println("         The output below ONLY gives the set of Nash equilibria corresponding to one");
					System.out.println("         pair of pure on-equilibrium-path moves for each move of player I (in any exists).\n");
				}
			}
		}
		
		SolutionCG output = new SolutionCG(betaI,betaII,equilibPayoffs);
		return output;
	}
	
	// method for printing the solution to the game
	public static void printEquilibria(List<Double[]> betaI, 
			List<List<List<Double[]>>> betaII,List<List<Double>> equilibPayoffs, 
			FactorGame g, int precision, int precisionPayoff){
		
		// moves player I
		List<String> movesI = g.getRoot().getMoves();
		
		for(int i = 0; i<betaI.size(); i++){
			// strategy player I
			Double[] s1 = betaI.get(i);
			
			System.out.print("Equilibrium set " + (i+1) + ":\nPlayer 1 commits to ");
			boolean first = true;
			int equilibMoveIndexI = 0;				
			for(int c = 0; c<s1.length; c++){
				if(s1[c] != null){
					equilibMoveIndexI = c;
					if(s1[c] == 1){// pure move strategy
						System.out.print(movesI.get(c));	
						break;
					}
					else if(first){// first element has not sign behind it unless negative but NO space between
						System.out.printf("%."+ precision + "f %s",s1[c] ,movesI.get(c));	
						first = false;
					}
					else if(s1[c]<0) // negative with space between
						System.out.printf(" - %s %s",String.format("%."+ precisionPayoff + "f", Math.abs(s1[c])), movesI.get(c));
					else
						System.out.printf(" + %."+ precision + "f %s",s1[c] ,movesI.get(c));	
				}
			}
			System.out.println();
			
			// strategy set for decision node leading from move i of player I
			List<List<Double[]>> correspondingStrategySetII = betaII.get(i);	
					
			// for each decision node player II print set of extreme local strategies together with valid pure move local strategies
			System.out.println("Player 2 responds");
			
			for(int h = 0; h<correspondingStrategySetII.size(); h++) {
				System.out.print("to " + movesI.get(h) + " with ");
				List<Double[]> extremePoints = correspondingStrategySetII.get(h);
				
				if(extremePoints.size()>1) // only convex set if more than one point
					System.out.print("conv(");

				List<String> movesCurrentNode = g.getRoot().getChildren().get(h).getMoves();
				
				for(int e = 0; e<extremePoints.size(); e++){// for each extreme point at decision node h
					Double[] s2 = extremePoints.get(e);
					boolean first2 = true;
					for(int c = 0; c<movesCurrentNode.size(); c++){
						if(s2[c] != null){
							if(s2[c] == 1){// pure move strategy
								System.out.print(columnLabels.get(c));	
								break;
							}
							else if(first2) {// first element has no " + " and if negative we want NO space between: "-"
								System.out.printf("%."+ precision + "f %s",s2[c],columnLabels.get(c));	
								first2 = false;
							}
							else if(s2[c]<0) // negative and we want space between: "- "
								System.out.printf(" - %s %s",String.format("%."+ precisionPayoff + "f", Math.abs(s2[c])), columnLabels.get(c));
							else
								System.out.printf(" + %."+ precision + "f %s",s2[c],columnLabels.get(c));	
						}
					}
					if(e!=extremePoints.size()-1)
						System.out.print(", ");
				}
				if(extremePoints.size()>1) // only convex set if more than one point
					System.out.print(")");
				
				if(extremePoints.size()>0 && h == equilibMoveIndexI) // print equilib payoff for reached decision node 
					System.out.printf(", equilibrium payoffs (%."+ precisionPayoff + "f, %."+ precisionPayoff + "f)\n", equilibPayoffs.get(i).get(0) , equilibPayoffs.get(i).get(1));
				else{// unreached decision node print the expected payoff constraint
					List<List<Double>> nodePayoffs = g.getRoot().getChildren().get(h).getPayoffs();
					System.out.println();
					System.out.print("   so that ");
					for(int c = 0; c<movesCurrentNode.size(); c++){
						Double payoff1 = nodePayoffs.get(c).get(0);
						if(c==0)// first element no sign unless negative but with NO space between
							System.out.printf("%."+ precisionPayoff + "f %s",payoff1 , "p(" + columnLabels.get(c) + ")");
						else if( payoff1 < 0)// negative with space between
							System.out.printf(" - %s %s",String.format("%."+ precisionPayoff + "f", Math.abs(payoff1)), "p(" + columnLabels.get(c) + ")");
						else
							System.out.printf(" + %."+ precisionPayoff + "f %s", payoff1 , "p(" + columnLabels.get(c) + ")");
					}
					System.out.printf(" <= %."+ precisionPayoff + "f\n", equilibPayoffs.get(i).get(0)); // again NO space if negative here
				}
			}
			// space for next equilibria set
			System.out.println("\n");
		}
	}
	
	// method prints the given game in (skew) matrix form
	public static void printGame(FactorGame g, int maxSizePayoff){
		List<String> movesI = g.getRoot().getMoves();
		
		System.out.println("2-Player Commitment Game G:\n");
		System.out.println("Matrix Representation: \n(Note: C"
				+ "olumn labels do not represent actual move labels of player 2) \n");
		// print payoff matrix for each player
		for(int player = 0; player<g.getPlayers().size();player++){
			System.out.println("Payoff matrix player " + (player+1) +":"); 
			System.out.printf("%3s","|");
			
			for(String move:columnLabels)
				System.out.printf("%"+(maxSizePayoff+2)+"s", move);
			
			System.out.printf("%2s\n","|");
			
			for(int z = 0; z<(maxSizePayoff+2)*columnLabels.size() + 5; z++)
				System.out.print("-");
			
			System.out.println();
			for(int i = 0; i<movesI.size(); i++){
				List<List<Double>> payoffsNodei = g.getRoot().getChildren().get(i).getPayoffs();
				System.out.print(movesI.get(i) + " |"); // print move player 1
				
				for(int j = 0; j<payoffsNodei.size(); j++)
					System.out.printf("%"+(maxSizePayoff+2)+".1f", payoffsNodei.get(j).get(player));						
				
				System.out.printf("%"+((maxSizePayoff+2)*(columnLabels.size() - payoffsNodei.size())+2) +"s","|");
				System.out.println();
			}
			for(int z = 0; z<(maxSizePayoff+2)*columnLabels.size() + 5; z++)
				System.out.print("-");
			
			System.out.println("\n");
		}
	}
	
	// method reads the input file and generates a factor game
	public static FactorGame readInput(File input,int precisionPayoff,int 
			numberOfPlayers){
		FactorGame g = null;
		
		Scanner in;
		try{
			in = new Scanner(input);
			in.nextLine();
			// number moves player I
			int numberOfMovesPlayer1 = in.nextInt();
			in.nextLine();
			in.nextLine(); 
			// number moves player II for each move player I
			int[] numberOfMovesPlayer2 = new int[numberOfMovesPlayer1];
			String numOfMovesP2 = in.nextLine();
			Scanner scanNumMovesP2 = new Scanner(numOfMovesP2);
			for(int i = 0; i<numberOfMovesPlayer1; i++){
				if(scanNumMovesP2.hasNextInt())
					numberOfMovesPlayer2[i] = scanNumMovesP2.nextInt();
				else // bimatrix game thus only one parameter for P2 as well
					numberOfMovesPlayer2[i] = numberOfMovesPlayer2[0]; 
			}
			scanNumMovesP2.close();
			
			Double[][] payoffsI = new Double[numberOfMovesPlayer1][];
			Double[][] payoffsII = new Double[numberOfMovesPlayer1][];
			
			// store max payoff length for printing later
			maxSizePayoff = 0;
			
			in.nextLine();
			// check if payoffs are be provided by user or random generate
			int checkRandom = in.nextInt();
			in.nextLine();

			// prepare if random generate
			Double LB = null;
			Double UB = null;
			Random rng = null;
			if(checkRandom ==1){ //random payoffs
				in.nextLine();
				// get lower bound for payoffs
				LB = in.nextDouble();
				in.nextLine();
				in.nextLine();
				// get upper bound for payoffs
				UB = in.nextDouble();
				rng = new Random();
			}

			for(int player = 0; player<numberOfPlayers; player++){
				if(checkRandom ==0)
					in.nextLine();
				for(int i = 0; i<numberOfMovesPlayer1; i++){
					int numMovesP2 = numberOfMovesPlayer2[i];
					Double[] payoffRow = new Double[numMovesP2];
					for(int j = 0; j<numMovesP2; j++){
						if(checkRandom==0)
							payoffRow[j]=in.nextDouble();
						else
							payoffRow[j]=rng.nextDouble()*(UB-LB)+LB;
						int sizePayoff = String.format("%."+precisionPayoff+"f",payoffRow[j]).length();
						if(sizePayoff > maxSizePayoff)
							maxSizePayoff = sizePayoff;
					}
					// add new row of payoffs to corresponding player
					if(player == 0)
						payoffsI[i] = payoffRow;
					else
						payoffsII[i] = payoffRow;
					
					if(checkRandom == 0){
						if(in.hasNextLine())
							in.nextLine();
					}
				}
			}
			in.close(); 
			
			// store move parameters for each player
			int[][] numberMoves = {{numberOfMovesPlayer1},numberOfMovesPlayer2};
			
			// construct factor game from parameters
			g = new FactorGame(numberOfPlayers,numberMoves,payoffsI,payoffsII);	

			// max number of moves at a node for player II
			int maxNumberMovesII = 0;
			for(int m : numberOfMovesPlayer2){	
				if(m > maxNumberMovesII)
					maxNumberMovesII = m;
					
			}
			// make list of 'move' labels for columns of output payoff matrices
			for(int c = 0; c<maxNumberMovesII; c++){
				// skip letter p used to denote probability in the output
				if(c>=15) 
					columnLabels.add(String.valueOf((char)(97 + c + 1))); 
				else
					columnLabels.add(String.valueOf((char)(97 + c)));
			}
		}
		catch (Exception e){
			System.err.print("Error: Invalid Input.");
		}	
		// return factor game
		return g;
	}	
}

import java.util.List;
//Candidate Number: 60670

// this class is a data structure for the solution to the gcbNE commitment game
// algorithm. It stores the equilib strategies for player I and II as well 
// and the corresponding payoffs 
public class SolutionCG 
{
	// equilibrium strategies player I
	List<Double[]> equilibStrategiesP1;
	// corresponding equilibrium strategies player II
	List<List<List<Double[]>>> equilibStrategiesP2;
	// corresponding equilibrium payoff pairs
	List<List<Double>> equilibPayoffs;

	// constructor accepts and assigns the solution data
	public SolutionCG(List<Double[]> equilibStrategiesP1,
			List<List<List<Double[]>>> equilibStrategiesP2,
			List<List<Double>> equilibPayoffs) {
		this.equilibStrategiesP1 = equilibStrategiesP1;
		this.equilibStrategiesP2 = equilibStrategiesP2;
		this.equilibPayoffs = equilibPayoffs;
	}

	// method gets the list of equilibrium strategies for player I
	public List<Double[]> getBetaI() {
		return equilibStrategiesP1;
	}
	
	// method gets the list of equilibrium strategies for player II
	public List<List<List<Double[]>>> getBetaII(){
		return equilibStrategiesP2;
	}

	// method gets the list of corresponding equilibrium payoffs
	public List<List<Double>> getEquilibPayoffs() {
		return equilibPayoffs;
	}
}
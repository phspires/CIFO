package cifo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class GeneticAlgorithm extends SearchMethod {

	protected boolean printFileFlag;
	protected ProblemInstance instance;
	protected int populationSize, numberOfGenerations;
	protected double mutationProbability;
	protected int tournamentSize;
	protected boolean printFlag;
	protected Solution currentBest;
	protected int currentGeneration;
	protected Solution[] population;
	protected Random r;
	protected Writer outFile;
	protected boolean crossover_two_points;
	protected boolean smoother_mutations;
	protected boolean best_parents;
	protected boolean allowrepeated;
	protected int numberOfTriangles;
	protected int numberOfTriangles_mutated;
	protected boolean elitism;
	protected boolean argumentfitnessSharing;
	protected boolean fitnessSharing;
	protected double averageGenotypicDistance=100;
	protected int nGensFitness;

	public GeneticAlgorithm() {
		instance = new ProblemInstance(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		numberOfGenerations = Main.NUMBER_OF_GENERATIONS;
		mutationProbability = Main.MUTATION_PROBABILITY;
		tournamentSize = Main.TOURNAMENT_SIZE;
		printFlag = false;
		printFileFlag = true;
		currentGeneration = 0;
		r = new Random();
		outFile = new Writer();
		String DATE_FORMAT = "yyyyMMddHHmmss";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String nameFile = sdf.format(new Date().getTime()) + "_run"+ Main.currentRun;	
		outFile.setFileName(nameFile);

		numberOfTriangles = Main.NUMBER_OF_TRIANGLES;
		crossover_two_points = Main.CROSSOVER_TWOPOINTS;
		smoother_mutations = Main.SMOOTHER_MUTATIONS;
		best_parents = Main.BEST_PARENTS;
		numberOfTriangles_mutated = Main.N_TRIANGLES_MUTATION;

		elitism = Main.ELITISM;
		fitnessSharing = false; 
	}

	public void run() {
		initialize();
		evolve();
		Main.addBestSolution(currentBest);
	}

	//creates initial random population
	public void initialize() {		
		population = new Solution[populationSize];
		for (int i = 0; i < population.length; i++) {
			population[i] = new Solution(instance);
			population[i].evaluate();
		}

		if(fitnessSharing) calculateFitnessSharing(); 

		updateCurrentBest();
		updateInfo();
		currentGeneration++;
	}

	private void calculateFitnessSharing(){
		for (int i = 0; i < population.length; i++) {
			double newFitness = population[i].getFitness()*calculateSharingCoefficient(population[i]);
			population[i].setSharingFitness(newFitness);
		}		
	}

	private double calculateSharingCoefficient(Solution currentSolution) {
		double sharingcoefficient =0.0;
		for (int i = 0; i < population.length; i++) {
			if(population[i] != currentSolution) 
				sharingcoefficient += 1.0/Math.sqrt(currentSolution.computeEuclidianDistance(population[i]));
		}
		//System.out.println("\nsharingcoefficient:"+(sharingcoefficient));
		return sharingcoefficient;   
	}

	public void updateCurrentBest() {
		currentBest = getBest(population);
	}

	public void evolve() {
		while (currentGeneration <= numberOfGenerations) {

			Solution[] offspring = new Solution[populationSize];

			for (int k = 0; k < population.length; k++) {

				int[] parents;
				//System.out.println(population.length/2); -- 12

				if(best_parents) {
					parents = selectBestParents();
					//System.out.println("selectBestParents");
				} else {
					parents = selectParents();
					//System.out.println("selectParents");
				}

				if(crossover_two_points) {
					offspring[k] = applyCrossover2points(parents);
					//System.out.println("applyCrossover2points");
				} else {
					offspring[k] = applyCrossover(parents);
					//System.out.println("applyCrossover");
				}
				//
				if (r.nextDouble() <= mutationProbability) {
					if(smoother_mutations){
						offspring[k] = offspring[k].applySofterMutation(10, 5,numberOfTriangles_mutated);
					} else {
						offspring[k] = offspring[k].applyMutation(numberOfTriangles_mutated);
					}
				}
				offspring[k].evaluate();
			}

			if(elitism) population = survivorSelection(offspring);
			else population = offspring;

			if(fitnessSharing) calculateFitnessSharing();

			setAverageGenotypicDistance(computeAverageGenotypicDistance());			

			updateCurrentBest();
			updateInfo();

			if(!avgGenotypicDistanceIsOK()){ 
				System.out.println("Fitness NOK");
				toggleFitnessSharing();
			} else 
				if(fitnessSharing) toggleFitnessSharing();
			
			currentGeneration++;
		}
	}

	private void toggleFitnessSharing() {
		fitnessSharing=!fitnessSharing;
	}

	private boolean avgGenotypicDistanceIsOK() {
		//System.out.println("averageGenotypicDistance :" + averageGenotypicDistance);
		if(Main.FITNESS_SHARING && averageGenotypicDistance < Main.AVG_GENOTYPIC_DIST_MIN_VALUE)
			return false;
		else return true;
	}

	// função pode selecionar os pais com o melhor fitness
	public int[] selectParents() {
		int[] parents = new int[2];
		parents[0] = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getSharedFitness()< population[parents[0]].getSharedFitness()) {
				parents[0] = temp;
			}
		}

		parents[1] = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getSharedFitness() < population[parents[1]].getSharedFitness()) {
				parents[1] = temp;
			}
		}
		return parents;
	}

	public void printGenotypicDiversity() {
		System.out.println("\nCurrent Gen:" + currentGeneration);
		System.out.printf("Genotypic distance to best: %.12f\n", computeGenotypicDistanceToBest());
		System.out.printf("Average genotypic distance: %.12f\n", computeAverageGenotypicDistance());
	}

	// computes the average Manhattan distance between the best individual and
	// the rest of the population
	public double computeGenotypicDistanceToBest() {
		double totalDistance = 0;
		for (int i = 0; i < population.length; i++) {
			if (population[i] != currentBest) {
			}
		}
		return totalDistance / (population.length - 1);
	}

	// computes the average Manhattan distance in the population
	public double computeAverageGenotypicDistance() {
		double totalDistance = 0;
		int count = 0;
		for (int i = 0; i < population.length; i++) {
			for (int k = i + 1; k < population.length; k++) {
				//totalDistance += population[i].computeManhattanDistance(population[k]);
				totalDistance+=population[i].computeEuclidianDistance(population[k]);
				count++;
			}
		}
		return totalDistance / count;
	}

	public void printFitnessDiversity() {
		System.out.printf("\nFitness distance to best: %.12f\n", computeFitnessDistanceToBest());
		System.out.printf("Average fitness distance: %.12f\n", computeAverageFitnessDistance());
	}

	// computes the average fitness absolute difference between the best
	// individual and the rest of the population
	public double computeFitnessDistanceToBest() {
		double totalDistance = 0;
		for (int i = 0; i < population.length; i++) {
			if (population[i] != currentBest) {
				totalDistance += currentBest.computeFitnessDistance(population[i]);
			}
		}
		return totalDistance / (population.length - 1);
	}

	// computes the average fitness absolute difference in the population
	public double computeAverageFitnessDistance() {
		double totalDistance = 0;
		int count = 0;
		for (int i = 0; i < population.length; i++) {
			for (int k = i + 1; k < population.length; k++) {
				totalDistance += population[i].computeFitnessDistance(population[k]);
				count++;
			}
		}
		return totalDistance / count;
	}	


	// Returns a uniformly distributed double value between 0.0 and 1.0
	double randUniformPositive() {
		// easiest implementation
		return new Random().nextDouble();
	}

	//seleccionar o melhor "casal"
	public int[] selectBestParents() {
		int[] parents = new int[2];

		//best parent
		int minFitPosition = 0;
		for (int i = 0; i < populationSize; i++) {
			if (population[i].getSharedFitness() < population[minFitPosition].getSharedFitness()) {
				minFitPosition = i;
			}
		}
		parents[0] = minFitPosition;

		//second best parent(excluding first parent ) 
		minFitPosition = 0;
		for (int i = 0; i < populationSize; i++) {
			if (population[i].getSharedFitness() < population[minFitPosition].getSharedFitness() && minFitPosition != parents[0]) {
				minFitPosition = i;
			}
		}
		parents[1] = minFitPosition;

		return parents;
	} 

	public Solution applyCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int crossoverPoint = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		for (int i = crossoverPoint; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			offspring.setValue(i, secondParent.getValue(i));
		}
		return offspring;
	}

	public Solution applyCrossover2points(int[] parents) {

		Solution offspring;
		Solution firstParent;
		Solution secondParent;

		if (r.nextBoolean()) {
			firstParent = population[parents[0]];
			secondParent = population[parents[1]];
		} else {
			firstParent = population[parents[1]];
			secondParent = population[parents[0]];
		}

		offspring = firstParent.copy();
		int crossoverPoint = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		int crossoverPoint2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);

		int startPoint = crossoverPoint;
		int endPoint = crossoverPoint2;

		if (crossoverPoint>crossoverPoint2)
			endPoint = crossoverPoint;
		startPoint = crossoverPoint2;

		for (int i = startPoint; i < endPoint; i++) {
			offspring.setValue(i, secondParent.getValue(i));
		}
		return offspring;
	}

	// is elitist
	public Solution[] survivorSelection(Solution[] offspring) {
		Solution bestParent = getBest(population);
		Solution bestOffspring = getBest(offspring);
		if (bestOffspring.getSharedFitness() <= bestParent.getSharedFitness()) {
			return offspring;
		} else {
			Solution[] newPopulation = new Solution[population.length];
			newPopulation[0] = bestParent;
			int worstOffspringIndex = getWorstIndex(offspring);
			for (int i = 0; i < newPopulation.length; i++) {
				if (i < worstOffspringIndex) {
					newPopulation[i + 1] = offspring[i];
				} else if (i > worstOffspringIndex) {
					newPopulation[i] = offspring[i];
				}
			}
			return newPopulation;
		}
	}

	public Solution getBest(Solution[] solutions) {
		Solution best = solutions[0];
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getSharedFitness() < best.getSharedFitness()) {
				best = solutions[i];
			}
		}
		return best;
	}

	public int getWorstIndex(Solution[] solutions) {
		Solution worst = solutions[0];
		int index = 0;
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getSharedFitness() > worst.getSharedFitness()) {
				worst = solutions[i];
				index = i;
			}
		}

		return index;
	}

	public void updateInfo() {
		currentBest.draw();
		series.add(currentGeneration, currentBest.getFitness());

		if (printFlag) {
			System.out.printf("Generation: %d\tFitness: %.1f\n", currentGeneration, currentBest.getFitness());
		}

		if (printFileFlag) {

			int c2p = (crossover_two_points) ? 1 : 0;
			int sm = (smoother_mutations) ? 1 : 0;
			int bp = (best_parents) ? 1 : 0;
			int fs = (fitnessSharing) ? 1 : 0;
			int elite = (Main.ELITISM) ? 1: 0;

			outFile.printLineToFile(new Timestamp(new Date().getTime())+";" + 
					Main.currentRun+";" + 
					currentGeneration + ";" + 
					currentBest.getFitness() + ";" + 
					populationSize + ";" + 
					tournamentSize + ";" +
					mutationProbability + ";" +
					numberOfTriangles + ";" +
					c2p + ";" +
					sm + ";" +
					bp + ";" +
					currentBest.getSharedFitness() + ";" +
					computeAverageFitnessDistance() + ";" +
					getAverageGenotypicDistance() + ";" + 
					fs + ";" +
					numberOfTriangles_mutated + ";" + 
					elite + ";" +
					Main.AVG_GENOTYPIC_DIST_MIN_VALUE);

			Main.data_base.insertResult("now()," + 
					Main.currentRun+"," + 
					currentGeneration + "," + 
					currentBest.getFitness() + "," + 
					populationSize + "," + 
					tournamentSize + "," +
					mutationProbability + "," +
					numberOfTriangles + "," +
					c2p + "," +
					sm + "," +
					bp + "," +
					currentBest.getSharedFitness());
		}		
	}

	public double getAverageGenotypicDistance() {
		return averageGenotypicDistance;
	}

	public void setAverageGenotypicDistance(double averageGenotypicDistance) {
		this.averageGenotypicDistance = computeAverageGenotypicDistance();
	}
}

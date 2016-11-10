package cifo;

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
	protected boolean smother_mutations;
	protected boolean best_parents;
	protected int numberOfTriangles;

	public GeneticAlgorithm() {
		instance = new ProblemInstance(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		numberOfGenerations = Main.NUMBER_OF_GENERATIONS;
		mutationProbability = Main.MUTATION_PROBABILIY;
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
		
		crossover_two_points = Main.CROSSOVER_TWOPOINTS;
		smother_mutations = Main.SMOTHER_MUTATIONS;
		best_parents = Main.BEST_PARENTS;
	
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
		updateCurrentBest();
		updateInfo();
		currentGeneration++;
	}

	public void updateCurrentBest() {
		currentBest = getBest(population);
	}

	public void evolve() {
		while (currentGeneration <= numberOfGenerations) {
			Solution[] offspring = new Solution[populationSize];
			for (int k = 0; k < population.length; k++) {

				int[] parents;
				if(best_parents) {
					parents = selectBestParents();
				}
				else {
					parents = selectParents();
				}
				if(crossover_two_points) {
					offspring[k] = applyCrossover2points(parents);
				}
				else {
					offspring[k] = applyCrossover(parents);
				}
				//
				if (r.nextDouble() <= mutationProbability) {
					if(smother_mutations){
						offspring[k] = offspring[k].applySofterMutation();
					} else {
					offspring[k] = offspring[k].applyMutation();
					}
				}
				offspring[k].evaluate();
			}

			population = survivorSelection(offspring);
			updateCurrentBest();
			updateInfo();
			currentGeneration++;
		}
	}

	// função pode selecionar os pais com o melhor fitness
	public int[] selectParents() {
		int[] parents = new int[2];
		parents[0] = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getFitness() < population[parents[0]].getFitness()) {
				parents[0] = temp;
			}
		}

		parents[1] = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getFitness() < population[parents[1]].getFitness()) {
				parents[1] = temp;
			}
		}
		return parents;
	}
	// seleccionar o melhor "casal"
	public int[] selectBestParents() {
		int[] parents = new int[2];

		//best parent
		for (int i = 0; i < populationSize; i++) {
			Solution temp = population[0];
			if (temp.getFitness() < population[i].getFitness()) {
				parents[0] = i;
			}
		}

		//second best parent
		for (int i = 0; i < populationSize; i++) {
			Solution temp = population[0];
			if (temp.getFitness() < population[i].getFitness() && parents[0] != i) {
				parents[1] = i;
			}
		}
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
		if (bestOffspring.getFitness() <= bestParent.getFitness()) {
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
			if (solutions[i].getFitness() < best.getFitness()) {
				best = solutions[i];
			}
		}
		return best;
	}

	public int getWorstIndex(Solution[] solutions) {
		Solution worst = solutions[0];
		int index = 0;
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getFitness() > worst.getFitness()) {
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
			int sm = (smother_mutations) ? 1 : 0;
			int bp = (best_parents) ? 1 : 0;
			
			outFile.printLineToFile(
							new Date().getTime()+";" + 
								Main.currentRun+";" + 
								currentGeneration + ";" + 
								currentBest.getFitness() + ";" + 
								populationSize + ";" + 
								tournamentSize + ";" +
								mutationProbability + ";" +
								numberOfTriangles + ";" +
								c2p + ";" +
								sm + ";" +
								bp
								);
		}		
	}
}

package cifo;

import gd.gui.GeneticDrawingApp;

public class Main {

	protected static int NUMBER_OF_TRIANGLES = 100;
	protected static int NUMBER_OF_RUNS = 50000; 
	protected static int NUMBER_OF_GENERATIONS = 50000; 
	protected static int POPULATION_SIZE = 10;
	protected static double MUTATION_PROBABILITY = 0.9; 
	protected static int TOURNAMENT_SIZE = 3; 
			
	protected static boolean SMOOTHER_MUTATIONS = false;
	protected static boolean BEST_PARENTS = false;
	protected static boolean CROSSOVER_TWOPOINTS = false;
	
	public static boolean KEEP_WINDOWS_OPEN = false;
	
	public static Solution[] bestSolutions = new Solution[NUMBER_OF_RUNS];
	public static double[] bestFitness = new double[NUMBER_OF_RUNS];
	public static int currentRun = 0;
	public static DB data_base = new DB();
	public static Double[] data_base_initial = new Double[50];
	protected static int id_database = 0;
	public static void main(String[] args) {		

		if	(args.length == 1 && Integer.parseInt(args[0]) == 11) {
			data_base.createDBconection();
			data_base_initial = data_base.selectNextRun();
			if(data_base_initial != null){
	    	NUMBER_OF_RUNS = data_base_initial[0].intValue(); // number of Runs
	    	NUMBER_OF_GENERATIONS = data_base_initial[1].intValue(); // number of Generations
	    	POPULATION_SIZE = data_base_initial[2].intValue(); // population size
	    	MUTATION_PROBABILITY = data_base_initial[3]; // mutation probability
	    	TOURNAMENT_SIZE = data_base_initial[4].intValue(); // tournament size		     
	    	
	    	int smooth = data_base_initial[5].intValue(); // smoother location
	        int bestPar = data_base_initial[6].intValue(); // best parents
	        int xOver2 = data_base_initial[7].intValue(); // xover 2 points	
	        int windOpen = data_base_initial[8].intValue(); // windows open
	        
	    	if (smooth == 1) SMOOTHER_MUTATIONS=true;
	    	if (bestPar == 1) BEST_PARENTS=true;
	    	if (xOver2 == 1) CROSSOVER_TWOPOINTS=true;	
	    	if (windOpen == 1) KEEP_WINDOWS_OPEN=true;		     

	    	NUMBER_OF_TRIANGLES = data_base_initial[9].intValue(); // number of triangles
	    	id_database = data_base_initial[10].intValue();
	    	data_base.updateState(id_database);
	    	
			} else {
				System.out.println("Database has no new records");
				System.exit(1);
			}
		}
		
		else {
		if	(args.length > 7) {
		    try {
		    	
		    	NUMBER_OF_RUNS = Integer.parseInt(args[0]); // number of Runs
		    	NUMBER_OF_GENERATIONS = Integer.parseInt(args[1]); // number of Generations
		    	POPULATION_SIZE = Integer.parseInt(args[2]); // population size
		    	MUTATION_PROBABILITY = Double.parseDouble(args[3])/100; // mutation probability
		    	TOURNAMENT_SIZE = Integer.parseInt(args[4]); // tournament size		     
		    	
		    	int smooth = Integer.parseInt(args[5]); // smoother location
		        int bestPar = Integer.parseInt(args[6]); // best parents
		        int xOver2 = Integer.parseInt(args[7]); // xover 2 points	
		        int windOpen = Integer.parseInt(args[8]); // windows open
		        
		    	if (smooth == 1) SMOOTHER_MUTATIONS=true;
		    	if (bestPar == 1) BEST_PARENTS=true;
		    	if (xOver2 == 1) CROSSOVER_TWOPOINTS=true;	
		    	if (windOpen == 1) KEEP_WINDOWS_OPEN=true;		     

		    	NUMBER_OF_TRIANGLES = Integer.parseInt(args[9]); // number of triangles
		    	
		    } catch (IllegalArgumentException e) {
		        System.err.println("Check your arguments");
		        System.exit(1);
		    } 
		}
		}
	    System.out.println("NUMBER_OF_RUNS is " + NUMBER_OF_RUNS);
		System.out.println("NUMBER_OF_GENERATIONS is " + NUMBER_OF_GENERATIONS);
		System.out.println("POPULATION_SIZE is " + POPULATION_SIZE);
		System.out.println("MUTATION_PROBABILITY is " + MUTATION_PROBABILITY);
		System.out.println("TOURNAMENT_SIZE is " + TOURNAMENT_SIZE);
		System.out.println("SMOOTHER_MUTATIONS is " + SMOOTHER_MUTATIONS);
		System.out.println("BEST_PARENTS is " + BEST_PARENTS);
		System.out.println("CROSSOVER_TWOPOINTS is " + CROSSOVER_TWOPOINTS);
		System.out.println("KEEP_WINDOWS_OPEN is " + KEEP_WINDOWS_OPEN);
		System.out.println("NUMBER_OF_TRIANGLES is " + NUMBER_OF_TRIANGLES);

		run();

	}

	public static void addBestSolution(Solution bestSolution) {
		bestSolutions[currentRun] = bestSolution;
		bestFitness[currentRun] = bestSolution.getFitness();
		System.out.printf("Got %.2f as a result for run %d\n", bestFitness[currentRun], currentRun + 1);
		System.out.print("All runs:");
		for (int i = 0; i <= currentRun; i++) {
			System.out.printf("\t%.2f", bestFitness[i]);
		}
		System.out.println();
		currentRun++;
		if (KEEP_WINDOWS_OPEN == false) {
			ProblemInstance.view.getFittestDrawingView().dispose();
			ProblemInstance.view.getFrame().dispose();
		}
		if (currentRun < NUMBER_OF_RUNS) {
			run();
		} else {
			presentResults();
		}
	}

	public static void presentResults() {
		double mean = Statistics.mean(bestFitness);
		double stdDev = Statistics.standardDeviation(bestFitness);
		double best = Statistics.min(bestFitness);
		double worst = Statistics.max(bestFitness);
		System.out.printf("\n\t\tMean +- std dev\t\tBest\t\tWorst\n\n");
		System.out.printf("Results\t\t%.2f +- %.2f\t%.2f\t%.2f\n", mean, stdDev, best, worst);
		
		if(id_database > 1) {
			String query = "";
			query = String.format("mean=%.2f, \"stdDev\"=%.2f,best=%.2f, "
					+ "worst=%.2f,datetime_end='now()',state='confirmed'"
					+ " ",mean, stdDev, best, worst);
			data_base.updateFinalResult(query);
			data_base.closeConection();
		}
	}

	public static void run() {
		GeneticDrawingApp.main(null);
	}
}

Para executar:

1) Colocar o jar CIFo.jar dentro do diretório CIFO_project ao mesmo nível que o diretório lib e src e bin
2) Navegar para este diretório na linha de comandos e executar:
	a)	java -jar CIFO.jar // para executar com os parâmetros por defeito
	b)	java -jar CIFO.jar 1 1 1 1 1 1 1 1 1 // para executar com os parâmetros por argumento

Important: None of the parameters can be configured without inserting the 
		   configuration for the all of the previous

Default Parameters(should be configured by this order)
NUMBER_OF_RUNS = 30; 
NUMBER_OF_GENERATIONS = 50000; 
POPULATION_SIZE = 25;
MUTATION_PROBABILIY = 0.5; 
TOURNAMENT_SIZE = 3; 
SMOOTHER_MUTATIONS = false; //should be 1 or 0
BEST_PARENTS = false; //should be 1 or 0
CROSSOVER_TWOPOINTS = false; //should be 1 or 0
KEEP_WINDOWS_OPEN = false; //should be 1 or 0
NUMBER_OF_TRIANGLES = 100;
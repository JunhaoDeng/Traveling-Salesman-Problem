// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main1 {
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        List<String> initialSetup = new ArrayList<>();
        File file = new File("C:\\Users\\23680\\IdeaProjects\\CS561hw1\\src\\exmp2.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                initialSetup.add(line);
            }
        }

        int num_of_cities = Integer.parseInt(initialSetup.get(0));
        List<int[]> city_coordination_list = new ArrayList<>();
        for (int i = 1; i <= num_of_cities; i++) {
            String[] coordinates = initialSetup.get(i).split(" ");
            int x_axis = Integer.parseInt(coordinates[0]);
            int y_axis = Integer.parseInt(coordinates[1]);
            int z_axis = Integer.parseInt(coordinates[2]);
            city_coordination_list.add(new int[]{x_axis, y_axis, z_axis});
        }

        System.out.println("cityCoordinationList:");
        for (int[] coordinates : city_coordination_list) {
            System.out.println(coordinates[0] + " " + coordinates[1] + " " + coordinates[2]);
        }
        System.out.println("numOfCities:\n" + num_of_cities);

        double[][] cities_abs_distance_table = doCitiesAbsDistanceTable(city_coordination_list);
        System.out.println("cities_abs_distance_table:");
        for (int i = 0; i < cities_abs_distance_table.length; i++) {
            for (int j = 0; j < cities_abs_distance_table[i].length; j++) {
                System.out.print(cities_abs_distance_table[i][j] + " ");
            }
            System.out.println();
        }

        int population_num = 4;
        int generation_num = 500;
        double mutate_prob = 0.25;
        double select_rate = 0.3;
        List<int[]> initial_population_list = createInitialPopulation(num_of_cities, population_num);
        System.out.println("initialPopulationList:");
        for (int[] gene : initial_population_list) {
            for (int value : gene) {
                System.out.print(value + " ");
            }
            System.out.println();
        }

        int[] best_gene = initial_population_list.get(0);
        System.out.println("best_gene:");
        for (int value : best_gene) {
            System.out.print(value + " ");
        }
        System.out.println();

        long endTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (endTime - startTime) + " milliseconds");
    }

    public static double[][] doCitiesAbsDistanceTable(List<int[]> inputList) {
        int n = inputList.size();
        double[][] distMat = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distance = absDistanceBetweenTwo3DPoints(inputList.get(i), inputList.get(j));
                distMat[i][j] = distance;
                distMat[j][i] = distance;
            }
        }
        return distMat;
    }

    public static double absDistanceBetweenTwo3DPoints(int[] pointA, int[] pointB) {
        double dx = pointA[0] - pointB[0];
        double dy = pointA[1] - pointB[1];
        double dz = pointA[2] - pointB[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static List<int[]> createInitialPopulation(int num_of_cities, int population_num) {
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < population_num; i++) {
            int[] genes = new int[num_of_cities];
            for (int j = 0; j < num_of_cities; j++) {
                genes[j] = j;
            }
            shuffleArray(genes);
            population.add(genes);
        }
        return population;
    }

    public static void shuffleArray(int[] array) {
        int index, temp;
        for (int i = array.length - 1; i > 0; i--) {
            index = (int) (Math.random() * (i + 1));
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main2 {

    static int num_of_cities;
    static double[][] cities_abs_distance_table;

    static int individual_num = 100;
    static int gen_num = 500;
    static double mutate_prob = 0.25;

    public static double abs_distance_between_two_3d_points(double[] point_a, double[] point_b) {
        double sum = 0;
        for (int i = 0; i < 3; i++) {
            sum += Math.pow(point_a[i] - point_b[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static double[][] do_cities_abs_distance_table(List<double[]> inputList) {
        int n = num_of_cities;
        double[][] dist_mat = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distance = abs_distance_between_two_3d_points(inputList.get(i), inputList.get(j));
                dist_mat[i][j] = distance;
                dist_mat[j][i] = distance;
            }
        }
        return dist_mat;
    }

    public static double tsp_total_distance(List<Integer> path, double[][] distanceTable) {
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += distanceTable[path.get(i)][path.get(i + 1)];
        }
        totalDistance += distanceTable[path.get(path.size() - 1)][path.get(0)];
        return totalDistance;
    }

    public static List<Integer> listDeepCopy(List<Integer> oldList) {
        List<Integer> newList = new ArrayList<>(oldList);
        return newList;
    }

    public static class Individual {
        List<Integer> route;
        double cumulateDistance;

        public Individual(List<Integer> route) {
            this.route = route;
            this.cumulateDistance = computeCumulateDistance();
        }

        public Individual() {
            route = new ArrayList<>();
            for (int i = 0; i < num_of_cities; i++) {
                route.add(i);
            }
            Collections.shuffle(route);
            cumulateDistance = computeCumulateDistance();
        }

        public double computeCumulateDistance() {
            double cumulateDistance = 0.0;
            for (int i = 0; i < num_of_cities - 1; i++) {
                int city1Idx = route.get(i);
                int city2Idx = route.get(i + 1);
                cumulateDistance += cities_abs_distance_table[city1Idx][city2Idx];
            }
            cumulateDistance += cities_abs_distance_table[route.get(route.size() - 1)][route.get(0)];
            return cumulateDistance;
        }
    }

    public static class Generation {
        List<Individual> individualList;
        Individual best;

        public Generation(double[][] table, int cityNum) {
            cities_abs_distance_table = table;
            num_of_cities = cityNum;
            individualList = new ArrayList<>();
            best = null;
        }

        public void cross(List<Individual> newGen) {
            for (int i = 0; i < individual_num - 1; i += 2) {
                List<Integer> route1 = newGen.get(i).route;
                List<Integer> route2 = newGen.get(i + 1).route;
                int index1 = new Random().nextInt(num_of_cities - 1);
                int index2 = new Random().nextInt(num_of_cities - index1) + index1;
                crossover(route1, route2, index1, index2);
            }
            individualList.addAll(newGen);
        }

        public static void crossover(List<Integer> list1, List<Integer> list2, int startIndex, int endIndex) {
            int[] pos1Recorder = new int[num_of_cities];
            int[] pos2Recorder = new int[num_of_cities];

            for (int i = startIndex; i <= endIndex; i++) {
                int value1 = list1.get(i);
                int value2 = list2.get(i);
                int pos1 = pos1Recorder[value2];
                int pos2 = pos2Recorder[value1];
                Collections.swap(list1, i, pos1);
                Collections.swap(list2, i, pos2);
                pos1Recorder[value1] = pos1;
                pos1Recorder[value2] = i;
                pos2Recorder[value1] = pos2;
                pos2Recorder[value2] = i;
            }
        }

        public List<Individual> mutate() {
            List<Individual> newGen = new ArrayList<>();
            Collections.shuffle(individualList);

            for (Individual individual : individualList) {
                if (Math.random() < mutate_prob) {
                    List<Integer> mutateIndividual = listDeepCopy(individual.route);
                    int numSwaps = (int) (num_of_cities * Math.random());
                    for (int i = 0; i < numSwaps; i++) {
                        int index1 = new Random().nextInt(num_of_cities - 1);
                        int index2 = new Random().nextInt(num_of_cities - index1) + index1;
                        Collections.swap(mutateIndividual, index1, index2);
                    }
                    newGen.add(new Individual(mutateIndividual));
                } else {
                    newGen.add(new Individual(listDeepCopy(individual.route)));
                }
            }
            return newGen;
        }

        public void select() {
            int groupNum = 10;
            int groupSize = 10;
            int groupWinner = individual_num / groupNum;
            List<Individual> winners = new ArrayList<>();

            for (int i = 0; i < groupNum; i++) {
                List<Individual> group = new ArrayList<>();
                for (int j = 0; j < groupSize; j++) {
                    Individual player = individualList.get(new Random().nextInt(individual_num));
                    group.add(new Individual(player.route));
                }
                Collections.sort(group, (a, b) -> Double.compare(a.cumulateDistance, b.cumulateDistance));
                winners.addAll(group.subList(0, groupWinner));
            }

            individualList = winners;
        }

        public void findBestIndividual() {
            best = individualList.get(0);
            for (Individual individual : individualList) {
                if (individual.cumulateDistance < best.cumulateDistance) {
                    best = individual;
                }
            }
        }

        public List<List<Integer>> train() {
            individualList = new ArrayList<>();
            for (int i = 0; i < individual_num; i++) {
                individualList.add(new Individual());
            }
            best = individualList.get(0);

            List<List<Integer>> resultLists = new ArrayList<>();
            List<Double> shortestRouteList = new ArrayList<>();

            for (int i = 0; i < gen_num; i++) {
                List<Individual> newGen = mutate();
                cross(newGen);
                select();
                findBestIndividual();

                List<Integer> result = listDeepCopy(best.route);
                result.add(result.get(0)); // Add the starting city to the end to complete the loop
                resultLists.add(result);
                shortestRouteList.add(best.cumulateDistance);
            }

            return resultLists;
        }
    }

    public static void main(String[] args) {
        long beginTime = System.currentTimeMillis();

        // Read city coordinates from input file
        // ...

        Generation ga = new Generation(cities_abs_distance_table, num_of_cities);
        List<List<Integer>> resultLists = ga.train();
        List<Integer> result = resultLists.get(resultLists.size() - 1);

        // Print result
        System.out.println("Result: " + result);
        double totalDistance = tsp_total_distance(result, cities_abs_distance_table);
        System.out.println("Total Distance: " + totalDistance);

        long endTime = System.currentTimeMillis();
        System.out.println("Execution Time: " + (endTime - beginTime) + " ms");
    }
}

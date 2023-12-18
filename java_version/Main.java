import java.io.*;
import java.util.*;

public class Main {
    public static int num_of_cities;
    public static double[][] cities_abs_distance_table;
    public static void main(String[] args) {
        long beginTime = System.currentTimeMillis();
        List<int[]> cityCoordinationList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\23680\\IdeaProjects\\CS561hw1\\src\\input1.txt"))) {
            String line = br.readLine();
            num_of_cities = Integer.parseInt(line);

            while ((line = br.readLine()) != null) {
                String[] coords = line.split(" ");
                int x_axis = Integer.parseInt(coords[0]);
                int y_axis = Integer.parseInt(coords[1]);
                int z_axis = Integer.parseInt(coords[2]);
                cityCoordinationList.add(new int[]{x_axis, y_axis, z_axis});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("cityCoordinationList:");
        for (int[] coords : cityCoordinationList) {
            System.out.println(coords[0] + " " + coords[1] + " " + coords[2]);
        }
        System.out.println("numOfCities: " + num_of_cities);

        cities_abs_distance_table = doCitiesAbsDistanceTable(cityCoordinationList);
        System.out.println("cities_abs_distance_table:");
        for (int i = 0; i < num_of_cities; i++) {
            for (int j = 0; j < num_of_cities; j++) {
                System.out.print(cities_abs_distance_table[i][j] + " ");
            }
            System.out.println();
        }

        Generation ga = new Generation();
        List<int[]> result = ga.train();
        // get the last result, which is the best_result
        int[] bestResult = result.get(result.size() - 1);
        System.out.println("bestResult:");
        System.out.println(Arrays.toString(bestResult));
        // bestresult里所有的值为city_coordination_list的index，输出result_pos_list为city_coordination_list中的坐标
        List<int[]> resultPosList = new ArrayList<>();
        for (int j : bestResult) {
            // j是city_coordination_list的index, city_coordination_list.get(j)是坐标,把坐标转换成int[]加入result_pos_list
            int[] cords = new int[3];
            for (int k = 0; k < 3; k++) {
                cords[k] = cityCoordinationList.get(j)[k];
            }
            resultPosList.add(cords);
        }
        System.out.println("result_pos_list:");
        for (int[] cords : resultPosList) {
            System.out.println(Arrays.toString(cords));
        }
//        System.out.println(Arrays.toString(result_pos_list.toArray()));
        double totalDistance = tspTotalDistance(bestResult, cities_abs_distance_table);
        System.out.println("best_total_distance: " + totalDistance);

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - beginTime) + " milliseconds");
//        put the result_pos_list to output.txt
        String fileName = "C:\\Users\\23680\\IdeaProjects\\CS561hw1\\src\\output1.txt";
        try {
            // 创建一个文件写入对象
            FileWriter writer = new FileWriter(fileName);
            writer.write(String.valueOf(totalDistance));
            writer.write("\r\n");
            // 遍历 result_pos_list 列表，写入每个元素到文件
            for (int[] coords : resultPosList) {
                writer.write(coords[0] + " " + coords[1] + " " + coords[2]);
                writer.write("\r\n");
            }

            // 关闭文件写入对象
            writer.close();
            System.out.println("写入成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static double tspTotalDistance(int[] path, double[][] distanceTable) {
        double totalDistance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            totalDistance += distanceTable[path[i]][path[i + 1]];
        }
        totalDistance += distanceTable[path[path.length - 1]][path[0]];
        return totalDistance;
    }

    private static double absDistanceBetweenTwo3DPoints(int[] pointA, int[] pointB) {
        double distance = 0.0;
        for (int i = 0; i < 3; i++) {
            distance += Math.pow(pointA[i] - pointB[i], 2);
        }
        return Math.sqrt(distance);
    }

    private static double[][] doCitiesAbsDistanceTable(List<int[]> inputList) {
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

//    private static int tspTotalDistance(List<int[]> path, double[][] distanceTable) {
//        int totalDistance = 0;
//        for (int i = 0; i < path.size() - 1; i++) {
//            totalDistance += distanceTable[path.get(i)[0]][path.get(i + 1)[0]];
//        }
//        totalDistance += distanceTable[path.get(path.size() - 1)[0]][path.get(0)[0]];
//        return totalDistance;
//    }

    private static class Individual {
        private int[] route;
        private double cumulateDistance;

        public Individual(int[] route) {
            this.route = route;
            this.cumulateDistance = computeCumulateDistance();
        }

        public double getCumulateDistance() {
            return cumulateDistance;
        }

        public int[] getRoute() {
            return route;
        }

        private double computeCumulateDistance() {
            double cumulateDistance = 0.0;
            for (int i = 0; i < num_of_cities - 1; i++) {
                int city1Idx = route[i];
                int city2Idx = route[i + 1];
                cumulateDistance += cities_abs_distance_table[city1Idx][city2Idx];
            }
            cumulateDistance += cities_abs_distance_table[route[num_of_cities - 1]][route[0]];
            return cumulateDistance;
        }
    }

    private static class Generation {

        private int individualNum = 100;
        private int genNum = 500;
        private double mutateProb = 0.25;

//        private double crossProb = 0.8;

        private List<Individual> individualList = new ArrayList<>();
        private List<int[]> resultList = new ArrayList<>();
        private List<Double> shortestRouteList = new ArrayList<>();
        private Individual best;

        private void initializeWithHeuristic() {
            List<Integer> unvisitedCities = new ArrayList<>();
            for (int i = 0; i < num_of_cities; i++) {
                unvisitedCities.add(i);
            }

            int startCity = new Random().nextInt(num_of_cities);
            List<Integer> path = new ArrayList<>();
            path.add(startCity);
            unvisitedCities.remove(Integer.valueOf(startCity));

            while (!unvisitedCities.isEmpty()) {
                int currentCity = path.get(path.size() - 1);
                int nearestCity = findNearestCity(currentCity, unvisitedCities);
                path.add(nearestCity);
                unvisitedCities.remove(Integer.valueOf(nearestCity));
            }

            int[] initialRoute = path.stream().mapToInt(i -> i).toArray();
            individualList.add(new Individual(initialRoute));
//            for (int i = 0; i < individualNum; i++) {
//                individualList.add(new Individual(initialRoute));
//                System.out.println(Arrays.toString(initialRoute));
//            }
        }

        private int findNearestCity(int currentCity, List<Integer> unvisitedCities) {
            int nearestCity = -1;
            double minDistance = Double.MAX_VALUE;

            for (int city : unvisitedCities) {
                double distance = cities_abs_distance_table[currentCity][city];
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCity = city;
                }
            }
            return nearestCity;
        }

        public Generation() {
//            best = new Individual(new int[num_of_cities]);
            individualList = new ArrayList<>();

            for (int i = 0; i < individualNum / 10; i++) {
                initializeWithHeuristic();
            }
            for (int i = individualNum /10 ; i < individualNum; i++) {
                int[] randomRoute = generateRandomRoute();
                individualList.add(new Individual(randomRoute));
            }
            best = individualList.get(0);
        }

        private int[] generateRandomRoute() {
            int[] route = new int[num_of_cities];
            List<Integer> cities = new ArrayList<>();
            for (int i = 0; i < num_of_cities; i++) {
                cities.add(i);
            }
            Collections.shuffle(cities);
            for (int i = 0; i < num_of_cities; i++) {
                route[i] = cities.get(i);
            }
            return route;
        }

        private List<int[]> train() {
            // Initial population
//            individualList = new ArrayList<>();
//            for (int i = 0; i < individualNum; i++) {
//                Individual individual = new Individual(generateRandomRoute());
////                System.out.println(Arrays.toString(individual.getRoute()));
//                individualList.add(individual);
//            }
//            System.out.println(individualList.size());
//            best = individualList.get(0);
//            System.out.println(Arrays.toString(Arrays.stream(best.route).toArray()));

            for (int i = 0; i < genNum; i++) {
                List<Individual> newGen = mutate();
//                System.out.println("newGen after mutate:");
//                for (i=0; i<newGen.size(); i++) {
//                    System.out.println(Arrays.toString(newGen.get(i).route));
//                }
//                printIndividualList(newGen);
//                System.out.println(Arrays.toString(newGen.toArray()));
                cross(newGen);
//                System.out.println("newGen after crossover:");
//                for (i=0; i<newGen.size(); i++) {
//                    System.out.println(Arrays.toString(newGen.get(i).getRoute()));
//                }
                select();
//                System.out.println("individual list after selection:");
//                for (i=0; i<individualList.size(); i++) {
//                    System.out.println(Arrays.toString(individualList.get(i).getRoute()));
//                }
//                System.out.println("end of this generation");
//                System.out.println();
                findBestIndividual();

                // Connect first and last city to form a complete route
                int[] result = Arrays.copyOf(best.getRoute(), num_of_cities + 1);
//                System.out.println(Arrays.toString(result));
                result[num_of_cities] = result[0];
//                System.out.println(Arrays.toString(result));
                resultList.add(result);
                shortestRouteList.add(best.getCumulateDistance());
//                System.out.println(Arrays.toString(resultList.get(resultList.size() - 1)));
            }

            return resultList;
        }

        private void cross(List<Individual> newGen) {
            for (int i = 0; i < individualNum - 1; i += 2) {
                int[] route1 = newGen.get(i).getRoute();
                int[] route2 = newGen.get(i + 1).getRoute();
//                System.out.println("before crossover:");
//                System.out.println(Arrays.toString(route1));
//                System.out.println(Arrays.toString(route2));
                int index1 = new Random().nextInt(num_of_cities - 1);
                int index2 = index1 + new Random().nextInt(num_of_cities - index1);
//                System.out.println("index1: " + index1 + " index2: " + index2);
//                int[][] newRoutes =
                crossover(route1, route2, index1, index2);
//                newGen.get(i).route = newRoutes[0];
//                newGen.get(i + 1).route = newRoutes[1];
//                System.out.println("after crossover:");
//                System.out.println(Arrays.toString(newGen.get(i).getRoute()));
//                System.out.println(Arrays.toString(newGen.get(i + 1).getRoute()));
//                System.out.println();
            }
            individualList.addAll(newGen);
        }

        private static Map<Integer, Integer> getIndexMap(int[] oldList) {
            Map<Integer, Integer> indexMap = new HashMap<>();
            for (int idx = 0; idx < oldList.length; idx++) {
                indexMap.put(oldList[idx], idx);
            }
            return indexMap;
        }

        private static void crossover(int[] list1, int[] list2, int startIndex, int endIndex) {
            Map<Integer, Integer> pos1Recorder = getIndexMap(list1);
            Map<Integer, Integer> pos2Recorder = getIndexMap(list2);

            for (int i = startIndex; i <= endIndex; i++) {
                int value1 = list1[i];
                int value2 = list2[i];
                int pos1 = pos1Recorder.get(value2);
                int pos2 = pos2Recorder.get(value1);
                list1[i] = value2;
                list1[pos1] = value1;
                list2[i] = value1;
                list2[pos2] = value2;
                pos1Recorder.put(value1, pos1);
                pos1Recorder.put(value2, i);
                pos2Recorder.put(value1, i);
                pos2Recorder.put(value2, pos2);
            }
//            System.out.println("after crossover:");
//            System.out.println(Arrays.toString(list1));
//            System.out.println(Arrays.toString(list2));
//            System.out.println();
//            return new int[][] { list1, list2 };
        }

        // mutate是成功的
        private List<Individual> mutate() {
            List<Individual> newGen = new ArrayList<>();
            Collections.shuffle(individualList);
            for (Individual individual : individualList) {
                if (Math.random() < mutateProb) {
                    int numMutations = (int) (num_of_cities * Math.random());
                    int[] mutateIndividual = Arrays.copyOf(individual.getRoute(), num_of_cities);
//                    System.out.println(Arrays.toString(mutateIndividual));
                    for (int i = 0; i < numMutations; i++) {
                        int index1 = new Random().nextInt(num_of_cities - 1);
                        int index2 = index1 + new Random().nextInt(num_of_cities - index1);
                        int temp = mutateIndividual[index1];
                        mutateIndividual[index1] = mutateIndividual[index2];
                        mutateIndividual[index2] = temp;
                    }
//                    System.out.println(Arrays.toString(mutateIndividual));
                    newGen.add(new Individual(mutateIndividual));
//                    System.out.println(Arrays.toString(newGen.get(newGen.size()-1).getRoute()));
//                    System.out.println();
                } else {
                    int[] unchangedIndividual = Arrays.copyOf(individual.getRoute(), num_of_cities);
                    newGen.add(new Individual(unchangedIndividual));
                }
            }
//            for (Individual individual : newGen) {
//                System.out.println(Arrays.toString(individual.getRoute()));
//            }
            return newGen;
        }

        private void select() {
            int groupNum = 10;
            int groupSize = 10;
            int groupWinner = individualNum / groupNum;
//            printIndividualList(individualList);
            List<Individual> winners = new ArrayList<>();

            for (int i = 0; i < groupNum; i++) {
                List<Individual> group = new ArrayList<>();
                for (int j = 0; j < groupSize; j++) {
                    Individual player = individualList.get(new Random().nextInt(individualList.size()));
                    player = new Individual(player.getRoute());
                    group.add(player);
                }
                group.sort(Comparator.comparingDouble(Individual::getCumulateDistance));
//                printIndividualList(group);
//                System.out.println();
                winners.addAll(group.subList(0, groupWinner));
            }
//            System.out.println("winners:");
//            for (Individual individual : winners) {
//              System.out.println(Arrays.toString(individual.getRoute()));
//            }
//            System.out.println("end of winners");
//            System.out.println();
            individualList = winners;
        }

        private void findBestIndividual() {
            for (Individual individual : individualList) {
//                System.out.println(Arrays.toString(individual.getRoute()));
//                System.out.println(individual.getCumulateDistance());
                if (individual.getCumulateDistance() < best.getCumulateDistance()) {
                    best = individual;
                }
            }
        }

//        private void printIndividualList(List<Individual> il) {
//            for (Individual individual : il) {
//                System.out.println(Arrays.toString(individual.getRoute()));
//            }
//        }
    }
}

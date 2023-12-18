import java.io.*;
import java.util.*;

public class homework1 {
    public static int numOfCities;
    public static double[][] citiesAbsDistanceTable;
    public static long beginTime;

    public static void main(String[] args) {
        beginTime = System.currentTimeMillis();
        List<int[]> cityCoordinationList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\23680\\IdeaProjects\\CS561hw1\\src\\input3.txt"))) {
            String line = br.readLine();
            numOfCities = Integer.parseInt(line);

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

        citiesAbsDistanceTable = doCitiesAbsDistanceTable(cityCoordinationList);

        Generation ga = new Generation();
        List<int[]> result = ga.train();
        int[] bestResult = result.get(result.size() - 1);

        List<int[]> resultPosList = new ArrayList<>();
        for (int j : bestResult) {
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
        double totalDistance = tspTotalDistance(bestResult, citiesAbsDistanceTable);
        System.out.println("best_total_distance: " + totalDistance);
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - beginTime) / 1000 + " seconds");
        String fileName = "C:\\Users\\23680\\IdeaProjects\\CS561hw1\\src\\output1.txt";
        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write(String.valueOf(totalDistance));
            writer.write("\r\n");
            for (int[] coords : resultPosList) {
                writer.write(coords[0] + " " + coords[1] + " " + coords[2]);
                writer.write("\r\n");
            }
            writer.close();
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
            for (int i = 0; i < numOfCities - 1; i++) {
                int city1Idx = route[i];
                int city2Idx = route[i + 1];
                cumulateDistance += citiesAbsDistanceTable[city1Idx][city2Idx];
            }
            cumulateDistance += citiesAbsDistanceTable[route[numOfCities - 1]][route[0]];
            return cumulateDistance;
        }

        public void twoOptSwap(int i, int j) {
            while (i < j) {
                int temp = route[i];
                route[i] = route[j];
                route[j] = temp;
                i++;
                j--;
            }
            this.cumulateDistance = computeCumulateDistance();
        }

        public void threeOptSwap(int i, int j, int k) {
            int[] newRoute = new int[route.length];
            for (int index = 0; index <= i - 1; index++) {
                newRoute[index] = route[index];
            }
            int dec = 0;
            for (int index = i; index <= j; index++) {
                newRoute[index] = route[j - dec];
                dec++;
            }
            dec = 0;
            for (int index = j + 1; index <= k; index++) {
                newRoute[index] = route[k - dec];
                dec++;
            }
            for (int index = k + 1; index < route.length; index++) {
                newRoute[index] = route[index];
            }
            route = newRoute;
            this.cumulateDistance = computeCumulateDistance();
        }


    }

    private static class Generation {

        private int individualNum = 1000;
        private int genNum = 500;
        private double mutateProb = 0.25;

        private List<Individual> individualList = new ArrayList<>();
        private List<int[]> resultList = new ArrayList<>();
        private List<Double> shortestRouteList = new ArrayList<>();
        private Individual best;

        private void initializeWithHeuristic(int startCity) {
            List<Integer> unvisitedCities = new ArrayList<>();
            for (int i = 0; i < numOfCities; i++) {
                unvisitedCities.add(i);
            }
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
            double totalDistance = tspTotalDistance(initialRoute, citiesAbsDistanceTable);
            individualList.add(new Individual(initialRoute));
        }

        private int findNearestCity(int currentCity, List<Integer> unvisitedCities) {
            int nearestCity = -1;
            double minDistance = Double.MAX_VALUE;

            for (int city : unvisitedCities) {
                double distance = citiesAbsDistanceTable[currentCity][city];
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCity = city;
                }
            }
            return nearestCity;
        }

        public Generation() {
            individualList = new ArrayList<>();

            for (int i = 0; i < numOfCities; i++) {
                initializeWithHeuristic(i);
            }

            for (int i = numOfCities; i < individualNum; i++) {
                int[] randomRoute = generateRandomRoute();
                individualList.add(new Individual(randomRoute));
            }
            best = individualList.get(0);
        }

        private int[] generateRandomRoute() {
            int[] route = new int[numOfCities];
            List<Integer> cities = new ArrayList<>();
            for (int i = 0; i < numOfCities; i++) {
                cities.add(i);
            }
            Collections.shuffle(cities);
            for (int i = 0; i < numOfCities; i++) {
                route[i] = cities.get(i);
            }
            return route;
        }

        private void localSearch(Individual individual) {
//            double bestGain = 0;
//            boolean improved = true;
//            while (improved) {
//                improved = false;
//                for (int i = 0; i < numOfCities - 1; i++) {
//                    for (int j = i + 2; j < numOfCities; j++) {
//                        double currentDistance = citiesAbsDistanceTable[individual.getRoute()[i]][individual.getRoute()[i + 1]] +
//                                citiesAbsDistanceTable[individual.getRoute()[j]][individual.getRoute()[(j + 1) % numOfCities]];
//                        double newDistance = citiesAbsDistanceTable[individual.getRoute()[i]][individual.getRoute()[j]] +
//                                citiesAbsDistanceTable[individual.getRoute()[i + 1]][individual.getRoute()[(j + 1) % numOfCities]];
//                        double gain = currentDistance - newDistance;
//                        if (gain > bestGain) {
//                            individual.twoOptSwap(i + 1, j);
//                            bestGain = gain;
//                            improved = true;
//                        }
//                    }
//                }
//            }
            boolean improved = true;
            while (improved) {
                improved = false;
                for (int i = 0; i < numOfCities - 2; i++) {
                    for (int j = i + 1; j < numOfCities - 1; j++) {
                        for (int k = j + 1; k < numOfCities; k++) {
                            double currentDistance = citiesAbsDistanceTable[individual.getRoute()[i]][individual.getRoute()[i + 1]] +
                                    citiesAbsDistanceTable[individual.getRoute()[j]][individual.getRoute()[j + 1]] +
                                    citiesAbsDistanceTable[individual.getRoute()[k]][individual.getRoute()[(k + 1) % numOfCities]];

                            individual.threeOptSwap(i, j, k);
                            double newDistance = individual.getCumulateDistance();
//                            System.out.println("improvement: " + (currentDistance - newDistance));
                            if (newDistance < currentDistance) {
                                improved = true;
                            } else {
                                // Swap back to the original state as there was no improvement
                                individual.threeOptSwap(i, j, k);
//                                System.out.println("No improvement");
                            }
                        }
                    }
                }
            }

        }

        private double threeOptGain(Individual individual, int i, int j, int k) {
            int[] route = individual.getRoute();

            double oldDistance = citiesAbsDistanceTable[route[i - 1]][route[i]] +
                    citiesAbsDistanceTable[route[j - 1]][route[j]] +
                    citiesAbsDistanceTable[route[k - 1]][route[k]];

            double newDistance = citiesAbsDistanceTable[route[i - 1]][route[j]] +
                    citiesAbsDistanceTable[route[j - 1]][route[k]] +
                    citiesAbsDistanceTable[route[k - 1]][route[i]];

            return newDistance - oldDistance;
        }

        private List<int[]> train() {
            for (int i = 0; i < genNum; i++) {
                long timeSnapshot = System.currentTimeMillis();
                long timeElapsed = (timeSnapshot - beginTime) / 1000;
                if (numOfCities <= 50) {
                    if (timeElapsed > 56) {
                        break;
                    }
                } else if (numOfCities <= 100) {
                    if (timeElapsed > 71) {
                        break;
                    }
                } else if (numOfCities <= 200) {
                    if (timeElapsed > 115) {
                        break;
                    }
                } else {
                    if (timeElapsed > 193) {
                        break;
                    }
                }
//                if ((timeSnapshot - beginTime) / 1000 > 195) {
//                    break;
//                }
                List<Individual> newGen = mutate();
                cross(newGen);
                select();
                for (Individual individual : individualList) {
                    localSearch(individual);
                }
                findBestIndividual();
                int[] result = Arrays.copyOf(best.getRoute(), numOfCities + 1);
                result[numOfCities] = result[0];
                resultList.add(result);
                shortestRouteList.add(best.getCumulateDistance());
            }
            return resultList;
        }

        private void cross(List<Individual> newGen) {
            for (int i = 0; i < individualNum - 1; i += 2) {
                if (Math.random() < 1) {
                    int[] route1 = newGen.get(i).getRoute();
                    int[] route2 = newGen.get(i + 1).getRoute();
                    int index1 = new Random().nextInt(numOfCities - 1);
                    int index2 = index1 + new Random().nextInt(numOfCities - index1);
                    crossover(route1, route2, index1, index2);
                }
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
        }

        private List<Individual> mutate() {
            List<Individual> newGen = new ArrayList<>();
            Collections.shuffle(individualList);
            for (Individual individual : individualList) {
                if (Math.random() < mutateProb) {
                    int numMutations = (int) (numOfCities * Math.random());
                    int[] mutateIndividual = Arrays.copyOf(individual.getRoute(), numOfCities);
                    for (int i = 0; i < numMutations; i++) {
                        int index1 = new Random().nextInt(numOfCities - 1);
                        int index2 = index1 + new Random().nextInt(numOfCities - index1);
                        int temp = mutateIndividual[index1];
                        mutateIndividual[index1] = mutateIndividual[index2];
                        mutateIndividual[index2] = temp;
                    }
                    newGen.add(new Individual(mutateIndividual));

                } else {
                    int[] unchangedIndividual = Arrays.copyOf(individual.getRoute(), numOfCities);
                    newGen.add(new Individual(unchangedIndividual));
                }
            }
            return newGen;
        }

        private void select() {
            int groupNum = 10;
            int groupSize = individualNum / groupNum * 2;
            int groupWinner = individualNum / groupNum;

            List<Individual> winners = new ArrayList<>();

            for (int i = 0; i < groupNum; i++) {
                List<Individual> group = new ArrayList<>();
                for (int j = 0; j < groupSize; j++) {
                    Individual player = individualList.get(new Random().nextInt(individualList.size()));
                    player = new Individual(player.getRoute());
                    group.add(player);
                }
                group.sort(Comparator.comparingDouble(Individual::getCumulateDistance));
                winners.addAll(group.subList(0, groupWinner));
            }
            individualList = winners;
        }

        private void findBestIndividual() {
            for (Individual individual : individualList) {
                if (individual.getCumulateDistance() < best.getCumulateDistance()) {
                    best = individual;
                }
            }
        }
    }
}


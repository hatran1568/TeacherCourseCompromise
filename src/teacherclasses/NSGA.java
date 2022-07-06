/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teacherclasses;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author ACER
 */
public class NSGA {

    public Data data;

    public NSGA(Data data) {
        this.data = data;
    }

    public void fast_nondominated_sort(Population population) {
        population.fronts = new ArrayList<ArrayList<Solution>>();
        ArrayList<Solution> lst = new ArrayList<Solution>();
        for (Solution individual : population.population) {
            individual.domination_count = 0;
            individual.dominated_solution = new ArrayList<Solution>();

            for (Solution other_individual : population.population) {
                if (individual.dominates(other_individual)) {
                    individual.dominated_solution.add(other_individual);
                } else {
                    if (other_individual.dominates(individual)) {
                        individual.domination_count++;
                    }
                }
            }

            if (individual.domination_count == 0) {
                individual.rank = 0;
                lst.add(individual);

            }

        }
        population.fronts.add(0, lst);

        int i = 0;
        while (population.fronts.get(i).size() > 0) {
            lst = new ArrayList<Solution>();
            for (Solution individual : population.fronts.get(i)) {
                for (Solution other_individual : individual.dominated_solution) {

                    other_individual.domination_count--;

                    if (other_individual.domination_count == 0) {
                        other_individual.rank = i + 1;
                        lst.add(other_individual);
                    }
                }
                i++;
                population.fronts.add(i, lst);
            }
        }
    }

    public void calculate_crowding_distance(ArrayList<Solution> front) {
        if (front.size() > 0) {
            int solution_num = front.size();

            for (Solution individual : front) {
                individual.crowding_distance = 0;
                individual.cal_objs(data);
            }

            for (int i = 0; i < front.get(0).objectives.length; i++) {
                final int idx = i;
                front.sort((o1, o2) -> {
                    int flag = 0;
                    if (o1.objectives[idx] < o2.objectives[idx]) {
                        flag = -1;
                    }
                    if (o1.objectives[idx] > o2.objectives[idx]) {
                        flag = 1;
                    }
                    return flag;
                });

                front.get(0).crowding_distance = Math.pow(10, 9);
                front.get(solution_num - 1).crowding_distance = Math.pow(10, 9);

                double max = -1;
                double min = -1;
                for (int j = 0; j < front.size(); j++) {
                    double curr = front.get(j).objectives[i];
                    if ((max == -1) || (curr > max)) {
                        max = curr;
                    }

                    if ((min == -1) || (curr < min)) {
                        min = curr;
                    }
                }

                double scale = max - min;
                if (scale == 0) {
                    scale = 1;
                }
                for (int j = 1; j < solution_num - 1; j++) {
                    front.get(j).crowding_distance += (front.get(j + 1).objectives[idx] - front.get(j - 1).objectives[idx]) / scale;
                }

            }
        }
    }

    public int crowding_operator(Solution individual, Solution other_individual) {
        if ((individual.rank < other_individual.rank) || ((individual.rank == other_individual.rank) && (individual.crowding_distance > other_individual.crowding_distance))) {
            return 1;
        } else {
            return 0;
        }
    }

    public ArrayList<Solution> create_children(Population population) {
        ArrayList<Solution> children = new ArrayList<Solution>();

        while (children.size() < population.population.size()) {
            Solution parent1 = _selection(population);
            Solution parent2 = parent1;
            while (parent2 == parent1) {
                parent2 = _selection(population);
            }

            Solution child;
            if (Math.random() < 0.9) {
                child = _crossover(parent1, parent2);
            } else {
                child = new Solution(data);
                child.chromosome = parent1.chromosome;
            }

            if (Math.random() < 0.1) {
                _mutate(child);
            }

            children.add(child);
        }
        return children;
    }

    public Solution _crossover(Solution Mom, Solution Dad) {
        Solution child = new Solution(data);

        for (int i = 0; i < data.M / 2; i++) {
            for (int j = 0; j < data.N; j++) {
                child.chromosome[i][j] = Dad.chromosome[i][j];
                child.chromosome[data.M - i - 1][j] = Mom.chromosome[data.M - i - 1][j];
            }
        }
        child.chromosome[data.M / 2] = Mom.chromosome[data.M / 2];
        return child;
    }

    public void _mutate(Solution s) {

        int maximum = data.N;
        int minimum = 0;
        int range = maximum - minimum;
        int randomNum;

        Random rn = new Random();
        randomNum = rn.nextInt(range) + minimum;
        int randomNum2;
        do {
            randomNum2 = rn.nextInt(range) + minimum;
        } while (randomNum2 == randomNum);
        int tmpRow[] = s.chromosome[randomNum];
        s.chromosome[randomNum] = s.chromosome[randomNum2];
        s.chromosome[randomNum2] = tmpRow;

    }

    public Solution _selection(Population population) {
        Solution best = null;
        for (int x = 0; x < 10; ++x) {
            int c = (int) (Math.random() * population.population.size());
            if ((best == null)) {
                best = population.population.get(c);
            } else {
                if ((crowding_operator(population.population.get(c), best) == 1) && (Math.random() < 0.9)) {
                    best = population.population.get(c);
                }
            }
        }
        return best;
    }
}

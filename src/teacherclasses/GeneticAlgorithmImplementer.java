/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package teacherclasses;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author saplab
 */
public class GeneticAlgorithmImplementer {

    Data data;
    NSGA utils;
    Population population;

    public GeneticAlgorithmImplementer(Data data) {
        this.data = data;
        utils = new NSGA(data);
        population = new Population(data);
    }

    public ArrayList<Solution> Search() {
        this.population = new Population(data);
        for (int i = 0; i < 500; i++) {
            Solution s = generateSolution();
//            while (s.check_Solution(data) == false) {
//                s = generateSolution();
//            }
            population.add(s);
        }
        utils.fast_nondominated_sort(population);
        for (ArrayList<Solution> front : population.fronts) {
            utils.calculate_crowding_distance(front);
        }
        ArrayList<Solution> children = new ArrayList<>();
        children = utils.create_children(population);
        Population return_population = null;
        for (int i = 0; i < 20; i++) {
            System.out.println(i);
            population.extend(children);
            utils.fast_nondominated_sort(population);
            Population new_population = new Population(data);
            int front_num = 0;
            while (new_population.population.size() + population.fronts.get(front_num).size() < 500) {
                utils.calculate_crowding_distance(population.fronts.get(front_num));
                new_population.extend(population.fronts.get(front_num));
                front_num++;
//                System.out.println(population.fronts.get(front_num).size()+" "+ front_num);
            }
            utils.calculate_crowding_distance(population.fronts.get(front_num));
            population.fronts.get(front_num).sort((o1, o2) -> {
                int flag = 0;
                if (o1.crowding_distance < o2.crowding_distance) {
                    flag = 1;
                }
                if (o1.crowding_distance > o2.crowding_distance) {
                    flag = -1;
                }
                return flag;
            });
            int remainSolution = 500 - new_population.population.size();
            for (int j = 0; j < remainSolution; j++) {
                new_population.add(population.fronts.get(front_num).get(j));
            }
            return_population = population;
            population = new_population;
            utils.fast_nondominated_sort(population);
            for (ArrayList<Solution> front : population.fronts) {
                utils.calculate_crowding_distance(front);
            }

            children = utils.create_children(population);
        }
        return return_population.fronts.get(0);
    }

    public Solution generateSolution() {
        int[] currentSlots = new int[data.N];

        int maximum = data.N;
        int minimum = 0;
        int range = maximum - minimum;
        int randomNum;
        Random rn = new Random();
        Solution s = new Solution(data);
        for (int i = 0; i < data.M; i++) {
            DistributedRandomNumberGenerator rnd = new DistributedRandomNumberGenerator();
            for (int j = 0; j < data.N; j++) {
                int sumSlot = 0;

                if (data.Rating[j][data.courses[i].getSubject()] > 0 && data.FSlot[j][data.courses[i].getSlot()] > 0 && data.FSub[j][data.courses[i].getSubject()] > 0) {
                    ArrayList<Integer> slot = new ArrayList<>();
                    for (int k = 0; k <= i; k++) {
                        if (s.chromosome[k][j] == 1) {
                            sumSlot++;
                            slot.add(data.courses[k].getSlot());
                        }
                    }
                    if (slot.contains(data.courses[i].getSlot()) == false && sumSlot + 1 <= data.teachers[j].getMaxClass()) {
                        rnd.addNumber(j, 0.1);
                    }

                }

            }
            //randomNum = rn.nextInt(range) + minimum;
            randomNum = rnd.getDistributedRandomNumber();
            s.chromosome[i][randomNum] = 1;
        }
        return s;
    }

    public Solution Crossover(Solution Mom, Solution Dad) {
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

    public Solution Mutate(Solution s) {
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
        return s;
    }

    public ArrayList<Solution> implementGA() {
        ArrayList<Solution> result = new ArrayList<>();
        ArrayList<Solution> current_generation = new ArrayList<>();
        ArrayList<Solution> next_generation = new ArrayList<>();
        int maximum = 500;
        int minimum = 0;
        int mutation_minimum = 25;
        int range = maximum - minimum;
        int randomNum;
        Random rn = new Random();

        int randomNum2;
        //Generate 1st gen
        for (int i = 0; i < 500; i++) {
            current_generation.add(generateSolution());
        }

        //sort 1st gen
        Collections.sort(current_generation, new Comparator<Solution>() {
            @Override
            public int compare(Solution o1, Solution o2) {

                return Double.compare(o1.cal_Fitness(data), o2.cal_Fitness(data));
            }
        });

        result.add(current_generation.get(0));
        for (int j = 0; j < 500; j++) {
            //Selection
            for (int i = 0; i < 50; i++) {
                next_generation.add(current_generation.get(i));
            }
            //Crossover
            for (int i = 0; i < 450; i++) {
                randomNum = rn.nextInt(range) + minimum;
                do {
                    randomNum2 = rn.nextInt(range) + minimum;
                } while (randomNum2 == randomNum);
                next_generation.add(Crossover(current_generation.get(randomNum), current_generation.get(randomNum2)));
            }
            //mutation
            for (int i = 0; i < 50; i++) {
                randomNum = rn.nextInt(range - 25) + mutation_minimum;
                next_generation.set(randomNum, Mutate(next_generation.get(randomNum)));
                randomNum = rn.nextInt(range - 25) + mutation_minimum;
                next_generation.set(randomNum, Mutate(next_generation.get(randomNum)));
            }
            Collections.sort(next_generation, new Comparator<Solution>() {
                @Override
                public int compare(Solution o1, Solution o2) {

                    return Double.compare(o1.cal_Fitness(data), o2.cal_Fitness(data));
                }

            });
            result.add(next_generation.get(0));
            current_generation.clear();
            current_generation.addAll(next_generation);
            next_generation.clear();
        }

        return result;
    }

    public static void writeSolution(ArrayList<Solution> solutions, Data data) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet1");

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Fitness");

        int rowCount = 0;

        for (Solution s : solutions) {
            row = sheet.createRow(++rowCount);
            cell = row.createCell(0);
            cell.setCellValue(s.cal_Fitness(data));

        }
        try (FileOutputStream outputStream = new FileOutputStream("Fitness.xlsx")) {
            workbook.write(outputStream);
            outputStream.close();
        }
    }

    public static void writeErrCourseToExcel(Solution solution, Data data) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet2");
        for (int i = 0; i < data.N; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(i);
            cell = row.createCell(1);
            cell.setCellValue(solution.cal_Err_Courses_PJ(data, i));
        }
        try (FileOutputStream outputStream = new FileOutputStream("Expectation.xlsx")) {
            workbook.write(outputStream);
            outputStream.close();
        }
    }

    public static void writeSolutionAsTimetable(Solution solution, Data data) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        for (int i = 0; i < data.N; i++) {

            XSSFSheet sheet = workbook.createSheet(i + "");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(1);
            cell.setCellValue("Monday");

            cell = row.createCell(2);
            cell.setCellValue("Tuesday");

            cell = row.createCell(3);
            cell.setCellValue("Wednesday");

            cell = row.createCell(4);
            cell.setCellValue("Thurday");

            cell = row.createCell(5);
            cell.setCellValue("Friday");

            for (int k = 1; k < 7; k++) {
                row = sheet.createRow(k);
                row.setHeight((short) 800);
                cell = row.createCell(0);
                cell.setCellValue(k);
            }

            for (int j = 0; j < data.M; j++) {
                if (solution.chromosome[j][i] == 1) {
                    String cellContent = data.courses[j].getSubjectName() + "\n" + data.courses[j].getClasses() + "\n" + data.courses[j].getRoom();
                    if (data.courses[j].getSlot() == 0) {
                        cell = sheet.getRow(1).createCell(1);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(1).createCell(3);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(1).createCell(5);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 1) {
                        cell = sheet.getRow(2).createCell(1);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(2).createCell(3);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(2).createCell(5);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 2) {
                        cell = sheet.getRow(3).createCell(1);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(3).createCell(3);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(3).createCell(5);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 3) {
                        cell = sheet.getRow(4).createCell(1);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(4).createCell(3);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(4).createCell(5);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 4) {
                        cell = sheet.getRow(5).createCell(1);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(5).createCell(3);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(5).createCell(5);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 5) {
                        cell = sheet.getRow(6).createCell(1);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(6).createCell(3);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(6).createCell(5);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 6) {
                        cell = sheet.getRow(1).createCell(2);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(2).createCell(2);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(1).createCell(4);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 7) {
                        cell = sheet.getRow(3).createCell(2);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(2).createCell(4);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(3).createCell(4);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 8) {
                        cell = sheet.getRow(4).createCell(2);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(5).createCell(2);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(4).createCell(4);
                        cell.setCellValue(cellContent);
                    }
                    if (data.courses[j].getSlot() == 9) {
                        cell = sheet.getRow(6).createCell(2);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(5).createCell(4);
                        cell.setCellValue(cellContent);
                        cell = sheet.getRow(6).createCell(4);
                        cell.setCellValue(cellContent);
                    }
                }
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream("Schedule.xlsx")) {
            workbook.write(outputStream);
            outputStream.close();
        }

    }
}

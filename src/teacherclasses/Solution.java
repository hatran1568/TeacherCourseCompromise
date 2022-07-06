/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package teacherclasses;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author saplab
 */
public class Solution {

    public int rank;
    public int domination_count;
    public ArrayList<Solution> dominated_solution;    
    public double crowding_distance = 0;
    public int chromosome[][];
    public Data data;
    public boolean all_Courses_Contraints;
    public boolean single_Teacher_Courses_Contraints;
    public boolean single_Slot_Contraints;
    public boolean inRange_Slot_Contraints;
    public boolean student_Rating_Constraint;
    public boolean self_Rating_Constraint;
    public boolean slot_Rating_Constraint;
    public double objectives[];
    public ArrayList<Integer> sum = new ArrayList<>();

    public int[][] getChromosome() {
        return chromosome;
    }

    public void setChromosome(int[][] chromosome) {
        this.chromosome = chromosome;
    }

    public Solution(Data data) {
        all_Courses_Contraints = true;
        single_Slot_Contraints = true;
        single_Teacher_Courses_Contraints = true;
        inRange_Slot_Contraints = true;
        student_Rating_Constraint = true;
        self_Rating_Constraint = true;
        slot_Rating_Constraint = true;
        chromosome = new int[data.M][data.N];
        this.data = data;
        for (int i = 0; i < data.M; i++) {
            for (int j = 0; j < data.N; j++) {
                chromosome[i][j] = 0;
            }
        }
    }
    public void cal_objs(Data data){
        objectives = new double[6];
        objectives[0] = cal_Quality_P0(data);
        objectives[1] = cal_Salary_P0(data);
        objectives[2] = cal_Favourite_Subs_All_PJ(data);
        objectives[3] = cal_Favourite_Slots_All_PJ(data);
        objectives[4] = cal_Periods_All_PJ(data);
        objectives[5] = cal_Err_Courses_All_PJ(data);
        
    }

    public double cal_Quality_P0(Data data) {
        double quality = 0;
        for (int i = 0; i < data.M; i++) {
            for (int j = 0; j < data.N; j++) {
                quality += chromosome[i][j] * data.Rating[j][data.courses[i].getSubject()];
            }
        }
        return Data.MAX_QUALITY_P0 - quality;
    }

    public double cal_Salary_P0(Data data) {
        double salary = 0;
        for (int i = 0; i < data.M; i++) {
            for (int j = 0; j < data.N; j++) {
                salary += chromosome[i][j] * data.teachers[j].getSalaryLevel();
            }
        }
        return salary;
    }

    public double cal_Payoff_P0(Data data) {
        double payoff;
        payoff = data.w1 * cal_Quality_P0(data) / (data.w2 * cal_Salary_P0(data));
        return payoff;
    }

    public double cal_Favourite_Subs_PJ(Data data, int teacher) {
        double favourite_Subs = 0;
        for (int i = 0; i < data.M; i++) {
            favourite_Subs += chromosome[i][teacher] * data.FSub[teacher][i];
        }
        return favourite_Subs;
    }

    public double cal_Favourite_Subs_All_PJ(Data data) {
        double favourite_Subs = 0;
        for (int i = 0; i < data.N; i++) {
            favourite_Subs += cal_Favourite_Subs_PJ(data, i);
        }
        return Data.MAX_FAVORITE_SUBS_PJ- favourite_Subs;
    }

    public double cal_Favourite_Slots_All_PJ(Data data) {
        double favoriteSlots = 0;
        for (int i = 0; i < data.N; i++) {
            favoriteSlots += cal_Favourite_Slots_PJ(data, i);
        }
        return Data.MAX_FAVORITE_SLOTS_PJ- favoriteSlots;
    }

    public double cal_Favourite_Slots_PJ(Data data, int teacher) {
        double favourite_Slots = 0;
        for (int i = 0; i < data.M; i++) {
            favourite_Slots += chromosome[i][teacher] * data.FSlot[teacher][i];
        }
        return  favourite_Slots;
    }

    public double cal_Err_Courses_All_PJ(Data data) {
        double errCourses = 0;
        for (int i = 0; i < data.N; i++) {
            errCourses += cal_Err_Courses_PJ(data, i);
        }
        return errCourses;
    }

    public double cal_Err_Courses_PJ(Data data, int teacher) {
        double Err_Courses = 0;
        for (int i = 0; i < data.M; i++) {
            Err_Courses += chromosome[i][teacher];
        }
        Err_Courses = data.teachers[teacher].getIdealClass() - Err_Courses;
        return Math.abs(Err_Courses);
    }

    public double cal_Periods_All_PJ(Data data) {
        double periods = 0;
        for (int i = 0; i < data.N; i++) {
            periods += cal_Periods_PJ(data, i);
        }
        return periods;
    }

    public double cal_Periods_PJ(Data data, int teacher) {

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < data.M; i++) {
            if (chromosome[i][teacher] == 1) {
                list.add(data.courses[i].getSlot());
            }
        }
        Collections.sort(list);
        int t = 1;
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) - 1 != list.get(i - 1)) {
                t++;
            }
        }

        return t;

    }

    public double cal_Payoff_PJ(Data data, int teacher) {
        double payoff = (data.w3 * cal_Favourite_Subs_PJ(data, teacher) + data.w4 * cal_Favourite_Slots_PJ(data, teacher)) / (data.w5 * cal_Err_Courses_PJ(data, teacher) + data.w6 * cal_Periods_PJ(data, teacher));
        return payoff;
    }

    public double cal_Payoff_All_PJ(Data data) {
        double payoff = 0;
        for (int i = 0; i < data.N; i++) {
            payoff += cal_Payoff_PJ(data, i);
        }
        return payoff;
    }

    public double cal_Fitness(Data data) {
        double fitness = 0;
        fitness += data.w1 * Math.pow(((cal_Salary_P0(data)-Data.MIN_SALARY_P0)/(Data.MAX_SALARY_P0-Data.MIN_SALARY_P0)),2); 
        fitness += data.w2 * Math.pow(((Data.MAX_QUALITY_P0-cal_Quality_P0(data))/(Data.MAX_QUALITY_P0-Data.MIN_QUALITY_P0)),2);
        fitness += data.w3 * Math.pow(((Data.MAX_FAVORITE_SUBS_PJ-cal_Favourite_Subs_All_PJ(data))/(Data.MIN_FAVORITE_SUBS_PJ-Data.MAX_FAVORITE_SUBS_PJ)),2);
        fitness += data.w4 * Math.pow(((Data.MAX_FAVORITE_SLOTS_PJ-cal_Favourite_Slots_All_PJ(data))/(Data.MAX_FAVORITE_SLOTS_PJ-Data.MIN_FAVORITE_SLOTS_PJ)),2);
        fitness += data.w5 * Math.pow(((cal_Err_Courses_All_PJ(data)-Data.MIN_ERR_COURSES_PJ)/(Data.MIN_ERR_COURSES_PJ-Data.MAX_ERR_COURSES_PJ)),2);
        fitness += data.w6 * Math.pow(((Data.MIN_PERIODS_PJ-cal_Periods_All_PJ(data))/(Data.MAX_PERIODS_PJ-Data.MIN_PERIODS_PJ)), 2);
        fitness = Math.sqrt(fitness);
        if(check_Solution(data)==false){
            fitness = fitness * 10000;
        }
        return fitness;
    }

    public boolean checkAllCourseConstraint(Data data) {
        int sumSlot = 0;
        for (int i = 0; i < data.M; i++) {
            for (int j = 0; j < data.N; j++) {
                sumSlot += chromosome[i][j];
            }
        }

        if (sumSlot == data.M) {
            //System.out.println("Not all courses assigned");
            all_Courses_Contraints = true;
            return true;
        }
        return false;
    }

    public boolean checkSingleTeacherCourseConstraint(Data data) {
        for (int i = 0; i < data.M; i++) {
            int sum2 = 0;
            for (int j = 0; j < data.N; j++) {
                sum2 += chromosome[i][j];
            }
            if (sum2 != 1) {
                //System.out.println("courses have 2 teacher");
                single_Teacher_Courses_Contraints = false;
                return false;
            }
            sum2 = 0;
        }
        return true;
    }

    public boolean checkSingleSlotConstraint(Data data) {
        for (int i = 0; i < data.N; i++) {
            ArrayList<Integer> list = new ArrayList<>();
            for (int j = 0; j < data.M; j++) {
                if (chromosome[j][i] == 1) {
                    list.add(data.courses[j].getSlot());
                }
            }
            Collections.sort(list);

            for (int k = 1; k < list.size(); k++) {
                if (list.get(k) == list.get(k - 1)) {
                    //System.out.println("teacher have 2 slot at same time");
                    single_Slot_Contraints = false;
                    return false;
                }
            }
            list.clear();
        }
        return true;
    }

    public boolean checkInRangeSlotConstraint(Data data) {
        for (int i = 0; i < data.N; i++) {
            int sum3 = 0;
            for (int j = 0; j < data.M; j++) {
                if (chromosome[j][i] == 1) {
                    sum3 += chromosome[j][i];
                }
            }
            sum.add(sum3);
            if (sum3 > data.teachers[i].getMaxClass() || sum3 < data.teachers[i].getMinClass()) {
                //System.out.println("Number of slot not in range");
                inRange_Slot_Contraints = false;
                return false;
            }
            sum3 = 0;
        }
        return true;
    }

    public boolean checkStudentRatingConstraint(Data data) {
        for (int i = 0; i < data.M; i++) {
            for (int j = 0; j < data.N; j++) {
                if (chromosome[i][j] == 1 && data.Rating[j][data.courses[i].getSubject()] < 1) {
                    student_Rating_Constraint = false;
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkSelfRatingConstaint(Data data) {
        for (int i = 0; i < data.M; i++) {
            for (int j = 0; j < data.N; j++) {
                if (chromosome[i][j] == 1 && data.FSub[j][data.courses[i].getSubject()] < 1) {
                    self_Rating_Constraint = false;
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkSlotRatingConstraint(Data data) {
        for (int i = 0; i < data.M; i++) {
            for (int j = 0; j < data.N; j++) {
                if (chromosome[i][j] == 1 && data.FSlot[j][data.courses[i].getSlot()] < 1) {
                    slot_Rating_Constraint = false;
                    return false;
                }
            }
        }
        return true;
    }

    public boolean check_Solution(Data data) {
        checkAllCourseConstraint(data);
        checkInRangeSlotConstraint(data);
        checkSelfRatingConstaint(data);
        checkSingleSlotConstraint(data);
        checkSingleTeacherCourseConstraint(data);
        checkSlotRatingConstraint(data);
        checkStudentRatingConstraint(data);
        return checkAllCourseConstraint(data) && checkInRangeSlotConstraint(data)
                && checkSingleSlotConstraint(data) && checkSingleTeacherCourseConstraint(data)
                && checkStudentRatingConstraint(data) && checkSelfRatingConstaint(data)
                && checkSlotRatingConstraint(data);
    }
    
    public boolean dominates(Object o) {

        if (!(o instanceof Solution)) {
            return false;
        }

        Solution other = (Solution) o;
        boolean and_condition = true;
        boolean or_condition = false;
        cal_objs(data);
        other.cal_objs(data);
        for(int i =0;i<6;i++){
            and_condition = and_condition && this.objectives[i]<=other.objectives[i];
            or_condition = or_condition || this.objectives[i] < other.objectives[i];
        }
        return (and_condition && or_condition);
    }

}

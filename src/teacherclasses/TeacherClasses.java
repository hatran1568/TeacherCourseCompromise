/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package teacherclasses;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author saplab
 */
public class TeacherClasses {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Data data = Data.readDataFromFile();
        GeneticAlgorithmImplementer Ga = new GeneticAlgorithmImplementer(data);
        
        ArrayList<Solution> result = Ga.Search();;
        for (Solution solution : result) {
            System.out.println(solution.all_Courses_Contraints);
            System.out.println(solution.single_Teacher_Courses_Contraints);
            System.out.println(solution.single_Slot_Contraints);
            System.out.println(solution.inRange_Slot_Contraints);
            System.out.println(solution.student_Rating_Constraint);
            System.out.println(solution.self_Rating_Constraint);
            System.out.println(solution.slot_Rating_Constraint);

            
            System.out.println(solution.cal_Fitness(data));
        }
        GeneticAlgorithmImplementer.writeSolutionAsTimetable(result.get(result.size()-1), data);
        GeneticAlgorithmImplementer.writeErrCourseToExcel(result.get(result.size()-1), data);
        GeneticAlgorithmImplementer.writeSolution(result, data);
    }

}

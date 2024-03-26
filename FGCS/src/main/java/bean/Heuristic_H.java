package bean;

import bean.entity.Task;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Heuristic_H {

//    Integer ORG=0;      //Original Order
//
//    Integer SRF=1;		// Smallest Requirement First
//
//    Integer LRF=2;      // Largest Requirement First
//
//    Integer SDF=3;     // Smallest Data First
//
//    Integer LDF=4;     //  Largest Data First
//
//    Integer SRD=5;    //  Smallest Requirement-Data-Ratio First
//
//    Integer LRD=6; 	 // Largest Requirement-Data-Ratio First


    public static List<Task> exe(List<Task> givenTaskList, Integer H) {
        if (H == 1){
            givenTaskList.sort(Comparator.comparing(Task::getR));
        }else if (H == 2){
            givenTaskList.sort(Comparator.comparing(Task::getR).reversed());
        }else if (H == 3){
            givenTaskList.sort(Comparator.comparing(Task::getD));
        }else if (H == 4){
            givenTaskList.sort(Comparator.comparing(Task::getD).reversed());
        }else if (H == 5){
            givenTaskList.sort(Comparator.comparing(Task::getRd));
        }else if (H == 6) {
            givenTaskList.sort(Comparator.comparing(Task::getRd).reversed());
        }
        return givenTaskList;
    }

    public static String getName(Integer H){
        if (H == 1){
            return "SRF";
        }else if (H == 2){
            return "LRF";
        }else if (H == 3){
            return "SDF";
        }else if (H == 4){
            return "LDF";
        }else if (H == 5){
            return "SRD";
        }else if (H == 6) {
            return "LRD";
        }
        return "ORG";
    }

    public static int getNumber(String H){
        if (Objects.equals(H, "SRF")){
            return 1;
        }else if (Objects.equals(H, "LRF")){
            return 2;
        }else if (Objects.equals(H, "SDF")){
            return 3;
        }else if (Objects.equals(H, "LDF")){
            return 4;
        }else if (Objects.equals(H, "SRD")){
            return 5;
        }else if (Objects.equals(H, "LRD")) {
            return 6;
        }
        return 7;
    }


}

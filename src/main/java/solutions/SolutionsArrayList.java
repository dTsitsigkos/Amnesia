/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solutions;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.ArrayList;
import jsoninterface.View;

/**
 *
 * @author jimakos
 */
public class SolutionsArrayList {
    @JsonView(View.Solutions.class)
    ArrayList<Solutions> solutions = null;
    @JsonView(View.Solutions.class)
    double pagePercentage = 0;
    @JsonView(View.Solutions.class)
    boolean suppress = false;
    @JsonView(View.Solutions.class)
    double percentageSuppress =0.0;
    @JsonView(View.Solutions.class)
    int k = 0;
    
    public SolutionsArrayList(){
        solutions = new ArrayList<Solutions>();
    }

    public ArrayList<Solutions> getSolutions() {
        return solutions;
    }

    public double getPagePercentange() {
        return pagePercentage;
    }

    public boolean isSuppress() {
        return suppress;
    }

    public double getPercentangeSuppress() {
        return percentageSuppress;
    }

    public int getK() {
        return k;
    }
    

    public void setSolutions(ArrayList<Solutions> solutions) {
        this.solutions = solutions;
    }

    public void setPagePercentange(double pagePercentange) {
        this.pagePercentage = pagePercentange;
    }
    
    public void addNewSolution( Solutions sol){
        solutions.add(sol);
    }


    public void setSuppress(boolean suppress) {
        this.suppress = suppress;
    }

    public void setPercentangeSuppress(double percentangeSuppress) {
        this.percentageSuppress = percentangeSuppress;
    }

    public void setK(int k) {
        this.k = k;
    }
    
}

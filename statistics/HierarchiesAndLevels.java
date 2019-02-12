/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

/**
 *
 * @author jimakos
 */
public class HierarchiesAndLevels {
    private String hierarchyName = null;
    private String level = null;
    
    public HierarchiesAndLevels(String _hierName, String _level){
        hierarchyName = _hierName;
        level = _level;
    }

    public String getHierarchyName() {
        return hierarchyName;
    }

    public String getLevel() {
        return level;
    }

    public void setHierarchyName(String hierarchyName) {
        this.hierarchyName = hierarchyName;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    
    
}

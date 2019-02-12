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
public class ColumnsNamesAndTypes {
    private String columnName = null;
    private String type = null;
    
    public ColumnsNamesAndTypes(String _columnName, String _type){
        this.columnName = _columnName;
        this.type = _type;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getType() {
        return type;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setType(String type) {
        this.type = type;
    }  
    
}

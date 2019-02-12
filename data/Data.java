/* 
 * Copyright (C) 2015 "IMIS-Athena R.C.",
 * Institute for the Management of Information Systems, part of the "Athena" 
 * Research and Innovation Centre in Information, Communication and Knowledge Technologies.
 * [http://www.imis.athena-innovation.gr/]
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 */
package data;

import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import dictionary.DictionaryString;
import hierarchy.Hierarchy;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;


/**
 * Interface of data
 * @author serafeim
 */
public interface Data {
    public double[][] getDataSet();
    public void setData(double[][] _data);
    public int getDataLenght();
    public void print();
    public String save(boolean[] checkColumns);
    public void preprocessing();
    public String readDataset(String[] columnTypes, boolean [] checkColumns);
    public void export(String file, Object[][] initialTable, Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues);
    public Map <Integer,String> getColNamesPosition();
    public Map <Integer,DictionaryString> getDictionary();

    public DictionaryString getDictionary(Integer column);
    public void setDictionary(Integer column, DictionaryString dict);
    public int getColumnByName(String column);
    public String getColumnByPosition(Integer columnIndex);
    public void replaceColumnDictionary(Integer column, DictionaryString dict);
    public void SaveClmnsAndTypeOfVar(String[] columnTypes,boolean[] checkColumns);
    public String findColumnTypes();
    public String[][] getSmallDataSet();
    public ArrayList<LinkedHashMap> getPage(int pageNum, int numOfRecords);
    public String[][] getTypesOfVariables(String [][]smallDataSet);
    public int getRecordsTotal();
    public Map<Integer, String> getColNamesType();
    public String getInputFile();

}

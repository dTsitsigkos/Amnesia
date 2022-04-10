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

import exceptions.LimitException;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import dictionary.DictionaryString;
import exceptions.DateParseException;
import exceptions.NotFoundValueException;
import hierarchy.Hierarchy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;


/**
 * Interface of data
 * @author serafeim
 */
public interface Data {
    public int online_rows = 5000;
    public String online_version = "onlinefasdfr";
    public double[][] getDataSet();
    public void setData(double[][] _data);
    public int getDataLenght();
    public int getDataColumns();
    public void print();
    public void exportOriginalData();
    public String save(boolean[] checkColumns) throws LimitException,DateParseException, NotFoundValueException;
    public void preprocessing() throws LimitException;
    public String readDataset(String[] columnTypes, boolean [] checkColumns) throws LimitException,DateParseException, NotFoundValueException;
    public void export(String file, Object[][] initialTable, Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues);
    public Map <Integer,String> getColNamesPosition();
//    public Map <Integer,DictionaryString> getDictionary();

    public DictionaryString getDictionary();
//    public void setDictionary(Integer column, DictionaryString dict);
    public int getColumnByName(String column);
    public String getColumnByPosition(Integer columnIndex);
    public String[] getColumnNames();
//    public void replaceColumnDictionary(Integer column, DictionaryString dict);
    public void SaveClmnsAndTypeOfVar(String[] columnTypes,boolean[] checkColumns);
    public String findColumnTypes();
    public String[][] getSmallDataSet();
    public ArrayList<LinkedHashMap> getPage(int pageNum, int numOfRecords);
    public String[][] getTypesOfVariables(String [][]smallDataSet);
    public int getRecordsTotal();
    public Map<Integer, String> getColNamesType();
    public String getInputFile();
    public SimpleDateFormat getDateFormat(int column);
    public void setMask(int column, int[] positions, char character, String option);
    public void setRegex(int column,  char character, String regex);
    public void computeInformationLossMetrics(Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues);
    public Map<String,Double> getInformationLoss();

}

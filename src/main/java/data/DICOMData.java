/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import anonymizeddataset.AnonymizedDataset;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import dictionary.DictionaryString;
import exceptions.DateParseException;
import exceptions.LimitException;
import exceptions.NotFoundValueException;
import hierarchy.Hierarchy;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import jsoninterface.View;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomOutputStream;
import com.pixelmed.display.SourceImage;
import controller.AppCon;
import static data.Data.online_rows;
import static data.Data.online_version;
import hierarchy.distinct.HierarchyImplString;
import hierarchy.ranges.HierarchyImplRangesDate;
import hierarchy.ranges.RangeDate;
import hierarchy.ranges.RangeDouble;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author dimak
 */
public class DICOMData implements Data {
    @JsonView(View.GetColumnNames.class)
    private String inputFile = "DICOM files";
    private String inputpath = null;
    private double dataSet[][] = null;
    private int sizeOfRows = 0;
    private int sizeOfCol = 5;
    private String[] formatsDate = null;
    private DictionaryString dictionary = null;
    private DictionaryString dictHier = null;
    
    @JsonView(View.GetDataTypes.class)
    private Map <Integer,String> colNamesType = null;
    private CheckVariables chVar = null;
    private Map <Integer,String> colNamesPosition = null;
    
    @JsonView(View.GetColumnNames.class)
    private String []columnNames = {"PatientID","PatientName","PatientAge","Modality","PatientSex","PatientBirthDate","PhotometricInterpretation","BodyPartExamined",
        "PatientOrientation","ViewPosition","ConversionType","SamplesPerPixel"};
    private AttributeTag []dcmAttributes = null;
    
    @JsonView(View.SmallDataSet.class)
    private String[][] smallDataSet;
    @JsonView(View.DataSet.class)
    private ArrayList<LinkedHashMap> data;
    @JsonView(View.SmallDataSet.class)
    private String[][] typeArr;
    @JsonView(View.GetDataTypes.class)
    boolean pseudoanonymized = false;
    @JsonView(View.GetDataTypes.class)
    Map<Integer,String> biggerSample = null;
    
    @JsonView(View.DataSet.class)
    private int recordsTotal;
    @JsonView(View.DataSet.class)
    private int recordsFiltered;
    
    private AttributeList [] dcmInfo = null;
    private String []fileNames = null;
    
    private Map<String,Double> informationLoss;
    
    public DICOMData(String ip, DictionaryString dict){
        this.inputpath = ip;
        this.dictHier = dict;
        chVar = new CheckVariables();
        dictionary = new DictionaryString();
        colNamesType = new TreeMap<Integer,String>();
        this.informationLoss = new HashMap();
        this.biggerSample = new HashMap();
        
        File folder = new File(this.inputpath);
//        this.sizeOfRows = folder.listFiles().length;
        for(File f: folder.listFiles()){
            if(f.getName().endsWith(".dcm")){
                this.sizeOfRows++;
            }
        }
        this.dcmInfo = new AttributeList[this.sizeOfRows];
        this.fileNames = new String[this.sizeOfRows];
        this.dcmAttributes = new AttributeTag[this.columnNames.length];
        colNamesPosition = new HashMap<Integer,String>();
        
        for(int i=0; i<this.columnNames.length; i++){
            this.dcmAttributes[i] = DicomDictionary.StandardDictionary.getTagFromName(this.columnNames[i]);
        }
    }

    @Override
    public double[][] getDataSet() {
        return dataSet;
    }

    @Override
    public void setData(double[][] _dataSet) {
         this.dataSet = _dataSet;
    }
    
    @JsonIgnore
    @Override
    public int getDataLenght() {
        return dataSet.length;
    }

    @Override
    public int getDataColumns() {
        return this.sizeOfCol;
    }

    @Override
    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportOriginalData() {
        try{
            for(int i=0; i<this.sizeOfRows; i++){
                for(int j=0; j<this.sizeOfCol; j++){
                    if (colNamesType.get(j).equals("double")){
                        if (Double.isNaN(dataSet[i][j])){
                            try{
                                this.dcmInfo[i].get(this.dcmAttributes[j]).setValue("");
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }
                        }
                        else{
                            this.dcmInfo[i].get(this.dcmAttributes[j]).setValue(""+dataSet[i][j]);
                        }
                    }
                    else if (colNamesType.get(j).equals("int")){
                        if (dataSet[i][j] == 2147483646.0){
                            try{
                                this.dcmInfo[i].get(this.dcmAttributes[j]).setValue("");
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }
                        }
                        else{
                            this.dcmInfo[i].get(this.dcmAttributes[j]).setValue(Integer.toString((int)dataSet[i][j])+"");
                        }
                    }
                    else{
                        String str = dictionary.getIdToString((int)dataSet[i][j]);
                        if(str == null){
                            str = this.dictHier.getIdToString((int)dataSet[i][j]);
                        }
                        if (str.equals("NaN")){
                            System.out.println("Column "+this.columnNames[j]);
                            try{
                             this.dcmInfo[i].get(this.dcmAttributes[j]).setValue("");
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }
                        }
                        else{
                            this.dcmInfo[i].get(this.dcmAttributes[j]).setValue(str);
                        }
                    }
                }
                DicomOutputStream dcmo = new DicomOutputStream(new FileOutputStream(this.inputpath+File.separator+this.fileNames[i]),"","");
                this.dcmInfo[i].write(dcmo);
            }
            File z = new File(this.inputpath+File.separator+"anonymized_files.zip");
            z.createNewFile();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(this.inputpath+File.separator+"anonymized_files.zip"));
            for(int i=0; i<this.fileNames.length; i++){
                FileInputStream in1 = new FileInputStream(this.inputpath+File.separator+this.fileNames[i]);
                out.putNextEntry(new ZipEntry(this.fileNames[i]));
                byte[] b = new byte[2048];
                int count=0;

                while ((count = in1.read(b)) > 0) {
                    out.write(b, 0, count);
                }
                in1.close();
                out.closeEntry();
            }
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error exportOriginalData: "+e.getMessage());
        }
    }

    @Override
    public String save(boolean[] checkColumns) throws LimitException, DateParseException, NotFoundValueException {
        int counter = 0;
        int stringCount;
        int counterSdf = 0;
        AttributeList list;
        SimpleDateFormat sdfDefault = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdf[] = new SimpleDateFormat[this.columnNames.length];
        
        try{
            if(dictionary.isEmpty() && dictHier.isEmpty()){
                System.out.println("Both empy load data");
                stringCount = 1;
            }
            else if(!dictionary.isEmpty() && !dictHier.isEmpty()){
                System.out.println("Both have values");
                if(dictionary.getMaxUsedId() > dictHier.getMaxUsedId()){
                    stringCount = dictionary.getMaxUsedId()+1;
                }
                else{
                    stringCount = dictHier.getMaxUsedId()+1;
                }
            }
            else if(dictionary.isEmpty()){
                System.out.println("Dict data empty");
                stringCount = dictHier.getMaxUsedId()+1;
            }
            else{
                System.out.println("Dict hier empty");
                stringCount = dictionary.getMaxUsedId()+1;
            }


            for ( int i = 0 ; i < this.columnNames.length ; i ++){
                
                if(colNamesType.get(i).contains("date")){
                    sdf[i] = new SimpleDateFormat(this.formatsDate[i]);
                }
                
            }
            dataSet = new double[sizeOfRows][columnNames.length];
            File folder = new File(this.inputpath);
            File[] dicomfiles = folder.listFiles();
            for(File df : dicomfiles){
                if(!df.getName().endsWith(".dcm")){
                    continue;
                }
                list = new AttributeList();
                list.read(df.getAbsolutePath());
                for (int i = 0; i < this.columnNames.length ; i ++ ){
                    String value = getTagInformation(list,this.dcmAttributes[i]).trim().replaceAll("[\uFEFF-\uFFFF]", "");
                    if ( colNamesType.get(i).contains("int") ){
                        if ( !value.equals("") && !value.equals("\"\"")){
                            try {
                                dataSet[counter][i] = Integer.parseInt(value);
                            } catch (java.lang.NumberFormatException exc) {
                                //ErrorWindow.showErrorWindow("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                exc.printStackTrace();
                                try {
                                    dataSet[counter][i] = new Double(value).intValue();
                                } catch (Exception exc1) {
                                    exc1.printStackTrace();
//                                        System.out.println("Column : " + colNames[i] + " is chosen as integer and you have double values");
                                    throw new NotFoundValueException("Value \""+value+"\" is not an integer, \""+ this.columnNames[i]+ "\" is an integer column");
                                }
                            }   
                        }
                        else{
                            dataSet[counter][i] = 2147483646;
                        }
                    }
                    else if (colNamesType.get(i).contains("double")){
                        if ( !value.equals("")  && !value.equals("\"\"")){
                            value = value.replaceAll(",", ".");
                            try{
                                dataSet[counter][i] = Double.parseDouble(value);
                            }catch(Exception ex){
                                throw new NotFoundValueException("Value \""+value+"\" is not a decimal, \""+ this.columnNames[i]+ "\" is a decimal column");
                            }
                        }
                        else{
                            dataSet[counter][i] = Double.NaN;
                        }
                    }
                    else if (colNamesType.get(i).contains("date")){
                        String var = null;
                        if ( !value.equals("") && !value.equals("\"\"")){
                            var = value;
                            try{
                                if(this.formatsDate[i].equals("dd/MM/yyyy")){
                                    var = sdf[i].parse(var) == null ? null : var;
                                }
                                else{
                                    Date d = sdf[i].parse(var);
                                    var = sdfDefault.format(d);
                                }

                                if(var == null){
                                    var = "NaN";
                                }
                            }catch(ParseException ep){
                                throw new DateParseException(ep.getMessage()+"\nDate format must be the same in column "+this.columnNames[i]);
                            }

                        }
                        else {
                            var = "NaN";
                        }
                        
                        if (var != null) {
                            if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                                if(var.equals("NaN")){
                                    dictionary.putIdToString(2147483646, var);
                                    dictionary.putStringToId(var,2147483646);
                                    dataSet[counter][i] = 2147483646.0;
                                }
                                else{
                                    dictionary.putIdToString(stringCount, var);
                                    dictionary.putStringToId(var,stringCount);
                                    dataSet[counter][i] = stringCount;
                                    stringCount++;
                                }
                            }
                            else{
                                //if string is present in the dictionary, get its id
                                if(dictionary.containsString(var)){
                                    int stringId = dictionary.getStringToId(var);
                                    dataSet[counter][i] = stringId;
                                }
                                else{
                                    int stringId = this.dictHier.getStringToId(var);
                                    dataSet[counter][i] = stringId;
                                }
                            }
                        }
                    }
                    else {
                       String var = null;

                        if ( !value.equals("") && !value.equals("\"\"")){
                            var = value;
                        }
                        else {
                            var = "NaN";
                        }
                        //if string is not present in the dictionary
                        if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                             if(var.equals("NaN")){
                                dictionary.putIdToString(2147483646, var);
                                dictionary.putStringToId(var,2147483646);
//                                        dictionary.put(counter1, tempDict);
                                dataSet[counter][i] = 2147483646.0;
                            }
                            else{
                                dictionary.putIdToString(stringCount, var);
                                dictionary.putStringToId(var,stringCount);
//                                    dictionary.put(counter1, tempDict);
                                dataSet[counter][i] = stringCount;
                                stringCount++;
                            }
                        }
                        else{
                            //if string is present in the dictionary, get its id
                            if(dictionary.containsString(var)){
                                int stringId = dictionary.getStringToId(var);
                                dataSet[counter][i] = stringId;
                            }
                            else{
                                int stringId = this.dictHier.getStringToId(var);
                                dataSet[counter][i] = stringId;
                            }
                        }
                        if(!var.equals("NaN")){
                            if(this.biggerSample.get(i) == null){
                                this.biggerSample.put(i, var);
                            }
                            else if(this.biggerSample.get(i).length() < var.length()){
                                this.biggerSample.put(i, var);
                            }
                        }
                    }
                }
                this.dcmInfo[counter] = list;
                System.out.println("File name: "+df.getName());
                this.fileNames[counter] = df.getName();
                counter++;
            }
        }
        catch(DateParseException de){
            throw new DateParseException(de);
        }
        catch(NotFoundValueException ne){
            throw new NotFoundValueException(ne.getMessage());
        }
        catch(Exception e){
            e.printStackTrace();
            System.err.println("Error dicom file save: "+e.getMessage());
            return null;
        }
        
        return "OK";
    }

    @Override
    public void preprocessing() throws LimitException {
        
        if(AppCon.os.equals(online_version) && this.sizeOfRows > online_rows){
            throw new LimitException("Dataset is too large, the limit is "+online_rows+" rows, please download desktop version, the online version is only for simple execution.");
        }
        
        recordsTotal = sizeOfRows;
        recordsFiltered = sizeOfRows;
    }

    @Override
    public String readDataset(String[] columnTypes, boolean[] checkColumns) throws LimitException, DateParseException, NotFoundValueException {
        SaveClmnsAndTypeOfVar(columnTypes,checkColumns);
        preprocessing();
        String result = save(checkColumns);
        return result;
    }
    
    private boolean isSuppressed(Object[] data, int[] qids, Map<Integer, Set<String>> suppressedValues){
        
        if (suppressedValues == null)
            return false;
        
        Object[] checkArr = new Object[1];
        
        //check for each and every qid if is suppressed
        for(int i=0; i<qids.length; i++){
            Set<String> suppressed = suppressedValues.get(qids[i]);
            if(suppressed != null){
                checkArr[0] = data[i];
                if(suppressed.contains(java.util.Arrays.toString(checkArr)))
                    return true;
            }
        }
        
        //check for all qids combined
        Set<String> suppressed = suppressedValues.get(-1);
        if(suppressed != null){
            if(suppressed.contains(java.util.Arrays.toString(data))){
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void computeInformationLossMetrics(Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues) {
        double ncp = 0;
        double total = 0;
        try {
            Object[] rowQIs = null;
            if(suppressedValues != null){
                rowQIs = new Object[qids.length];
            }
            
            for (int row = 0; row < anonymizedTable.length; row++){
                if(suppressedValues != null){


                    //get qids of this row
                    for(int i=0; i<qids.length; i++){
                        rowQIs[i] = anonymizedTable[row][qids[i]];
                    }


                    //check if row is suppressed
                    if(isSuppressed(rowQIs, qids, suppressedValues)){
                        continue;
                    }
                }
                
                for(int column = 0; column < anonymizedTable[0].length; column++){
                    if(hierarchies.containsKey(column) && !anonymizedTable[row][column].equals("(null)")){
                        Hierarchy h = hierarchies.get(column);
                        
                        if(this.colNamesType.get(column).equals("int")){
                            if(h.getHierarchyType().contains("range")){
                                if(anonymizedTable[row][column] instanceof String && ((String)anonymizedTable[row][column]).contains("-")){
                                    RangeDouble rd =  new RangeDouble((double)Integer.parseInt(((String)anonymizedTable[row][column]).split("-")[0]),(double)Integer.parseInt(((String)anonymizedTable[row][column]).split("-")[1]));
                                    Double globalRange = ((RangeDouble) h.getRoot()).upperBound-((RangeDouble) h.getRoot()).lowerBound;
                                    ncp += ((rd.upperBound-rd.lowerBound)/globalRange)/hierarchies.size();
                                    total += (h.getHeight()-h.getLevel(rd))/((double)h.getHeight())/hierarchies.size();
                                }
                            }
                            else{
                                int leafAnonymized = h.findAllChildren(((Integer)anonymizedTable[row][column]).doubleValue(), 0,true);
                                if(leafAnonymized == 1){
                                    continue;
                                }
                                int allLeaves = h.findAllChildren(h.getRoot(), 0,true);
                                
                                ncp += (leafAnonymized/((double)allLeaves))/hierarchies.size();
                                total += h.getLevel(((Integer)anonymizedTable[row][column]).doubleValue())/((double)h.getHeight()-1)/hierarchies.size();
                            }
                        }
                        else if(this.colNamesType.get(column).equals("double")){
                            if(h.getHierarchyType().contains("range")){
                                if(anonymizedTable[row][column] instanceof String && ((String)anonymizedTable[row][column]).contains("-")){
                                    RangeDouble rd =  new RangeDouble(Double.parseDouble(((String)anonymizedTable[row][column]).split("-")[0]),Double.parseDouble(((String)anonymizedTable[row][column]).split("-")[1]));
                                    Double globalRange = ((RangeDouble) h.getRoot()).upperBound-((RangeDouble) h.getRoot()).lowerBound;
                                    ncp += ((rd.upperBound-rd.lowerBound)/globalRange)/hierarchies.size();
                                    total += (h.getHeight()-h.getLevel(rd))/((double)h.getHeight())/hierarchies.size();
                                }
                            }
                            else{
                                int leafAnonymized = h.findAllChildren(anonymizedTable[row][column], 0,true);
                                if(leafAnonymized == 1){
                                    continue;
                                }
                                int allLeaves = h.findAllChildren(h.getRoot(), 0,true);
                                
                                ncp += (leafAnonymized/((double)allLeaves))/hierarchies.size(); 
                                total += h.getLevel(((Double)anonymizedTable[row][column]).doubleValue())/((double)h.getHeight()-1)/hierarchies.size();
                            }
                            
                        }
                        else if(this.colNamesType.get(column).equals("date")){
                            long startDate,endDate;
                            if(anonymizedTable[row][column] instanceof RangeDate){
                                startDate = ((RangeDate) anonymizedTable[row][column]).upperBound.getTime();
                                endDate = ((RangeDate) anonymizedTable[row][column]).lowerBound.getTime();
                            }
                            else if(anonymizedTable[row][column] instanceof String && ((String)anonymizedTable[row][column]).contains("-")){
                                startDate = ((HierarchyImplRangesDate) h).getDateFromString(((String)anonymizedTable[row][column]).split("-")[0],true).getTime();
                                endDate = ((HierarchyImplRangesDate) h).getDateFromString(((String)anonymizedTable[row][column]).split("-")[1],false).getTime();
                            }
                            else{
                                continue;
                            }
                            
                            Long globalRangeTime = ((RangeDate)h.getRoot()).upperBound.getTime()-((RangeDate)h.getRoot()).lowerBound.getTime();
                            ncp += (((double)(endDate-startDate))/globalRangeTime)/hierarchies.size();
                            total += (h.getHeight()-h.getLevel(new RangeDate(new Date(startDate),new Date(endDate))))/((double)h.getHeight())/hierarchies.size();
                            
                        }
                        else{
                            if(anonymizedTable[row][column] instanceof String){
                                String anonymizedValue = (String) anonymizedTable[row][column];
                                Integer anonymizedId = this.getDictionary().getStringToId().get(anonymizedValue);
                                if(anonymizedId == null){
                                    anonymizedId = HierarchyImplString.getWholeDictionary().getStringToId().get(anonymizedValue);
                                }
                                int leafAnonymized = h.findAllChildren(anonymizedId.doubleValue(), 0,true);
                                if(leafAnonymized == 1){
                                    continue;
                                }
                                int allLeaves = h.findAllChildren(h.getRoot(), 0,true);

                                ncp += (leafAnonymized/((double)allLeaves))/hierarchies.size(); 
                                total += h.getLevel(anonymizedId.doubleValue())/((double)h.getHeight()-1)/hierarchies.size();
                            }
                        }
                        
                    }
                }
                
            }
            
            ncp = ncp/this.recordsTotal;
            total = total/this.recordsTotal;
            this.informationLoss.put("NCP", ncp);
            this.informationLoss.put("Total", total);
            
            
        }catch (Exception e) {
            System.err.println("Error in computeInformationLossMetrics function for DICOMData: "+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void export(String file, Object[][] initialTable, Object[][] anonymizedTable, int[] qids, Map<Integer, Hierarchy> hierarchies, Map<Integer, Set<String>> suppressedValues) {
        System.out.println("Export dicom data...");
        
        Object[][] temp = null;
        if ( initialTable != null ){
            temp = initialTable;
        }
        else{
            temp = anonymizedTable;
        }
        
        try{
            for(int i=0; i<this.sizeOfRows; i++){
                for(int j=0; j<this.sizeOfCol; j++){
                    Object value = temp[i][j];
                    if (!value.equals("(null)")){
                        this.dcmInfo[i].get(this.dcmAttributes[j]).setValue(value+"");
                    }
                    else{
                        this.dcmInfo[i].get(this.dcmAttributes[j]).setValue("");
                    }

                }
                DicomOutputStream dcmo = new DicomOutputStream(new FileOutputStream(this.inputpath+File.separator+this.fileNames[i]),"","");
                this.dcmInfo[i].write(dcmo);
            }
            File z = new File(this.inputpath+File.separator+"anonymized_dicom_files.zip");
            z.createNewFile();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(this.inputpath+File.separator+"anonymized_dicom_files.zip"));
            for(int i=0; i<this.fileNames.length; i++){
                FileInputStream in1 = new FileInputStream(this.inputpath+File.separator+this.fileNames[i]);
                out.putNextEntry(new ZipEntry(this.fileNames[i]));
                byte[] b = new byte[2048];
                int count=0;

                while ((count = in1.read(b)) > 0) {
                    out.write(b, 0, count);
                }
                in1.close();
                out.closeEntry();
            }
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error export: "+e.getMessage());
        }
    }

    @Override
    public Map<Integer, String> getColNamesPosition() {
        return colNamesPosition;
    }

    @Override
    public DictionaryString getDictionary() {
        return dictionary;
    }

    @Override
    public int getColumnByName(String column) {
        for(Integer i : this.colNamesPosition.keySet()){
            if(this.colNamesPosition.get(i).equals(column)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getColumnByPosition(Integer columnIndex) {
        return this.colNamesPosition.get(columnIndex);
    }

    @Override
    public String[] getColumnNames() {
        return this.columnNames;
    }

    @Override
    public void SaveClmnsAndTypeOfVar(String[] columnTypes, boolean[] checkColumns) {
        int counter = 0;
        boolean removedColumn = false;
        String [] newFormatDate = null;
        
        for ( int i = 0 ; i < this.columnNames.length ; i ++){
            if ( checkColumns[i] == true){
                System.out.println("Edww mpainei");
                colNamesType.put(counter,null);
                colNamesPosition.put(counter,this.columnNames[i]);
                counter++;
            }
        }
        if(counter != columnNames.length){
            newFormatDate = new String[counter];
            removedColumn = true;
        }
        
        counter = 0 ;
        
        for ( int i = 0 ; i < columnTypes.length ; i ++ ){
            if ( checkColumns[i] == true){
                if (columnTypes[i].equals("int")){
                    colNamesType.put(counter, "int");
                }
                else if (columnTypes[i].equals("double")){
                    colNamesType.put(counter, "double");
                }
                else if (columnTypes[i].equals("date")){
                    colNamesType.put(counter, "date");
                    if(removedColumn){
                        newFormatDate[counter] = this.formatsDate[i]; 
                    }
                }
                else{
                    System.out.println("Edww mpainei2");
                    colNamesType.put(counter, "string");
                }

                counter++;
            }
        }
        
        if(counter!=columnTypes.length){
            this.columnNames =  colNamesPosition.values().toArray(new String[this.colNamesPosition.size()]);
            this.formatsDate = newFormatDate;
            
            this.dcmAttributes = new AttributeTag[this.columnNames.length];;
            for(int i=0; i<this.columnNames.length; i++){
                this.dcmAttributes[i] = DicomDictionary.StandardDictionary.getTagFromName(this.columnNames[i]);
            }
        }
        
        sizeOfCol = columnNames.length;
    }
    
    
    @JsonView(View.SmallDataSet.class)
    @Override
    public String findColumnTypes() {
        
        File folder = new File(this.inputpath);
        this.formatsDate = new String[this.columnNames.length];
        try{
            File[] dicomfiles = folder.listFiles();
            int sample=6;
            if(dicomfiles.length < sample){
                sample=dicomfiles.length;
            }
            smallDataSet = new String[sample][this.columnNames.length];
            for(int i=0; i<sample; i++){
                AttributeList list = new AttributeList();
                System.out.println("File "+dicomfiles[i].getAbsolutePath());
                if(!dicomfiles[i].getName().endsWith(".dcm")){
                    continue;
                }
                list.read(dicomfiles[i].getAbsolutePath());
                
                for(int j=0; j<this.columnNames.length; j++){
                    String value = getTagInformation(list,this.dcmAttributes[j]).trim().replaceAll("[\uFEFF-\uFFFF]", "");
                    System.out.println(this.columnNames[j]+": "+value);
                    if(i==0){
                        if ( !value.equals("")){
                            if (chVar.isInt(value)){
                                smallDataSet[i][j] = "int";
                            }
                            else if (chVar.isDouble(value)){
                                smallDataSet[i][j] = "double";
                            }
                            else if(chVar.isDate(value)){
                                smallDataSet[i][j] = "date";
                                this.formatsDate[j] = chVar.lastFormat;
                            }
                            else{  
                                smallDataSet[i][j] = "string";
                            }
                        }
                        smallDataSet[i+1][j] = value;
                    }
                    else{
                        smallDataSet[i][j] = value;
                        if ( !value.equals("")){
                            if ( smallDataSet[0][j] != null ){
                                if (smallDataSet[0][j].equals("int")){
                                    if (!chVar.isInt(value)){
                                        if (chVar.isDouble(value)){
                                            smallDataSet[0][j] = "double";
                                        }
                                        else {
                                            smallDataSet[0][j] = "string";
                                        }
                                    }
                                }
                                else if(smallDataSet[0][j].equals("double")){
                                    if (!chVar.isInt(value) && !chVar.isDouble(value)){
                                        smallDataSet[0][j] = "string";
                                    }
                                }
                            }
                            else{
                                if (chVar.isInt(value)){
                                    smallDataSet[0][j] = "int";
                                }
                                else if (chVar.isDouble(value)){
                                    smallDataSet[0][j] = "double";
                                }
                                else if(chVar.isDate(value)){
                                    smallDataSet[0][j] ="date";
                                    this.formatsDate[j] = chVar.lastFormat;
                                }
                                else{  
                                    smallDataSet[0][j] = "string";
                                }
                            }
                        }
                    }
                }
            }
            
            for (int i = 0 ; i < smallDataSet[0].length ; i ++ ){
                if ( smallDataSet[0][i] == null){
                    smallDataSet[0][i]= "string";
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error dicom files, findColumnTypes: " + e.getMessage());
        }
        
        return "Ok";
    }
    
    private String getTagInformation(AttributeList list, AttributeTag attrTag) {
        System.out.println(DicomDictionary.StandardDictionary.getNameFromTag(attrTag));
        return Attribute.getDelimitedStringValuesOrEmptyString(list, attrTag);
    }

    @Override
    public String[][] getSmallDataSet() {
        return smallDataSet;
    }

    @Override
    public ArrayList<LinkedHashMap> getPage(int start, int length) {
        data = new ArrayList<LinkedHashMap>();
        int counter = 0 ;
        
        LinkedHashMap linkedHashTemp = null;
        int max;
        
        if ( start + length <= sizeOfRows ){
            max = start + length;
        }
        else{
            max = sizeOfRows;
        }
        
       
                

        for ( int i = start ; i < max ; i ++){
            linkedHashTemp = new LinkedHashMap<>();
            for (int j = 0 ; j < colNamesType.size() ; j ++){
                if (colNamesType.get(j).equals("double")){
                    if (Double.isNaN(dataSet[i][j])){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
                        Object a = dataSet[i][j];
                        linkedHashTemp.put(columnNames[j], dataSet[i][j]);
                    }
                }
                else if (colNamesType.get(j).equals("int")){
                    if (dataSet[i][j] == 2147483646.0){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
                        linkedHashTemp.put(columnNames[j], Integer.toString((int)dataSet[i][j])+"");
                    }
                }
                else{
                    String str = dictionary.getIdToString((int)dataSet[i][j]);
                    if(str == null){
                        str = this.dictHier.getIdToString((int)dataSet[i][j]);
                    }

                    if (str.equals("NaN")){
                        linkedHashTemp.put(columnNames[j],"");
                    }
                    else{
                        linkedHashTemp.put(columnNames[j], str);
                    }
                }
            }
            data.add(linkedHashTemp);
            counter ++;
            
        }
        
        
        
        
        recordsTotal = sizeOfRows;
        recordsFiltered = sizeOfRows;
        

        
        
        return data;
    }

    @Override
    public String[][] getTypesOfVariables(String[][] smallDataSet) {
        this.smallDataSet = smallDataSet;
        String []str = null;
        String []columnTypes = null;
        typeArr = new String[smallDataSet[0].length][];
    
        for ( int i = 0 ; i < 1 ; i ++){
            columnTypes = new String[smallDataSet[i].length];
            for ( int j = 0 ; j < smallDataSet[i].length ; j ++ ){
                if ( smallDataSet[i][j] != null ){
                    if (smallDataSet[i][j].equals("string")){
                        str = new String[1];
                        str[0] = "string";
                        columnTypes[j] = "string";
                    }
                    else if (smallDataSet[i][j].equals("int")){
                        str = new String[3];
                        str[0] = "int";
                        str[1] = "string";
                        str[2] = "double";
                        columnTypes[j] = "int";
                    }
                    else if (smallDataSet[i][j].equals("date")){
                        str = new String[2];
                        str[0] = "date";
                        str[1] = "string";
                        columnTypes[j] = "date";
                    }
                    else {
                        str = new String[2];
                        str[0] = "double";
                        str[1] = "string";
                        columnTypes[j] = "double";

                    }
                }
                else{
                    str = new String[4];
                    str[0] = "string";
                    str[1] = "int";
                    str[2] = "double";
                    str[3] = "date";
                    columnTypes[j] = "string";
                }
                typeArr[j] = str;
            }
            
        
        }

        return typeArr;
    }

    @Override
    public int getRecordsTotal() {
        return recordsTotal;
    }

    @Override
    public Map<Integer, String> getColNamesType() {
        return colNamesType;
    }

    @Override
    public String getInputFile() {
        return this.inputFile;
    }

    @Override
    public SimpleDateFormat getDateFormat(int column) {
        return new SimpleDateFormat("dd/MM/yyyy");
    }

    @Override
    public void setMask(int column, int[] positions, char character, String option) {
        int stringCount;
        if(dictionary.isEmpty() && dictHier.isEmpty()){
            System.out.println("Both empy load data");
            stringCount = 1;
        }
        else if(!dictionary.isEmpty() && !dictHier.isEmpty()){
            System.out.println("Both have values");
            if(dictionary.getMaxUsedId() > dictHier.getMaxUsedId()){
                stringCount = dictionary.getMaxUsedId()+1;
            }
            else{
                stringCount = dictHier.getMaxUsedId()+1;
            }
        }
        else if(dictionary.isEmpty()){
            System.out.println("Dict data empty");
            stringCount = dictHier.getMaxUsedId()+1;
        }
        else{
            System.out.println("Dict hier empty");
            stringCount = dictionary.getMaxUsedId()+1;
        }
        
        for(int i=0; i<this.sizeOfRows; i++){
            String var = dictionary.getIdToString((int)dataSet[i][column]);
            if(var == null){
                var = this.dictHier.getIdToString((int)dataSet[i][column]);
            }
            
            if(var.equals("NaN")){
                continue;
            }
            
            if(option.equals("suffix")){
                var = new StringBuilder(var).reverse().toString();
            }
            
            for(int pos : positions){
                if(pos<var.length()){
                    var = var.substring(0,pos)+character+var.substring(pos+1);
                }
            }
            
            if(option.equals("suffix")){
                var = new StringBuilder(var).reverse().toString();
            }


            if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                if(var.equals("NaN")){
                   dictionary.putIdToString(2147483646, var);
                   dictionary.putStringToId(var,2147483646);
    //                                        dictionary.put(counter1, tempDict);
                   dataSet[i][column] = 2147483646.0;
               }
               else{
                   dictionary.putIdToString(stringCount, var);
                   dictionary.putStringToId(var,stringCount);
    //                                    dictionary.put(counter1, tempDict);
                   dataSet[i][column] = stringCount;
                   stringCount++;
               }
           }
           else{
               //if string is present in the dictionary, get its id
               if(dictionary.containsString(var)){
                   int stringId = dictionary.getStringToId(var);
                   dataSet[i][column] = stringId;
               }
               else{
                   int stringId = this.dictHier.getStringToId(var);
                   dataSet[i][column] = stringId;
               }
           }
        }
        this.pseudoanonymized = true;
        
        String var = this.biggerSample.get(column);
        if(option.equals("suffix")){
            var = new StringBuilder(var).reverse().toString();
        }

        for(int pos : positions){
            if(pos<var.length()){
                var = var.substring(0,pos)+character+var.substring(pos+1);
            }
        }

        if(option.equals("suffix")){
            var = new StringBuilder(var).reverse().toString();
        }
        
        this.biggerSample.put(column, var);
    }

    @Override
    public Map<String, Double> getInformationLoss() {
        return this.informationLoss;
    }

    @Override
    public void setRegex(int column, char character, String regex) {
        int stringCount;
        if(dictionary.isEmpty() && dictHier.isEmpty()){
            System.out.println("Both empy load data");
            stringCount = 1;
        }
        else if(!dictionary.isEmpty() && !dictHier.isEmpty()){
            System.out.println("Both have values");
            if(dictionary.getMaxUsedId() > dictHier.getMaxUsedId()){
                stringCount = dictionary.getMaxUsedId()+1;
            }
            else{
                stringCount = dictHier.getMaxUsedId()+1;
            }
        }
        else if(dictionary.isEmpty()){
            System.out.println("Dict data empty");
            stringCount = dictHier.getMaxUsedId()+1;
        }
        else{
            System.out.println("Dict hier empty");
            stringCount = dictionary.getMaxUsedId()+1;
        }
        
        for(int i=0; i<this.sizeOfRows; i++){
            String var = dictionary.getIdToString((int)dataSet[i][column]);
            if(var == null){
                var = this.dictHier.getIdToString((int)dataSet[i][column]);
            }
            
            if(var.equals("NaN")){
                continue;
            }
            
            var = var.replaceAll(regex, character+"");


            if (!dictionary.containsString(var) && !this.dictHier.containsString(var)){
                if(var.equals("NaN")){
                   dictionary.putIdToString(2147483646, var);
                   dictionary.putStringToId(var,2147483646);
    //                                        dictionary.put(counter1, tempDict);
                   dataSet[i][column] = 2147483646.0;
               }
               else{
                   dictionary.putIdToString(stringCount, var);
                   dictionary.putStringToId(var,stringCount);
    //                                    dictionary.put(counter1, tempDict);
                   dataSet[i][column] = stringCount;
                   stringCount++;
               }
           }
           else{
               //if string is present in the dictionary, get its id
               if(dictionary.containsString(var)){
                   int stringId = dictionary.getStringToId(var);
                   dataSet[i][column] = stringId;
               }
               else{
                   int stringId = this.dictHier.getStringToId(var);
                   dataSet[i][column] = stringId;
               }
           }
        }
        this.pseudoanonymized = true;
        
        String var = this.biggerSample.get(column);
        var = var.replaceAll(regex, character+"");
        this.biggerSample.put(column, var);
        
        
    }
    
}

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
package dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A dictionary for a string attribute
 * @author serafeim
 */
public class DictionaryString {
    public Map<Integer, String> idToString = null;
    private Map<String,Integer> stringToId = null;
    
    
    public DictionaryString(){
        idToString = new HashMap<Integer,String>();
        stringToId = new HashMap<String,Integer>();
    }
    
    /**
     * Assign id to string
     * @param key key
     * @param value string value
     */
    public void putIdToString(Integer key, String value){
        idToString.put(key, value);
    }
    
    /**
     * Gets string for the specified key
     * @param key key
     * @return string associated with key
     */
    public String getIdToString(Integer key){
        return idToString.get(key);
    }
    
    /**
     * Assign string to id
     * @param key key 
     * @param value string value
     */
    public void putStringToId(String key, Integer value){
        stringToId.put(key, value);
    }
    
    /**
     * Gets id for the specified string
     * @param key id
     * @return string value
     */
    public Integer getStringToId(String key){
        return stringToId.get(key);
    }
    
    
    /**
     * if string is present in the dictionary
     * @param key the string value
     * @return true if present, false otherwise
     */
    public boolean containsString(String key){
        return stringToId.containsKey(key);
    }
    
    /**
     * Checks if this dictionary is subset of another one
     * @param dict2 the other dictionary
     * @return returns null if this dictionary is subset of dict2, otherwise 
     * the first string that is not present in hierarchy's dictionary
     */
    public String isSubsetOf(DictionaryString dict2){
        
        Set<String> dict2Keyset = dict2.getKeyset();
        for(String s : this.stringToId.keySet()){
            if(!dict2Keyset.contains(s)){
                return s;
            }
        }
        
        return null;
    }
    
    /**
     * Get the set of strings in the dictionary
     * @return the set of dictionary's strings 
     */
    public Set<String> getKeyset(){
        return this.stringToId.keySet();
    }
    
    public void remove(int id){
        String key = this.idToString.get(id);
        if(key != null){
            this.idToString.remove(id);
            this.stringToId.remove(key);
        }
    }
    
    public int getMaxUsedId(){
        return Collections.max(this.idToString.keySet());
    }

    public Map<Integer, String> getIdToString() {
        return idToString;
    }

    public Map<String, Integer> getStringToId() {
        return stringToId;
    }
    
    
}

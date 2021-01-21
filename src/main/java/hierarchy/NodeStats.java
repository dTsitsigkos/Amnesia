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
package hierarchy;

/**
 * Class keeping stats for hierarchy nodes
 * @author serafeim
 */
public class NodeStats {
    /**
     * level of the node
     */
    public int level;
    
    /**
     * weight of hierarchy node (number of times present in the dataset) 
     */
    public int weight; 

    public NodeStats(int l){
        level = l;
        weight = 0;
    }

    public void setWeight(int w){
        this.weight = w;
    }

    public void setLevel(int l){
        this.level = l;
    }

    public int getWeight(){
        return this.weight;
    }

    public int getLevel(){
        return this.level;
    }
    
}

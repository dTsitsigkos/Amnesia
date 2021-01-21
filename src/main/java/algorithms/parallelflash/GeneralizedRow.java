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
package algorithms.parallelflash;

import java.util.Arrays;

/**
 *
 * @author serafeim
 */
public class GeneralizedRow {
    public String[] generalizedColumns = null;

    public GeneralizedRow (int size){
        generalizedColumns = new String[size];
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Arrays.deepHashCode(this.generalizedColumns);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeneralizedRow other = (GeneralizedRow) obj;
        if (!Arrays.deepEquals(this.generalizedColumns, other.generalizedColumns)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return  Arrays.toString(generalizedColumns);
    }
    
}

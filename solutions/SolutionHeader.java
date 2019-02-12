/*
 * Copyright (C) 2015 serafeim
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package solutions;

import data.Data;
import java.util.Arrays;


/**
 *
 * @author serafeim
 */
public class SolutionHeader implements Comparable<SolutionHeader>{
        public int[] qids;
        public int[] levels;
        Data dataset;
        
        public SolutionHeader(int[] _qids, int[] _levels, Data _dataset){
            this.qids = _qids;
            this.levels = _levels;
            this.dataset = _dataset;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Arrays.hashCode(this.qids);
            hash = 19 * hash + Arrays.hashCode(this.levels);
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
            final SolutionHeader other = (SolutionHeader) obj;
            if (!Arrays.equals(this.qids, other.qids)) {
                return false;
            }
            if (!Arrays.equals(this.levels, other.levels)) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            String stringLabel = "";
            for(int i=0; i<qids.length; i++){
                stringLabel += dataset.getColumnByPosition(qids[i]) + " ";
            }
            return stringLabel.trim();
        }
        
        public String toStringExtenteded(){
            return Arrays.toString(qids) + " " + Arrays.toString(levels);
        }
        @Override
        public int compareTo(SolutionHeader o) {
            return this.toStringExtenteded().compareTo(o.toStringExtenteded());
        }

        public String getLevelsToString(){
            String strLevel = null;
            if ( levels.length == 1){
                return ""+levels[0];
            }
            else{
                boolean FLAG = false;
                for ( int i = 0; i < levels.length ; i++){
                    if ( FLAG == false){
                        strLevel = "" + levels[i];
                        FLAG = true;
                    }
                    else{
                        strLevel = strLevel + "," +levels[i];
                    }
                }   
            }
            return strLevel;
        }
        
    }

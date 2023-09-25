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
package algorithms.flash;

import java.util.Arrays;

/**
 *
 * @author serafeim
 */

public class GridBuilder {
    private int[] qidColumns = null;
    
    private int[] minLayers = null;
    private int[] maxLayers = null;
    
    private GridNode[][] grid_layers = null;
    
    
    public GridBuilder(final int[] qidColumns, final int[] miL, final int[] maL) {
        this.qidColumns = qidColumns;
        
        this.minLayers = miL;
        this.maxLayers = maL;
        
    }
    
    
    public Grid construct() {
        int nodes_num = constructLayersMap();
        
        return new Grid(qidColumns, grid_layers, nodes_num);
    }
    
    
    private int constructLayersMap() {
        final int nQIs = maxLayers.length;
        int nNodes = 1;
        
        final Integer[] maximumIdxs = new Integer[nQIs];
        final Integer[] balances = new Integer[nQIs];
        
        int identifier = 0;
        int maxiumumLevel = 1;
        
        
        int i=0;
        while(i<nQIs){
            balances[i] = nNodes;
            int curMaximumGenHeight = maxLayers[i] + 1;
            
            maximumIdxs[i] = curMaximumGenHeight - 1;
            
            nNodes = nNodes * (curMaximumGenHeight - minLayers[i]);
            maxiumumLevel = maxiumumLevel + (curMaximumGenHeight - 1);
            
            i++;
        }
        
        
      
        Integer[] layerSize = new Integer[maxiumumLevel];
        Arrays.fill(layerSize, 0);
        GridNode[] arrGridNode = new GridNode[nNodes];
        
        i=0;
        while(i < arrGridNode.length){
            arrGridNode[i++] = new GridNode(identifier);
            identifier++;
        }
        
        
            
        for (int c = 0; c < nNodes; c++) {
            
            Integer[] status = new Integer[nQIs];
            int temp = c;
            
            int layer = 0;
            
            int nUp = 0;
            int nDown = 0;
            
            i=status.length - 1;
            while(i >= 0){
                status[i] = temp / balances[i] + minLayers[i];
                temp = temp -  balances[i] * (status[i] - minLayers[i]);
                layer = layer + status[i];
                
                if (status[i] < maximumIdxs[i]) {
                    nUp++;
                }
                if (status[i] != minLayers[i]) {
                    nDown++;
                }
                i--;
            }
 
            
            GridNode Gnode = arrGridNode[c];
            
            
            
            Gnode.setTopNodes(new GridNode[nUp]);
            Gnode.setBottomNodes(new GridNode[nDown]);
            
            Gnode.setTransformation(status, layer);
            
            layerSize[layer]++;
        }
        
        GridNode[][] curLayers = new GridNode[maxiumumLevel][];
        
        i=0;
        while(i < curLayers.length){
            curLayers[i] = new GridNode[layerSize[i]];
            i++;
        }
        
        i=0;
        while(i<arrGridNode.length){
            GridNode node = arrGridNode[i];
            Integer layer = node.getLayer();
            
            layerSize[node.getLayer()] = layerSize[node.getLayer()] - 1;
            
            int idx = curLayers[layer].length - 1 - layerSize[layer];
            
            curLayers[layer][idx] = node;
            int[] transform = node.getTransformation();
            
            for (int j=0; j < transform.length; j++) {
                if (transform[j] < maximumIdxs[j]) {

                    GridNode reachableNode = arrGridNode[i + balances[j]];
                    
                    node.addTopNode(reachableNode);
                    reachableNode.addBottomNode(node);
                }
            }
            i++;
        }
        
        this.grid_layers = curLayers;
        return nNodes;
    }
    
}

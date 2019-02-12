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
public class Results {
    private String nonAnonymizeOccurrences = null;
    private String anonymizedOccurrences = null;
    private String possibleOccurences = null;
    private String estimatedRate = null;
    
    
    public Results (String _nonAnonymizeOccurrences, String _anonymizedOccurrences, String _possibleOccurence, String _estimatedRate){
        nonAnonymizeOccurrences = _nonAnonymizeOccurrences;
        anonymizedOccurrences = _anonymizedOccurrences;
        possibleOccurences = _possibleOccurence;
        estimatedRate = _estimatedRate;     
    }
    
    public Results (){
         
    }

    public String getNonAnonymizeOccurrences() {
        return nonAnonymizeOccurrences;
    }

    public String getAnonymizedOccurrences() {
        return anonymizedOccurrences;
    }

    public String getPossibleOccurences() {
        return possibleOccurences;
    }

    public String getEstimatedRate() {
        return estimatedRate;
    }


    public void setNonAnonymizeOccurrences(String nonAnonymizeOccurrences) {
        this.nonAnonymizeOccurrences = nonAnonymizeOccurrences;
    }

    public void setAnonymizedOccurrences(String anonymizedOccurrences) {
        this.anonymizedOccurrences = anonymizedOccurrences;
    }

    public void setPossibleOccurences(String possibleOccurences) {
        this.possibleOccurences = possibleOccurences;
    }

    public void setEstimatedRate(String estimatedRate) {
        this.estimatedRate = estimatedRate;
    }

    
}

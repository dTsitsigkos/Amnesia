/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsoninterface;

/**
 *
 * @author jimakos
 */
public class View {
    public interface GetColumnNames{}
    public interface DataSet {}
    //public interface Hier{}
    public interface Graph{}
    public interface SmallDataSet extends GetColumnNames {}
    public interface GetDataTypes extends GetColumnNames{}
    public interface Solutions{}
}

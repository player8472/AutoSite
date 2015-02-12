/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;
import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.model.Series;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Markus
 */
public class main {
    
    private static com.omertron.thetvdbapi.model.Series series;

     public static void main(String args[]) {
         TheTVDBApi tvDB=new TheTVDBApi("43F0D2F1CBFC07AD");
         try{
         List<Series> serien=tvDB.searchSeries("The Blacklist", "en");
         List<String> seriesid=new ArrayList();
         
         for(Series s : serien){
             seriesid.add(s.getId());
             System.out.println(s.getSeriesName());
             System.out.println("Imdb-ID: "+s.getImdbId());
         }
         }catch(Exception e){System.out.println(e.getMessage());}
         
         
      
     }
    
}

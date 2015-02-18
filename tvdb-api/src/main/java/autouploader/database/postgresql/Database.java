/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autouploader.database.postgresql;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Series;
import com.omertron.thetvdbapi.model.Episode;


/**
 *
 * @author Markus
 */
public class Database {
    public static int newRelease(Connection con,String rlsname){
        try{
            String cmdString="Select * from tblRelease where RlsName="+rlsname+";";
            ResultSet rs;
            Statement st;
            st=con.createStatement();
            rs=st.executeQuery(cmdString);
            if(rs.next()){
                System.out.println(rs.getString(1));
                return -1;
            }
            else{
                int EpisodeID=Database.getEpisodeID(con,Database.rlsNameToShowName(rlsname),Database.rlsNameToSeasonNr(rlsname),Database.rlsNameToEpisodeNumber(rlsname));
                cmdString="Hier insert-statement einfügen (view für rls mit episodeID";
                st=con.createStatement();
                st.executeQuery(cmdString);
                rs=st.getGeneratedKeys();
                if(rs.next()){
                    return rs.getInt(1);
                }
                else{
                    throw new SQLException("Insert Fehlgeschlagen");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    public static String rlsNameToShowName(String rlsn){
        //Dummy
        return rlsn;
    }
    public static int rlsNameToEpisodeNumber(String rlsname){
        //Hier Regex einbauen
        return 1;
    }
    
     private static int rlsNameToSeasonNr(String rlsname) {
        // Hier regex einbauen
         return 1;
    }

    private static String getShowID(Connection con, String rlsNameToShowName) throws TvDbException{
        
        // Hier Abfrage einbauen, falls leere Ergebnismenge aus tvdb abfragen, eintragen und wert zurückgeben
        TheTVDBApi api=new TheTVDBApi("test123");
        Series s=api.searchSeries(rlsNameToShowName, "en").get(0);
        String seriesID=s.getId();
        
        return seriesID;
        
    }

    private static int getEpisodeID(Connection con, String rlsNameToShowName, int snr,int enr) throws TvDbException{
        int epiNr=1;
        if(true /* Hier Abfrage einbauen, welche aus der DB abfragt*/){
            //Setze epiNr auf gerade abgefragten wert
            epiNr=2;
           // Hier epiNr auf abgefragte NR ändern
        }
        else{
            String showid=Database.getShowID(con, rlsNameToShowName);
            
            TheTVDBApi api=new TheTVDBApi("apikey");
            Episode e=api.getDVDEpisode(showid, snr, enr, "en");
            epiNr=e.getEpisodeNumber();
            //Insert new Episode into DB            
        }
        return epiNr;
    }
    public static String[] rarFile(String path){
        //Hier müssen Aufrufe rein, wleche zuerst die Datei packen (Aufruf der rar.EXE)
        //Und im Anschluss das Zielverzeichnis nach den Entsprechenden part*.rar durchsuchen
        //Letztere werden dann schlussendlich zurückgegeben
        
        return new String[]{"",""};
    }

   
    
}
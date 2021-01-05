package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    
 
    @Mock
    private static InputReaderUtil inputReaderUtil;
  
    
    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }
   
    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();

    }
   
    @AfterAll
    private static void tearDown(){
  
    }
    
 
    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        //------------------------------------------------------------------------------------------------
        // Initialisation des boolean de resultats pour les 2 tables parking et ticket
        boolean ParkingMaj=false;
        boolean ticketExist=false;
        
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();
            //Le ticket est-il enregistré ?
            PreparedStatement ps1 =connection.prepareStatement("Select * from ticket where PARKING_NUMBER=1 AND VEHICLE_REG_NUMBER='ABCDEF'");
            		//-- analyse du résultat vrai si il existe au moins 1 résultats
           		ResultSet rs1 = ps1.executeQuery();
           		ticketExist = rs1.next();
           	//la table Parking est-elle maj ?
           	PreparedStatement ps2 =connection.prepareStatement("Select * from parking WHERE PARKING_NUMBER=1 and AVAILABLE = false");
           		//-- analyse du résultat vrai si il existe au moins 1 résultats
           		ResultSet rs2 = ps2.executeQuery();
           		ParkingMaj = rs2.next();
            // verif visuel dans la console
            System.out.println( ticketExist + " et " + ParkingMaj );

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
        assertEquals( true, ticketExist);
        assertEquals( true, ParkingMaj);
    }
    
    
   
    @Test
    public void testParkingLotExit(){
        testParkingACar();
        Double fare = null;
        String outTime = null;
     	
     	Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();
            //-- Modification de la date d'entrée pour avoir un pris  > 3.00 
            PreparedStatement ps = connection.prepareStatement("update ticket set IN_TIME = ?");
            Date inTime = new Date();	
         	inTime.setTime(System.currentTimeMillis() - (180 * 60 * 1000));        	
         	String dateToStr = DateFormatUtils.format(inTime, "yyyy-MM-dd HH:mm");
            ps.setString(1,dateToStr);
            ps.execute(); 
            System.out.println("\n------------------------------------------------------------------");
            System.out.println("\nUpdate Recorded IN_TIME for vehicle number ABCDEF so that PRICE >3" );
            // --- Test  ----       
            PreparedStatement ps1 =connection.prepareStatement("Select ID,PARKING_NUMBER,VEHICLE_REG_NUMBER,IN_TIME from ticket");
            	//-- résultat vrai si il existe au moins 1 résultats mais ici nous allons récupérer les données dans un ResultSetMetadata
           		ResultSet rs1 = ps1.executeQuery(); 		
    			// On récupère les MetaData
    			ResultSetMetaData resultMeta = rs1.getMetaData();  			
    			System.out.println("\n******************************************************************************************");
    			// On affiche le nom des colonnes de la table 
    			for(int i = 1; i <=  resultMeta.getColumnCount(); i++)
    				System.out.print("\t" + resultMeta.getColumnName(i).toUpperCase() + "\t *"); 			
    			System.out.println("\n******************************************************************************************");			
    			// On affiche les données 
    			while(rs1.next()){			
    				for(int i = 1; i <=  resultMeta.getColumnCount(); i++)
    					System.out.print("\t" + rs1.getObject(i).toString() + "\t |");  				
    				System.out.println("\n----------------------------------------------------------------------------------");
    			}         
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
     	
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);      
        parkingService.processExitingVehicle();
        // Verification des données! ont-elles bien été maj dans la base après le parkingService.processExitingVehicle()?
        //-- Origin -- TODO: check that the fare generated and out time are populated correctly in the database  --
        // Nous avons généré au dessus une nouvelle In_Time avec -3 heures que sera effective en -2h avec le décalage de 1h de la base
        // le PRICE devrait donc être de 3.00
            
        try{
            connection = dataBaseTestConfig.getConnection();         
            // --- Test  ----
          // Le pris est-il enregistré ?
            PreparedStatement ps1 =connection.prepareStatement("Select * from ticket");
            	//-- résultat vrai si il existe au moins 1 résultats mais ici nous allons récupérer les données dans un ResultSetMetadata
           		ResultSet rs2 = ps1.executeQuery();
    			// On récupère les MetaData
    			ResultSetMetaData resultMeta2 = rs2.getMetaData();
    			
    			System.out.println("\n**********************************************************************************************************************************");
    			// On affiche le nom des colonnes de la table 
    			for(int i = 1; i <=  resultMeta2.getColumnCount(); i++)
    				System.out.print("\t" + resultMeta2.getColumnName(i).toUpperCase() + "\t *");   			
    			System.out.println("\n**********************************************************************************************************************************");
    			// On affiche les données
    			while(rs2.next()){			
    				for(int i = 1; i <=  resultMeta2.getColumnCount(); i++)
    					System.out.print("\t" + rs2.getObject(i).toString() + "\t |");
    					
    				System.out.println("\n----------------------------------------------------------------------------------------------------------------------------------");
    				fare = Double.parseDouble(rs2.getObject(4).toString());
    				outTime = rs2.getObject(6).toString();
    				System.out.println(fare);
    			}   
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    	// THEN       
        assertNotNull(outTime, outTime);
     	assertEquals(true, fare>=3);
     	//Double pris = 3.00;
        //assertEquals( pris , fare ); 
        //assertEquals((double) Math.round((2* Fare.CAR_RATE_PER_HOUR) * 100) / 100, fare);     
        
    }

}

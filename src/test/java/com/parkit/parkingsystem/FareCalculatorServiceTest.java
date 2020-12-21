package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FareCalculatorServiceTest {
	@InjectMocks
    private  FareCalculatorService fareCalculatorService;
    private Ticket ticket;
 

    @Mock
    private  TicketDAO ticketDAO; 
    
    @BeforeEach
    private  void setUp() {
    	
        fareCalculatorService = new FareCalculatorService();
        when(ticketDAO.existTicketPassed(anyString())).thenReturn(false);
       
    }
 
    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }
  
    @Test
    public void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
 
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime); 
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.CAR_RATE_PER_HOUR,ticket.getPrice());
    }

    @Test
    public void calculateFareBike(){ 
    	
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    @DisplayName("Test de l'exception Type inconnu de FareCalculatorService")
    public void calculateFareUnkownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);
 
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
      
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
 
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }
 
    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (double)Math.round((0.75 * Fare.BIKE_RATE_PER_HOUR)* 100) / 100  , ticket.getPrice());
    }
 
    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);     
        assertEquals( (double)Math.round((0.75 * Fare.CAR_RATE_PER_HOUR)* 100) / 100  , ticket.getPrice());
    }
 
    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
      
    @Test
    @DisplayName(" Passer 30 minutes ou moins le parking est gratuis pour voiture") 
    public void calculateFareCarWithLessThanThirtyMinutesParkingTime(){
    	// GIVEN
        Date inTime = new Date();
        // with a ramdon, même si c'est pas bien ^^
        int timePark=(int)(Math.random()*(30-0)+1);      
        inTime.setTime( System.currentTimeMillis() - ( timePark * 60 * 1000) );//30 minutes or less parking time should give Free parking Fare (0)
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        // WHEN
        fareCalculatorService.calculateFare(ticket);
        
        // THEN
        assertEquals( (0 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
       
    @Test
    @DisplayName(" Passer 30 minutes ou moins le parking est gratuis pour Vélo")
    public void calculateFareBikeWithLessThanThirtyMinutesParkingTime(){
    	//GIVEN
        Date inTime = new Date();
        // Plus dans la norme avec une liste de valeurs ^^, tests négatifs effectués avec des nombres > 30 
        int[] timePark = {0, 2, 3, 8, 9, 12, 17, 20, 29, 30};
        for ( int i=0; i<timePark.length; i++) {
        inTime.setTime( System.currentTimeMillis() - (  timePark[i] * 60 * 1000) );//30 minutes or less parking time should give Free parking Fare (0)
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        // WHEN
        fareCalculatorService.calculateFare(ticket);
        // THEN 
        assertEquals( (0 * Fare.BIKE_RATE_PER_HOUR) , ticket.getPrice());    
        }
    }
    
    @Test
    @DisplayName(" Pour les vélos à 36 minutes le pris du parking dois être de 0.6*le prix de l'heure")
    public void calculateFareBikeWithThanThirtySixMinutesParkingTime(){
    	//GIVEN
        Date inTime = new Date();
        // Plus dans la norme avec une liste de valeurs ^^, tests négatifs effectués avec des nombres > 30 
        inTime.setTime( System.currentTimeMillis() - (  36 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        // WHEN
        fareCalculatorService.calculateFare(ticket);
        // THEN 
        assertEquals( (0.6 * Fare.BIKE_RATE_PER_HOUR) , ticket.getPrice());       
    }
    
    @Test
    @DisplayName(" Pour les voitures à 36 minutes le pris du parking dois être de 0.6*le prix de l'heure")
    public void calculateFareCARWithThanThirtySixMinutesParkingTime(){
    	//GIVEN
        Date inTime = new Date();
        // Plus dans la norme avec une liste de valeurs ^^, tests négatifs effectués avec des nombres > 30 
        inTime.setTime( System.currentTimeMillis() - (  36 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(3, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        // WHEN
        fareCalculatorService.calculateFare(ticket);
        // THEN 
        assertEquals( (double)Math.round((0.6 * Fare.CAR_RATE_PER_HOUR)* 100) / 100  , ticket.getPrice());
    }
       
    @Test
    @DisplayName("5% dès la deuxième venue via ticketDAO")
    public void calculateFareCarWithFivePourcentReductionParkingTime(){   	
    	//GIVEN	
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (180 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
           
        // WHEN
        fareCalculatorService.calculateFare(ticket);
        // THEN 
        assertEquals( (double)Math.round((0.95*3* Fare.CAR_RATE_PER_HOUR)* 100) / 100  , ticket.getPrice());
        //assertEquals( (double)Math.round((3* Fare.CAR_RATE_PER_HOUR)* 100) / 100  , ticket.getPrice());
    }
   
}

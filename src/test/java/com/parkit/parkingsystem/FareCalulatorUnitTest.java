package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

@ExtendWith(MockitoExtension.class)
public class FareCalulatorUnitTest {
	

	private static FareCalculatorService fareCalculatorService;	
    private Ticket ticket; 
 
    @Mock
    private static TicketDAO ticketDAO;
	 	  		
	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
		fareCalculatorService = new FareCalculatorService(ticketDAO);
    }
	
	@Test
	@DisplayName("5% dès la deuxième venue via ticketDAO")
	public void calculateFareCarWithFivePourcentReductionParkingTime() {
		// GIVEN				
		Date inTime = new Date();	
		inTime.setTime(System.currentTimeMillis() - (180 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);	
		ticket = new Ticket();
		ticket.setVehicleRegNumber("abcd");
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		  
		when(ticketDAO.existTicketPassed(anyString())).thenReturn(true);
		// WHEN		 		
		fareCalculatorService.calculateFare(ticket);
  
		// THEN	
		assertEquals((double)Math.round((0.95*3* Fare.CAR_RATE_PER_HOUR)* 100) / 100, ticket.getPrice());	
	}
}

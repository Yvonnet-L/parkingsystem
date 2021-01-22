package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

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
	@DisplayName("Vérification du tarif pour 1 heure pour une voiture")
	public void calculateFareCar() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// Then
		assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
	}
 
	@Test
	@DisplayName("Vérification du tarif pour 1 heure pour un vélo")
	public void calculateFareBike() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
	}

	@Test
	@DisplayName("Test de l'exception Type NullPointerException de FareCalculatorService pour le type de véhicule")
	public void calculateFareUnkownType() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		// THEN
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	@DisplayName("Test de l'exception IllegalArgumentException de FareCalculatorService pour une date d'entrée dans l'avenir")
	public void calculateFareBikeWithFutureInTime() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		// THEN
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	@DisplayName("Tarif pour moins de 45 min pour un vélo")
	public void calculateFareBikeWithLessThanOneHourParkingTime() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000)); // 45 minutes parking time should give 3/4th //
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((double) Math.round((0.75 * Fare.BIKE_RATE_PER_HOUR) * 100) / 100, ticket.getPrice());
	}

	@Test
	@DisplayName("Tarif pour 45 min pour une voiture")
	public void calculateFareCarWithLessThanOneHourParkingTime() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((double) Math.round((0.75 * Fare.CAR_RATE_PER_HOUR) * 100) / 100, ticket.getPrice());
	}

	@Test
	@DisplayName("Tarif pour 1 jour pour une voiture")
	public void calculateFareCarWithMoreThanADayParkingTime() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));// 24 hours parking time should give 24 *
																			// parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	@DisplayName("Tarif pour 1 jour pour un vélo")
	public void calculateFareBikeWithMoreThanADayParkingTime() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));// 24 hours parking time should give 24 *
																			// parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((24 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	@DisplayName("Tarif pour 6 mois pour une voiture")
	public void calculateFareCarWithMoreThanAYearParkingTime() {
		// GIVEN
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -6);
		calendar.add(Calendar.HOUR, +1); // du au decalage de 1h la base
		Date inTime = calendar.getTime();

		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((184 * 24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	@DisplayName("Tarif pour 1 ans  pour un vélo")
	public void calculateFareBikeWithMoreThanTwoMonthParkingTime() {
		// GIVEN
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		Date inTime = calendar.getTime();

		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((366 * 24 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	@DisplayName("Passer 30 minutes ou moins le parking est gratuis pour voiture")
	public void calculateFareCarWithLessThanThirtyMinutesParkingTime() {
		// GIVEN
		Date inTime = new Date();
		// with a ramdon
		long timePark = (long) (Math.random() * (29 - 0) + 1);
		inTime.setTime(System.currentTimeMillis() - (timePark * 60L * 1000L));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((0 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}
	
	@Test
	@DisplayName(" Passer 30 minutes ou moins le parking est gratuis pour Vélo")
	public void calculateFareBikeWithLessThanThirtyMinutesParkingTime() {
		// GIVEN
		Date inTime = new Date();
		long[] timePark = { 0, 2, 3, 8, 9, 12, 17, 20, 29, 30 };
		for (int i = 0; i < timePark.length; i++) {
			inTime.setTime(System.currentTimeMillis() - (timePark[i] * 60L * 1000L));
			Date outTime = new Date();
			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
			ticket.setInTime(inTime);
			ticket.setOutTime(outTime);
			ticket.setParkingSpot(parkingSpot);
			// WHEN
			fareCalculatorService.calculateFare(ticket);
			// THEN
			assertEquals((0 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
		}
	}

	@Test
	@DisplayName("Pour les vélos à 36 minutes le pris du parking dois être de 0.6*le prix de l'heure")
	public void calculateFareBikeWithThanThirtySixMinutesParkingTime() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (36 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((0.6 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	@DisplayName("Pour les voitures à 36 minutes le pris du parking dois être de 0.6*le prix de l'heure")
	public void calculateFareCARWithThanThirtySixMinutesParkingTime() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (36 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(3, ParkingType.CAR, false);
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((double) Math.round((0.6 * Fare.CAR_RATE_PER_HOUR) * 100) / 100, ticket.getPrice());
	}

	@Test
	@DisplayName("5% dès la deuxième venue via ticketDAO")
	public void calculateFareCarWithFivePourcentReductionParkingTime() {
		// GIVEN
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (180 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket.setVehicleRegNumber("abcd");
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);

		when(ticketDAO.existTicketPassed(anyString())).thenReturn(true);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		verify(ticketDAO, Mockito.times(1)).existTicketPassed(anyString());
		assertEquals((double) Math.round((0.95 * 3 * Fare.CAR_RATE_PER_HOUR) * 100) / 100, ticket.getPrice());

	}

}

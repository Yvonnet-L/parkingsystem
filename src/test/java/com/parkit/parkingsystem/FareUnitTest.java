package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;

public class FareUnitTest {

	Fare fare;
	private Double prixC = 1.5;
	private Double prixB = 1.0;

	@BeforeEach
	private void setUpPerTest() {
		fare = new Fare();
	}

	@Test
	@DisplayName("Test des prix")
	public void testFare() throws InterruptedException {
		// GIVEN
		Double fareC = Fare.CAR_RATE_PER_HOUR;
		Double fareB = Fare.BIKE_RATE_PER_HOUR;
		// THEN
		assertEquals(prixC, fareC);
		assertEquals(prixB, fareB);
	}

}

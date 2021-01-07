package com.parkit.parkingsystem;


import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.DBConstants;

public class DBConstantsUnitTest {

	
	DBConstants dbConstants; 

	@BeforeEach
	private void setUpPerTest() {		
		dbConstants = new DBConstants();
	}

	@Test
	@DisplayName("Test des prix")
	public void testFare() {
		// GIVEN	
		String requete1 = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?"	;
		String res = DBConstants.GET_NEXT_PARKING_SPOT;
		// THEN		
		assertEquals(requete1, res);
	}
	
}

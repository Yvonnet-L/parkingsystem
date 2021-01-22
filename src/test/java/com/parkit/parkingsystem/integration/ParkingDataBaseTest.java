package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseTest {
 
	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
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
	private static void tearDown() {
	}

	@Test
	public void testParkingACar() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		// TODO: check that a ticket is actualy saved in DB and Parking table is updated
		// with availability
		// ------------------------------------------------------------------------------------------------
		// Initialisation des boolean de resultats pour les 2 tables parking et ticket
		boolean ParkingMaj = false;
		boolean ticketExist = false;

		Connection connection = null;
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			connection = dataBaseTestConfig.getConnection();
			// Le ticket est-il enregistré ?
			ps1 = connection
					.prepareStatement("Select * from ticket where PARKING_NUMBER=1 AND VEHICLE_REG_NUMBER='ABCDEF'");
			// -- analyse du résultat vrai si il existe au moins 1 résultats
			rs1 = ps1.executeQuery();
			ticketExist = rs1.next();
			// la table Parking est-elle maj ?
			ps2 = connection.prepareStatement("Select * from parking WHERE PARKING_NUMBER=1 and AVAILABLE = false");
			// -- analyse du résultat vrai si il existe au moins 1 résultats
			rs2 = ps2.executeQuery();
			ParkingMaj = rs2.next();
			// verif visuel dans la console
			System.out.println(ticketExist + " et " + ParkingMaj);

			dataBaseTestConfig.closePreparedStatement(ps2);

			dataBaseTestConfig.closeResultSet(rs2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dataBaseTestConfig.closePreparedStatement(ps1);
			dataBaseTestConfig.closeResultSet(rs1);
			dataBaseTestConfig.closePreparedStatement(ps2);
			dataBaseTestConfig.closeResultSet(rs2);
			dataBaseTestConfig.closeConnection(connection);
		}
		assertEquals(true, ticketExist);
		assertEquals(true, ParkingMaj);
	}

	@Test
	public void testParkingLotExit() {
		testParkingACar();
		Double fare = null;
		String outTime = null;
		Connection connection = null;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		try {
			connection = dataBaseTestConfig.getConnection();
			// -- Modification de la date d'entrée pour avoir un pris > 3.00
			ps = connection.prepareStatement("update ticket set IN_TIME = ?");
			Date inTime = new Date();
			inTime.setTime(System.currentTimeMillis() - (180 * 60 * 1000));
			String dateToStr = DateFormatUtils.format(inTime, "yyyy-MM-dd HH:mm:ss");
			ps.setString(1, dateToStr);
			ps.execute();
			// --- Test ----
			ps1 = connection.prepareStatement("Select ID,PARKING_NUMBER,VEHICLE_REG_NUMBER,IN_TIME from ticket");
			// -- résultat vrai si il existe au moins 1 résultats mais ici nous allons
			// récupérer les données dans un ResultSetMetadata
			rs1 = ps1.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dataBaseTestConfig.closePreparedStatement(ps);
			dataBaseTestConfig.closePreparedStatement(ps1);
			dataBaseTestConfig.closeResultSet(rs1);
			dataBaseTestConfig.closeConnection(connection);
		}

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();
		// Verification des données! ont-elles bien été maj dans la base après le
		// parkingService.processExitingVehicle()?
		// TODO: check that the fare generated and out time are populated correctly in
		// the database --
		// Nous avons généré au dessus une nouvelle In_Time avec -3 heures qui sera
		// effective en -2h avec le décalage de 1h de la base
		// le PRICE devrait donc être de 3.00

		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			connection = dataBaseTestConfig.getConnection();
			// --- Test ----
			// Le pris est-il enregistré ?
			ps2 = connection.prepareStatement("Select * from ticket");
			// -- résultat vrai si il existe au moins 1 résultats mais ici nous allons
			// récupérer les données dans un ResultSetMetadata
			rs2 = ps2.executeQuery();
			// On récupère les MetaData
			ResultSetMetaData resultMeta2 = rs2.getMetaData();			
			// On récupère les données
			while (rs2.next()) {
				for (int i = 1; i <= resultMeta2.getColumnCount(); i++)							
				fare = Double.parseDouble(rs2.getObject(4).toString());
				outTime = rs2.getObject(6).toString();		
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dataBaseTestConfig.closePreparedStatement(ps2);
			dataBaseTestConfig.closeResultSet(rs2);
			dataBaseTestConfig.closeConnection(connection);
		}
		// THEN
		assertNotNull(outTime);
		assertTrue(fare >= 3);
	}

	@Test
	public void testParkingACarWithTicketDAO() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		assertEquals(false, ticketDAO.getTicket("ABCDEF").getParkingSpot().isAvailable());
		assertNotNull(ticketDAO.getTicket("ABCDEF").getParkingSpot().getId());
		assertNotNull(ticketDAO.getTicket("ABCDEF").getId());
		assertNotNull(ticketDAO.getTicket("ABCDEF").getInTime());
		assertNotNull(ticketDAO.getTicket("ABCDEF").getParkingSpot().getParkingType());
		assertNull(ticketDAO.getTicket("ABCDEF").getOutTime());
	}

	@Test
	public void testParkingLotExitWithTicketDAO() {
		testParkingACarWithTicketDAO();
		try {
			Thread.sleep(300);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();

		assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
		assertNotNull(ticketDAO.getTicket("ABCDEF").getPrice());
	}

}

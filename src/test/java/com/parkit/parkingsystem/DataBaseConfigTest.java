package com.parkit.parkingsystem;

import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.config.DataBaseConfig;

@ExtendWith(MockitoExtension.class)
public class DataBaseConfigTest {
   
	private DataBaseConfig databaseconfig;
    
	@Mock
	private Connection con;
 
	@Mock
	private PreparedStatement ps;
	@Mock
	private ResultSet rs;
  
	@BeforeEach
	private void setUpPerTest() {
		databaseconfig = new DataBaseConfig();
	}

	@Test
	@DisplayName("Test du close connection")
	public void testCloseConDataBaseCongig() throws SQLException {
		// GIVEN
		databaseconfig.closeResultSet(rs);
		databaseconfig.closePreparedStatement(ps);
		databaseconfig.closeConnection(con);
		// THEN
		verify(rs, Mockito.times(1)).close();
		verify(ps, Mockito.times(1)).close();
		verify(con, Mockito.times(1)).close();	
	}
}
package com.parkit.parkingsystem.service;


import java.util.concurrent.TimeUnit;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
 
	private  TicketDAO ticketDAO;
	 
	public FareCalculatorService(TicketDAO ticketDAO) {
		super();
		this.ticketDAO = ticketDAO;
	}

	public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
              
        // double duration = outHour - inHour : is synthesized here on these 3 lines 
        long diffInMillies = Math.abs(ticket.getOutTime().getTime() - ticket.getInTime().getTime());
        double duration= TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        duration = duration/3600000; 
        
        //if the duration is less than or equal to 30 minutes (duration <= 0.5) 
        //then parking will be offered so duration = 0
    	String plaque = ticket.getVehicleRegNumber(); 
        if ( duration <= 0.5 ) {
        	duration = 0;
        }else {
	        	//the recurring user benefits from a 5% discount
        		Boolean exist = false; 
        		exist =	ticketDAO.existTicketPassed(plaque); 
        		if ( exist == true){       	
        			System.out.println("the recurring user benefits from a 5% discount" +"\r\n");
        			duration = duration*0.95;
        		}   
        }  
        //----------------------------------------------------------------------
        double price;
        switch (ticket.getParkingSpot().getParkingType()){  
            case CAR: {
                price = (double)Math.round((duration * Fare.CAR_RATE_PER_HOUR)* 100) / 100;
                ticket.setPrice(price);
                break;
            }
            case BIKE: {
                price =(double) Math.round((duration * Fare.BIKE_RATE_PER_HOUR)* 100) / 100;
                ticket.setPrice(price);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}
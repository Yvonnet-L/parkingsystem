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
       
		String plaque = ticket.getVehicleRegNumber(); 
         
        //double duration = outHour - inHour; 
        
        double diffInMillies = Math.abs(ticket.getOutTime().getTime() - ticket.getInTime().getTime());
        //double duration= TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        double duration = diffInMillies;
        duration = duration/3600000; 
          
        //if the duration is less than or equal to 30 minutes (duration <= 0.5) 
        //then parking will be offered so duration = 0
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
        //double price = (double) Math.round(ticket.getPrice() * 100) / 100;
        switch (ticket.getParkingSpot().getParkingType()){
          
            case CAR: {
            	double price;
                price = (double)Math.round((duration * Fare.CAR_RATE_PER_HOUR)* 100) / 100;
                ticket.setPrice(price);
                break;
            }
            case BIKE: {
            	double price;
                price =(double) Math.round((duration * Fare.BIKE_RATE_PER_HOUR)* 100) / 100;
                ticket.setPrice(price);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}
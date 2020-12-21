package com.parkit.parkingsystem.service;


import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
 
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        // Code d'origine
        //int inHour = ticket.getInTime().getHours();
        //int outHour = ticket.getOutTime().getHours();
        
         
        Date inHour = new Date();
        Date outHour = new Date();
        inHour = ticket.getInTime();
        outHour = ticket.getOutTime();
        
         
        //double duration = outHour - inHour; 
        
        long diffInMillies = Math.abs(outHour.getTime() - inHour.getTime());
        double duration= TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        duration = duration/3600000; 
        
        //if the duration is less than or equal to 30 minutes (duration <= 0.5) 
        //then parking will be offered so duration = 0
        if ( duration <= 0.5 ) {
        	duration = 0;
        }
        //the recurring user benefits from a 5% discount
        TicketDAO ticketDAO = new TicketDAO() ; 
        if (ticketDAO.existTicketPassed(ticket.getVehicleRegNumber()) == true){
        	duration = duration * 0.95;
        	System.out.println("the recurring user benefits from a 5% discount ^^");
        	if (duration == 0) {
        		System.out.println("but today you stayed less than 30 minutes ;-) !" +"\r\n");
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
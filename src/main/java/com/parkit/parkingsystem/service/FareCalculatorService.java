package com.parkit.parkingsystem.service;


import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
 
        //int inHour = ticket.getInTime().getHours();
        //int outHour = ticket.getOutTime().getHours();
        
        
        Date inHour = new Date();
        Date outHour = new Date();
        inHour = ticket.getInTime();
        outHour = ticket.getOutTime();
        
 
        //TODO: Some tests are failing here. Need to check if this logic is correct
        //double duration = outHour - inHour; 
        
        long diffInMillies = Math.abs(outHour.getTime() - inHour.getTime());
        double duration= TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        duration = duration/3600000; 

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}
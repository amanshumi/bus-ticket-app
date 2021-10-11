package com.example.cardapp;

public class Ticket {

    public int ticketId, ticketNumber, companyId, busId, seatAvailable;
    double ticketPrice;
    String origin, destination, busImg, travelDate, busName;
    boolean isValid;

    public Ticket() {

    }

//    public Ticket(int ticketId, String busName, String departingTime, int ticketPrice) {
//        this.ticketId = ticketId;
//        this.busName = busName;
//        this.departingTime = departingTime;
//        this.ticketPrice = ticketPrice;
//    }

    public Ticket(int ticketId, String busName, int busId, int seatAvailable, double ticketPrice, String travelDate, String origin, String destination, String busImg) {
        this.ticketId = ticketId;
        this.busName = busName;
        this.busId = busId;
        this.travelDate = travelDate;
        this.seatAvailable = seatAvailable;
        this.ticketPrice = ticketPrice;
        this.origin = origin;
        this.destination = destination;
        this.busImg = busImg;
    }

    public String getBusName() {
        return busName;
    }

    public String getTravelDate() {
        return travelDate;
    }

    public int getTicketId() {
        return ticketId;
    }

    public int getBusId() {
        return busId;
    }

    public int getSeatAvailable() {
        return seatAvailable;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getBusImg (){
        return busImg;
    }
}

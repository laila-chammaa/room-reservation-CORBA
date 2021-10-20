package client;

import common.Timeslot;

import static common.CampusID.*;

public class StudentDriver {

    public static void main(String[] args) {
        String sid1 = "DVLS1234";
        String sid2 = "DVLS2234";
        String sid3 = "DVLA1234";
        String sid4 = "DVLD1234";

        Timeslot timeSlot = new Timeslot(19, 20);
        Timeslot timeSlot2 = new Timeslot(2, 15);
        Timeslot timeSlot3 = new Timeslot(12, 13);
        Timeslot invalidTimeSlot = new Timeslot(26, 20);

        try {
            StudentClient testClient1 = new StudentClient(args, sid1);
//            StudentClient testClient2 = new StudentClient(args, sid2);
//            StudentClient adminClient = new StudentClient(args, sid3);
//            StudentClient invalidClient = new StudentClient(args, sid4);

//            String serverTimeSlot = testClient1.getAvailableTimeSlot("03/01/2020");
//            System.out.println(serverTimeSlot);

            //testing invalid parameters:
//            testClient1.bookRoom(KKL, 201, "03/01/2020", invalidTimeSlot);
//            adminClient.bookRoom(KKL, 201, "03/01/2020", timeSlot);

            //testing max booking for student
            String bookingID = testClient1.bookRoom(DVL, 201, "03/01/2020", timeSlot);
//            testClient1.bookRoom(WST, 211, "04/01/2020", timeSlot);
//          testClient1.bookRoom(DVL, 203, "01/01/2020", timeSlot);
//            testClient1.bookRoom(DVL, 203, "01/01/2020", timeSlot3);
//            //testClient1.bookRoom(DVL, 203, "01/01/2020", timeSlot3);
//
            testClient1.cancelBooking(bookingID); //shouldn't work since it's not the student who booked
//            testClient1.cancelBooking(bookingID);
//            testClient1.bookRoom(DVL, 203, "01/01/2020", timeSlot3);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
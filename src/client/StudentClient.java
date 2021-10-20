package client;

import common.ServerInterface;
import common.ServerInterfaceHelper;
import common.Timeslot;
import model.CampusID;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.logging.Logger;

import static client.ClientLogUtil.initiateLogger;

public class StudentClient {

    private String studentID;
    private CampusID campusID;
    private Logger logger;
    private ServerInterface servant;
    private ORB orb;

    static final int USER_TYPE_POS = 3;

    public StudentClient(String[] args, String userID) throws Exception {
        validateStudent(userID);

        try {
            this.logger = initiateLogger(campusID, userID);
        } catch (Exception e) {
            throw new Exception("Login Error: Invalid ID.");
        }

        try {
            // create and initialize the ORB
            orb = ORB.init(args, null);
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is
            // part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            servant = ServerInterfaceHelper.narrow(ncRef.resolve_str(campusID.toString()));

//            servant.shutdown();

        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
        }


        System.out.println("Login Succeeded. | Student ID: " +
                this.studentID + " | Campus ID: " + this.campusID.toString());
    }

    private void validateStudent(String userID) throws Exception {
        char userType = userID.charAt(USER_TYPE_POS);
        String campusName = userID.substring(0, USER_TYPE_POS);

        if (userType != 'S') {
            throw new Exception("Login Error: This client is for students only.");
        }
        this.studentID = userID;

        try {
            this.campusID = CampusID.valueOf(campusName);
        } catch (Exception e) {
            throw new Exception("Login Error: Invalid ID.");
        }
    }

    public synchronized String bookRoom(common.CampusID campusID, int roomNumber, String date,
                                        Timeslot timeSlot) throws Exception {

        this.logger.info(String.format("Client Log | Request: bookRoom | Campus: %s | StudentID: %s | " +
                        "Room number: %d | Date: %s | Timeslot: %s", CampusID.valueOf(campusID.name()), studentID, roomNumber, date,
                timeSlot.toString()));
        String result = servant.bookRoom(studentID, campusID, (short) roomNumber, date, timeSlot);
        this.logger.info(result);
        if (result.contains("BookingID: ")) {
            return result.substring(result.indexOf("ID: ") + 4);
        }
        return null;
    }

    public synchronized String getAvailableTimeSlot(String date) throws Exception {
        this.logger.info(String.format("Client Log | Request: getAvailableTimeSlot | Date: %s", date));
        try {
            return servant.getAvailableTimeSlot(date);
        } catch (Exception e) {
            this.logger.warning(e.getMessage());
            return "";
        }
    }

    public synchronized void cancelBooking(String bookingID) throws Exception {
        this.logger.info(String.format("Client Log | Request: cancelBooking | StudentID: %s | " +
                "BookingID: %s", studentID, bookingID));

        this.logger.info(servant.cancelBooking(studentID, bookingID));
    }

}

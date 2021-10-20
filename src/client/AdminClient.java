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

public class AdminClient {

    private String adminID;
    private CampusID campusID;
    private Logger logger;
    private ServerInterface servant;
    private ORB orb;

    private static final int USER_TYPE_POS = 3;
    private static final int CAMPUS_NAME_POS = 3;

    public AdminClient(String userID) throws Exception {
        validateAdmin(userID);
        try {
            this.logger = initiateLogger(campusID, userID);
        } catch (Exception e) {
            throw new Exception("Login Error: Invalid ID.");
        }

        try {
            // create and initialize the ORB
            //orb = ORB.init(args, null);
            orb = ORB.init();
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is
            // part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            servant = ServerInterfaceHelper.narrow(ncRef.resolve_str(campusID.toString()));

            servant.shutdown();

        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
        }

        System.out.println("Login Succeeded. | Admin ID: " +
                this.adminID + " | Campus ID: " + this.campusID.toString());
    }

    private void validateAdmin(String userID) throws Exception {
        char userType = userID.charAt(USER_TYPE_POS);
        String campusName = userID.substring(0, CAMPUS_NAME_POS);

        if (userType != 'A') {
            throw new Exception("Login Error: This client is for admins only.");
        }
        this.adminID = userID;

        try {
            this.campusID = CampusID.valueOf(campusName);
        } catch (Exception e) {
            throw new Exception("Login Error: Invalid ID.");
        }
    }

    public synchronized void createRoom(short roomNumber, String date,
                                        Timeslot[] listOfTimeSlots) throws Exception {
        this.logger.info(String.format("Client Log | Request: createRoom | AdminID: %s | Room number: %d | Date: %s",
                adminID, roomNumber, date));
        this.logger.info(servant.createRoom(adminID, roomNumber, date, listOfTimeSlots));
    }

    public synchronized void deleteRoom(short roomNumber, String date, Timeslot[] listOfTimeSlots)
            throws Exception {
        this.logger.info(String.format("Client Log | Request: deleteRoom | AdminID: %s | Room number: %d | Date: %s",
                adminID, roomNumber, date));
        this.logger.info(servant.deleteRoom(adminID, roomNumber, date, listOfTimeSlots));
    }
}

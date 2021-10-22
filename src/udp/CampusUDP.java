package udp;

import common.ServerInterface;
import common.ServerInterfaceHelper;
import common.ServerInterfacePOA;
import model.CampusID;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;

public class CampusUDP implements CampusUDPInterface {
    private static final long serialVersionUID = 1L;


    private String studentID;
    private String bookingID;
    private short newRoomNo;
    private common.Timeslot newTimeSlot;
    private common.CampusID newCampusID;

    private String operationType;
    private boolean transferStatus = false;
    private int availableTimeSlot;

    //Constructor for inter-campus booking change
    public CampusUDP(String studentID, String bookingId, common.CampusID newCampusName, short newRoomNo,
                     common.Timeslot newTimeSlot) {
        this.studentID = studentID;
        this.bookingID = bookingId;
        this.newCampusID = newCampusName;
        this.newRoomNo = newRoomNo;
        this.newTimeSlot = newTimeSlot;

        this.operationType = "bookingChange";
    }

    //Constructor for get total available time slots.
    public CampusUDP() {
        this.operationType = "getAvailableTimeSlots";
    }

    public boolean isTransferStatus() {
        return transferStatus;
    }

    public int getLocalAvailableTimeSlot() {
        return availableTimeSlot;
    }

    @Override
    public void execute(ServerInterfacePOA server, CampusID campusID) {
        Properties sysProperties = System.getProperties();

        sysProperties.setProperty("org.omg.CORBA.ORBClass", "com.sun.corba.se.internal.POA.POAORB");
        sysProperties.setProperty("org.omg.CORBA.ORBSingletonClass", "com.sun.corba.se.internal.corba.ORBSingleton");

        sysProperties.put("org.omg.CORBA.ORBInitialHost", "localhost");
        sysProperties.put("org.omg.CORBA.ORBInitialPort", "1050");

        ORB orb = ORB.init(new String[1], sysProperties);

        try {
            org.omg.CORBA.Object objNS = orb.resolve_initial_references("NameService");
            NamingContextExt namingContext = NamingContextExtHelper.narrow(objNS);

            org.omg.CORBA.Object objBranch = namingContext.resolve_str(campusID.toString());
            ServerInterface campusServer = ServerInterfaceHelper.narrow(objBranch);

            if (this.operationType.equals("fundTransfer")) {
                String resultLog = campusServer.changeReservation(studentID, bookingID, newCampusID, newRoomNo, newTimeSlot);
                transferStatus = resultLog.contains("success");
            } else if (this.operationType.equals("getTotalClients")) {
                availableTimeSlot = campusServer.getLocalAvailableTimeSlot();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
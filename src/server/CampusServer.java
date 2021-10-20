package server;

import model.CampusID;
import common.ServerInterface;
import common.ServerInterfacePOA;
import common.Timeslot;
import model.Booking;
import org.omg.CORBA.ORB;
import udp.UDPServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class CampusServer extends ServerInterfacePOA {
    private static final int MAX_NUM_BOOKING = 3;
    private static final int USER_TYPE_POS = 3;

    //Variable for each separate bank server
    private CampusID campusID;

    private static ArrayList<HashMap<String, Integer>> stuBkngCntMap;

    private static int recordIdCount = 1;

    private HashMap<String, Map.Entry<String, Integer>> roomRecords;
    private HashMap<String, List<Booking>> bookingRecords;

    private Logger logger;

    //CORBA Variables
    private ORB orb;
    private int UDPPort;
    private String UDPHost;

    //UDP Server for listening incoming requests
    private udp.UDPServer UDPServer;

    //Holds other servers' addresses : ["ServerName", "hostName:portNumber"]
    HashMap<String, String> serversList;

    public CampusServer(CampusID campusID, ORB orb, String host, int port, HashMap<String, String> serversList) {
        this.campusID = campusID;
        this.orb = orb;
        this.UDPHost = host;
        this.UDPPort = port;
        this.serversList = serversList;

        this.UDPServer = new UDPServer(UDPHost, UDPPort, this);

        this.roomRecords = new HashMap<>();
        this.bookingRecords = new HashMap<>();

        stuBkngCntMap = new ArrayList<>(55);
        for (int i = 0; i < 55; i++)
            stuBkngCntMap.add(new HashMap<>());

        initiateLogger();
        this.logger.info("Initializing Server ...");


        this.logger.info("Server: " + campusID + " initialization success.");
        this.logger.info("Server: " + campusID + " port is : " + UDPPort);
    }

    private void initiateLogger() {
        Logger logger = Logger.getLogger("Server Logs/" + this.campusID.toString() + "- Server Log");
        FileHandler fh;

        try {
            //FileHandler Configuration and Format Configuration
            fh = new FileHandler("Server Logs/" + this.campusID + " - Server Log.log");

            //Disable console handling
            //logger.setUseParentHandlers(false);
            logger.addHandler(fh);

            //Formatting configuration
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            System.err.println("Server Log: Error: Security Exception " + e);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Server Log: Error: IO Exception " + e);
            e.printStackTrace();
        }

        System.out.println("Server Log: Logger initialization success.");

        this.logger = logger;
    }

    @Override
    public String createRoom(String adminID, short roomNumber, String date, common.Timeslot[] listOfTimeSlots) {
        String resultLog;
        resultLog = validateAdmin(adminID);
        if (resultLog != null) {
            return resultLog;
        }
        resultLog = validateTimeSlot(listOfTimeSlots);
        if (resultLog != null) {
            return resultLog;
        }

        this.logger.info(String.format("Server Log | Request: createRoom | AdminID: %s | Room number: %d | Date: %s",
                adminID, roomNumber, date));

        //TODO: null checks
        Optional<Map.Entry<String, Map.Entry<String, Integer>>> record = roomRecords.entrySet().stream()
                .filter(h -> h.getValue().getKey().equals(date) && h.getValue().getValue() == roomNumber).findFirst();
        if (record.isPresent()) {
            String recordID = record.get().getKey();
            resultLog = updateRecord(recordID, listOfTimeSlots);
        } else {
            resultLog = createRecord(roomNumber, date, listOfTimeSlots);
        }
        return resultLog;
    }

    private String createRecord(int roomNumber, String date, Timeslot[] listOfTimeSlots) {
        String resultLog;
        String recordID = "RR" + String.format("%05d", recordIdCount);
        incrementRecordID();
        while (roomRecords.get(recordID) != null) {
            incrementRecordID();
            recordID = "RR" + recordIdCount;
        }
        roomRecords.put(recordID, new AbstractMap.SimpleEntry<>(date, roomNumber));
        List<Booking> newBookings = new ArrayList<>();
        for (Timeslot slot : listOfTimeSlots) {
            newBookings.add(new Booking(recordID, null, slot));
        }
        bookingRecords.put(recordID, newBookings);
        resultLog = String.format("Server Log | Room record %s was created successfully", recordID);
        this.logger.info(resultLog);
        return resultLog;
    }

    private String updateRecord(String recordID, Timeslot[] listOfTimeSlots) {
        String resultLog;
        List<Booking> previousBookings = bookingRecords.get(recordID);
        List<Booking> newBookings = new ArrayList<>(previousBookings);
        for (Timeslot slot : listOfTimeSlots) {
            newBookings.add(new Booking(recordID, null, slot));
        }
        bookingRecords.put(recordID, newBookings);
        resultLog = String.format("Server Log | Room record %s was already created. " +
                "It was updated successfully", recordID);
        this.logger.info(resultLog);
        return resultLog;
    }

    @Override
    public String deleteRoom(String adminID, short roomNumber, String date, common.Timeslot[] listOfTimeSlots) {

        String resultLog;
        resultLog = validateAdmin(adminID);
        if (resultLog != null) {
            return resultLog;
        }
        resultLog = validateTimeSlot(listOfTimeSlots);
        if (resultLog != null) {
            return resultLog;
        }

        this.logger.info(String.format("Server Log | Request: deleteRoom | AdminID: %s | Room number: %d | Date: %s",
                adminID, roomNumber, date));

        //TODO: null checks
        Optional<Map.Entry<String, Map.Entry<String, Integer>>> record = roomRecords.entrySet().stream()
                .filter(h -> h.getValue().getKey().equals(date) && h.getValue().getValue() == roomNumber).findFirst();
        if (record.isPresent()) {
            String recordID = record.get().getKey();
            List<Booking> removedBookings = bookingRecords.get(recordID)
                    .stream().filter(b -> Arrays.asList(listOfTimeSlots).contains(b.getTimeslot())).collect(Collectors.toList());
            bookingRecords.get(recordID).removeIf(removedBookings::contains);
            for (Booking removedBooking : removedBookings) {
                if (removedBooking.getBookedBy() != null) {
                    //booked by the student, reducing the student's bookingCount
                    setStuBookingCnt(removedBooking.getBookedBy(), date, -1);
                }
            }
            resultLog = String.format("Server Log | Room record %s was deleted successfully", recordID);
            this.logger.info(resultLog);
        } else {
            resultLog = String.format("Server Log | ERROR: Room was not found | Request: deleteRoom | " +
                    "Room number: %d | Date: %s", roomNumber, date);
            this.logger.warning(resultLog);
        }
        return resultLog;
    }

    @Override
    public String bookRoom(String studentID, common.CampusID campusIDCorba, short roomNumber, String date,
                           common.Timeslot timeslot) {

        CampusID campusID = CampusID.valueOf(campusIDCorba.name());

        String resultLog;
        resultLog = validateStudent(studentID);
        if (resultLog != null) {
            return resultLog;
        }
        resultLog = validateTimeSlot(new Timeslot[]{timeslot});
        if (resultLog != null) {
            return resultLog;
        }

        //forward request to other server
        if (campusID != this.campusID) {
            ServerInterface otherServer = null; //TODO: how to find other server
            try {
                this.logger.info(String.format("Server Log | Forwarding Request to %s Server: bookRoom | StudentID: %s " +
                                "| Room number: %d | Date: %s | Timeslot: %s", campusID.toString(), studentID, roomNumber,
                        date, timeslot.toString()));
//                otherServer = (ServerInterface) registry.lookup(campusID.toString());
                common.CampusID corbaCampusID = common.CampusID.valueOf(campusID.name());
                return otherServer.bookRoom(studentID, corbaCampusID, roomNumber, date, timeslot);
            } catch (Exception e) {
                resultLog = "Server Log | Request: bookRoom | ERROR: " + campusID.toString() + " Not Bound.";
                this.logger.severe(resultLog);
                return resultLog;
            }
        }
        this.logger.info(String.format("Server Log | Request: bookRoom | StudentID: %s | " +
                "Room number: %d | Date: %s | Timeslot: %s", studentID, roomNumber, date, timeslot.toString()));

        if (getStuBookingCnt(studentID, date) >= MAX_NUM_BOOKING) {
            resultLog = String.format("Server Log | ERROR: Booking limit (%d) for the week was reached | " +
                    "StudentID %s", MAX_NUM_BOOKING, studentID);
            this.logger.warning(resultLog);
            return resultLog;
        }
        //TODO: null check
        Optional<Map.Entry<String, Map.Entry<String, Integer>>> record = roomRecords.entrySet().stream()
                .filter(h -> h.getValue().getKey().equals(date) && h.getValue().getValue() == roomNumber).findFirst();
        if (record.isPresent()) {
            String recordID = record.get().getKey();
            Optional<Booking> booking = bookingRecords.get(recordID)
                    .stream().filter(b -> b.getTimeslot().equals(timeslot)).findFirst();
            if (booking.isPresent() && booking.get().getBookedBy() == null) {
                booking.get().book(studentID);
                setStuBookingCnt(studentID, date, 1);
                String bookingID = booking.get().getBookingID();
                resultLog = String.format("Server Log | Room record %s was booked successfully. BookingID: %s",
                        recordID, bookingID);
                this.logger.info(resultLog);
            } else {
                resultLog = String.format("Server Log | ERROR: Time slot was not available | Request: bookRoom | " +
                        "Room number: %d | Date: %s | Timeslot: %s", roomNumber, date, timeslot.toString());
                this.logger.warning(resultLog);
            }

        } else {
            resultLog = String.format("Server Log | ERROR: Room was not found | Request: bookRoom | " +
                    "Room number: %d | Date: %s", roomNumber, date);
            this.logger.warning(resultLog);
        }
        return resultLog;
    }

    @Override
    public String cancelBooking(String studentID, String bookingID) {

        String resultLog;
        resultLog = validateStudent(studentID);
        if (resultLog != null) {
            return resultLog;
        }

        this.logger.info(String.format("Server Log | Request: cancelBooking | StudentID: %s | " +
                "BookingID: %s", studentID, bookingID));
        List<Booking> bookingList = bookingRecords.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Optional<Booking> booking = bookingList.stream().filter(b -> b.getBookingID() != null &&
                b.getBookingID().equals(bookingID)).findFirst();
        if (booking.isPresent() && booking.get().getBookedBy().equals(studentID)) {
            booking.get().setBookedBy(null);
            booking.get().setBookingID(null);
            resultLog = String.format("Server Log | Booking %s was cancelled successfully.", bookingID);
            String date = roomRecords.get(booking.get().getRecordID()).getKey();
            setStuBookingCnt(studentID, date, -1);
            this.logger.info(resultLog);
        } else {
            resultLog = String.format("Server Log | ERROR: Booking was not found | Request: cancelBooking | BookingID: %s",
                    bookingID);
            this.logger.warning(resultLog);
        }
        return resultLog;
    }

    //Account Format: QCMA1234
    //Account Format: [Branch ID][AccountType][Last Name 1st Letter][4 Digits]
    @Override
    public synchronized String changeReservation(String bookingId, common.CampusID newCampusName, short newRoomNo, common.Timeslot newTimeSlot) {
        return null;
    }


    @Override
    public int getUDPPort() {
        return UDPPort;
    }

    @Override
    public String getUDPHost() {
        return UDPHost;
    }

    //Will destroy ORB and stop UDPServer from listening requests.
    @Override
    public void shutdown() {
        this.orb.shutdown(false);
        this.UDPServer.stop();
    }

    @Override
    public String getAvailableTimeSlot(String date) {
        this.logger.info(String.format("Server Log | Request: getAvailableTimeSlot | Date: %s", date));

        HashMap<CampusID, Integer> totalTimeSlotCount = new HashMap<>();
        int localTimeSlotCount = getLocalAvailableTimeSlot();
        totalTimeSlotCount.put(this.campusID, localTimeSlotCount);

        DatagramSocket socket;
        String resultLog;

        //1. Create UDP Socket
        try {
            socket = new DatagramSocket(this.UDPPort);
            String[] campusServers = serversList.keySet().toArray(new String[0]);

            //2. Get RMI Registry List of other servers.
            for (String campusServer : campusServers) {
                if (campusServer.equals(this.campusID.toString())) {
                    continue;
                }

                ServerInterface otherServer;
                Integer rData = 0;

                totalTimeSlotCount.put(CampusID.valueOf(campusServer), rData);
                resultLog = "Server Log | Getting the available timeslots was successful.";
                this.logger.info(resultLog);
            }
            socket.close();
        } catch (SocketException e) {
            this.logger.severe("Server Log | getAvailableTimeSlot() ERROR: " + e.getMessage());
            //TODO: throw?
//            throw new Exception(e.getMessage());
        }

        return totalTimeSlotCount.toString();
    }

    @Override
    public int getLocalAvailableTimeSlot() {
        List<Booking> bookingList = bookingRecords.values().stream().flatMap(List::stream).collect(Collectors.toList());
        List<Booking> nullBookings = bookingList.stream().filter(b -> b.getBookedBy() == null).collect(Collectors.toList());
        return nullBookings.size();
    }

    private int getStuBookingCnt(String studentID, String d) {
        Calendar cal = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(d);
        } catch (ParseException e) {
            e.printStackTrace(); //TODO: better handling
        }
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);

        HashMap<String, Integer> stuMap = stuBkngCntMap.get(week);
        Integer cnt = stuMap.get(studentID);
        if (cnt == null)
            return 0;
        else
            return cnt;
    }

    private void setStuBookingCnt(String studentID, String d, int offset) {
        Calendar cal = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);

        HashMap<String, Integer> stuMap = stuBkngCntMap.get(week);
        Integer cnt = stuMap.get(studentID);
        if (cnt == null) cnt = 0;
        if (offset > 0) {
            stuMap.put(studentID, ++cnt);
        } else {
            stuMap.put(studentID, --cnt);
        }
    }

    private synchronized static void incrementRecordID() {
        recordIdCount++;
    }

    private String validateAdmin(String userID) {
        char userType = userID.charAt(USER_TYPE_POS);
        if (userType != 'A') {
            return "Login Error: This request is for admins only.";
        }
        return null;
    }

    private String validateStudent(String userID) {
        char userType = userID.charAt(USER_TYPE_POS);
        if (userType != 'S') {
            return "Login Error: This request is for students only.";
        }
        return null;
    }

    private String validateTimeSlot(Timeslot[] listOfTimeSlots) {
        for (Timeslot slot : listOfTimeSlots) {
            if (slot.start < 0 || slot.start >= 24 || slot.end < 0 ||
                    slot.end >= 24 || slot.start >= slot.end) {
                return "Invalid timeslot format. Use the 24h clock.";
            }
        }
        return null;
    }

    public CampusID getCampusID() {
        return this.campusID;
    }
}

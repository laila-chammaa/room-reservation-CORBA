package server;

import common.ServerInterface;
import common.ServerInterfaceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.HashMap;
import java.util.Properties;

import static common.CampusID.*;

public class ServerDriver {
    public static void main(String[] args) {
        HashMap<String, String> serverDetails = new HashMap<>();
        HashMap<String, CampusServer> serverDirectory = new HashMap<>();

        String[] serverList = {"KKL", "DVL", "WST"};

        try {
            Properties properties = new Properties();

            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");

            //1. Create and initialize ORB
            ORB orb = ORB.init(args, properties);

            //2. Get reference to RootPOA and activate the POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            //3.0 Get the root naming context.
            //3.1 NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

            //4.0 Use NamingContextExt which is part of the Interoperable
            //4.1 Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            //5. Create servers details
            serverDetails.put("KKL", "localhost:30100");
            serverDetails.put("DVL", "localhost:30200");
            serverDetails.put("WST", "localhost:30300");

            //6.0 Put the servers details in a HashMap
            //6.1 Create the servers
            CampusServer serverKKL = new CampusServer(KKL, orb, "localhost", 30100, serverDetails);
            CampusServer serverDVL = new CampusServer(DVL, orb, "localhost", 30200, serverDetails);
            CampusServer serverWST = new CampusServer(WST, orb, "localhost", 30300, serverDetails);

            serverDirectory.put("KKL", serverKKL);
            serverDirectory.put("DVL", serverDVL);
            serverDirectory.put("WST", serverWST);

            for (String serverName : serverList) {
                //7. Get object reference from the servant
                org.omg.CORBA.Object ref = rootPOA.servant_to_reference(serverDirectory.get(serverName));

                //8. Cast the reference to a CORBA reference
                ServerInterface href = ServerInterfaceHelper.narrow(ref);

                //9. Bind the Object Reference in Naming
                NameComponent[] path = ncRef.to_name(serverName);
                ncRef.rebind(path, href);

                System.out.println("ServerDriver Log: | Campus Server: " + serverName + " initialized.");
            }

            //10. Wait for invocation from clients
            orb.run();
        } catch (Exception e) {
            System.err.println("ServerDriver Log: Server initialization failure | Error: " + e.getMessage());
            e.printStackTrace(System.out);
        }

    }
}

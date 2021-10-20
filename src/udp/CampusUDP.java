package udp;

import common.ServerInterface;
import common.ServerInterfaceHelper;
import common.ServerInterfacePOA;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;

public class CampusUDP implements CampusUDPInterface {
    private static final long serialVersionUID = 1L;

    private String clientIDSource;
    private String clientIDDest;
    private float amount;

    private String operationType;
    private boolean transferStatus = false;
    private int totalClientsCount;

    //Constructor for inter-campus fund transfer
    public CampusUDP(String clientIDSource, String clientIDDest, float amount) {
        this.clientIDSource = clientIDSource;
        this.clientIDDest = clientIDDest;
        this.amount = amount;

        this.operationType = "fundTransfer";
    }

    //Constructor for get total client numbers.
    public CampusUDP() {
        this.operationType = "getTotalClients";
    }

    public boolean isTransferStatus() {
        return transferStatus;
    }

    public int getTotalClientsCount() {
        return totalClientsCount;
    }

    @Override
    public void execute(ServerInterfacePOA server, common.CampusID campusID) {
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
            ServerInterface bankServer = ServerInterfaceHelper.narrow(objBranch);

//            if (this.operationType.equals("fundTransfer")) {
//                transferStatus = bankServer.transferFund(clientIDSource, amount, clientIDDest);
//            } else if (this.operationType.equals("getTotalClients")) {
//                totalClientsCount = bankServer.getLocalAccountCount();
//            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
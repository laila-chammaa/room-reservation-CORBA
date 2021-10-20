package udp;

import java.io.Serializable;

import common.ServerInterfacePOA;
import common.CampusID;

public interface CampusUDPInterface extends Serializable {
    void execute(ServerInterfacePOA campusServer, CampusID campusID);
}

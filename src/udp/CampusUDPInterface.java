package udp;

import java.io.Serializable;

import common.ServerInterfacePOA;
import model.CampusID;

public interface CampusUDPInterface extends Serializable {
    void execute(ServerInterfacePOA campusServer, CampusID campusID);
}

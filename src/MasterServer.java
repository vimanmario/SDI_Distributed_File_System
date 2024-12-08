import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
public class MasterServer extends UnicastRemoteObject implements MasterServerInterface {
    private Map<String, ReplicaLoc> replicaLocations = new HashMap<>();
    private Map<String, Boolean> replicaStatus = new HashMap<>();  // Pentru a urmări statusul replicilor

    public MasterServer() throws RemoteException {
        super();
    }

    @Override
    public synchronized void registerReplicaServer(String name, ReplicaLoc location) throws RemoteException {
        replicaLocations.put(name, location);
        replicaStatus.put(name, true);  // Replica este activă când se înregistrează
        System.out.println("Registered replica: " + name + " at " + location.getHost());
    }

    @Override
    public synchronized List<ReplicaLoc> getReplicaLocations(String fileName) throws RemoteException {
        List<ReplicaLoc> activeReplicas = new ArrayList<>();
        // Returnează doar replicile active
        for (Map.Entry<String, Boolean> entry : replicaStatus.entrySet()) {
            if (entry.getValue()) {
                activeReplicas.add(replicaLocations.get(entry.getKey()));
            }
        }
        return activeReplicas;
    }

    // Metodă pentru a marca o replică ca inactivă
    public synchronized void markReplicaAsInactive(String replicaName) {
        replicaStatus.put(replicaName, false);
        System.out.println("Replica " + replicaName + " is now inactive.");
    }

    // Metodă pentru a marca o replică ca activă
    public synchronized void markReplicaAsActive(String replicaName) {
        replicaStatus.put(replicaName, true);
        System.out.println("Replica " + replicaName + " is now active.");
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(3008);  // Crează registry pe portul 3008
            MasterServer master = new MasterServer();
            Naming.rebind("rmi://192.168.100.28:3008/MasterServer", master);  // Înregistrează MasterServer pe IP-ul corect
            System.out.println("Master Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
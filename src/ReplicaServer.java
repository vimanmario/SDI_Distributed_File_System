import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;

public class ReplicaServer extends UnicastRemoteObject implements ReplicaServerInterface {
    private String name;
    private Map<String, String> fileStorage = new HashMap<>();

    public ReplicaServer(String name) throws RemoteException {
        super();
        this.name = name;
    }

    @Override
    public synchronized void write(String fileName, String data) throws RemoteException {
        fileStorage.put(fileName, data);
        System.out.println("[" + name + "] File written: " + fileName);
    }

    @Override
    public synchronized String read(String fileName) throws RemoteException {
        return fileStorage.getOrDefault(fileName, "File not found");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java ReplicaServer <replica_name>");
            return;
        }
        String replicaName = args[0];
        try {
            int port = 1100 + Integer.parseInt(replicaName);  // Port personalizat pe baza numelui replicii
            LocateRegistry.createRegistry(port);  // Crează registry pe portul corespunzător
            ReplicaServer replica = new ReplicaServer(replicaName);
            Naming.rebind("rmi://localhost:" + port + "/ReplicaServer" + replicaName, replica);  // Înregistrează ReplicaServer
            // Înregistrare cu MasterServer
            MasterServerInterface master = (MasterServerInterface) Naming.lookup("//192.168.100.28:3008/MasterServer");
            ReplicaLoc location = new ReplicaLoc(replicaName, "localhost", true);
            master.registerReplicaServer(replicaName, location);
            System.out.println("Replica Server " + replicaName + " is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

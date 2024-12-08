import java.rmi.*;
import java.util.List;

public class Client {
    public static void main(String[] args) {
        try {
            // Conectează-te la MasterServer
            MasterServerInterface master = (MasterServerInterface) Naming.lookup("//192.168.100.28:3008/MasterServer");
            System.out.println("Fetching replica locations...");
            List<ReplicaLoc> replicas = master.getReplicaLocations("anyFile");

            if (replicas.isEmpty()) {
                System.out.println("No replicas available.");
                return;
            }

            // Încearcă să accesezi replicile una câte una
            ReplicaServerInterface replica = null;
            String fileName = "testFile.txt";
            String fileContent = "Hello from Client!";
            boolean replicaFound = false;

            // Iterează prin toate replicile active
            for (ReplicaLoc replicaLoc : replicas) {
                try {
                    System.out.println("Trying to connect to replica at " + replicaLoc.getHost());
                    replica = (ReplicaServerInterface) Naming.lookup("//" + replicaLoc.getHost() + ":" + (1100 + Integer.parseInt(replicaLoc.getId())) + "/ReplicaServer" + replicaLoc.getId());
                    // Dacă replica este accesibilă, o folosim
                    replicaFound = true;
                    break;
                } catch (Exception e) {
                    System.out.println("Replica " + replicaLoc.getHost() + " failed, trying next...");
                    e.printStackTrace();
                }
            }

            if (!replicaFound) {
                System.out.println("No available replicas.");
                return;
            }

            // Scriere date
            replica.write(fileName, fileContent);
            System.out.println("File written: " + fileName);

            // Citire date
            String retrievedData = replica.read(fileName);
            System.out.println("File content: " + retrievedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
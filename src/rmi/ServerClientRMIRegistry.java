package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import util.trace.port.rpc.rmi.RMIRegistryCreated;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

public class ServerClientRMIRegistry {	
	public static void main (String[] args) {
		try {
			Registry rmi = LocateRegistry.createRegistry(1099);
			Scanner scanner = new Scanner(System.in);
			scanner.nextLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


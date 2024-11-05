import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;

// GUI for Parking System
public class ParkingSystemGUI {
    private JFrame frame;
    private JTextArea displayArea;
    private JTextField searchField;
    private JButton searchButton;
    private JButton checkStorageButton;
    private ParkingLot parkingLot;

    public ParkingSystemGUI() {
        parkingLot = new ParkingLot();

        frame = new JFrame("Parking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new FlowLayout());

        displayArea = new JTextArea(15, 40);
        displayArea.setEditable(false);
        frame.add(new JScrollPane(displayArea));

        // Search components
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        frame.add(new JLabel("Enter Plate Number:"));
        frame.add(searchField);
        frame.add(searchButton);

        // Button to check if vehicles are stored correctly
        checkStorageButton = new JButton("Check Stored Vehicles");
        frame.add(checkStorageButton);

        frame.setVisible(true);

        // Search button action listener
        searchButton.addActionListener(e -> {
            String plateNumber = searchField.getText().trim();
            String details = parkingLot.searchVehicle(plateNumber);
            if (!details.isEmpty()) {
                JOptionPane.showMessageDialog(frame, details, "Vehicle Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Vehicle not found.", "Search Result", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Check storage button action listener
        checkStorageButton.addActionListener(e -> displayAllParkedVehicles());

        // Start the server to receive plate data and slot info
        new Thread(this::startServer).start();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Java server started, waiting for plate data and slot info...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String plateText = in.readLine();
                String slotInfo = in.readLine();

                if (plateText != null && slotInfo != null && !plateText.isEmpty() && !slotInfo.isEmpty()) {
                    int slotNumber = Integer.parseInt(slotInfo.trim());
                    parkingLot.parkVehicle(plateText, slotNumber);
                    System.out.println("Stored vehicle: " + plateText + " in slot " + slotNumber);
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error in server connection: " + e.getMessage());
        }
    }

    public void displayAllParkedVehicles() {
        String details = parkingLot.getAllParkedVehicles();
        JOptionPane.showMessageDialog(frame, details.isEmpty() ? "No vehicles are currently parked." : details, 
                "Stored Vehicles", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new ParkingSystemGUI();
    }
}

// Node class for the BST
class BSTNode {
    Vehicle vehicle;
    BSTNode left, right;

    public BSTNode(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.left = this.right = null;
    }
}

// Binary Search Tree for managing vehicles
class BinarySearchTree {
    private BSTNode root;

    public void insert(Vehicle vehicle) {
        root = insertRec(root, vehicle);
    }

    private BSTNode insertRec(BSTNode root, Vehicle vehicle) {
        if (root == null) {
            root = new BSTNode(vehicle);
            return root;
        }
        if (vehicle.getPlateNumber().compareTo(root.vehicle.getPlateNumber()) < 0)
            root.left = insertRec(root.left, vehicle);
        else if (vehicle.getPlateNumber().compareTo(root.vehicle.getPlateNumber()) > 0)
            root.right = insertRec(root.right, vehicle);
        return root;
    }

    public Vehicle search(String plateNumber) {
        return searchRec(root, plateNumber);
    }

    private Vehicle searchRec(BSTNode root, String plateNumber) {
        if (root == null || root.vehicle.getPlateNumber().equalsIgnoreCase(plateNumber))
            return (root != null) ? root.vehicle : null;
        if (plateNumber.compareTo(root.vehicle.getPlateNumber()) < 0)
            return searchRec(root.left, plateNumber);
        return searchRec(root.right, plateNumber);
    }

    public String inorderTraversal() {
        StringBuilder sb = new StringBuilder();
        inorderRec(root, sb);
        return sb.toString();
    }

    private void inorderRec(BSTNode root, StringBuilder sb) {
        if (root != null) {
            inorderRec(root.left, sb);
            sb.append("Plate: ").append(root.vehicle.getPlateNumber())
                    .append(", Slot: ").append(root.vehicle.getSlotNumber())
                    .append(", Entry Time: ").append(root.vehicle.getEntryTime()).append("\n");
            inorderRec(root.right, sb);
        }
    }
}

// ParkingLot class to manage parking slots and vehicle data
class ParkingLot {
    private BinarySearchTree bst;

    public ParkingLot() {
        this.bst = new BinarySearchTree();
    }

    public void parkVehicle(String plateNumber, int slotNumber) {
        Vehicle vehicle = new Vehicle(plateNumber, slotNumber, LocalDateTime.now());
        bst.insert(vehicle);
        System.out.println("Vehicle with Plate " + plateNumber + " parked in Slot " + slotNumber);
    }

    public String searchVehicle(String plateNumber) {
        Vehicle vehicle = bst.search(plateNumber);
        if (vehicle != null) {
            return "Plate: " + vehicle.getPlateNumber() +
                    "\nSlot Number: " + vehicle.getSlotNumber() +
                    "\nEntry Time: " + vehicle.getEntryTime();
        }
        return ""; 
    }

    public String getAllParkedVehicles() {
        return bst.inorderTraversal();
    }
}

// Vehicle class to represent a parked vehicle's details
class Vehicle {
    private String plateNumber;
    private int slotNumber;
    private LocalDateTime entryTime;

    public Vehicle(String plateNumber, int slotNumber, LocalDateTime entryTime) {
        this.plateNumber = plateNumber;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }
}


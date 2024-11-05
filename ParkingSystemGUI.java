import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParkingSystemGUI {
    private JFrame frame;
    private JTextArea displayArea;
    private JTextField searchField;
    private JButton searchButton;
    private JButton checkStorageButton; // Button to check stored vehicles
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
        checkStorageButton.addActionListener(e -> parkingLot.displayAllParkedVehicles());

        // Start the server to receive plate data and slot info
        new Thread(this::startServer).start();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Java server started, waiting for plate data and slot info...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String plateText = in.readLine(); // Receive plate number
                String slotInfo = in.readLine();  // Receive slot number

                if (plateText != null && slotInfo != null && !plateText.isEmpty() && !slotInfo.isEmpty()) {
                    int slotNumber = Integer.parseInt(slotInfo.trim());
                    parkingLot.parkVehicle(plateText, slotNumber);  // Store vehicle with received slot
                    System.out.println("Stored vehicle: " + plateText + " in slot " + slotNumber);
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error in server connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ParkingSystemGUI();
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

// ParkingLot class to manage parking slots and vehicle data
class ParkingLot {
    private List<Vehicle> parkedVehicles; // Store vehicles in the parking lot

    public ParkingLot() {
        this.parkedVehicles = new ArrayList<>();
    }

    public void parkVehicle(String plateNumber, int slotNumber) {
        Vehicle vehicle = new Vehicle(plateNumber, slotNumber, LocalDateTime.now());
        parkedVehicles.add(vehicle);
        System.out.println("Vehicle with Plate " + plateNumber + " parked in Slot " + slotNumber);
    }

    // Display all parked vehicles
    public void displayAllParkedVehicles() {
        if (parkedVehicles.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No vehicles are currently parked.", "Stored Vehicles", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder details = new StringBuilder("Stored Vehicles:\n");
            for (Vehicle vehicle : parkedVehicles) {
                details.append("Plate: ").append(vehicle.getPlateNumber())
                        .append(", Slot: ").append(vehicle.getSlotNumber())
                        .append(", Entry Time: ").append(vehicle.getEntryTime()).append("\n");
            }
            JOptionPane.showMessageDialog(null, details.toString(), "Stored Vehicles", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Search for a vehicle by its plate number
    public String searchVehicle(String plateNumber) {
        for (Vehicle vehicle : parkedVehicles) {
            if (vehicle.getPlateNumber().equalsIgnoreCase(plateNumber)) {
                return "Plate: " + vehicle.getPlateNumber() +
                        "\nSlot Number: " + vehicle.getSlotNumber() +
                        "\nEntry Time: " + vehicle.getEntryTime();
            }
        }
        return ""; // Return empty string if not found
    }
}


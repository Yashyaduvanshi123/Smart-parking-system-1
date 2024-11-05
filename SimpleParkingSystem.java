public class SimpleParkingSystem {

    // Array to represent parking slots (0 = vacant, 1 = occupied)
    private int[] parkingSlots;

    // Constructor to initialize parking slots
    public SimpleParkingSystem(int numSlots) {
        parkingSlots = new int[numSlots];
    }

    // Display status of all parking slots
    public void showSlots() {
        System.out.println("Parking Slots Status:");
        for (int i = 0; i < parkingSlots.length; i++) {
            if (parkingSlots[i] == 0) {
                System.out.println("Slot " + (i + 1) + ": Vacant");
            } else {
                System.out.println("Slot " + (i + 1) + ": Occupied");
            }
        }
        System.out.println();
    }

    // Park a car in the next available slot
    public void parkCar(int slotNumber) {
        if (slotNumber < 1 || slotNumber > parkingSlots.length) {
            System.out.println("Invalid slot number.");
            return;
        }

        if (parkingSlots[slotNumber - 1] == 0) { // If slot is vacant
            parkingSlots[slotNumber - 1] = 1;    // Park the car
            System.out.println("Car parked in Slot " + slotNumber);
        } else {
            System.out.println("Slot " + slotNumber + " is already occupied.");
        }
        showSlots();
    }

    // Remove a car from a specific slot
    public void removeCar(int slotNumber) {
        if (slotNumber < 1 || slotNumber > parkingSlots.length) {
            System.out.println("Invalid slot number.");
        } else if (parkingSlots[slotNumber - 1] == 1) {
            parkingSlots[slotNumber - 1] = 0; // Mark slot as vacant
            System.out.println("Car removed from Slot " + slotNumber);
        } else {
            System.out.println("Slot " + slotNumber + " is already vacant.");
        }
        showSlots();
    }

    // Simulate the car entry detection via an IR sensor
    public void carEntryDetected() {
        for (int i = 0; i < parkingSlots.length; i++) {
            if (parkingSlots[i] == 0) { // Find the first vacant slot
                parkCar(i + 1); // Park in the available slot (i + 1 for human-friendly slot number)
                return;
            }
        }
        System.out.println("No available slots for parking.");
    }

    // Main method to test the parking system
    public static void main(String[] args) {
        SimpleParkingSystem parking = new SimpleParkingSystem(3); // 3 parking slots

        parking.carEntryDetected(); // Simulates a car entering
        parking.carEntryDetected(); // Another car enters
        parking.removeCar(1); // Removes a car from Slot 1
        parking.carEntryDetected(); // A car enters again
    }
}

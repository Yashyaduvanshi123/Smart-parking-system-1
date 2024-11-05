import cv2
import matplotlib.pyplot as plt
import os
import easyocr
import socket

# Initialize EasyOCR reader with English language
reader = easyocr.Reader(['en'], gpu=False)

# Ensure directories exist for saving detected plates and text
os.makedirs("plates", exist_ok=True)
os.makedirs("output_text", exist_ok=True)

# Function to send plate data and slot info to the Java server
def send_plate_to_java_server(plate_text, slot_number):
    try:
        # Set up socket connection to Java server
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect(('localhost', 5000))  # Adjust IP and port if needed

        # Send plate text and slot number to Java GUI
        message = f"{plate_text}\n{slot_number}"  # Format message to include both plate and slot
        client_socket.send(message.encode('utf-8'))
        print(f"Sent plate data to Java GUI: {plate_text} in slot {slot_number}")  # Debug statement
    except Exception as e:
        print(f"Error sending data to Java GUI: {e}")
    finally:
        client_socket.close()

# Load Haarcascade model for license plate detection
harcascade = "model/haarcascade_russian_plate_number.xml"
cap = cv2.VideoCapture(0)
cap.set(3, 480)
cap.set(4, 480)

# Parameters for detection
min_area = 500   # Minimum area for a valid plate detection
count = 0        # Counter for saving images
frame_skip = 5   # Process every 5th frame for efficiency
frame_count = 0  # Initialize frame counter

# Function to display the detected image using matplotlib
def show_image(img):
    plt.imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    plt.axis('off')
    plt.show(block=False)
    plt.pause(0.001)

# Main detection loop
try:
    while True:
        # Capture frame from webcam
        success, img = cap.read()
        if not success:
            print("Failed to read from the webcam. Please check the connection.")
            break

        frame_count += 1
        if frame_count % frame_skip != 0:
            continue  # Skip frames for efficiency

        # Convert image to grayscale for processing
        img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        plate_cascade = cv2.CascadeClassifier(harcascade)
        plates = plate_cascade.detectMultiScale(img_gray, 1.1, 4)

        # Loop through detected plates
        for (x, y, w, h) in plates:
            if w * h > min_area:
                # Draw rectangle around detected plate
                cv2.rectangle(img, (x, y), (x + w, y + h), (0, 255, 0), 2)
                
                # Extract region of interest (ROI) for OCR
                img_roi = img_gray[y:y + h, x:x + w]
                result = reader.readtext(img_roi)
                plate_text = " ".join([text[1] for text in result])

                # Save detected plate text to a file
                with open(f"output_text/plate_text_{count}.txt", "w") as f:
                    f.write(plate_text)

                print(f"Detected Plate Text: {plate_text}")

                # Send the plate text to the Java GUI with a slot number (example slot number 1)
                if plate_text:
                    slot_number = 1  # Example slot number; in practice, you may set this dynamically
                    send_plate_to_java_server(plate_text, slot_number)

                # Save the ROI image of the detected plate
                cv2.imwrite(f"plates/scanned_img_{count}.jpg", img[y:y + h, x:x + w])
                count += 1

        # Display the processed frame with detected plates
        show_image(img)

except KeyboardInterrupt:
    print("Program interrupted.")

finally:
    # Release the webcam and close all displays
    cap.release()
    plt.close('all')
    print("Webcam feed closed.")

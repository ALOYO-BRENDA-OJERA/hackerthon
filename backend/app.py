from flask import Flask, request
import requests

app = Flask(__name__)

# Africa's Talking credentials
USERNAME = "sandbox"
API_KEY = "atsk_58a187078d683f500c97c809e1a19e34ce2b70d7223267b3ce8aa25d2688f6084e47d1c5"

@app.route('/ussd', methods=['POST'])
def ussd():
    phone_number = request.form.get('0788862158')
    session_id = request.form.get('sessionId')
    text = request.form.get('text')

    # Start the USSD session
    if text == "":
        response = "CON Welcome to the Emergency Alert System\n"
        response += "1. Trigger Emergency Alert\n"
        response += "2. Check Status\n"
        response += "3. Exit"
    elif text == "1":
        response = "CON Please enter the contact number to alert:"
    elif text.startswith("1"):
        contact_number = text.split("*")[1]
        response = f"END Alert will be sent to {contact_number}."
        send_alert(contact_number, phone_number)  # Call the function to send alert
    elif text == "2":
        response = "END System is operational."
    else:
        response = "END Invalid option."

    return response

def send_alert(contact_number, user_number):
    # Replace this function with your app's SMS sending logic
    message = f"Emergency Alert! User {user_number} needs help."
    url = f"https://api.africastalking.com/version1/messaging"
    headers = {"Content-Type": "application/json"}
    payload = {
        "username": USERNAME,
        "to": [contact_number],
        "message": message,
        "from": "YourSenderID"
    }
    
    response = requests.post(url, json=payload, headers=headers, auth=(USERNAME, API_KEY))
    return response.json()

if __name__ == '__main__':
    app.run(debug=True)

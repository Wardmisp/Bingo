from flask import Flask, jsonify

app = Flask(__name__)

# A basic route for your Android app to connect to.
@app.route('/')
def home():
    return jsonify(message="Hello from the Python backend!")

# A sample API endpoint to send data to the Android app.
@app.route('/api/data')
def get_data():
    data = {
        "title": "Latest News",
        "content": "This is a message from your deployed Python web service on Render."
    }
    return jsonify(data)

# A new endpoint to receive data via a POST request
@app.route('/submit', methods=['POST'])
def submit_data():
    # Get the JSON data from the request body
    data = request.get_json()
    
    # Check if the data is valid and has a 'message' field
    if not data or 'message' not in data:
        return jsonify({"error": "Invalid data provided"}), 400
    
    received_message = data['message']
    
    # Print the received message to the server's console
    print(f"Received message from app: {received_message}")
    
    # Send back a confirmation response
    response = {
        "status": "success",
        "message": "Message received and processed."
    }
    return jsonify(response)
if __name__ == '__main__':
    # Use a hardcoded port for local development
    # Gunicorn will handle the port in production on Render
    app.run(port=3000)

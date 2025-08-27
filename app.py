from flask import Flask, jsonify, request

app = Flask(__name__)

# A list to store messages received from the app
messages = []

# This is your existing route that responds with JSON
@app.route('/')
def get_data():
    return jsonify({
      "message": "Hello from the Python backend!",
      "messages_received": messages # This key will contain your list of messages
    })

# Your new route to receive messages via POST
@app.route('/submit', methods=['POST'])
def submit_data():
    data = request.get_json()
    
    if not data or 'message' not in data:
        return jsonify({"error": "Invalid data provided"}), 400
    
    received_message = data['message']
    
    # Add the new message to the list
    messages.append(received_message)
    
    # Print it to the console for your logs
    print(f"Received message from app: {received_message}")
    
    # Send back a confirmation response
    response = {
        "status": "success",
        "message": "Message received and added to list."
    }
    return jsonify(response)


# A sample API endpoint to send data to the Android app.
@app.route('/api/data')
def get_data():
    data = {
        "title": "Latest News",
        "content": "This is a message from your deployed Python web service on Render."
    }
    return jsonify(data)

if __name__ == '__main__':
    # Use a hardcoded port for local development
    # Gunicorn will handle the port in production on Render
    app.run(port=3000)

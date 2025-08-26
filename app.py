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

if __name__ == '__main__':
    # Use a hardcoded port for local development
    # Gunicorn will handle the port in production on Render
    app.run(port=3000)

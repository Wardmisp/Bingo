from flask import Flask, request, jsonify, render_template
import logging

app = Flask(__name__)

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# In-memory database
players_db = [
    {"id": 1, "name": "Alice"},
    {"id": 2, "name": "Bob"},
    {"id": 3, "name": "Charlie"}
]

@app.route('/')
def serve_page():
    """
    Serves the index.html page for a web browser.
    """
    logging.info("GET request received for / endpoint. Serving index.html.")
    return render_template('index.html')

@app.route('/players', methods=['GET'])
def get_players():
    """
    Returns a list of all registered players as JSON.
    """
    logging.info("GET request received for /players endpoint.")
    
    # Log the data being sent
    logging.info(f"Sending player data: {players_db}")
    
    return jsonify(players_db)

@app.route('/register-player', methods=['POST'])
def register_player():
    """
    Registers a new player.
    """
    try:
        data = request.get_json()
        player_name = data.get('name')
        
        # Log the received request data
        logging.info(f"POST request received for /register-player. Data: {data}")
        
        if not player_name:
            logging.warning("Player name not provided in POST request.")
            return jsonify({"error": "Player name is required."}), 400

        # Check for duplicate players
        if any(p['name'] == player_name for p in players_db):
            logging.warning(f"Attempt to register existing player: {player_name}")
            return jsonify({"error": f"Player '{player_name}' already exists."}), 409

        # Add new player to the database
        new_id = max([p['id'] for p in players_db], default=0) + 1
        new_player = {"id": new_id, "name": player_name}
        players_db.append(new_player)
        
        logging.info(f"Successfully registered new player: {new_player}")
        return jsonify(new_player), 201

    except Exception as e:
        logging.error(f"Error during player registration: {e}")
        return jsonify({"error": "An internal server error occurred."}), 500

if __name__ == '__main__':
    # In order to access it from everywhere
    app.run(host='0.0.0.0', port=5000)
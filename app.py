from flask import Flask, request, jsonify, render_template
import logging
# Import your db_manager script
import db_manager

app = Flask(__name__)

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Remove the in-memory database, as we are now using the db_manager
# players_db = [
#     {"id": 1, "name": "Alice"},
#     {"id": 2, "name": "Bob"},
#     {"id": 3, "name": "Charlie"}
# ]

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
    
    # Use the db_manager to get all players
    players_data = db_manager.get_all_players()
    
    # Log the data being sent
    logging.info(f"Sending player data: {players_data}")
    
    return jsonify(players_data)

@app.route('/register-player', methods=['POST'])
def register_player():
    """
    Registers a new player and generates a bingo card.
    """
    try:
        data = request.get_json()
        player_name = data.get('name')
        
        # Log the received request data
        logging.info(f"POST request received for /register-player. Data: {data}")
        
        if not player_name:
            logging.warning("Player name not provided in POST request.")
            return jsonify({"error": "Player name is required."}), 400

        # Use the db_manager function to add the new player with a bingo card
        success = db_manager.add_new_player(player_name)
        
        if success:
            logging.info(f"Successfully registered new player: {player_name}")
            return jsonify({"message": f"Player '{player_name}' registered successfully."}), 201
        else:
            logging.warning(f"Attempt to register existing player: {player_name}")
            return jsonify({"error": f"Player '{player_name}' already exists."}), 409

    except Exception as e:
        logging.error(f"Error during player registration: {e}")
        return jsonify({"error": "An internal server error occurred."}), 500
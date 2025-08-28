import os
from flask import Flask, request, jsonify, render_template
import logging
from pymongo import MongoClient
from bingo_card_generator import generate_bingo_card


# Load the environment variable from Render
mongo_uri = os.getenv("MONGO_URI")

app = Flask(__name__)

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Connect to MongoDB Atlas
try:
    client = MongoClient(mongo_uri)
    db = client.bingo_game
    players_collection = db.players
    logging.info("Successfully connected to MongoDB Atlas!")
except Exception as e:
    logging.error(f"Error connecting to MongoDB Atlas: {e}")

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
    Returns a list of all registered players as JSON from MongoDB.
    """
    logging.info("GET request received for /players endpoint.")
    
    players_data = list(players_collection.find({}, {"_id": 0}))
    
    logging.info(f"Sending player data: {players_data}")
    
    return jsonify(players_data)

@app.route('/register-player', methods=['POST'])
def register_player():
    """
    Registers a new player and generates a bingo card in MongoDB.
    """
    try:
        data = request.get_json()
        player_name = data.get('name')
        
        logging.info(f"POST request received for /register-player. Data: {data}")
        
        if not player_name:
            logging.warning("Player name not provided in POST request.")
            return jsonify({"error": "Player name is required."}), 400

        existing_player = players_collection.find_one({"name": player_name})
        
        if existing_player:
            logging.warning(f"Attempt to register existing player: {player_name}")
            return jsonify({"error": f"Player '{player_name}' already exists."}), 409

        bingo_card = generate_bingo_card()

        new_player = {
            "name": player_name,
            "bingo_card": bingo_card
        }
        
        players_collection.insert_one(new_player)
        
        logging.info(f"Successfully registered new player: {new_player}")
        return jsonify({"message": f"Player '{player_name}' registered successfully."}), 201

    except Exception as e:
        logging.error(f"Error during player registration: {e}")
        return jsonify({"error": "An internal server error occurred."}), 500
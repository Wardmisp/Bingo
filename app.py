import os
from random import randint
import uuid
from flask import Flask, request, jsonify, render_template
import logging
from pymongo import MongoClient
from bingo_card_generator import generate_bingo_card

# Some utils
def is_game_id_unique(game_id):
    """Checks if a game ID already exists in the players collection."""
    if players_collection is None:
        return False
    return players_collection.find_one({"gameId": game_id}) is None

def generate_unique_numeric_id():
    """Generates a unique 6-digit numeric ID."""
    if players_collection is None:
        return None
    while True:
        # Generates a random 6-digit number
        new_id = str(randint(100000, 999999))
        if is_game_id_unique(new_id):
            return new_id

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

@app.route('/players/<gameId>', methods=['GET'])
def get_players(gameId):
    """
    Returns a list of all players in a specific game as JSON from MongoDB.
    """
    logging.info(f"GET request received for /players/{gameId} endpoint.")
    
    # Correctly filter players by gameId
    players_data = list(players_collection.find({"gameId": gameId}, {"_id": 0}))
    
    logging.info(f"Sending player data for game {gameId}: {players_data}")
    
    return jsonify(players_data)

@app.route('/register-player', methods=['POST'])
def register_player():
    """
    Registers a new player and creates a unique numeric game ID.
    """
    if players_collection is None:
        return jsonify({"error": "Database connection not available."}), 500

    try:
        data = request.get_json()
        player_name = data.get('name')

        logging.info(f"POST request received for /register-player. Data: {data}")

        if not player_name:
            logging.warning("Player name not provided in POST request.")
            return jsonify({"error": "Player name is required."}), 400

        # Generate a unique numeric game ID
        new_game_id = generate_unique_numeric_id()
        if new_game_id is None:
            return jsonify({"error": "Could not generate a unique game ID."}), 500
        
        bingo_card = generate_bingo_card()
        new_player = {
            "name": player_name,
            "gameId": new_game_id,
            "bingo_card": bingo_card
        }
        players_collection.insert_one(new_player)
        logging.info(f"Successfully registered new player and created game: {new_player}")
        
        # Return the new gameId so the host can share it with others
        return jsonify({
            "message": f"Player '{player_name}' registered successfully and joined game '{new_game_id}'.",
            "playerId": str(new_player.get('_id')),
            "gameId": new_game_id
        }), 201

    except Exception as e:
        logging.error(f"Error during player registration: {e}")
        return jsonify({"error": "An internal server error occurred."}), 500

@app.route('/join-game', methods=['POST'])
def join_game():
    """
    Adds a new player to an existing game lobby.
    """
    try:
        data = request.get_json()
        player_name = data.get('name')
        game_id = data.get('gameId')
        
        logging.info(f"POST request received for /join-game. Data: {data}")
        
        if not player_name or not game_id:
            logging.warning("Player name or game ID not provided.")
            return jsonify({"error": "Player name and game ID are required."}), 400

        # Check if the game ID exists by finding at least one player in that game
        if players_collection.count_documents({"gameId": game_id}) == 0:
            logging.warning(f"Attempt to join non-existent game: {game_id}")
            return jsonify({"error": "Game not found. Please check the ID."}), 404

        # Check if the player already exists in this specific game
        existing_player = players_collection.find_one({"name": player_name, "gameId": game_id})
        
        if existing_player:
            logging.warning(f"Attempt to register existing player: {player_name} in game {game_id}")
            return jsonify({"error": f"Player '{player_name}' already exists in this game."}), 409

        # Generate the bingo card
        bingo_card = generate_bingo_card()

        new_player = {
            "name": player_name,
            "gameId": game_id,
            "bingo_card": bingo_card
        }
        
        players_collection.insert_one(new_player)
        
        logging.info(f"Successfully joined game {game_id} with new player: {player_name}")
        return jsonify({"message": f"Player '{player_name}' registered successfully in game {game_id}."}), 201

    except Exception as e:
        logging.error(f"Error during player join: {e}")
        return jsonify({"error": "An internal server error occurred."}), 500
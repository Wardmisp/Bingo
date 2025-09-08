import os
from flask import Flask, request, jsonify, render_template
import logging
from pymongo import MongoClient
from bingo_card_generator import generate_bingo_card
import random

# Load the environment variable from Render
mongo_uri = os.getenv("MONGO_URI")

app = Flask(__name__)

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Connect to MongoDB Atlas
try:
    client = MongoClient(mongo_uri)
    db = client.bingo_game
    # Use a single, static collection for all players
    players_collection = db.players
    logging.info("Successfully connected to MongoDB Atlas and using 'players' collection.")
except Exception as e:
    logging.error(f"Error connecting to MongoDB Atlas: {e}")

@app.route('/')
def serve_page():
    """
    Serves the index.html page for a web browser.
    """
    logging.info("GET request received for / endpoint. Serving index.html.")
    return render_template('index.html')

@app.route('/players/<game_id>', methods=['GET'])
def get_players(game_id):
    """
    Returns a list of all registered players for a specific game as JSON from the single collection.
    """
    logging.info(f"GET request for players in game ID: {game_id}")
    
    # Correctly filter players by gameId
    players_data = list(players_collection.find({"gameId": game_id}, {"_id": 0}))
    
    logging.info(f"Found {len(players_data)} players for game {game_id}.")
    
    return jsonify(players_data)

@app.route('/create-game', methods=['POST'])
def create_game():
    """
    Registers a new player and creates a new game.
    """
    try:
        data = request.get_json()
        player_name = data.get('name')
        
        logging.info(f"POST request received for /create-game. Data: {data}")
        
        if not player_name:
            logging.warning("Player name not provided in POST request.")
            return jsonify({"error": "Player name is required."}), 400

        # Generate a unique game ID
        def generate_unique_numeric_id():
            while True:
                new_id = str(random.randint(100000, 999999))
                # Check for uniqueness across the entire players collection
                if players_collection.count_documents({"gameId": new_id}) == 0:
                    return new_id
        
        game_id = generate_unique_numeric_id()
        
        bingo_card = generate_bingo_card()

        new_player = {
            "name": player_name,
            "gameId": game_id,
            "bingo_card": bingo_card
        }
        
        players_collection.insert_one(new_player)
        
        logging.info(f"Successfully created new game with ID {game_id} and registered player: {player_name}")
        return jsonify({"message": f"Player '{player_name}' registered successfully.", "gameId": game_id}), 201

    except Exception as e:
        logging.error(f"Error during game creation and player registration: {e}")
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

@app.route('/debug/players', methods=['GET'])
def debug_players():
    """
    DEBUG: Returns the entire players collection as JSON and logs it.
    """
    logging.info("GET request received for /debug/players.")
    all_players_data = list(players_collection.find({}, {"_id": 0}))
    logging.info(f"Returning all player data: {all_players_data}")
    return jsonify(all_players_data)

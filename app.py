import os
from random import randint
import uuid
from flask import Flask, request, jsonify, render_template, stream_with_context, Response
import logging
from pymongo import MongoClient
from bingo_card_generator import generate_bingo_card
from bson.objectid import ObjectId
from bson.errors import InvalidId
import time
from concurrent.futures import ThreadPoolExecutor

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

def _is_winner(card):
    # Check rows ONLY
    for row in card:
        if all(x == -1 for x in row if x is not None):
            return True


def _send_game_over_event(game_id, winner_name):
    """Sends game over notifications to all players in a game."""
    global streams # Make sure to get your global streams dictionary
    
    if game_id not in streams:
        return

    player_queues = streams[game_id]
    
    for player_id, player_queue in player_queues:
        # Get player's name from players_collection
        player_doc = players_collection.find_one({"_id": ObjectId(player_id)})
        current_player_name = player_doc['name'] if player_doc else "Unknown Player"

        if current_player_name == winner_name:
            # Send congratulatory message to the winner
            logging.debug("Congratulations! You won!")
            message = f"event: game_over\ndata: Congratulations! You won!\n\n"
        else:
            # Send 'try again' message to losers
            logging.debug("Congratulations! You lose!")
            message = f"event: game_over\ndata: Game over! {winner_name} won. Try again!\n\n"
        
        player_queue.put(message.encode('utf-8'))

# Load the environment variable from Render
mongo_uri = os.getenv("MONGO_URI")

# To manage threads 
executor = ThreadPoolExecutor(max_workers=8)

app = Flask(__name__)

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Connect to MongoDB Atlas
try:
    client = MongoClient(mongo_uri)
    db = client.bingo_game
    players_collection = db.players
    bingo_cards_collection = db.bingo_cards  # New collection for bingo cards
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
    
    This endpoint now EXCLUDES the bingo card data to reduce payload size.
    """
    logging.info(f"GET request received for /players/{gameId} endpoint.")
    
    # Correctly filter players by gameId and exclude the bingo_card field
    players_data = list(players_collection.find({"gameId": gameId}, {"_id": 0, "bingo_card_id": 0}))
    
    logging.info(f"Sending player data for game {gameId}: {players_data}")
    
    return jsonify(players_data)

@app.route('/player-card/<gameId>/<playerId>')
def fetch_bingo_card(gameId, playerId):
    try:
        # It's better to fetch the card by player ID if you don't have the card ID
        player_doc = players_collection.find_one({"_id": ObjectId(playerId)})
        if not player_doc:
            return jsonify({"success": False, "error": "Player not found"}), 404

        card_doc = bingo_cards_collection.find_one({"_id": player_doc['cardId']})

        if not card_doc:
            # Handle the case where a player is found but their card isn't
            logging.error(f"Card not found for player: {playerId}")
            return jsonify({"success": False, "error": "Card not found"}), 404
        
        # Return the card data
        return jsonify(card_doc), 200

    except InvalidId:
        logging.warning(f"Invalid player or card ID received: {playerId} / {gameId}")
        return jsonify({"success": False, "error": "Invalid ID format."}), 400

    except Exception as e:
        logging.error(f"An unexpected error occurred: {e}")
        return jsonify({"success": False, "error": "Internal server error"}), 500

@app.route('/create-game', methods=['POST'])
def create_game():
    """
    Registers a new player, creates a unique numeric game ID, and generates a bingo card.
    """
    if players_collection is None:
        return jsonify({"error": "Database connection not available."}), 500

    try:
        data = request.get_json()
        player_name = data.get('name')

        logging.info(f"POST request received for /create-game. Data: {data}")

        if not player_name:
            logging.warning("Player name not provided in POST request.")
            return jsonify({"error": "Player name is required."}), 400

        # Generate a unique numeric game ID
        new_game_id = generate_unique_numeric_id()
        if new_game_id is None:
            return jsonify({"error": "Could not generate a unique game ID."}), 500
        
        # Generate and insert the bingo card first
        bingo_card = generate_bingo_card()
        card_result = bingo_cards_collection.insert_one({"card": bingo_card})
        bingo_card_id = str(card_result.inserted_id)

        # Then, create the player and link the bingo card
        new_player_id = str(uuid.uuid4())
        new_player = {
            "name": player_name,
            "gameId": new_game_id,
            "playerId": new_player_id,
            "gameStarted": False,
            "bingo_card_id": bingo_card_id
        }
        players_collection.insert_one(new_player)
        logging.info(f"Successfully registered new player and created game: {new_player}")
        
        # Return the new gameId and playerId to the host
        return jsonify({
            "message": f"Player '{player_name}' registered successfully and joined game '{new_game_id}'.",
            "playerId": new_player_id,
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

        # Generate and insert the bingo card first
        bingo_card = generate_bingo_card()
        card_result = bingo_cards_collection.insert_one({"card": bingo_card})
        bingo_card_id = str(card_result.inserted_id)

        # Then, create the player and link the bingo card
        new_player_id = str(uuid.uuid4())
        new_player = {
            "name": player_name,
            "gameId": game_id,
            "playerId": new_player_id,
            "gameStarted": False,
            "bingo_card_id": bingo_card_id
        }
        players_collection.insert_one(new_player)

        logging.info(f"Successfully joined game {game_id} with new player: {player_name}")
        return jsonify({
            "message": f"Player '{player_name}' registered successfully in game {game_id}.",
            "playerId": new_player_id,
            "name": player_name,
            "gameId": game_id
        }), 201

    except Exception as e:
        logging.error(f"Error during player join: {e}")
        return jsonify({"error": "An internal server error occurred."}), 500

@app.route('/player-card/<cardId>/<int:number>', methods=['POST'])
def click_number_on_bingo_card(cardId, number):
    """
    Finds a bingo card by ID and updates the specified number to -1.
    """
    logging.info(f"POST request received to update card {cardId} with number {number}.")
    
    # Validate the number
    if not 1 <= number <= 75:
        logging.warning(f"Invalid number provided: {number}")
        return jsonify({"success": False, "error": "Invalid number. Must be between 1 and 75."}), 400

    try:
        # Find the document by its ObjectId
        card_doc = bingo_cards_collection.find_one({"_id": ObjectId(cardId)})
        
        if not card_doc:
            logging.warning(f"Bingo card not found with ID: {cardId}")
            return jsonify({"success": False, "error": "Bingo card not found."}), 404

        # Iterate through the 2D array to find and update the number
        card_updated = False
        updated_card_data = card_doc['card']
        for i in range(len(updated_card_data)):
            for j in range(len(updated_card_data[i])):
                if updated_card_data[i][j] == number:
                    updated_card_data[i][j] = -1
                    card_updated = True
                    break
            if card_updated:
                break
        
        if not card_updated:
            logging.warning(f"Number {number} not found on card {cardId}.")
            return jsonify({"success": False, "error": "Number not found on the bingo card."}), 404
            
        # Update the document in the database
        result = bingo_cards_collection.update_one(
            {"_id": ObjectId(cardId)},
            {"$set": {"card": updated_card_data}}
        )

        if result.matched_count == 0:
            logging.error(f"Failed to find and update document with ID: {cardId}")
            return jsonify({"success": False, "error": "Failed to update card."}), 500

        logging.info(f"Successfully updated card {cardId}: {result.matched_count} document matched, {result.modified_count} modified.")

        player_id = card_doc['playerId']
        player_doc = players_collection.find_one({"_id": ObjectId(player_id)})
        
        # Check for a win (any line, column, or diagonal of -1s)
        if _is_winner(updated_card_data):
            # If a win is detected, get the winner's name and send the event
            winner_name = player_doc['name'] if player_doc else "Unknown Player"
            game_id = player_doc['gameId'] if player_doc else None
            if game_id:
                executor.submit(_send_game_over_event, game_id, winner_name)

        return jsonify(True), 200

    except Exception as e:
        logging.error(f"An error occurred: {e}")
        return jsonify({"success": False, "error": "An internal server error occurred."}), 500
    
@app.route('/bingo-stream/<gameId>')
def bingo_stream(gameId):
    # This is a simplified example. In a real app, you would
    # get the next number from a shared state or a database
    def generate_numbers():
        # A simple list of numbers to stream
        numbers = list(range(1, 76))
        
        while numbers:
            next_number = numbers.pop(0)
            yield f"event: bingo_number\ndata: {next_number}\n\n"
            logging.info(f"SSE request received for /bingo-stream/{gameId}. number: {next_number}")
            time.sleep(7) # Wait for 7 seconds

    return Response(stream_with_context(generate_numbers()), mimetype='text/event-stream')
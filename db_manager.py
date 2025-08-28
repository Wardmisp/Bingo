import random
import json
import os

DATABASE_FILE = "bingo_data.json"

def read_data():
    """Reads all data from the JSON file."""
    print(f"Attempting to read data from {DATABASE_FILE}")
    if not os.path.exists(DATABASE_FILE):
        print(f"Database file not found: {DATABASE_FILE}. Returning empty list.")
        return []
    with open(DATABASE_FILE, 'r') as f:
        data = json.load(f)
        print("Successfully read data from file.")
        return data

def write_data(data):
    """Writes all data to the JSON file."""
    print(f"Attempting to write data to {DATABASE_FILE}")
    with open(DATABASE_FILE, 'w') as f:
        json.dump(data, f, indent=4)
    print("Successfully wrote data to file.")

def generate_bingo_card():
    """Generates a random 3x3 bingo card."""
    numbers = random.sample(range(1, 26), 9)
    card_grid = [numbers[i:i+3] for i in range(0, 9, 3)]
    print(f"Generated a new bingo card: {card_grid}")
    return card_grid

def add_new_player(player_name):
    """
    Registers a new player and generates a unique bingo card.
    Returns True on success, False if the player already exists.
    """
    players_data = read_data()
    print(f"Checking for existing player: '{player_name}'")
    
    if any(p.get('name') == player_name for p in players_data):
        print(f"Error: Player '{player_name}' already exists.")
        return False
    
    bingo_card = generate_bingo_card()
    new_player = {
        "name": player_name,
        "bingo_card": bingo_card
    }
    players_data.append(new_player)
    
    print(f"Adding new player to data: {new_player}")
    write_data(players_data)
    return True

def get_all_players():
    """Retrieves all players from the JSON file."""
    print("Fetching all players.")
    return read_data()

def clear_all_players():
    """Clears all players from the JSON file."""
    print("Clearing all players from the database.")
    write_data([])
    print("All players cleared.")
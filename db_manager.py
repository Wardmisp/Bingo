import random
import json
import os

DATABASE_FILE = "bingo_data.json"

def read_data():
    """Reads all data from the JSON file."""
    if not os.path.exists(DATABASE_FILE):
        return []
    with open(DATABASE_FILE, 'r') as f:
        return json.load(f)

def write_data(data):
    """Writes all data to the JSON file."""
    with open(DATABASE_FILE, 'w') as f:
        json.dump(data, f, indent=4)

def generate_bingo_card():
    """Generates a random 3x3 bingo card."""
    numbers = random.sample(range(1, 26), 9)
    card_grid = [numbers[i:i+3] for i in range(0, 9, 3)]
    return card_grid

def add_new_player(player_name):
    """
    Registers a new player and generates a unique bingo card.
    Returns True on success, False if the player already exists.
    """
    players_data = read_data()
    # Check if the player already exists
    if any(p['player_name'] == player_name for p in players_data):
        print(f"Error: Player '{player_name}' already exists.")
        return False
    
    bingo_card = generate_bingo_card()
    new_player = {
        "player_name": player_name,
        "bingo_card": bingo_card
    }
    players_data.append(new_player)
    write_data(players_data)
    return True

def get_all_players():
    """Retrieves all players from the JSON file."""
    return read_data()

def clear_all_players():
    """Clears all players from the JSON file."""
    write_data([])
    print("All players cleared.")
from random import random


def generate_unique_numeric_id(players_collection):
    """Generates a unique 6-digit numeric ID."""
    if players_collection is None:
        return None
    while True:
        # Generates a random 6-digit number
        new_id = str(random.randint(100000, 999999))
        if players_collection.find_one({"gameId": new_id}) is None:
            return new_id
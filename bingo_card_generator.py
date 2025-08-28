import random

def generate_bingo_card():
    """
    Generates a random 3x3 bingo card with numbers from 1 to 25.
    """
    numbers = random.sample(range(1, 26), 9)
    card_grid = [numbers[i:i+3] for i in range(0, 9, 3)]
    return card_grid
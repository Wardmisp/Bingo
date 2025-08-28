import random

def generate_bingo_card(devious = False):
    """
    Generates a random 5x3 bingo card with 6 blank spaces.
    """
    if not devious :
        # Step 1: Generate 9 random numbers from 1 to 25.
        numbers = random.sample(range(1, 26), 9)

        # Step 2: Create a list with the numbers and 6 blank spaces (e.g., None or an empty string).
        # Using None is a good way to represent the blank spaces.
        card_elements = numbers + [None] * 6

        # Step 3: Shuffle the list to randomize the position of the numbers and blanks.
        random.shuffle(card_elements)

        # Step 4: Reshape the flat list into a 5-column, 3-row grid.
        # This creates a list of lists, where each inner list represents a row.
        card_grid = [card_elements[i:i+5] for i in range(0, 15, 5)]

    return card_grid
from flask import Flask, jsonify, request, render_template
import db_manager

app = Flask(__name__)

# This route serves the HTML page to the browser.
@app.route('/')
def serve_page():
    return render_template('index.html')

# This endpoint is for your Android app to register a new player.
@app.route('/players/register', methods=['POST'])
def register_player():
    data = request.get_json()
    if not data or 'player_name' not in data:
        return jsonify({"error": "Player name not provided"}), 400
    
    player_name = data['player_name']
    
    if db_manager.add_new_player(player_name):
        return jsonify({
            "status": "success",
            "message": f"Player '{player_name}' registered successfully!"
        })
    else:
        return jsonify({"error": f"Player '{player_name}' already exists"}), 409

# This endpoint is for your webpage to get all players and their cards.
@app.route('/players')
def get_players():
    players = db_manager.get_all_players()
    return jsonify(players)

# A special endpoint to clear the database for a new game.
@app.route('/game/reset', methods=['POST'])
def reset_game():
    db_manager.clear_all_players()
    return jsonify({"status": "success", "message": "All players cleared for a new game."})

if __name__ == '__main__':
    # Initialize the file on app start if it doesn't exist
    if not os.path.exists(db_manager.DATABASE_FILE):
        db_manager.write_data([])
    app.run(port=3000)
# Bingo Game 🎲 (Under Construction)

A real-time mobile bingo game built with Kotlin and Jetpack Compose for the Android front-end and Python/Flask for the web API backend. This project is currently under development and aims to be a full-stack application with a mobile client and a web dashboard.

## 🌟 Features (Under Development)

- **Mobile App (Android)**: A client-side application that allows players to join a game.
- **Web API (Python/Flask)**: A RESTful API that handles player registration, generates a unique bingo card for each player, and stores the data.
- **Real-Time Web Dashboard**: A web page that displays all registered players and their unique bingo cards in real-time. This page automatically updates every 3 seconds to reflect new registrations.
- **Persistent Data**: Player and bingo card data are stored in a local JSON file (`bingo_data.json`) on the server, ensuring data isn't lost when the server restarts.

## 🛠️ Technologies

- **Android**: Kotlin, Jetpack Compose, Coroutines, ViewModel, Retrofit, and OkHttp.
- **Backend**: Python, Flask, Gunicorn (for production).
- **Database**: JSON file (`bingo_data.json`) for simple, file-based persistence.
- **Hosting**: Render (for the public web service).

## 🚀 Getting Started

### Backend Setup (Local)

1.  **Clone the repository**:
    ```bash
    git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)
    cd your-repo-name/backend
    ```
2.  **Create a virtual environment** and install dependencies:
    ```bash
    python -m venv venv
    source venv/bin/activate  # On Windows, use `venv\Scripts\activate`
    pip install -r requirements.txt
    ```
    Your `requirements.txt` should contain `Flask`.
3.  **Run the server**:
    ```bash
    python app.py
    ```
    The server will run on `http://127.0.0.1:5000`.

### Mobile App Setup (Android)

1.  **Open the project** in Android Studio.
2.  **Update the API URL**:
    - If you're using the **local server**, find your computer's local IP address and update the `BASE_URL` in your `ApiService` file:
      ```kotlin
      const val BASE_URL = "http://YOUR_LOCAL_IP:5000/"
      ```
    - If you're using the **public web service** hosted on Render, update the `BASE_URL` to the public URL:
      ```kotlin
      const val BASE_URL = "[https://bingo-jl6k.onrender.com/](https://bingo-jl6k.onrender.com/)"
      ```
3.  **Build and run** the app on an emulator or a physical device.

## 🖥️ Web Dashboard

The web dashboard is a live view of all registered players and their cards. You can access it at:

**[https://bingo-jl6k.onrender.com/](https://bingo-jl6k.onrender.com/)**

This page will automatically update as new players are registered through the mobile app.

## 📁 Project Structure

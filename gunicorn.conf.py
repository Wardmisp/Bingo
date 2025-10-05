import os

# This setting tells Gunicorn to bind to the IP and PORT that Render provides.
# '0.0.0.0' means it will listen on all available network interfaces.
bind = f"0.0.0.0:{os.environ.get('PORT', 8080)}"

# -- THE FIX FOR YOUR PID ISSUE --
# This is the single most important line for your problem. It ensures only ONE
# process is running, so your global dictionaries (streams, game_sequences) are shared.
workers = 1

# Threads can handle multiple requests within a single worker process.
# This is a good way to get concurrency without the memory separation issue.
threads = 4

# -- THE FIX FOR YOUR TIMEOUT ISSUE --
# This prevents Gunicorn from killing your long-lived SSE connections.
# The value is in seconds.
timeout = 120

# -- A PERFORMANCE BOOST FOR YOUR APP --
# 'gevent' is an asynchronous worker class, which is highly recommended for
# applications that have many long-lived, idle connections like your SSE stream.
worker_class = 'gevent'

# -- LOGGING CONFIGURATION --
# Send access logs and error logs to stdout so they appear in your Render log stream.
# The '-' means stdout.
accesslog = '-'
errorlog = '-'
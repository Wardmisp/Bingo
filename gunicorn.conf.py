import os

# -- ADD THIS NEW FUNCTION --
def post_fork(server, worker):
    """
    Called in a worker process after it has been forked.
    This is the correct place to start background tasks.
    """
    # We must import the app components here, inside the function.
    from app import number_generator_thread
    import threading

    server.log.info("Starting number generator thread in worker %s", worker.pid)
    thread = threading.Thread(target=number_generator_thread)
    thread.daemon = True
    thread.start()


# This setting tells Gunicorn to bind to the IP and PORT that Render provides.
# '0.0.0.0' means it will listen on all available network interfaces.
bind = f"0.0.0.0:{os.environ.get('PORT', 8080)}"

# This is the single most important line for your problem. It ensures only ONE
# process is running, so your global dictionaries (streams, game_sequences) are shared.
workers = 1

# Threads can handle multiple requests within a single worker process.
# This is a good way to get concurrency without the memory separation issue.
threads = 4

# This prevents Gunicorn from killing your long-lived SSE connections.
# The value is in seconds.
timeout = 120

# 'gevent' is an asynchronous worker class, which is highly recommended for
# applications that have many long-lived, idle connections like your SSE stream.
worker_class = 'gevent'

# Send access logs and error logs to stdout so they appear in your Render log stream.
# The '-' means stdout.
accesslog = '-'
errorlog = '-'


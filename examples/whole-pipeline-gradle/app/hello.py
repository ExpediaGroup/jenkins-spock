import counter
from flask import Flask

app = Flask(__name__)
greeted_times = 0

@app.route("/")
def hello():
    global greeted_times
    
    greeting = "Hello World! I've greeted {0} times!".format( greeted_times )
    
    greeted_times = counter.plusone( greeted_times )
    
    return greeting
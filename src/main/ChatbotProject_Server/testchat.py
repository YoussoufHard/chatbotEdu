from flask import Flask, request, render_template_string, jsonify
import google.generativeai as genai
import os

from dotenv import load_dotenv

# Charger les variables d'environnement depuis le fichier .env
load_dotenv()

# Récupérer la clé API de l'environnement
api_key = os.getenv("API_KEY")

# Vérifier que la clé API est présente
if not api_key:
    raise ValueError("API_KEY is not set in environment variables.")

# Configure Gemini avec la clé API
genai.configure(api_key=api_key)
model = genai.GenerativeModel("gemini-1.5-flash")

# Créer l'application Flask
app = Flask(__name__)

@app.route('/', methods=['GET', 'POST'])
def index():
    response_text = ""
    if request.method == 'POST':
        # Récupérer le prompt envoyé par l'utilisateur
        prompt = request.form.get("prompt", "")
        if prompt:
            try:
                # Appel API pour générer le contenu
                response = model.generate_content(prompt)
                response_text = response.text
            except Exception as e:
                response_text = f"Error: {str(e)}"
        else:
            response_text = "No prompt provided."
    
    # Rendre la page HTML avec le formulaire et la réponse
    return render_template_string("""
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Question Answer</title>
        </head>
        <body>
            <h1>Ask a Question</h1>
            <form method="POST">
                <label for="prompt">Your Question:</label><br>
                <input type="text" id="prompt" name="prompt" required><br><br>
                <input type="submit" value="Submit">
            </form>

            {% if response_text %}
                <h2>Response:</h2>
                <p>{{ response_text }}</p>
            {% endif %}
        </body>
        </html>
    """, response_text=response_text)

if __name__ == '__main__':
    # Lancer l'application Flask
    app.run(debug=True, host="0.0.0.0", port=5000)

from flask import Flask, request, jsonify
import google.generativeai as genai
import os
from werkzeug.utils import secure_filename
import logging

from dotenv import load_dotenv

# Charger les variables d'environnement depuis le fichier .env
load_dotenv()

# Récupérer la clé API de l'environnement
api_key = os.getenv("API_KEY")

# Configuration des journaux
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Configure Gemini
try:
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel("gemini-1.5-flash")
    logger.info("Modèle Gemini configuré avec succès.")
except Exception as e:
    logger.error(f"Erreur de configuration du modèle Gemini : {e}")
    raise RuntimeError("Erreur lors de la configuration de l'API Gemini.")

# Initialisation de l'application Flask
app = Flask(__name__)

# Définir les extensions de fichier autorisées
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'pdf'}

# Vérifier si le fichier uploadé a une extension valide
def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/generate', methods=['POST'])
def generate_response():
    """
    Générer une réponse basée sur un prompt donné.
    """
    data = request.json
    prompt = data.get('prompt', '')
    if not prompt:
        return jsonify({'error': 'Aucun prompt fourni.'}), 400

    try:
        response = model.generate_content(prompt)
        if not hasattr(response, 'text'):
            raise ValueError("Réponse invalide retournée par l'API Gemini.")
        logger.info(f"Reponse generer envoyé avec succès")
        return jsonify({'response': response.text})
    except Exception as e:
        logger.error(f"Erreur lors de la génération de contenu : {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/upload', methods=['POST'])
def upload_file():
    """
    Upload de fichiers avec validation des extensions.
    """
    if 'file' not in request.files:
        return jsonify({'error': 'Aucun fichier fourni.'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'Aucun fichier sélectionné.'}), 400

    if file and allowed_file(file.filename):
        # Sécuriser le nom de fichier
        filename = secure_filename(file.filename)
        file_path = os.path.join('uploads', filename)

        # Créer le répertoire "uploads" s'il n'existe pas
        os.makedirs('uploads', exist_ok=True)

        try:
            file.save(file_path)
            logger.info(f"Fichier sauvegardé à : {file_path}")
            return jsonify({'message': f'Fichier {filename} uploadé avec succès.'})
        except Exception as e:
            logger.error(f"Erreur lors de la sauvegarde du fichier : {e}")
            return jsonify({'error': 'Erreur lors de la sauvegarde du fichier.'}), 500
    else:
        return jsonify({'error': 'Type de fichier invalide. Autorisés : PNG, JPG, JPEG, PDF.'}), 400

# Point d'entrée de l'application
if __name__ == '__main__':
    app.run(port=5000, debug=True)

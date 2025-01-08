import google.generativeai as genai
import os

# Récupérer la clé API de l'environnement système
api_key = os.getenv("API_KEY")

# Vérifier si la clé API est présente dans les variables d'environnement
if not api_key:
    raise ValueError("La clé API n'est pas définie dans les variables d'environnement.")

# Configure Gemini avec la clé API
genai.configure(api_key=api_key)
model = genai.GenerativeModel("gemini-1.5-flash")

# Fonction pour générer du contenu avec Gemini
def generate_content(prompt):
    try:
        response = model.generate_content(prompt)
        if not hasattr(response, 'text'):
            raise ValueError("Réponse invalide retournée par l'API Gemini.")
        return response.text
    except Exception as e:
        print(f"Erreur lors de la génération de contenu : {e}")
        return str(e)

import faiss
import numpy as np
from flask_cors import CORS  # Importer CORS
from sentence_transformers import SentenceTransformer
from flask import Flask, request, jsonify

from gemini_api import generate_content  # Utilisation de Gemini via gemini_api.py
from faiss_index import get_documents_from_db 

# Chargement de l'index FAISS
def load_faiss_index(index_file="documents_index.faiss"):
    return faiss.read_index(index_file)

# Fonction pour rechercher les documents dans l'index FAISS
def search_documents(query, index, documents):
    model = SentenceTransformer('paraphrase-MiniLM-L6-v2')  # Utilisation du même modèle pour encoder la requête
    query_vector = model.encode([query])  # Encoder la requête de l'utilisateur en vecteur
    distances, indices = index.search(np.array(query_vector), k=3)  # Chercher les 3 documents les plus proches
    relevant_docs = [documents[i] for i in indices[0]]  # Récupérer les titres et contenus des documents pertinents
    return relevant_docs

# Générer la réponse avec Gemini via gemini_api.py
def generate_answer(query, documents):
    # Créer le contexte à partir des documents pertinents
    context = "\n".join([doc[1] for doc in documents])  # Concaténer les contenus des documents
    
    # Créer le prompt en concaténant la query et le contexte
    prompt = f"Voici des informations pertinentes : {context}\n\nRépondez à cette question : {query}"
    
    # Appeler generate_content avec le prompt complet
    answer = generate_content(prompt)  # Passer un seul argument 'prompt'
    
    return answer  # Retourner la réponse générée

# Initialisation de Flask pour l'API
app = Flask(__name__)

CORS(app)  # Permet toutes les origines

# Endpoint pour interroger le chatbot via une API REST
@app.route('/rag', methods=['POST'])
def rag_handler():
    data = request.json
    query = data.get('query', '')
    
    if not query:
        return jsonify({'error': 'Aucune requête fournie.'}), 400
    
    # Charger l'index FAISS et les documents
    index = load_faiss_index("documents_index.faiss")
    documents = get_documents_from_db()  # Récupérer les documents depuis la base de données
    
    # Rechercher les documents pertinents
    relevant_docs = search_documents(query, index, documents)
    
    # Générer la réponse avec Gemini
    answer = generate_answer(query, relevant_docs)
    
    # Extraire les titres des documents consultés pour les afficher sur l'interface
    document_titles = [doc[0] for doc in relevant_docs]  # Supposons que doc[0] est le titre
    
    # Retourner la réponse avec les titres des documents consultés
    return jsonify({
        'response': answer,
        'consulted_documents': document_titles
    })

# Lancer l'application Flask
if __name__ == '__main__':
    app.run(port=5000, debug=True)
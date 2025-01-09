import os
import torch
from transformers import CLIPProcessor, CLIPModel
import easyocr  # Pour l'OCR
from flask import Flask, request, jsonify
from flask_cors import CORS  # Pour activer les requêtes cross-origin
import faiss
import numpy as np
from PIL import Image
from sentence_transformers import SentenceTransformer

from gemini_api import generate_content  # Utilisation de Gemini via gemini_api.py
from faiss_index import get_documents_from_db 

# Chargement du modèle CLIP
model_name = "openai/clip-vit-base-patch16"  # Modèle alternatif
clip_model = CLIPModel.from_pretrained(model_name)
clip_processor = CLIPProcessor.from_pretrained(model_name)

# Chargement du modèle SentenceTransformer
st_model = SentenceTransformer('paraphrase-MiniLM-L6-v2')

# OCR pour extraire le texte d'une image
def extract_text_from_image_with_ocr(image_path):
    reader = easyocr.Reader(['fr', 'en'])  # Supporte plusieurs langues
    results = reader.readtext(image_path, detail=0)  # Extraire uniquement le texte
    return " ".join(results)  # Combiner tout le texte extrait

# CLIP pour comprendre le contexte de l'image
def extract_context_with_clip(image_path):
    image = Image.open(image_path).convert("RGB")
    inputs = clip_processor(images=image, return_tensors="pt", padding=True)
    image_features = clip_model.get_image_features(**inputs)

    # Liste de descriptions pour le contexte
    descriptions = ["texte dans l'image", "diagramme", "graphique", "photo", "image de tableau"]
    text_inputs = clip_processor(text=descriptions, return_tensors="pt", padding=True)
    text_features = clip_model.get_text_features(**text_inputs)

    # Calculer les similarités
    similarities = torch.nn.functional.cosine_similarity(image_features, text_features)
    best_match_idx = similarities.argmax().item()

    return descriptions[best_match_idx]  # Retourner la meilleure correspondance

# Fonction pour enrichir la requête avec OCR + CLIP
def enrich_query_with_image(image_path):
    ocr_text = extract_text_from_image_with_ocr(image_path)
    image_context = extract_context_with_clip(image_path)

    # Combiner le texte OCR et le contexte
    enriched_query = f"{ocr_text} (Contexte : {image_context})"

      # Supprimer le fichier temporaire après utilisation
    if os.path.exists(image_path):
        os.remove(image_path)  # Supprimer le fichier image temporaire
    return enriched_query

# Chargement de l'index FAISS
def load_faiss_index(index_file="documents_index.faiss"):
    return faiss.read_index(index_file)

# Recherche de documents dans l'index FAISS
def search_documents(query, index, documents):
    query_vector = st_model.encode([query])  # Encoder la requête
    distances, indices = index.search(np.array(query_vector, dtype="float32"), k=3)  # Top 3 documents
    relevant_docs = [documents[i] for i in indices[0]]  # Récupérer les documents pertinents
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

# Route principale /rag qui gère à la fois texte et image
@app.route('/rag', methods=['POST'])
def rag_handler():
    # Vérifier le type de contenu
    if request.content_type == 'application/json':
        data = request.json  # Récupérer le contenu JSON
        query = data.get('query', '')  # Récupérer la requête texte
        image_file = None  # Aucune image pour cette requête

    elif 'multipart/form-data' in request.content_type:
        # Pour les fichiers et les données (texte + image)
        query = request.form.get('query', '')  # Texte de la requête
        image_file = request.files.get('image', None)  # Fichier image

    else:
        return jsonify({'error': 'Type de contenu non pris en charge.'}), 415

    if not query and not image_file:
        return jsonify({'error': 'Aucune requête ou image fournie.'}), 400

    enriched_query = query  # Par défaut, la requête est simplement le texte fourni par l'utilisateur

    # Si l'image est fournie, l'analyser avec OCR et CLIP
    if image_file:
        image_path = "temp_image.jpg"  # Enregistrer temporairement l'image
        image_file.save(image_path)
        enriched_query = enrich_query_with_image(image_path)  # Enrichir la requête avec le texte extrait de l'image et le contexte

    # Charger l'index FAISS et les documents
    index = load_faiss_index("documents_index.faiss")
    documents = get_documents_from_db()

    # Rechercher les documents pertinents
    relevant_docs = search_documents(enriched_query, index, documents)

    # Générer la réponse avec Gemini ou tout autre modèle
    answer = generate_answer(enriched_query, relevant_docs)

    # Extraire les titres des documents consultés pour les afficher sur l'interface
    document_titles = [doc[0] for doc in relevant_docs]  # Titres des documents

    # Retourner la réponse avec les titres des documents consultés
    return jsonify({
        'response': answer,
        'consulted_documents': document_titles
    })

# Lancer l'application Flask
if __name__ == '__main__':
    clip_model = CLIPModel.from_pretrained(model_name)  # Initialiser le modèle CLIP ici
    app.run(port=5000, debug=True)

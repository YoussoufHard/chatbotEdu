import faiss
import numpy as np
import mysql.connector
from sentence_transformers import SentenceTransformer

# Configuration de la base de données MySQL
db_config = {
    "host": "localhost",
    "user": "root",
    "password": "",  # Mot de passe MySQL
    "database": "chatbot_edu"  # Nom de la base de données
}

# Connexion à la base de données MySQL
def connect_to_db():
    return mysql.connector.connect(**db_config)

# Fonction pour récupérer les documents de la base de données
def get_documents_from_db():
    db = connect_to_db()
    cursor = db.cursor()
    query = "SELECT title, content FROM documents"
    cursor.execute(query)
    documents = cursor.fetchall()
    cursor.close()
    db.close()
    return documents

# Transformer les documents en vecteurs avec un modèle pré-entraîné
def embed_documents(documents):
    model = SentenceTransformer('paraphrase-MiniLM-L6-v2')  # Utilisation d'un modèle pré-entraîné
    texts = [doc[1] for doc in documents]  # Récupérer le contenu des documents
    vectors = model.encode(texts)  # Encoder les documents en vecteurs
    return np.array(vectors)

# Fonction pour indexer les documents dans FAISS
def index_documents(vectors):
    dim = vectors.shape[1]  # Dimension des vecteurs
    index = faiss.IndexFlatL2(dim)  # Créer un index FAISS de type L2 (distance euclidienne)
    index.add(vectors)  # Ajouter les vecteurs dans l'index
    return index

# Fonction pour sauvegarder l'index FAISS sur le disque
def save_faiss_index(index, index_file):
    faiss.write_index(index, index_file)

# Fonction principale pour lier le tout
def main():
    # Récupérer les documents de la base de données
    documents = get_documents_from_db()
    
    # Convertir les documents en vecteurs
    vectors = embed_documents(documents)
    
    # Indexer les vecteurs dans FAISS
    index = index_documents(vectors)
    
    # Sauvegarder l'index FAISS sur le disque
    index_file = "documents_index.faiss"
    save_faiss_index(index, index_file)
    
    print(f"Index FAISS sauvegardé dans : {index_file}")

if __name__ == "__main__":
    main()

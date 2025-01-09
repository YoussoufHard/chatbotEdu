import os
import mysql.connector
from PyPDF2 import PdfReader

# Configuration de la base de données
db_config = {
    "host": "localhost",  # Hôte de ta base de données
    "user": "root",       # Utilisateur MySQL
    "password": "",  # Mot de passe MySQL (à remplacer par ton propre mot de passe)
    "database": "chatbot_edu"  # Nom de ta base de données
}

# Chemin vers le dossier contenant les documents
directory = r'C:\Users\Lenovo\Documents\II-BDCC ENSET\BIG DATA S3\S3_2024-2025\POO JAVA\ChatbotProject_server\ENSET docs'

# Connexion à la base de données MySQL
def connect_to_db():
    return mysql.connector.connect(**db_config)

# Fonction pour lire le contenu d'un PDF
def extract_text_from_pdf(file_path):
    try:
        reader = PdfReader(file_path)
        text = ""
        for page in reader.pages:
            text += page.extract_text()
        return text
    except Exception as e:
        return f"Erreur lors de l'extraction du PDF : {str(e)}"

# Fonction pour vérifier si le document existe déjà dans la base de données
def document_exists(cursor, title):
    query = "SELECT COUNT(*) FROM documents WHERE title = %s"
    cursor.execute(query, (title,))
    result = cursor.fetchone()
    return result[0] > 0  # Si le compte est supérieur à 0, le document existe

# Fonction pour insérer un document dans la base de données
def insert_document(cursor, title, content, file_path):
    query = "INSERT INTO documents (title, content, file_path) VALUES (%s, %s, %s)"
    cursor.execute(query, (title, content, file_path))

# Fonction pour lire tous les fichiers dans le dossier
def read_documents_from_directory(directory, cursor):
    for filename in os.listdir(directory):
        file_path = os.path.join(directory, filename)
        if filename.endswith(".pdf"):
            # Vérifier si le document existe déjà
            if not document_exists(cursor, filename):
                # Lire le contenu du fichier PDF
                content = extract_text_from_pdf(file_path)
                insert_document(cursor, filename, content, file_path)
                print(f"Document '{filename}' ajouté.")
            else:
                print(f"Le document '{filename}' existe déjà dans la base de données.")
        elif filename.endswith(".docx"):
            # Ajouter la gestion des fichiers Word (facultatif)
            pass
        # Ajouter d'autres formats de fichiers si nécessaire

# Exécution principale
def main():
    try:
        db = connect_to_db()
        cursor = db.cursor()

        # Lire et insérer les documents du dossier
        read_documents_from_directory(directory, cursor)

        # Commit des changements
        db.commit()
        print("Documents ajoutés avec succès à la base de données.")
    except Exception as e:
        print(f"Erreur lors du traitement : {str(e)}")
    finally:
        cursor.close()
        db.close()

if __name__ == "__main__":
    main()

import requests

# L'URL de l'API Flask
api_url = "http://127.0.0.1:5000/rag"  # Remplacez par l'URL de votre serveur Flask

# 1. Tester avec une requête simple (texte uniquement)
def test_query():
    query = "Quel est l'impact de l'intelligence artificielle sur la société meme si tu trouve pas dans les document repond d'une maniere general?"
    payload = {
        'query': query
    }
    
    headers = {
        'Content-Type': 'application/json'
    }

    response = requests.post(api_url, json=payload, headers=headers)

    if response.status_code == 200:
        print("Réponse de l'API (texte) :")
        print(response.json())
    else:
        print(f"Erreur lors de l'appel API : {response.status_code}")

# 2. Tester avec une image (et une requête texte)
def test_image():
    query = "Décrivez le contenu de cette image."
    image_path = "image.png"  # Remplacez par le chemin de votre image

    files = {
        'image': open(image_path, 'rb')
    }

    data = {
        'query': query
    }

    response = requests.post(api_url, files=files, data=data)

    if response.status_code == 200:
        print("Réponse de l'API (image) :")
        print(response.json())
    else:
        print(f"Erreur lors de l'appel API : {response.status_code}")

# Appeler les tests
if __name__ == "__main__":
    print("Test avec requête texte :")
    test_query()
    print("\nTest avec image :")
    test_image()

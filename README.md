# ChatbotEdu - Chatbot Éducatif pour l'ENSET

**ChatbotEdu** est une application éducative innovante destinée à fournir des réponses pertinentes et contextualisées aux étudiants de l'ENSET. Ce projet exploite une architecture RAG (Retrieval-Augmented Generation) pour combiner un modèle pré-entraîné et des données documentaires enrichies issues de la base de données pédagogique de l'ENSET. L'application prend également en charge des fonctionnalités multimodales, permettant des interactions via texte, documents et images.

---

## 🎯 Objectif du Projet

Dans le but de doter les étudiants d'outils numériques adaptés aux tendances actuelles, **ChatbotEdu** vise à :  
1. Proposer un chatbot éducatif basé sur l'architecture RAG.  
2. Enrichir les réponses avec des documents pédagogiques (PDFs, notes, présentations).  
3. Fournir une interaction multimodale (texte, documents visuels comme images et schémas).  
4. Offrir une interface utilisateur intuitive et interactive.

---

## 🏗️ Architecture du Projet

### Schéma général
```plaintext
Frontend (JavaFX) ↔ Backend (FastAPI ou Flask)
Backend ↔ Gemini API (génération de texte et multimodalité)
Backend ↔ Base de données (MySQL/PostgreSQL)
Backend ↔ Moteur de recherche avancé (FAISS)
Backend ↔ Moteur multimodal (CLIP / Gemini Vision)
```

### Composants principaux
1. **Frontend (JavaFX)** : Interface utilisateur intuitive pour une interaction fluide avec le chatbot.
2. **Backend (Flask ou FastAPI)** : Gestion des API REST pour les interactions entre le chatbot, la base de données, le moteur de recherche et le modèle LLM.
3. **Base de données (MySQL/PostgreSQL)** : Stockage des questions, réponses et documents pédagogiques.
4. **Moteur de recherche (FAISS)** : Recherche rapide et précise des documents pertinents.
5. **Moteur multimodal (CLIP / Gemini Vision)** : Analyse et traitement des requêtes basées sur des images.

---

## 📋 Étapes de Développement

### Étape 1 : Configuration de l'architecture
- Mise en place de l'architecture Frontend-Backend-Base de données.
- Configuration du moteur FAISS pour la recherche documentaire.

### Étape 2 : Première incrémentation - Chatbot textuel simple (MVP)
1. Implémente un chatbot textuel basé sur des règles ou un ensemble de données pré-défini.
2. Utilise Flask ou FastAPI pour le backend.
3. Interface JavaFX simple pour les questions/réponses.
4. Stockage des interactions dans MySQL.

### Étape 3 : Intégration d’un moteur de recherche avancé
1. Importation de documents pédagogiques dans FAISS.
2. Service Python pour interroger FAISS et récupérer des documents pertinents.
3. Intégration de la recherche documentaire dans le backend et l'interface utilisateur.

### Étape 4 : Génération de texte avec un LLM
1. Connexion au LLM (par exemple, Gemini API ou GPT-4 via Hugging Face).
2. Utilisation des résultats FAISS pour enrichir les réponses générées.
3. Ajout de la fonctionnalité "sources utilisées" dans l'interface.

### Étape 5 : Fonctionnalités multimodales
1. Intégration d'un modèle comme CLIP ou Gemini Vision pour analyser les images.
2. Ajout d'une interface JavaFX pour télécharger et afficher les images.
3. Affichage des résultats d'analyse multimodale (texte généré à partir des images).

---

## 🌟 Fonctionnalités Clés

- **Architecture RAG** : Intégration des données locales et d'un modèle LLM pour des réponses enrichies.
- **Recherche avancée** : Utilisation de FAISS pour un moteur de recherche rapide et précis.
- **Multimodalité** : Analyse de schémas, graphiques et autres contenus visuels.
- **Interface conviviale** : JavaFX pour une interaction fluide et un affichage intuitif des résultats.
- **Historique des interactions** : Suivi des conversations pour une expérience utilisateur améliorée.

---

## 🛠️ Installation et Utilisation

### Prérequis
- [Java 17+](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Python 3.9+](https://www.python.org/downloads/)
- [Maven](https://maven.apache.org/)
- MySQL ou PostgreSQL
- Bibliothèques Python : `fastapi`, `flask`, `faiss`, `transformers`, `mysql-connector-python`.

### Étapes
1. Clonez le projet :
   ```bash
   git clone https://github.com/YoussoufHard/chatbotEdu.git
   ```
2. Installez les dépendances pour le backend Python :
   ```bash
   pip install -r requirements.txt
   ```
3. Configurez la base de données :
   - Créez une base de données et chargez les documents pédagogiques.
   - Mettez à jour le fichier de configuration avec vos identifiants.

4. Compilez et exécutez le frontend :
   ```bash
   mvn install
   mvn javafx:run
   ```
5. Lancez le serveur backend mais avant de lancer le server il faut executer import_documents.py et aussi faiss_index.py:
   ```bash
   python import_documents.py
   pyhton faiss_index.py
   python server.py
   ```

---

## 🧪 Tests et Évaluation

1. **Textuel** : Interactions simples avec des questions/réponses prédéfinies.
2. **Documentaire** : Recherche et récupération de documents pédagogiques.
3. **Multimodalité** : Analyse et génération de texte à partir d'images.

---

## 👨‍💻 Contributeurs
- **TANGARA YOUSSOUF** - Développeur principal.
- - **TSEH KOKOU BENOIT** - Développeur principal.

---

## 📄 Licence
Ce projet est sous licence [MIT](LICENSE).

---

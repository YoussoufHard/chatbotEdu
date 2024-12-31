from flask import Flask, request, jsonify
import google.generativeai as genai

# Configure Gemini
genai.configure(api_key="AIzaSyAtLKzD_wSVGBK8yF5OaWXWTO3M-LMxftg")
model = genai.GenerativeModel("gemini-1.5-flash")

# Create Flask app
app = Flask(__name__)

@app.route('/generate', methods=['POST'])
def generate():
    data = request.json
    prompt = data.get("prompt", "")
    if not prompt:
        return jsonify({"error": "No prompt provided"}), 400
    try:
        # Appel API pour générer le contenu
        response = model.generate_content(prompt)
        
        # Renvoyer la réponse brute de l'API sans transformation
        return jsonify({"response": response.text})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000)

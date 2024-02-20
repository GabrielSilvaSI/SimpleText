from flask import Flask, jsonify, request
from cryptography.hazmat.primitives.asymmetric import dh
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.backends import default_backend

app = Flask(__name__)

@app.route('/public-key', methods=['GET'])
def get_public_key():
    # Gerar par de chaves Diffie-Hellman
    parameters = dh.generate_parameters(generator=2, key_size=2048, backend=default_backend())
    private_key = parameters.generate_private_key()
    public_key = private_key.public_key().public_bytes(encoding=serialization.Encoding.PEM, format=serialization.PublicFormat.SubjectPublicKeyInfo)
    return jsonify({'public_key': public_key.decode()})

@app.route('/shared-secret', methods=['POST'])
def calculate_shared_secret():
    data = request.get_json()
    public_key_bytes = data['public_key'].encode()
    public_key = serialization.load_pem_public_key(public_key_bytes, backend=default_backend())
    shared_key = private_key.exchange(public_key)
    return jsonify({'shared_secret': shared_key.hex()})

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")

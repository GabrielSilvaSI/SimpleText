package com.banilla.simplechat

import java.math.BigInteger
import javax.crypto.KeyAgreement
import javax.crypto.spec.DHParameterSpec
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

class DiffieHellmanHelper {

    private val keyPair: KeyPair

    init {
        // Passo 1: Geração dos Parâmetros
        val prime = BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74"
                + "020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6D"
                + "F25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B"
                + "0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651"
                + "ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F"
                + "83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E"
                + "4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E8603"
                + "9B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497C"
                + "EA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33"
                + "A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85"
                + "A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B"
                + "F12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE11757"
                + "7A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E"
                + "4B82D120A92108011A723C12A787E6D788719A10BDBA5B2699C32718"
                + "AF7846B4FCC8864E8D3E82AFFFFFFFFFFFFFFFF", 16)
        val generator = BigInteger("2")
        val dhParamSpec = DHParameterSpec(prime, generator)

        // Passo 2: Geração das Chaves
        val keyPairGenerator = KeyPairGenerator.getInstance("DH")
        keyPairGenerator.initialize(dhParamSpec)
        keyPair = keyPairGenerator.generateKeyPair()
    }
    fun getPublicKey(): ByteArray {
        // Retorna a chave pública codificada
        return keyPair.public.encoded
    }

    fun computeSharedSecret(otherPublicKey: ByteArray): ByteArray {
        // Passo 4: Derivação do Segredo Compartilhado
        val keyAgreement = KeyAgreement.getInstance("DH")
        val keyFactory = KeyFactory.getInstance("DH")

        // Convertendo a chave pública do outro lado para um objeto PublicKey
        val otherPublicKeySpec = X509EncodedKeySpec(otherPublicKey)
        val otherPublicKeyObj = keyFactory.generatePublic(otherPublicKeySpec)

        // Inicializando o KeyAgreement com a chave privada própria
        keyAgreement.init(keyPair.private)

        // Executando o cálculo do segredo compartilhado
        keyAgreement.doPhase(otherPublicKeyObj, true)

        // Retorna o segredo compartilhado
        return keyAgreement.generateSecret()
    }
}

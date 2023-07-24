package it.smartcommunitylab.aac.openid.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for generating hashes for access tokens and authorization codes
 * to be included in an ID Token.
 *
 * @author Amanda Anganes
 *
 */
public class IdTokenHashUtils {

    private static final Logger logger = LoggerFactory.getLogger(IdTokenHashUtils.class);

    /**
     * Compute the SHA hash of an authorization code
     *
     * @param signingAlg
     * @param code
     * @return
     */
    public static Base64URL getCodeHash(JWSAlgorithm signingAlg, String code) {
        return getHash(signingAlg, code.getBytes());
    }

    /**
     * Compute the SHA hash of a token
     *
     * @param signingAlg
     * @param token
     * @return
     */
    public static Base64URL getAccessTokenHash(JWSAlgorithm signingAlg, String token) {
        return getHash(signingAlg, token.getBytes());
    }

    /**
     * Compute the SHA hash of a token
     *
     * @param signingAlg
     * @param token
     * @return
     */
    public static Base64URL getAccessTokenHash(JWSAlgorithm signingAlg, SignedJWT token) {
        byte[] tokenBytes = token.serialize().getBytes();

        return getHash(signingAlg, tokenBytes);
    }

    public static Base64URL getHash(JWSAlgorithm signingAlg, byte[] bytes) {
        //Switch based on the given signing algorithm - use SHA-xxx with the same 'xxx' bitnumber
        //as the JWSAlgorithm to hash the token.
        String hashAlg = null;

        if (
            signingAlg.equals(JWSAlgorithm.HS256) ||
            signingAlg.equals(JWSAlgorithm.ES256) ||
            signingAlg.equals(JWSAlgorithm.RS256) ||
            signingAlg.equals(JWSAlgorithm.PS256)
        ) {
            hashAlg = "SHA-256";
        } else if (
            signingAlg.equals(JWSAlgorithm.ES384) ||
            signingAlg.equals(JWSAlgorithm.HS384) ||
            signingAlg.equals(JWSAlgorithm.RS384) ||
            signingAlg.equals(JWSAlgorithm.PS384)
        ) {
            hashAlg = "SHA-384";
        } else if (
            signingAlg.equals(JWSAlgorithm.ES512) ||
            signingAlg.equals(JWSAlgorithm.HS512) ||
            signingAlg.equals(JWSAlgorithm.RS512) ||
            signingAlg.equals(JWSAlgorithm.PS512)
        ) {
            hashAlg = "SHA-512";
        }

        if (hashAlg != null) {
            try {
                MessageDigest hasher = MessageDigest.getInstance(hashAlg);
                hasher.reset();
                hasher.update(bytes);

                byte[] hashBytes = hasher.digest();
                byte[] hashBytesLeftHalf = Arrays.copyOf(hashBytes, hashBytes.length / 2);
                Base64URL encodedHash = Base64URL.encode(hashBytesLeftHalf);

                return encodedHash;
            } catch (NoSuchAlgorithmException e) {
                logger.error("No such algorithm error: ", e);
            }
        }

        return null;
    }
}

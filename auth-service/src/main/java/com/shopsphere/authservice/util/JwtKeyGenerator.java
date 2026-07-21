package com.shopsphere.authservice.util;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;
public class JwtKeyGenerator {
    public static void main(String[] args) {

        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        String secret = Base64.getEncoder()
                .encodeToString(key.getEncoded());

        System.out.println(secret);
    }
}

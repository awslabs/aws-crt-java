/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.security.PrivateKey;
import java.security.Signature;
import java.io.ByteArrayOutputStream;

/*
 * TlsKeyOperationHandler implementation for using an Android PrivateKey on TlsKeyOperations
 */
public class TlsAndroidPrivateKeyOperationHandler implements TlsKeyOperationHandler {
    private PrivateKey privateKey;

    /*
    * DER encoded DigestInfo value to be prefixed to the hash, used for RSA signing
    * See https://tools.ietf.org/html/rfc3447#page-43
    */
    static byte[] sha1PrefixToRsaSig = { (byte)0x30, (byte)0x21, (byte)0x30, (byte)0x09, (byte)0x06, (byte)0x05, (byte)0x2b, (byte)0x0e, (byte)0x03, (byte)0x02, (byte)0x1a, (byte)0x05, (byte)0x00, (byte)0x04, (byte)0x14 };
    static byte[] sha224PrefixToRsaSig = { (byte)0x30, (byte)0x2d, (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x60, (byte)0x86, (byte)0x48, (byte)0x01, (byte)0x65, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x04, (byte)0x05, (byte)0x00, (byte)0x04, (byte)0x1c };
    static byte[] sha256PrefixToRsaSig = { (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x60, (byte)0x86, (byte)0x48, (byte)0x01, (byte)0x65, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01, (byte)0x05, (byte)0x00, (byte)0x04, (byte)0x20 };
    static byte[] sha384PrefixToRsaSig = { (byte)0x30, (byte)0x41, (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x60, (byte)0x86, (byte)0x48, (byte)0x01, (byte)0x65, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x02, (byte)0x05, (byte)0x00, (byte)0x04, (byte)0x30 };
    static byte[] sha512PrefixToRsaSig = { (byte)0x30, (byte)0x51, (byte)0x30, (byte)0x0d, (byte)0x06, (byte)0x09, (byte)0x60, (byte)0x86, (byte)0x48, (byte)0x01, (byte)0x65, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x03, (byte)0x05, (byte)0x00, (byte)0x04, (byte)0x40 };

    public TlsAndroidPrivateKeyOperationHandler(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void performOperation(TlsKeyOperation operation){
        try{
            if (operation.getType() != TlsKeyOperation.Type.SIGN) {
                operation.completeExceptionally(new Throwable("Android KeyChain PrivateKey only handles SIGN operations."));
                return;
            }

            // A SIGN operation's inputData is the 32bytes of the SHA-256 digest.
            // Before doing the RSA signature, we need to construct a PKCS1 v1.5 DigestInfo.
            // See https://datatracker.ietf.org/doc/html/rfc3447#section-9.2
            byte[] dataToSign = operation.getInput();

            Signature signature = Signature.getInstance("NONEwith" + operation.getSignatureAlgorithm().name());;

            switch(operation.getSignatureAlgorithm()){
                case RSA:
                    /*
                     * DER encoded DigestInfo value to be prefixed to the hash, used for RSA signing
                     * See https://tools.ietf.org/html/rfc3447#page-43
                     */
                    byte[] digestAlgorithm;
                    switch(operation.getDigestAlgorithm()){
                        case SHA1:
                            digestAlgorithm = sha1PrefixToRsaSig;
                        break;
                        case SHA224:
                            digestAlgorithm = sha224PrefixToRsaSig;
                        break;
                        case SHA256:
                            digestAlgorithm = sha256PrefixToRsaSig;
                        break;
                        case SHA384:
                            digestAlgorithm = sha384PrefixToRsaSig;
                        break;
                        case SHA512:
                            digestAlgorithm = sha512PrefixToRsaSig;
                        break;
                        case UNKNOWN:
                        default:
                            operation.completeExceptionally(new Throwable("An UNKNOWN digest algorithm was encountered during a SIGN operation against an Android KeyChain PrivateKey."));
                            return;
                    }

                    ByteArrayOutputStream digestInfoStream = new ByteArrayOutputStream();
                    digestInfoStream.write(digestAlgorithm);
                    digestInfoStream.write(dataToSign);
                    byte[] digestInfo = digestInfoStream.toByteArray();

                    signature.initSign(privateKey);
                    signature.update(digestInfo);
                    byte[] signatureBytesRSA = signature.sign();

                    operation.complete(signatureBytesRSA);
                    return;

                case ECDSA:

                    signature.initSign(privateKey);
                    signature.update(dataToSign);
                    byte[] signatureBytesECC = signature.sign();

                    operation.complete(signatureBytesECC);
                    return;

                case UNKNOWN:
                default:

                    operation.completeExceptionally(new Throwable("An UNKNOWN signature algorithm was encountered during a SIGN operation against an Android KeyChain PrivateKey."));
                    return;
            }
        } catch (Exception ex){
            operation.completeExceptionally(new Throwable("Exception caught during Android KeyChain PrivateKey operation.", ex));
        }
    }
}
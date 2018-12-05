# To make the tests work:
1. Add a thing to your IoT Service Things with [the wizard](https://console.aws.amazon.com/iot/home)
2. Download the cert and keys, or the package from the last step of the wizard
3. Unpack your certs (pem and private key) into this folder 
    * Certificate must end in certificate.pem.crt
    * Private key must end in private.pem.key
    * The default keys from the console are in this format, the wizard created keys may need renaming
4. `mvn test` or similar
5. The test should pick them up. If they are not found, the test will just print a warning and pretend it succeeded.

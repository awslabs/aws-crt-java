Place your Thing certificates in this folder. You should have:
* AmazonRootCA1.pem
* {certId}-certificate.pem.crt
* {certId}-private.pem.key

The certificate and private key can be obtained by creating a certificate in the 
[AWS IoT console](https://console.aws.amazon.com/iot/home) and extracting the certs into this folder. The root
certificate can be obtained via the [X.509 Certificates and AWS IoT](https://docs.aws.amazon.com/iot/latest/developerguide/managing-device-certs.html)
page.

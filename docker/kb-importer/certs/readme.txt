This folder contains certs which are needed to connect with your backend or cia URL
Certs can be downloaded with the following command -
echo yes | openssl s_client -connect domain_name:port_number | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'

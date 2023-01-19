# Profile3 usage


## Prerequisites

### Add server certificate to trust store
Configure local java trust store to trust backend certificate. For this you need:
1. fetch backend certificate

    ```shell
    openssl x509 -in <(openssl s_client -connect <backend_dns>:443 -prexit 2>/dev/null) -out backend-cert.pem
    ```
   * here `<backend_dns>` could `ocpp.everon.io` or `ocpp.staging.everon.io`. Same <backend_dns> should be used when you switch simulator to profile3

2. import backend certificate from (1) into system java trust store
    ```shell
    keytool -importcert -trustcacerts -cacerts -file backend-cert.pem -alias backend-cert
    ```
   * default password is `changeit`

### Generate private and public keys
1. Generate EC private key
    ```shell
    openssl ecparam -name prime256v1 -genkey -noout -out private.key
    ```
2. Extract public key from private
    ```shell
    openssl ec -in private.key -pubout -out public.key
    ```
3. To check your private key details use 
    ```shell
    openssl ec -inform pem  -in private.key -text -noout
    ```
4. Remember path to directory with your keys, it will be required for simulator start arguments `<path_to_public_and_private_key>`

### Create CSR and sign it on backend
1. Create CSR
    ```shell
    openssl req -key private.key  -new -out staging.csr
    ```
    Provide data that what openssl tool will ask you from console.
    Example of input:
    ```shell
    openssl req -key private.key  -new -out staging.csr
    You are about to be asked to enter information that will be incorporated
    into your certificate request.
    What you are about to enter is what is called a Distinguished Name or a DN.
    There are quite a few fields but you can leave some blank
    For some fields there will be a default value,
    If you enter '.', the field will be left blank.
    -----
    Country Name (2 letter code) []:NL
    State or Province Name (full name) []:North Holland
    Locality Name (eg, city) []:Amsterdam
    Organization Name (eg, company) []:EVBox Intelligence B.V.
    Organizational Unit Name (eg, section) []:AC Wallbox
    Common Name (eg, fully qualified host name) []:<hardware_serial_number>
    Email Address []:eugeny.kuznetsov@evbox.com
    
    Please enter the following 'extra' attributes
    to be sent with your certificate request
    A challenge password []:
    ```
   * Make sure that in `Common Name` you have provided station ocpp serial number. You need to use same serial number as `station.hardwareConfiguration.serialNumber` (`<hardware_serial_number>`) in startup arguments for simulator 

2. You can read csr with:
    ```shell
    openssl req -in station.csr -noout -text
    ```

3. Sign CSR on backend
   ```shell
   curl --location --request POST 'https://api.staging.everon.io/certificate-management/v1/certificates/EvBoxManufactureCertificate:sign' \
   --header 'tenantId: <tenant_id>' \
   --header 'Authorization: Bearer <jwt_token>' \
   --header 'Content-Type: application/json' \
   --data-raw '{
       "csr": "<csr>"
   }'
   ```
   * <jwt_token> - valid jwt token to access api of CM service
   * <tenant_id> - valid tenant_id
   * <csr> - CSR generated at step 1

4. Process Certificate chain from response
   1. Replace `\n` with newLines
   2. Remove `"`, spaces `^\s+`, and commas with new line `^,\n`
   3. Eventually you should have valid certificate chain
   4. Save certificate chain to file(later we will need absolute path to this file to run simulator `<path_to_certificate_chain>`)


## Station simulator connection with mTLS
1. Use gradle run with arguments
   ```shell
   ./gradlew run -Parguments="ws://everon.io/ocpp --configuration {'stations':[{'id':'<station_identity_code>','evse':{'count':1,'connectors':1},'keyPairPath':'<path_to_public_and_private_key>','manufacturerCertificatePath':'<path_to_certificate_chain>','hardwareConfiguration':{'serialNumber':'<hardware_serial_number>'}}]}"
   ```
   * <path_to_public_and_private_key> - absolute path to directory with keys that you've generated before, names of keys should be `private.key`, `public.key`
   * <station_identity_code> - identity code on backend, eg `EKZ-500-000-001`

2. Run command `cert` to see certificate that has been loaded

3. Run command `profile3 wss://<backend_dns>/ocpp`

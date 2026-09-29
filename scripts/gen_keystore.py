"""Generate a PKCS12 signing keystore for the Ganesh Bhaji Market Android app.

The same keystore must be used for every future release so updates can
install over older versions without uninstalling.
"""
import datetime
import os

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives.serialization import pkcs12 as pkcs12_ser
from cryptography.x509.oid import NameOID

KEYSTORE_PATH = "keystore/ganesh-market.keystore"
PASSWORD = "ganesh-market-2026"


def main():
    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)

    subject = issuer = x509.Name([
        x509.NameAttribute(NameOID.COMMON_NAME, "Ganesh Bhaji Market"),
        x509.NameAttribute(NameOID.ORGANIZATION_NAME, "Ganesh Bhaji Market"),
        x509.NameAttribute(NameOID.COUNTRY_NAME, "IN"),
    ])

    now = datetime.datetime.now(datetime.timezone.utc)
    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now - datetime.timedelta(days=1))
        .not_valid_after(now + datetime.timedelta(days=365 * 25))
        .add_extension(
            x509.BasicConstraints(ca=True, path_length=None), critical=True
        )
        .sign(key, hashes.SHA256())
    )

    pkcs12 = pkcs12_ser.serialize_key_and_certificates(
        name=b"ganesh-market",
        key=key,
        cert=cert,
        cas=None,
        encryption_algorithm=serialization.BestAvailableEncryption(PASSWORD.encode()),
    )

    os.makedirs(os.path.dirname(KEYSTORE_PATH), exist_ok=True)
    with open(KEYSTORE_PATH, "wb") as f:
        f.write(pkcs12)

    print("Keystore written to", KEYSTORE_PATH)
    print("Store password:", PASSWORD)


if __name__ == "__main__":
    main()

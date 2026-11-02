import os
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
def config():
    values = dict(DB_HOST="127.0.0.1", DB_PORT="3306", DB_NAME="pet_matcher_db", DB_USER="root", DB_PASSWORD="")
    path = ROOT / ".env"
    if path.exists():
        for line in path.read_text(encoding="utf-8-sig").splitlines():
            if "=" in line and not line.lstrip().startswith("#"):
                key, value = line.split("=", 1)
                values[key.strip()] = value.strip().strip('"').strip("'")
    values.update({k: os.environ[k] for k in values if k in os.environ})
    if values["DB_HOST"] not in ("127.0.0.1", "localhost", "::1"):
        raise ValueError("Local-only mode requires a loopback DB_HOST")
    if not values["DB_NAME"].replace("_", "").isascii() or not values["DB_NAME"].replace("_", "").isalnum():
        raise ValueError("Invalid DB_NAME")
    port = int(values["DB_PORT"])
    if not 1 <= port <= 65535: raise ValueError("Invalid port")
    return dict(host=values["DB_HOST"], port=port, database=values["DB_NAME"], user=values["DB_USER"], password=values["DB_PASSWORD"], connection_timeout=5)

from pathlib import Path
from db_config import config

def statements(text):
    delimiter=";"; buffer=[]
    for line in text.splitlines():
        if line.strip().upper().startswith("DELIMITER "):
            delimiter=line.strip().split(None,1)[1]; continue
        if line.lstrip().startswith("--") or not line.strip(): continue
        buffer.append(line)
        joined="\n".join(buffer).strip()
        if joined.endswith(delimiter):
            yield joined[:-len(delimiter)].strip();buffer=[]
    if "\n".join(buffer).strip(): raise ValueError("Unterminated SQL statement")

def main():
    import mysql.connector
    options=config(); db=options.pop("database")
    with mysql.connector.connect(**options) as c:
        for path in sorted((Path(__file__).resolve().parents[1]/"database").glob("*.sql")):
            print("Applying",path.name)
            sql=path.read_text(encoding="utf-8-sig").replace("pet_matcher_db",db)
            with c.cursor() as q:
                for statement in statements(sql): q.execute(statement)
            c.commit()
    print("Schema ready. Existing records preserved. Create staff with scripts/create_admin.py.")
if __name__=="__main__":main()

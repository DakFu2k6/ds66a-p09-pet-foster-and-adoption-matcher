import base64, getpass, hashlib, re, secrets
from db_config import config

def main():
    import mysql.connector
    username=input("Username (3-50 letters/digits/_.-): ").strip()
    if not re.fullmatch(r"[A-Za-z0-9_.-]{3,50}",username): raise ValueError("Invalid username")
    name=input("Display name: ").strip() or username
    password=getpass.getpass("Password (6+ characters): ")
    if len(password)<6 or password!=getpass.getpass("Confirm password: "): raise ValueError("Password too short or mismatch")
    salt=secrets.token_bytes(16); iterations=210000
    digest=hashlib.pbkdf2_hmac("sha256",password.encode("utf-8"),salt,iterations)
    encoded=f"{iterations}:{base64.b64encode(salt).decode()}:{base64.b64encode(digest).decode()}"
    c=mysql.connector.connect(**config())
    try:
        with c.cursor() as q:
            q.execute("INSERT INTO staff_users(username,display_name,password_hash) VALUES(%s,%s,%s)",(username,name,encoded))
        c.commit();print("Staff account created.")
    except Exception:
        c.rollback();raise
    finally:c.close()
if __name__=="__main__":main()

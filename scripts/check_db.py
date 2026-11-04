from db_config import config
import sys
try:
    import mysql.connector
    with mysql.connector.connect(**config()) as connection:
        cursor=connection.cursor()
        cursor.execute("SELECT VERSION(),DATABASE()")
        print("Connected:",cursor.fetchone())
        for table in ("pets","care_profiles","foster_families","applications","placements","matching_results","matching_criteria_results","staff_users","match_criteria"):
            cursor.execute(f"SELECT COUNT(*) FROM {table}")
            print(table,cursor.fetchone()[0])
except Exception as error:
    code=getattr(error,"errno",None)
    print("FAIL",code,{1045:"Check DB_USER / DB_PASSWORD / host grants",1049:"Import database/01_create_database.sql",1146:"Import database/02_schema.sql",2003:"Start MySQL84 service and check DB_PORT"}.get(code,"Check .env and MySQL service"))
    sys.exit(1)

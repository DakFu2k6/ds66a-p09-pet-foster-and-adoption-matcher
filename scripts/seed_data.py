import argparse
import random
from db_config import config

def generate(pets=150, families=60, seed=42):
    rng = random.Random(seed)
    pet_rows = []
    for i in range(pets):
        species = rng.choice(["DOG", "CAT"])
        special = rng.random() < .2
        pet_rows.append((f"Demo {rng.choice(['Milo','Luna','Buddy','Mochi','Coco','Lucky'])} {i+1}", species,
            rng.choice(["Mixed Breed", "Domestic", "Rescue"]), rng.randint(2, 180),
            "SPECIAL_NEEDS" if special else rng.choice(["GOOD", "RECOVERING"]),
            rng.choice(["CALM", "FRIENDLY", "SHY", "ACTIVE"]), special,
            rng.choice(["ANY", "HOUSE", "APARTMENT", "YARD"]), "AVAILABLE"))
    family_rows = [(f"Demo Family {i+1}", f"Demo Contact {i+1}", f"09{i:08d}",
        rng.choice(["HOUSE", "APARTMENT", "FARM"]), rng.choice(["BEGINNER", "INTERMEDIATE", "EXPERIENCED"]),
        rng.randint(1, 5), 0, rng.choice([True, False]), rng.choice([True, False]), True) for i in range(families)]
    return pet_rows, family_rows

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    for name, default in [("pets",150),("families",60),("applications",100),("seed",42)]:
        parser.add_argument("--"+name,type=int,default=default)
    parser.add_argument("--dry-run",action="store_true")
    a=parser.parse_args()
    if min(a.pets,a.families,a.applications)<0 or a.applications>a.pets*a.families:
        parser.error("Counts must be nonnegative and applications <= pets * families")
    pets,families=generate(a.pets,a.families,a.seed)
    if a.dry_run:
        print(f"Validated: {len(pets)} pets, {len(families)} families, {a.applications} applications; no DB writes")
        return
    import mysql.connector
    connection=mysql.connector.connect(**config())
    try:
        cursor=connection.cursor()
        pet_ids=[]; family_ids=[]
        for row in pets:
            cursor.execute("INSERT INTO pets(name,species,breed,age_months,health_status,temperament,special_care_required,housing_requirement,status) VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s)",row)
            pet_ids.append(cursor.lastrowid)
            cursor.execute("INSERT INTO care_profiles(pet_id,daily_exercise_minutes,feeding_notes,medical_notes) VALUES(%s,%s,%s,%s)",
                (pet_ids[-1],60 if row[1]=="DOG" else 20,"Synthetic demo: feed twice daily","Synthetic demo: veterinary review" if row[6] else "Synthetic demo: routine checks"))
        for row in families:
            cursor.execute("INSERT INTO foster_families(family_name,contact_name,phone,housing_type,experience_level,capacity,active_pet_count,has_yard,has_children,is_active) VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",row)
            family_ids.append(cursor.lastrowid)
        rng=random.Random(a.seed)
        for index in rng.sample(range(len(pet_ids)*len(family_ids)),a.applications):
            p,f=divmod(index,len(family_ids))
            cursor.execute("INSERT INTO applications(pet_id,family_id,notes) VALUES(%s,%s,%s)",(pet_ids[p],family_ids[f],"Synthetic demo application"))
        connection.commit()
        print(f"Committed {a.pets} pets + care profiles, {a.families} families, {a.applications} applications.")
        print("Re-running appends another batch. Existing records are preserved.")
    except Exception:
        connection.rollback(); raise
    finally:
        connection.close()
if __name__=="__main__": main()

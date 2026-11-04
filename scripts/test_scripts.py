import unittest
from pathlib import Path
from unittest.mock import patch
from seed_data import generate
from init_db import statements
from db_config import config
class ScriptTests(unittest.TestCase):
    def test_seed_reproducible(self):
        self.assertEqual(generate(150,60,42),generate(150,60,42))
    
    def test_seed_has_valid_values(self):
        pets,families=generate(150,60,42)
        self.assertEqual(len(pets),150);self.assertEqual(len(families),60)
        self.assertTrue(all(p[1] in ('DOG','CAT') and p[3]>=0 for p in pets))
        self.assertTrue(all(f[5]>0 and f[6]==0 for f in families))
    
    def test_delimiter_parser(self):
        sql='DELIMITER //\nCREATE PROCEDURE demo()\nBEGIN\n SELECT 1;\n SELECT 2;\nEND//\nDELIMITER ;\nCALL demo();'
        parts=list(statements(sql));self.assertEqual(len(parts),2);self.assertIn('SELECT 2;',parts[0]);self.assertEqual(parts[1],'CALL demo()')
    
    def test_all_deliverable_sql_files_parse(self):
        files=sorted((Path(__file__).resolve().parents[1]/'database').glob('*.sql'))
        self.assertEqual(len(files),8)
        for p in files:self.assertGreater(len(list(statements(p.read_text()))),0,p.name)
    
    def test_nonlocal_host_rejected(self):
        with patch.dict('os.environ',{'DB_HOST':'example.com'}):
            with self.assertRaises(ValueError):config()
    
    def test_invalid_port_rejected(self):
        with patch.dict('os.environ',{'DB_HOST':'127.0.0.1','DB_PORT':'70000'}):
            with self.assertRaises(ValueError):config()
    
    def test_invalid_database_rejected(self):
        with patch.dict('os.environ',{'DB_HOST':'127.0.0.1','DB_NAME':'db;DROP'}):
            with self.assertRaises(ValueError):config()

if __name__=='__main__':unittest.main()

import unittest
import counter

class TestCounter(unittest.TestCase):
    def test_plusone(self):
        self.assertEqual(2, counter.plusone(1))
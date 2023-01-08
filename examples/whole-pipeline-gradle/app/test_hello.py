import unittest
import hello

class TestCounter(unittest.TestCase):
    def test_hello(self):
        self.assertEqual( "Hello World! I've greeted 0 times!", hello.hello() )
        self.assertEqual( "Hello World! I've greeted 1 times!", hello.hello() )
        self.assertEqual( "Hello World! I've greeted 2 times!", hello.hello() )
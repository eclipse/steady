import unittest
import os
from vulas import cli_wrapper
from vulas import bom_command

class MyTestCase(unittest.TestCase):

    def test_jar_works(self):
        self.assertEqual(True, cli_wrapper.is_available())

    def test_sys_props(self):
        dict = {
            'abc':'123',
            'xyz':'789'
        }
        props = cli_wrapper.dict_to_java_sys_props(dict)
        self.assertEqual(set(['-Dabc=123', '-Dxyz=789']), set(props))

    def test_cfg_file(self):
        config_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), "vulas-python-test.cfg")
        file = cli_wrapper.read_vulas_configuration(config_path)
        self.assertTrue(file)

if __name__ == '__main__':
    unittest.main()

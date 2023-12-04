from distutils.core import Command
from vulas import cli_wrapper
import os

class clean(Command):

    description = 'Runs the vulnerability-assessment-tool goal CLEAN'

    user_options = [('debug', 'd', "call the Java CLI in debug mode")]

    boolean_options = ['debug']

    app = {}
    file = {}

    def initialize_options(self):
        self.debug = False

    def finalize_options(self):
        assert self.debug in (None, True, False), 'True/False'

    def assert_bool(dist, attr, value):
        """Verify that value is True, False, 0, or 1"""
        if bool(value) != value:
            raise DistutilsSetupError(
                "%r must be a boolean value (got %r)" % (attr, value)
            )

    def run(self):
        print("Starting vulnerability-assessment-tool goal: CLEAN")

        # Collect all arguments
        args = {}

        # App identifier
        for key in self.get_vulas_app().keys():
            args[key] = self.get_vulas_app()[key]

        # vulas-python.cfg
        path = cli_wrapper.read_vulas_configuration_path()
        conf = cli_wrapper.read_vulas_configuration(path)
        for key in conf.keys():
            args[key] = conf[key]

        # Other
        # src_dir = ''
        # for p in self.distribution.packages:
        #     if not src_dir == '':
        #         src_dir += ','
        #     src_dir += os.path.join(os.getcwd(), p)

        args['vulas.core.app.sourceDir'] = os.getcwd()

        print("Arguments:")
        for key in args:
            print("    " + key + " = " + args[key])

        # Run the CLI
        rc = cli_wrapper.run(args, "clean", self.debug)

        if rc != True:
            raise RuntimeError("Command line interface returned status code 1")

    def get_vulas_app(self):
        if not self.app:
            self.app =  {
                'vulas.core.appContext.group':self.distribution.get_name(),
                'vulas.core.appContext.artifact':self.distribution.get_name(),
                'vulas.core.appContext.version':self.distribution.get_version()
            }
        return self.app

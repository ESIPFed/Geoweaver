from subprocess import Popen
import os

script_dir = os.path.dirname(os.path.realpath(__file__))
bat_path = os.path.join(script_dir, "launch.bat")

p = Popen(bat_path, cwd=script_dir)
stdout, stderr = p.communicate()
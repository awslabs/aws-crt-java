import Builder
import sys
import subprocess
import atexit

class LocalhostSetup(Builder.Action):

    def run(self, env):
        python = sys.executable
        
        result = env.shell.exec(python,
                                     '-m', 'pip', 'install', 'h11', 'h2', 'trio')
        if result.returncode != 0:
            print(
                "Could not install python HTTP/2 server." +
                " The localhost integration tests will fail if you run them.", file=sys.stderr)
            return
        
        server_dir = f"{env.root_dir}/crt/aws-c-http/tests/mock_server"
        
        p1 = subprocess.Popen([python, "h2tls_mock_server.py"], cwd=server_dir)
        p2 = subprocess.Popen([python, "h2non_tls_server.py"], cwd=server_dir)
        p3 = subprocess.Popen([python, "h11mock_server.py"], cwd=server_dir)
        
        @atexit.register
        def cleanup():
            p1.terminate()
            p2.terminate()
            p3.terminate()
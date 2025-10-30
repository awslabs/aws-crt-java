import Builder
import subprocess
import socket
import sys
import time
import os


class LocalhostTest(Builder.Action):

    def start(self, env):
        python = sys.executable
        venv_path = os.path.join(env.root_dir,'crt','aws-c-http','tests','mock_server', '.venv')

        result = env.shell.exec(python, '-m', 'venv', venv_path)
        if result.returncode != 0:
            print("Could not start a virtual environment. The localhost integration tests will fail.", file=sys.stderr)
            return
        
        python = os.path.join(venv_path, "bin", "python")
        
        result = env.shell.exec(python, '-m', 'pip', 'install', 'h11', 'h2', 'trio')
        if result.returncode != 0:
            print("Could not install python HTTP dependencies. The localhost integration tests will fail.", file=sys.stderr)
            return
        
        server_dir = os.path.join(env.root_dir,'crt','aws-c-http','tests','mock_server')
        
        p1 = subprocess.Popen([python, "h2tls_mock_server.py"], cwd=server_dir)
        p2 = subprocess.Popen([python, "h2non_tls_server.py"], cwd=server_dir)
        p3 = subprocess.Popen([python, "h11mock_server.py"], cwd=server_dir)
        
        # Wait for servers to be ready
        ports = [3443, 3280, 8082, 8081]
        for port in ports:
            for attempt in range(30):
                try:
                    with socket.create_connection(("localhost", port), timeout=1):
                        print(f"Server on port {port} is ready")
                        break
                except (socket.error, ConnectionRefusedError, OSError):
                    if attempt == 29:
                        print(f"ERROR: Server on port {port} failed to start", file=sys.stderr)
                    time.sleep(1)

    def run(self, env):
        self.start(env)
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')

        if os.system("mvn test -DredirectTestOutputToFile=true -DforkCount=0 \
            -Daws.crt.memory.tracing=2 \
            -Daws.crt.debugnative=true \
            -Daws.crt.aws_trace_log_per_test \
            -Daws.crt.localhost=true"):
            # Failed
            actions.append("exit 1")

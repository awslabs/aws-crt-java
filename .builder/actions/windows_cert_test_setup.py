"""
Prepare for Windows certificate tests
"""
import Builder


class WindowsCertTestSetup(Builder.Action):
    """
    Imports the given PFX file (that does NOT have a password) into the Windows Certificate Store
    and sets an environment variable called: "AWS_TEST_MQTT311_IOT_CORE_WINDOWS_CERT_STORE"
    and/or "AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE"
    with the path to the certificate in the store.

    NOTE: The PFX file has to NOT have a password set due to how SecureStrings work in Powershell
    and there not being a good (or any) way to get a SecureString in Python that can be passed
    to Import-PfxCertificate... In theory a Powershell script can be used, but this has its own quirks.

    This action should be run in the 'pre_build_steps' or 'build_steps' stage after
    the setup_ci_from_files step to work properly.
    """

    def run(self, env):
        self.env = env
        self.windows_certificate_folder = "Cert:\\CurrentUser\\My"

        self._create_cert_store(
            "AWS_TEST_MQTT311_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS",
            "AWS_TEST_MQTT311_IOT_CORE_WINDOWS_CERT_STORE")
        self._create_cert_store(
            "AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS",
            "AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE")
        print ("Windows Cert Setup: success!")

    def _create_cert_store(self, certificate_env, location_env):
        # Is the environment variable set?
        if (self.env.shell.getenv(certificate_env) == None):
            print(f"Windows Cert Setup: {certificate_env} not set. Skipping...")
            return
        pfx_cert_path = self.env.shell.getenv(certificate_env)

        # Import the PFX into the Windows Certificate Store
        # (Passing '$mypwd' is required even though it is empty and our certificate has no password. It fails CI otherwise)
        import_pfx_arguments = [
            "Import-PfxCertificate",
            "-FilePath", pfx_cert_path,
            "-CertStoreLocation", self.windows_certificate_folder]
        import_result = self.env.shell.exec("powershell.exe", import_pfx_arguments, check=True)

        # Get the certificate thumbprint from the output:
        import_pfx_output = str(import_result.output)
        # We know the Thumbprint will always be 40 characters long, so we can find it using that
        # TODO: Extract this using a better, more fool-proof method
        thumbprint = ""
        current_str = ""
        # The input comes as a string with some special characters still included, so we need to remove them!
        import_pfx_output = import_pfx_output.replace("\\r", " ")
        import_pfx_output = import_pfx_output.replace("\\n", "\n")
        for i in range(0, len(import_pfx_output)):
            if (import_pfx_output[i] == " " or import_pfx_output[i] == "\n"):
                if (len(current_str) == 40):
                    thumbprint = current_str
                    break
                current_str = ""
            else:
                current_str += import_pfx_output[i]
        if (thumbprint == ""):
            print (f"Windows Cert Setup: {certificate_env} - ERROR - could not find certificate thumbprint")
            return

        self.env.shell.setenv(location_env, self.windows_certificate_folder + "\\" + thumbprint)
        print ("Windows Cert Setup: success!")

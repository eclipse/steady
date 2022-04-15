

class Import implements Runnable {

    private static final SOURCE_TAR = "changed-source-code.tar.gz"
    private static final STATEMENT_YAML = "statement.yaml"
    
  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private BackendConnector backendConnector;

    public Import(Path vulnDir) {
        this.vulnDir = vulnDir;
    }

    @Override
    public void run() {

        /* How to pass the argumens?
        set configuration in the manager instead?
        Object uploadConstruct = args.get(UPLOAD_CONSTRUCT_OPTION);
        VulasConfiguration.getGlobal()
            .setProperty(
                CoreConfiguration.BACKEND_CONNECT,
                (uploadConstruct != null
                    ? CoreConfiguration.ConnectType.READ_WRITE.toString()
                    : CoreConfiguration.ConnectType.READ_ONLY.toString()));*/

        Boolean overwrite = (Boolean) args.get(OVERWRITE_OPTION);
        if (bugExists) {
            if (overwrite) {
                args.put(DELETE, true);
            } else {
                log.info("Bug [{}] already exists in backend, analysis will be skipped", vulnId);
                return;
            }
        }
        Path statementPath = findStatementPath();
        if (statementPath != null) {
            Vulnerability vuln = Metadata.getFromYaml(statementPath);

            ExtractOrClone extractOrClone = new ExtractOrClone(this.vulnDir, args);
            ImportVulnerability importVulnerability = new ImportVulnerability(vuln, args);
            ImportAffectedLibraries importAffectedLibraries = new ImportAffectedLibraries(vuln, args);

            extractOrClone.execute();
            importVulnerability.execute();
            importAffectedLibraries.execute();
        }


    }

    public Path findTarPath() {
        if (FileUtil.isAccessibleFile(vulnDir + File.separator + SOURCE_TAR)) {
            return vulnDir + File.separator + SOURCE_TAR;
        } 
    } 

    public Path findStatementPath() {
        // TODO: Should also check for metadata.json?
        // Review this function
        if (FileUtil.isAccessibleFile(vulnDir + File.separator + STATEMENT_YAML)) {
            return vulnDir + File.separator + STATEMENT_YAML;
        } 
        /* Since there is one Import task per vulnerability, there is no need to loop over subdirectories
        else if (FileUtil.isAccessibleDirectory(vulnDir)) {
            File directory = new File(vulnDir);
            File[] fList = directory.listFiles();
            if (fList != null) {
                for (File file : fList) {
                    if (file.isDirectory()) {
                        if (FileUtil.isAccessibleFile(
                            file.getAbsolutePath() + File.separator + STATEMENT_YAML)) {
                            return file.getAbsolutePath() + File.separator + STATEMENT_YAML;
                        } else {
                            Import.log.warn(
                                "Skipping {} as the directory does not contain statement.yaml"
                                    + " file",
                                file.getAbsolutePath());
                        }
                    }
                }
            }
        }*/
        else {
            Import.log.error("Invalid directory {}", vulnDir);
        }
        return null;
    }
}
package org.eclipse.steady.kb.task;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.ArrayList;
import org.apache.logging.log4j.Logger;

import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.model.Commit;
import org.eclipse.steady.kb.Import;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.kb.Manager;

public class ExtractOrClone {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static final String GIT_DIRECTORY = "git-repos";
  private final Manager manager;
  private final Vulnerability vuln;
  private final String vulnId;
  private final String dirPath;
  private final File tarFile;

  public ExtractOrClone(Manager manager, Vulnerability vuln, File dir) {
    this.manager = manager;
    this.vuln = vuln;
    this.vulnId = vuln.getVulnId();
    this.dirPath = dir.getPath();
    this.tarFile = getTarFile(dirPath);
    //System.out.println("ExtractOrClone constructor");
  }

  public void execute() {

    //System.out.println("ExtractOrClone.execute()");
    if (tarFile != null) {
      //System.out.println("if (tarFile != null)");
      extract(tarFile, dirPath);
    } else {
      //System.out.println("else");
      //System.out.println("skipping clone");
      log.info("Skipping clone for vulnerability " + this.vulnId);
      manager.setVulnStatus(this.vulnId, Manager.VulnStatus.FAILED);
      //clone(vuln, dirPath);
    }
    log.info("ExtractOrClone : done (" + dirPath + ")");
  }

  public File getTarFile(String dirPath) {
    if (FileUtil.isAccessibleFile(dirPath + File.separator + Import.SOURCE_TAR)) {
      return new File(dirPath + File.separator + Import.SOURCE_TAR);
    } else return null;
    /*
    File tarFile = null;
    File[] cveFiles = dir.listFiles();
    for (File cveFile : cveFiles) {
      String filename = cveFile.getName();
      String[] splitted = filename.split("[.]");
      if (splitted.length == 0) {
        continue;
      }
      String extension = splitted[splitted.length - 1];
      if (extension.equals("tar")
          || (splitted.length > 2 && splitted[splitted.length - 2].equals("tar"))) {
        tarFile = cveFile;
        break;
      }
    }
    return tarFile;*/
  }

  public void extract(File tarFile, String dirPath) {

    log.info("Extracting vulnerability " + vulnId);
    String extractCommand = "tar -xf " + tarFile.getPath() + " --directory " + dirPath;

    try {

      Process process = Runtime.getRuntime().exec(extractCommand);
      process.waitFor();

      List<Commit> commits = vuln.getCommits();
      for (Commit commit : commits) {
        String commitDirPath = dirPath + File.separator + commit.getCommitId();
        createAndWriteCommitMetadata(commit, null, commitDirPath);
      }

    } catch (IOException | InterruptedException e) {
      String vulnId = dirPath.split(File.separator)[dirPath.split(File.separator).length - 1];
      manager.setVulnStatus(vulnId, Manager.VulnStatus.FAILED);
      e.printStackTrace();
    }
  }

  public void clone(Vulnerability vuln, String dirPath) {

    List<Commit> commits = vuln.getCommits();
    if (commits.size() == 0) {
      log.warn("No commits for vulnerability " + this.vulnId);
      manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.NO_FIXES);
      return;
    }

    for (Commit commit : commits) {
      String repoUrl = commit.getRepoUrl();
      String commitId = commit.getCommitId();
      String commitDirPath = dirPath + File.separator + commitId;
      //System.out.println("commitDirPath : " + commitDirPath);
      File commitDir = new File(commitDirPath);
      commitDir.mkdir();
      String repoDirPath = dirPath + File.separator + 
          GIT_DIRECTORY + File.separator + repoUrl.replace("https://", "").replace("/", "_");
      manager.lockRepo(repoUrl);
      try {
        cloneOnce(repoUrl, repoDirPath);
        //System.out.println("after cloneOnce");
        createAndWriteCommitMetadata(commit, repoDirPath, commitDirPath);
        //System.out.println("after createAndWriteCommitMetadata");
        writeCommitDiff(commitId, repoDirPath, commitDirPath);
        //System.out.println("after writeCommmitDiff");

      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED);
        break;
      }
      manager.unlockRepo(repoUrl);
    }
  }

  public void createAndWriteCommitMetadata(Commit commit, String repoDirPath, String commitDirPath)
      throws IOException {

    String commitId = commit.getCommitId();
    String commitMetadataPath = commitDirPath + File.separator + Import.METADATA_JSON;
    File commitMetadataFile = new File(commitDirPath);
    // if (!Files.exists(commitMetadataPath)) {
    HashMap<String, String> commitMetadata = new HashMap<String, String>();

    String timestamp;
    if (repoDirPath == null) {
      Path timestampPath = Paths.get(commitDirPath + File.separator + "timestamp");
      timestamp = new String(Files.readAllBytes(timestampPath)).replace("\n", "");
    } else {
      String gitShowCommand =
          "git -C " + repoDirPath + " show --no-patch --no-notes --pretty='%at' " + commitId;
      Process gitShow = Runtime.getRuntime().exec(gitShowCommand);

      BufferedReader gitShowStdInput =
          new BufferedReader(new InputStreamReader(gitShow.getInputStream()));
      log.info("Executing: " + gitShowCommand);
      try {
        gitShow.waitFor();
      } catch (InterruptedException e) {
        return;
      }
      if ((timestamp = gitShowStdInput.readLine()) == null || timestamp == null) {
        BufferedReader gitShowError =
            new BufferedReader(new InputStreamReader(gitShow.getErrorStream()));
        String repoUrl = commit.getRepoUrl();
        log.error("Failed to get commit timestamp for repository " +repoUrl + " commit id " + commitId);
        String error = gitShowError.readLine();
        log.error("git show: " + error);
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED);
      }
    }
    /*
    while ((timestamp = gitShowStdInput.readLine()) == null) {
      System.out.println("timestamp : " + timestamp);
    }*/
    commitMetadata.put("repository", commit.getRepoUrl());
    commitMetadata.put("branch", commit.getBranch());
    commitMetadata.put("timestamp", timestamp);
    commitMetadata.put("commit_id", commitId);

    Metadata.writeCommitMetadata(commitDirPath, commitMetadata);
  }

  public void cloneOnce(String repoUrl, String repoDirPath)
      throws IOException, InterruptedException {

    String gitCloneCommand = "git clone " + repoUrl + " " + repoDirPath;

    if (Files.exists(Paths.get(repoDirPath))) {
      log.info("Folder " + repoDirPath + " exists. Skipping git clone.");
    } else {
      log.info("Cloning repository " + repoUrl);
      Process gitClone = Runtime.getRuntime().exec(gitCloneCommand);
      /*BufferedReader gitCloneStdInput =
          new BufferedReader(new InputStreamReader(gitClone.getInputStream()));
      BufferedReader gitCloneErrorInput =
          new BufferedReader(new InputStreamReader(gitClone.getErrorStream()));
      String line;
      while ((line = gitCloneStdInput.readLine()) != null) {
        System.out.println("git clone");
        System.out.println(line);
        if ((line = gitCloneErrorInput.readLine()) != null) {
          System.out.println(line);
        }
      }*/
      gitClone.waitFor();
    }
  }

  public void writeCommitDiff(String commitId, String repoDirPath, String commitDirPath)
      throws IOException, InterruptedException {
    String gitDiffCommand =
        "git -C " + repoDirPath + " diff --name-only " + commitId + "^.." + commitId;
    log.info("Executing: " + gitDiffCommand);
    Process gitDiff = Runtime.getRuntime().exec(gitDiffCommand);
    BufferedReader gitDiffStdInput =
        new BufferedReader(new InputStreamReader(gitDiff.getInputStream()));

    String filename;
    while ((filename = gitDiffStdInput.readLine()) != null) {
      execGitDiffFile(repoDirPath, commitId, filename, true);
      execGitDiffFile(repoDirPath, commitId, filename, false);
    }
  }

  public void execGitDiffFile(String repoDirPath, String commitId, String filename, boolean before)
      throws IOException, InterruptedException {

    String commitDirPath = dirPath + File.separator + commitId;
    String commitStr;
    if (before) { 
      commitStr = commitId + "~1:";
    } else {
      commitStr = commitId + ":";
    }
    String beforeOrAfter = before? "before" : "after";
    // for each file modified in the commit...
    String gitCatCommand =
        "git -C " + repoDirPath + " cat-file -e " + commitStr + filename;// + " &> /dev/null";
    Process gitCat = Runtime.getRuntime().exec(gitCatCommand);
    log.info("Executing: " + gitCatCommand);
    BufferedReader gitCatErrorInput =
        new BufferedReader(new InputStreamReader(gitCat.getErrorStream()));
    gitCat.waitFor();
    if (gitCat.exitValue() == 0) {
      //System.out.println("git cat-file works");
      String filepath = commitDirPath + File.separator + beforeOrAfter + File.separator + filename;
      File file = new File(filepath);
      File dir = file.getParentFile();
      // Paths.createDirectories(dir.getPath());
      dir.mkdirs();
      // git -C $repo_dir show $commit_id~1:$F > $vulnerability_id/$commit_id/before/$F
      String diffFileCommand =
          "git -C "
              + repoDirPath
              + " show "
              + commitId
              + "~1:"
              + filename;

      log.info("Executing: " + diffFileCommand);
      Process gitDiffFile = Runtime.getRuntime().exec(diffFileCommand);
    
      writeCmdOutputToFile(gitDiffFile, filepath);
    
      gitDiffFile.waitFor();

    } else {
      log.error("git cat-file didn't work");
      log.error(gitCatErrorInput.readLine());
      // What to do in case it doesn't work?
      // manager.setVulnStatus(vulnId, Manager.VulnStatus.FAILED);
    }
  }

  public void writeCmdOutputToFile(Process process, String filepath) throws IOException {
    BufferedReader stdInput =
        new BufferedReader(new InputStreamReader(process.getInputStream()));
    /*BufferedReader errorInput =
        new BufferedReader(new InputStreamReader(process.getErrorStream()));
    if ((line = errorInput.readLine()) != null) {
      System.out.println(line);
    }*/
    String line;
    String lines = "";
    while ((line = stdInput.readLine()) != null) {
      lines += line + "\n";
    }
    Path path = Paths.get(filepath);
    byte[] bytes = lines.getBytes();

    Files.write(path, bytes);
  }

}

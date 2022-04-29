package org.eclipse.steady.kb.task;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.HashMap;

import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.model.Commit;
import org.eclipse.steady.kb.Import;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.kb.Manager;

public class ExtractOrClone {

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
    System.out.println("ExtractOrClone constructor");
  }

  public void execute() {

    System.out.println("ExtractOrClone.execute()");
    if (tarFile != null) {
      System.out.println("if (tarFile != null)");
      extract(tarFile, dirPath);
    } else {
      System.out.println("else");
      //System.out.println("skipping clone");
      //manager.setVulnStatus(this.vulnId, Manager.VulnStatus.FAILED);
      clone(vuln, dirPath);
    }
    System.out.println("ExtractOrClone : done (" + dirPath + ")");
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

    System.out.println("extract");
    String extractCommand = "tar -xf " + tarFile.getPath() + " --directory " + dirPath;
    try {
      Process process = Runtime.getRuntime().exec(extractCommand);
      process.waitFor();

      File dir = new File(dirPath);

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
    System.out.println("clone");
    System.out.println(vuln);

    List<Commit> commits = vuln.getCommits();
    System.out.println(commits);
    if (commits.size() == 0) {
      System.out.println("NO COMMITS");
      return;
    }

    for (Commit commit : commits) {
      String repoUrl = commit.getRepoUrl();
      String commitId = commit.getCommitId();
      String commitDirPath = dirPath + File.separator + commitId;
      System.out.println("commitDirPath : " + commitDirPath);
      File commitDir = new File(commitDirPath);
      String repoDirPath =
          GIT_DIRECTORY + File.separator + repoUrl.replace("https://", "").replace("/", "_");
      manager.lockRepo(repoUrl);
      try {
        cloneOnce(repoUrl, repoDirPath);
        createAndWriteCommitMetadata(commit, repoDirPath, commitDirPath);
        writeCommitDiff(commitId, repoDirPath, commitDirPath);
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
      System.out.println("repoDirPath == null");
      Path timestampPath = Paths.get(commitDirPath + File.separator + "timestamp");
      System.out.println(commitDirPath + File.separator + "timestamp");
      timestamp = new String(Files.readAllBytes(timestampPath)).replace("\n", "");
      System.out.println(timestamp);
    } else {
      String gitShowCommand =
          "git -C " + repoDirPath + " show --no-patch --no-notes --pretty='%at' " + commitId;
      Process gitShow = Runtime.getRuntime().exec(gitShowCommand);

      BufferedReader gitShowStdInput =
          new BufferedReader(new InputStreamReader(gitShow.getInputStream()));
      System.out.println(gitShowCommand);

      if ((timestamp = gitShowStdInput.readLine()) == null || timestamp == null) {
        BufferedReader gitShowError =
            new BufferedReader(new InputStreamReader(gitShow.getErrorStream()));
        System.out.println("Error: Failed to get commit timestamp");
        String error = gitShowError.readLine();
        System.out.println("git show Error: " + error);
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED);
      }
    }
    /*
    while ((timestamp = gitShowStdInput.readLine()) == null) {
      System.out.println("timestamp : "+timestamp);
    }*/
    System.out.println("timestamp : " + timestamp);
    commitMetadata.put("repository", commit.getRepoUrl());
    commitMetadata.put("branch", commit.getBranch());
    commitMetadata.put("timestamp", timestamp);
    commitMetadata.put("commit_id", commitId);

    Metadata.writeCommitMetadata(commitDirPath, commitMetadata);
  }

  public void cloneOnce(String repoUrl, String repoDirPath)
      throws IOException, InterruptedException {
    String gitCloneCommand = "git clone " + repoUrl + " " + repoDirPath;
    File repoDir = new File(repoDirPath);

    if (Files.exists(Paths.get(repoDirPath))) {
      System.out.println("Folder " + repoDirPath + " exists. Skipping git clone.");
    } else {
      Process gitClone = Runtime.getRuntime().exec(gitCloneCommand);
      BufferedReader gitCloneStdInput =
          new BufferedReader(new InputStreamReader(gitClone.getInputStream()));
      BufferedReader gitCloneErrorInput =
          new BufferedReader(new InputStreamReader(gitClone.getErrorStream()));
      String line;
      /*while ((line = gitCloneStdInput.readLine()) != null) {
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
      throws IOException {
    String gitDiffCommand =
        "git -C " + repoDirPath + " diff --name-only " + commitId + "^.." + commitId;
    Process gitDiff = Runtime.getRuntime().exec(gitDiffCommand);
    BufferedReader gitDiffStdInput =
        new BufferedReader(new InputStreamReader(gitDiff.getInputStream()));
    String filename;
    while ((filename = gitDiffStdInput.readLine()) != null) {
      // for each file modified in the commit...
      String gitCatBeforeCommand =
          "git -C " + repoDirPath + " cat-file -e " + commitId + "~1:" + filename + " &> /dev/null";
      Process gitCatBefore = Runtime.getRuntime().exec(gitCatBeforeCommand);
      if (gitCatBefore.exitValue() == 0) {
        // git -C $repo_dir show $commit_id~1:$F > $vulnerability_id/$commit_id/before/$F
        String diffFileCommand =
            "git -C "
                + repoDirPath
                + " show "
                + commitId
                + "~1:"
                + filename
                + " > "
                + commitDirPath
                + File.separator
                + "before"
                + File.separator
                + filename;
      } else {
        System.out.println("Error: git cat-file didn't work");
        manager.setVulnStatus(vulnId, Manager.VulnStatus.FAILED);
      }

      String gitCatAfterCommand =
          "git -C " + repoDirPath + " cat-file -e " + commitId + ":" + filename + " &> /dev/null";
      Process gitCatAfter = Runtime.getRuntime().exec(gitCatBeforeCommand);
      if (gitCatAfter.exitValue() == 0) {
        // git -C $repo_dir show $commit_id:$F > $vulnerability_id/$commit_id/after/$F
        String diffFileCommand =
            "git -C "
                + repoDirPath
                + " show "
                + commitId
                + ":"
                + filename
                + " > "
                + commitDirPath
                + File.separator
                + "after"
                + File.separator
                + filename;
      } else {
        System.out.println("Error: git cat-file didn't work");
        manager.setVulnStatus(vulnId, Manager.VulnStatus.FAILED);
      }
    }
  }
}

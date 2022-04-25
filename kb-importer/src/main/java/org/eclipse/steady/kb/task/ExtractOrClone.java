package org.eclipse.steady.kb.task;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
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

  public ExtractOrClone(Manager manager){
    this.manager = manager;
  }

  public void execute(File dir, Vulnerability vuln) {
    String dirPath = dir.getPath();
    File tarFile = getTarFile(dirPath);

    if (tarFile != null) {
      extract(tarFile, dirPath);
    } else {
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
    String extractCommand = "tar -xf " + tarFile.getPath() + " --directory " + dirPath;
    try {
      Process process = Runtime.getRuntime().exec(extractCommand);
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void clone(Vulnerability vuln, String dirPath) {
    List<Commit> commits = vuln.getCommits();
    if (commits.size() == 0) {
      System.out.println("NO COMMITS");
      return;
    }
    for (Commit commit : commits) {
      // TODO : lock the repo from this point
      // can the bugs have multiple repositories?
      String repoUrl = commit.getRepoUrl();
      String commitId = commit.getCommitId();
      String commitDirPath = dirPath + File.separator + commitId;
      System.out.println("commitDirPath : " + commitDirPath);
      File commitDir = new File(commitDirPath);
      String repoDirPath =
          GIT_DIRECTORY + File.separator + repoUrl.split("/")[repoUrl.split("/").length - 1];
      try {
          manager.start(repoUrl);
          cloneOnce(repoUrl, repoDirPath);
          createAndWriteCommitMetadata(commit, repoDirPath, commitDirPath);
          writeCommitDiff(commitId, repoDirPath, commitDirPath);
          manager.complete(repoUrl);
      } catch (IOException e) {
        e.printStackTrace();
        continue;
      }
    }
  }

  public void createAndWriteCommitMetadata(Commit commit, String repoDirPath, String commitDirPath)
      throws IOException {

    String commitId = commit.getCommitId();
    String commitMetadataPath = commitDirPath + File.separator + Import.METADATA_JSON;
    File commitMetadataFile = new File(commitDirPath);
    // if (!Files.exists(commitMetadataPath)) {
    HashMap<String, String> commitMetadata = new HashMap<String, String>();
    String gitShowCommand =
        "git -C " + repoDirPath + " show --no-patch --no-notes --pretty='%at' " + commitId;
    Process gitShow =
        Runtime.getRuntime()
            .exec(gitShowCommand); // have problems probably caused by parallelization

    BufferedReader gitShowStdInput =
        new BufferedReader(new InputStreamReader(gitShow.getInputStream()));
    BufferedReader gitShowError =
        new BufferedReader(new InputStreamReader(gitShow.getErrorStream()));
    String timestamp;
    if ((timestamp = gitShowStdInput.readLine()) == null) {
      System.out.println("NULL 1");
    }
    System.out.println("timestamp1");
    System.out.println(timestamp);
    if (timestamp == null) {
      System.out.println("NULL!!!");
    }
    String error = gitShowError.readLine();
    System.out.println("git show Error: " + error);
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

  public void cloneOnce(String repoUrl, String repoDirPath) {
    String gitCloneCommand = "git clone " + repoUrl + " " + repoDirPath;
    File repoDir = new File(repoDirPath);
    if (Files.exists(Paths.get(repoDirPath))) {
      System.out.println("Folder " + repoDirPath + " exists. Skipping git clone.");
    } else {
      try {
        Process gitClone = Runtime.getRuntime().exec(gitCloneCommand);
        gitClone.waitFor();
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
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
      }
    }
  }
}

package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class CurrentState implements Serializable {
    private HashMap<String, String> blobs;
    private String currentCommit;
    private HashMap<String, String> branches;
    private String currentBranch;

    public CurrentState() {
        blobs = new HashMap<>();
        branches = new HashMap<>();
    }

    public String getCurrentCommit() {
        return currentCommit;
    }

    public void setCurrentCommit(String currentCommit) {
        this.currentCommit = currentCommit;
    }

    public String getBranchHeadCommitNode(String branchTitle) {
            return branches.get(branchTitle);
    }

    public void putBranch(String title, String commit) {
        branches.put(title, commit);
    }

    public void deleteBranch(String title) {
        branches.remove(title);
    }

    public String getCurrentBranchTitle() {
        return currentBranch;
    }

    public void setCurrentBranch(String title) {
        currentBranch = title;
    }

    public void addBlob(String filename, String blobFilename) {
        blobs.put(filename, blobFilename);
    }

    public String getBlobFilename(String filename) {
        return blobs.get(filename);
    }

    public void removeBlob(String filename) {
        blobs.remove(filename);
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }
}

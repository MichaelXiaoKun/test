package gitlet;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main implements Serializable {
    private CurrentState currentState;
    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */

    public CommitNode getCommitNode(String hashCode) {
        if (hashCode == null) {
            return null;
        }
        return (CommitNode)deserialize(hashCode + ".commit");
    }

    public void saveCommitNode(CommitNode node) {
        serialize(node, node.getHashcode() + ".commit");
    }

    public ArrayList<CommitNode> getAllCommitNode() {
        ArrayList<CommitNode> result = new ArrayList<>();
        for (String string: currentState.getCommitList()) {
            result.add(getCommitNode(string));
        }
        return result;
    }

    public void globalLog() {
        ArrayList<CommitNode> commitNodeList = getAllCommitNode();
        for (CommitNode node : commitNodeList) {
            System.out.print(node.getLog());
        }
    }

    public Blob getBlob(String filename) {
        return (Blob)deserialize(filename);
    }

    public void saveBlob(Blob blob) {
        serialize(blob, blob.getBlobFilename());
    }

    public void loadCurrentState() {
        currentState = (CurrentState)deserialize("currentState");
    }

    public void saveCurrentState() {
        serialize(currentState, "currentState");
    }


    public CommitNode getCurrentCommit() {
        if (currentState == null) {
            return null;
        }

        return getCommitNode(currentState.getCurrentCommit());
    }

    public void setCurrentCommit(CommitNode node) {
        if (currentState == null) {
            return;
        }

        currentState.setCurrentCommit(node.getHashcode());
    }

    public static void serialize(Object obj, String path) {
        File file = new File(".gitlet");
        File outFile = new File(file, path + ".ser");
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(outFile));
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            return;
        }
    }

    public static Object deserialize(String path) {
        Object obj = null;
        File file = new File(".gitlet");
        File inFile = new File(file, path + ".ser");
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(inFile));
            obj = inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            obj = null;
        }
        return obj;
    }


    public void init() {
        File file = new File(".gitlet");
        if (file.exists()) {
            System.out.println("A gitlet version-control system " +
                    "already exists in the current directory.");
            return;
        }
        file.mkdir();
        currentState = new CurrentState();
        CommitNode initialCommit = new CommitNode("initial commit", new HashMap<>(), null);
        saveCommitNode(initialCommit);
        currentState.setCurrentCommit(initialCommit.getHashcode());
        currentState.putBranch("master", initialCommit.getHashcode());
        currentState.setCurrentBranch("master");
        currentState.putCommitList(initialCommit.getHashcode());
        saveCurrentState();
    }

    public void add(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        CommitNode currentCommit = getCurrentCommit();
        Blob currentVersionBlob = Blob.createBlobFromFile(filename);
        if (currentCommit.containsFile(filename)) {
            if (currentVersionBlob.getBlobFilename().equals(currentState.getBlobFilename(filename))) {
                // Same Content
                return;
            }
        }
        saveBlob(currentVersionBlob);
        currentState.addBlob(currentVersionBlob.getFilename(), currentVersionBlob.getBlobFilename());
    }

    public void commit(String msg) {
        if (msg == null) {
            System.out.println("Please enter a commit message.");
            return;
        }
        File exist = new File(".gitlet/StagingArea");
        CommitNode currentNode = getCurrentCommit();
        if (currentState.getBlobs().equals(currentNode.getBlobs())) {
            System.out.println("No changes added to the commit.");
            return;
        }
        CommitNode newNode = new CommitNode(msg, currentState.getBlobs(), getCurrentCommit());
        saveCommitNode(newNode);
        currentState.putBranch(currentState.getCurrentBranchTitle(), newNode.getHashcode());
        currentState.setCurrentCommit(newNode.getHashcode());
        currentState.putCommitList(newNode.getHashcode());
    }

    public void remove(String fileName) {
        if (currentState.getBlobFilename(fileName) == null && !getCurrentCommit().containsFile(fileName)){
            System.out.println("No reason to remove the file.");
            return;
        }
        if (!getCurrentCommit().containsFile(fileName)) {

            if(currentState.getBlobFilename(fileName) == null){
                File f = new File(fileName);
                f.delete();
            }
            else{
                currentState.removeBlob(fileName);
                File f = new File(fileName);
                f.delete();
            }
        }
    }

    public void log() {
        CommitNode commit = getCurrentCommit();
        while (commit != null) {
            System.out.print(commit.getLog());
            commit = getCommitNode(commit.getPreviousCommitNodeFilename());
        }
    }

    public void reset(String commitID) {
        currentState.getBlobs().clear();
        CommitNode node = getCommitNode(commitID);
        HashMap<String, String> map = node.getBlobs();
        for (String filename: map.keySet()) {
            currentState.getBlobs().put(filename, map.get(filename));
        }
        currentState.setCurrentCommit(node.getHashcode());
        currentState.putBranch(currentState.getCurrentBranchTitle(), commitID);
    }


    public void branch(String branchName) {
        if (currentState.containsBranch(branchName)) {
            System.out.println("A branch with that name already exists.");
        }
        currentState.putBranch(branchName, currentState.getCurrentCommit());
    }

    public void rmbranch(String branchName) {
        if (!currentState.containsBranch(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (currentState.getCurrentBranchTitle().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        }
        currentState.deleteBranch(branchName);
    }

    public void checkout() {

    }


    public static void main(String... args) {
        Main main = new Main();
        if (args.length == 0) return;
        if (args[0].equals("init")) {
            main.init();
        } else {
            if (! new File(".gitlet").exists()) {
                System.out.println("Not in an initialized gitlet directory.");
                return;
            }
            main.loadCurrentState();
            if (args[0].equals("add")) {
                main.add(args[1]);
            } else if (args[0].equals("commit")) {
                main.commit(args[1]);
            } else if (args[0].equals("log")) {
                main.log();
            } else if (args[0].equals("global-log")) {
                main.globalLog();
            } else if (args[0].equals("reset")) {
                main.reset(args[1]);
            } else if (args[0].equals("remove")){
                main.remove(args[1]);
            }
        }
        main.saveCurrentState();
    }
}

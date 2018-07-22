package gitlet;

import java.io.*;
import java.io.File;
import java.io.IOException;
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

    public HashSet<CommitNode> getAllCommitNode() {
        HashSet<CommitNode> result = new HashSet<>();

        if (result.contains(CommitNode))
        return result;
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
        saveCurrentState();
    }

    public void Add(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        CommitNode currentCommit = getCurrentCommit();
        Blob currentVersionBlob = Blob.createBlobFromFile(filename);
        if (currentCommit.containsFile(filename)) {
            if (currentVersionBlob.getBlobFilename().equals(currentCommit.getBlobFilename(filename))) {
                // Same Content
                return;
            }
        }
        saveBlob(currentVersionBlob);
        currentState.addBlob(currentVersionBlob.getFilename(), currentVersionBlob.getBlobFilename());
    }

    public void Commit(String msg) {
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
    }

    public void log() {
        CommitNode commit = getCurrentCommit();
        while (commit != null) {
            System.out.print(commit.getLog());
            commit = getCommitNode(commit.getPreviousCommitNodeFilename());
        }
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
                main.Add(args[1]);
            } else if (args[0].equals("commit")) {
                main.Commit(args[1]);
            } else if (args[0].equals("log")) {
                main.log();
            }
        }
        main.saveCurrentState();
    }
}

package gitlet;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main implements Serializable {
    private HashMap<String, CommitNode> commitTree;
    private HashMap<String, CommitNode> branchMap;
    private HashSet<String> keepTrackFileNameList;
    private CommitNode currentCommit;
    private Branch currentBranch;
    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */

    public static void serialize(Main obj, String path) {
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

    public static Main deserialize(String path) {
        Main obj = null;
        File file = new File(".gitlet");
        File inFile = new File(file, path + ".ser");
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(
                    inFile));
            obj = (Main) inp.readObject();
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
        CommitNode initialCommit = new CommitNode("initial commit", keepTrackFileNameList, null);
        currentBranch = new Branch("master", initialCommit);
        branchMap = new HashMap<>();
        branchMap.put("master", initialCommit);
        File subfile = new File(".gitlet.StagingArea");
        subfile.mkdir();
    }

    public void Add(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        if (currentCommit.getFilenames().contains(filename)) {
            File currentfile = new File(filename);
            File checkfile = currentCommit.getCommitedFile(filename);
            if (Arrays.equals(Utils.readContents(checkfile), Utils.readContents(currentfile))) {
                return;
            }
        } else {
            Path path = FileSystems.getDefault().getPath(filename);
            Path targetPath = FileSystems.getDefault().getPath(".gitlet", "StagingArea", filename);
            try {
                Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("IOException");
            }
            keepTrackFileNameList.add(filename);
        }
    }

    public void Commit(String msg) {
        if (msg == null) {
            System.out.println("Please enter a commit message.");
            return;
        }
        File exist = new File(".gitlet/StagingArea");
        if (exist.list().length == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        CommitNode newNode = new CommitNode(msg, keepTrackFileNameList, currentCommit);
        File file = new File(".gitlet/StagingArea");
        file.renameTo(new File(".gitlet/" + newNode.getHashCode()));
        File NewArea = new File(".gitlet/StagingArea");
        NewArea.mkdir();
        currentCommit.addNext(newNode);
        currentCommit = newNode;
        currentBranch.setNode(newNode);
        commitTree.put(newNode.getHashCode(), currentCommit);
    }


    public static void main(String... args) {
        if (args[0].equals("init")) {
            Main main = new Main();
            main.init();
            serialize(main, ".gitlet/metadata");
        }
        Main main = deserialize(".gitlet/metadata");
        if (args[0].equals("add")) {
            main.Add(args[1]);
        } else if (args[0].equals("commit")) {
            main.Commit(args[1]);
        }
        serialize(main, ".gitlet/metadata");
    }
}

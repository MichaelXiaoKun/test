package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static gitlet.Utils.sha1;

public class CommitNode implements Serializable {
    private String message;
    private HashSet<String> filenames;
    private LocalDateTime dateTime;
    private ArrayList<String> next;
    private String prev;

    public CommitNode(String msg, HashSet<String> filenames, CommitNode prev) {
        message = msg;
        this.filenames = filenames;
        this.prev = Utils.sha1(prev);
        this.next = new ArrayList<String>();
        dateTime = LocalDateTime.now();
    }

    public String getHashcode() {
        return Utils.sha1(dateTime, message);
    }

    public String getMessage() {
        return message;
    }

    public HashSet<String> getFilenames() {
        return filenames;
    }

    public void setPrev(String p) {
        this.prev = p;
    }

    public void setNext(ArrayList<String> x) {
        this.next = x;
    }

    public ArrayList<String> getNext() {
        return next;
    }

    public void addNext(CommitNode x) {
        next.add(x.getHashcode());
    }

    public File getCommitedFile(String filename) {
        return new File(".gitlet/" + getHashcode() + "/" + filename);
    }

}

package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Branch implements Serializable {
    private String name;
    private CommitNode node;

    Branch(String branchname, CommitNode currentNode) {
        name = branchname;
        this.node = currentNode;
    }

    public void setNode(CommitNode s) {
        this.node = s;
    }

    public String getName() {
        return name;
    }


}

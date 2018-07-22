package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Branch implements Serializable {
    private String name;
    private String node;

    Branch(String branchName, String currentNode) {
        this.name = branchName;
        this.node = currentNode;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }

    public String getName() {
        return name;
    }


}

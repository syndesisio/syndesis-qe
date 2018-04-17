package io.syndesis.qe.pages.integrations.editor.add.steps.basicfilter;

public class BasicFilterRule {

    private String path;
    private String op;
    private String value;

    public BasicFilterRule(String path, String op, String value) {
        this.path = path;
        this.op = op;
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BasicFilterRule [path=" + path + ", op=" + op + ", value=" + value + "]";
    }
}

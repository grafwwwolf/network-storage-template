package ru.gb.storage.commons.message;

public class FileRequestMessage extends Message {

    private String pathFromFile;
    private String pathToFile;

    public String getPathFromFile() {
        return pathFromFile;
    }

    public void setPathFromFile(String path) {
        this.pathFromFile = path;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }
}

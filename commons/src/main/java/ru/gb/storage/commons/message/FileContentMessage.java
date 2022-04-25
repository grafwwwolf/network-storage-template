package ru.gb.storage.commons.message;

public class FileContentMessage extends Message {

    private Long startPosition;

    private byte[] content;

    private boolean last;

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Long startPosition) {
        this.startPosition = startPosition;
    }
}

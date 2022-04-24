package deno.farbgame;

public enum MessageTypes {

    CHAT(1),
    ACTION_BAR(2),
    INVISIBLE_ACTION_BAR(3);

    private final int type;

    MessageTypes(int type) {

        this.type = type;

    }
    public int getType() {

        return this.type;

    }

}
